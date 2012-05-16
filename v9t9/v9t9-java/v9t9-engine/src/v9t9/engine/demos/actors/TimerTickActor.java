/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;

import ejs.base.properties.IProperty;

import v9t9.common.cpu.ICpu;
import v9t9.common.demo.IDemoActor;
import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.demo.IDemoPlayer;
import v9t9.common.demo.IDemoRecorder;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.events.TimerTick;

/**
 * @author ejs
 *
 */
public class TimerTickActor implements IDemoActor {

	private Runnable timerTask;
	private IProperty pauseDemoSetting;
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return TimerTick.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(final IDemoRecorder recorder) throws IOException {
		pauseDemoSetting = Settings.get(recorder.getMachine(), 
				IDemoHandler.settingDemoPaused);
		
		timerTask = new Runnable() {
			
			@Override
			public void run() {
				try {
					synchronized (recorder) {
						IDemoOutputStream os = recorder.getOutputStream();
						if (os != null) {
							recorder.flushData();
						}
					}
				} catch (final Throwable t) {
					recorder.fail(t);
				}				
			}
		};
		
		recorder.getMachine().getFastMachineTimer().scheduleTask(
				timerTask, recorder.getOutputStream().getTimerRate());
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		if (!pauseDemoSetting.getBoolean()) {
			recorder.getOutputStream().writeEvent(
					new TimerTick(recorder.getOutputStream().getElapsedTime()));
		}

	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		recorder.getMachine().getFastMachineTimer().
			cancelTask(timerTask);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		// contribute time, so sound, etc. will proceed
		IMachine machine = player.getMachine();
		ICpu cpu = machine.getCpu();
		
		cpu.tick();
		int cycles = (int) (cpu.getBaseCyclesPerSec() / player.getInputStream().getTimerRate());
		
		IVdpChip vdp = machine.getVdp();
		vdp.addCpuCycles(cycles);
		vdp.tick();
		vdp.syncVdpInterrupt(machine);
	}

}
