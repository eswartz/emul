/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.List;

import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoPlayer;
import v9t9.common.demo.IDemoRecorder;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.FullMemoryWriteTracker;
import v9t9.common.memory.IMemoryDomain;
import v9t9.engine.demos.events.SoundWriteDataEvent;

/**
 * @author ejs
 *
 */
public class SoundMmioDataDemoActor extends BaseDemoActor {
	private FullMemoryWriteTracker soundDataListener;
	private int soundMmioAddr;
	private byte[] soundBytes = new byte[256];
	private int soundIdx;
	private IMemoryDomain console;
	
	/**
	 * 
	 */
	public SoundMmioDataDemoActor(int mmioAddr) {
		this.soundMmioAddr = mmioAddr;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return SoundWriteDataEvent.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		console = machine.getConsole();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		soundDataListener = new FullMemoryWriteTracker(console, 0);
		soundDataListener.addMemoryRange(soundMmioAddr, 1);
		soundDataListener.addMemoryListener();

		// send silence
		recorder.getOutputStream().writeEvent(
				new SoundWriteDataEvent(soundMmioAddr, 
						new byte[] { (byte) 0x9f, (byte) 0xbf, (byte) 0xdf, (byte) 0xff }));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		synchronized (soundDataListener) {
			List<Integer> changes = soundDataListener.getChanges();
			synchronized (changes) {
				for (Integer chg : changes) { 
					if (soundIdx >= soundBytes.length) {
						recorder.getOutputStream().writeEvent(
								new SoundWriteDataEvent(soundMmioAddr, soundBytes, soundIdx));
						soundIdx = 0;
					}
					soundBytes[soundIdx++] = (byte) (chg & 0xff);
				}
				if (soundIdx > 0) {
					recorder.getOutputStream().writeEvent(
							new SoundWriteDataEvent(soundMmioAddr, soundBytes, soundIdx));
					soundIdx = 0;
				}
			}
			soundDataListener.clearChanges();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		soundDataListener.removeMemoryListener();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		SoundWriteDataEvent ev = (SoundWriteDataEvent) event;
		for (int i = 0; i < ev.getLength(); i++) {
			console.writeByte(ev.getAddress(), ev.getData()[i]);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.engine.demos.actors.BaseDemoActor#cleanupPlayback(v9t9.common.demo.IDemoPlayer)
	 */
	@Override
	public void cleanupPlayback(IDemoPlayer player) {
		super.cleanupPlayback(player);
		player.getMachine().getSound().reset();

	}
}
