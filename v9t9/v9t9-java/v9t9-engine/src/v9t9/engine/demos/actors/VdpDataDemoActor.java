/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.BitSet;
import java.util.LinkedList;

import v9t9.common.demos.IDemoActorProvider;
import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlaybackActor;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.demos.IDemoRecordingActor;
import v9t9.common.demos.IDemoReversePlaybackActor;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.ByteMemoryAccess;
import v9t9.common.memory.SimpleMemoryWriteTracker;
import v9t9.engine.demos.events.VideoWriteDataEvent;

/**
 * @author ejs
 *
 */
public class VdpDataDemoActor extends BaseDemoActor implements IDemoReversePlaybackActor {
	public static class Provider implements IDemoActorProvider {
		@Override
		public String getEventIdentifier() {
			return VideoWriteDataEvent.ID;
		}
		@Override
		public IDemoPlaybackActor createForPlayback() {
			return new VdpDataDemoActor();
		}
		@Override
		public IDemoRecordingActor createForRecording() {
			return new VdpDataDemoActor();
		}
		@Override
		public IDemoReversePlaybackActor createForReversePlayback() {
			return new VdpDataDemoActor();
		}
		
	}

	
	protected SimpleMemoryWriteTracker vdpMemoryListener;
	protected IVdpChip vdp;
	private byte[] videoBytes = new byte[256];
	private LinkedList<IDemoEvent> reversedEventList;
	
	
	
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

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#setupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void setupReversePlayback(IDemoPlayer player) {
		reversedEventList = new LinkedList<IDemoEvent>();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#queueEventForReversing(v9t9.common.demos.IDemoPlayer, v9t9.common.demos.IDemoEvent)
	 */
	@Override
	public void queueEventForReversing(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		VideoWriteDataEvent ev = (VideoWriteDataEvent) event;
		byte[] oldBytes = new byte[ev.getLength()];
		for (int i = 0; i < ev.getLength(); i++) {
			byte byt = vdp.readAbsoluteVdpMemory(ev.getAddress() + i);
			oldBytes[i] = byt;
		}
		reversedEventList.add(new VideoWriteDataEvent(ev.getAddress(), oldBytes));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#emitReversedEvents(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public IDemoEvent[] emitReversedEvents(IDemoPlayer player)
			throws IOException {
		IDemoEvent[] evs = (IDemoEvent[]) reversedEventList.toArray(new IDemoEvent[reversedEventList.size()]);
		reversedEventList.clear();
		return evs;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demos.IDemoReversePlaybackActor#cleanupReversePlayback(v9t9.common.demos.IDemoPlayer)
	 */
	@Override
	public void cleanupReversePlayback(IDemoPlayer player) {
		reversedEventList = null;
	}

}
