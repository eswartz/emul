/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.List;

import v9t9.common.demos.IDemoEvent;
import v9t9.common.demos.IDemoPlayer;
import v9t9.common.demos.IDemoRecorder;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.machine.FullRegisterWriteTracker;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.engine.demos.events.SoundWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public class SoundRegisterDemoActor extends BaseDemoActor {
	private FullRegisterWriteTracker soundRegisterListener;
	private ISoundChip sound;
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return SoundWriteRegisterEvent.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		sound = machine.getSound();		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		soundRegisterListener = new FullRegisterWriteTracker(sound);
		soundRegisterListener.addRegisterListener();
		
		// send sound regs 
		IRegisterAccess sra = sound;
		int slastReg = sra.getFirstRegister() + sra.getRegisterCount();
		for (int i = sra.getFirstRegister(); i < slastReg; i++) {
			recorder.getOutputStream().writeEvent(
					new SoundWriteRegisterEvent(i, sra.getRegister(i)));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		synchronized (soundRegisterListener) {
			List<Long> changes = soundRegisterListener.getChanges();
			synchronized (changes) {
				for (Long ent : changes) {
					recorder.getOutputStream().writeEvent(
							new SoundWriteRegisterEvent(
									(int) (ent >> 32), (int) (ent & 0xffffffff)));
				}
			}
			soundRegisterListener.clearChanges();
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		soundRegisterListener.removeRegisterListener();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		SoundWriteRegisterEvent ev = (SoundWriteRegisterEvent) event;
		sound.setRegister(ev.getReg(), ev.getVal());
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
