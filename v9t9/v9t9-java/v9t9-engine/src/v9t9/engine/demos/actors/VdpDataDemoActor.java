/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.BitSet;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoPlayer;
import v9t9.common.demo.IDemoRecorder;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.SimpleMemoryWriteTracker;
import v9t9.engine.demos.events.VideoWriteDataEvent;

/**
 * @author ejs
 *
 */
public class VdpDataDemoActor extends BaseDemoActor {

	protected SimpleMemoryWriteTracker vdpMemoryListener;
	protected IVdpChip vdp;
	private byte[] videoBytes = new byte[256];
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return VideoWriteDataEvent.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		vdp = machine.getVdp();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		vdpMemoryListener = createMemoryTracker();
		vdpMemoryListener.addMemoryRange(0, vdp.getMemorySize());
		vdpMemoryListener.addMemoryListener();
		
		// send VDP data
		int memSize = vdp.getMemorySize();
		for (int addr = 0; addr < memSize; ) {
			int toUse = Math.min(255, memSize - addr);
			ByteMemoryAccess access = vdp.getByteReadMemoryAccess(addr);
			recorder.getOutputStream().writeEvent(
					new VideoWriteDataEvent(addr, access.memory, access.offset, toUse));
			addr += toUse;
		}
	}

	/**
	 * @return
	 */
	protected SimpleMemoryWriteTracker createMemoryTracker() {
		return new SimpleMemoryWriteTracker(vdp.getVideoMemory(), 8);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		synchronized (vdpMemoryListener) {
			BitSet changes = vdpMemoryListener.getChangedMemory();
			synchronized (changes) {
				int firstVidAddr, nextVidAddr;
				int videoIdx;
				firstVidAddr = nextVidAddr = 0;
				videoIdx = 0;
				for (int idx = changes.nextSetBit(0); idx >= 0; idx = changes.nextSetBit(idx + 1)) { 
					if (videoIdx >= videoBytes.length || idx != nextVidAddr) {
						recorder.getOutputStream().writeEvent(
								new VideoWriteDataEvent(firstVidAddr, videoBytes, videoIdx));
						firstVidAddr = nextVidAddr = idx;
						videoIdx = 0;
					}
					videoBytes[videoIdx++] = vdp.readAbsoluteVdpMemory(idx);
					nextVidAddr++;
				}
				if (videoIdx > 0) {
					recorder.getOutputStream().writeEvent(
							new VideoWriteDataEvent(firstVidAddr, videoBytes, videoIdx));
				}
			}
			vdpMemoryListener.clearChanges();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		vdpMemoryListener.removeMemoryListener();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		VideoWriteDataEvent ev = (VideoWriteDataEvent) event;
		for (int i = 0; i < ev.getLength(); i++) {
			vdp.writeAbsoluteVdpMemory(ev.getAddress() + i, 
					ev.getData()[i + ev.getOffset()]);
		}
	}

}
