/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;

import v9t9.common.demo.IDemoActor;
import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoPlayer;
import v9t9.common.demo.IDemoRecorder;
import v9t9.common.hardware.IVdpV9938;
import v9t9.common.hardware.IVdpV9938.IAccelListener;
import v9t9.common.machine.IMachine;
import v9t9.engine.demos.events.VdpV9938AccelCommandEvent;
import v9t9.engine.demos.events.VideoAccelCommandEvent;

/**
 * @author ejs
 * @deprecated does not work as expected yet
 */
public class VdpV9938AccelDemoActor implements IDemoActor {
	
	private IAccelListener accelListener;
	private IVdpV9938 vdp;
//	private IDemoActor videoDataActor;
//	private IDemoActor videoRegsActor;

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return VdpV9938AccelCommandEvent.ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#setup(v9t9.common.machine.IMachine)
	 */
	@Override
	public void setup(IMachine machine) {
		vdp = (IVdpV9938) machine.getVdp();		
	}
	
	@Override
	public void connectForRecording(final IDemoRecorder recorder) {
//		videoDataActor = recorder.getMachine().getDemoManager().findActor(DemoFormat.VIDEO_DATA);
//		videoRegsActor = recorder.getMachine().getDemoManager().findActor(DemoFormat.VIDEO_REGS);
		
		accelListener = new IVdpV9938.IAccelListener() {
			
			@Override
			public void accelCommandStarted() {
				// flush so the command can proceed
				try {
					flushVideoDataAndRegs(recorder);
					
					recorder.getOutputStream().writeEvent(
							new VdpV9938AccelCommandEvent(VideoAccelCommandEvent.ACCEL_STARTED));
				} catch (IOException e) {
					recorder.fail(e);
				}
				
			}
			
			@Override
			public void accelCommandWork() {
				try {
					flushVideoDataAndRegs(recorder);
					
					recorder.getOutputStream().writeEvent(
							new VdpV9938AccelCommandEvent(VideoAccelCommandEvent.ACCEL_WORK));
				} catch (IOException e) {
					recorder.fail(e);
				}						
			}
			
			@Override
			public void accelCommandEnded() {
				try {
					flushVideoDataAndRegs(recorder);
					
					recorder.getOutputStream().writeEvent(
							new VdpV9938AccelCommandEvent(VideoAccelCommandEvent.ACCEL_ENDED));
					
				} catch (IOException e) {
					recorder.fail(e);
				}
			}
		};
		vdp.addAccelListener(accelListener);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	protected void flushVideoDataAndRegs(IDemoRecorder recorder) throws IOException {
		recorder.flushData();
//		if (videoRegsActor != null) {
//			videoRegsActor.flushRecording(recorder);
//		}
//		if (videoDataActor != null) {
//			videoDataActor.flushRecording(recorder);
//		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoOutputEventBuffer)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder)
			throws IOException {
		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.machine.IMachine)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {

		if (accelListener != null)
			vdp.removeAccelListener(accelListener);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		VdpV9938AccelCommandEvent ev = (VdpV9938AccelCommandEvent) event;
		
		switch (ev.getCode()) {
		case VdpV9938AccelCommandEvent.ACCEL_STARTED:
			// wait for previous command to finish 
			while (vdp.isAccelActive()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
			break;
		case VdpV9938AccelCommandEvent.ACCEL_WORK:
			vdp.work();
			break;
		case VdpV9938AccelCommandEvent.ACCEL_ENDED:
			// poll until the command is finished
			while (vdp.isAccelActive()) {
				try {
//					IMachine machine = player.getMachine();
//					ICpu cpu = machine.getCpu();
//					
//					cpu.tick();
//					int cycles = (int) (cpu.getBaseCyclesPerSec() / player.getInputStream().getTimerRate());
//					
//					IVdpChip vdp = machine.getVdp();
//					vdp.addCpuCycles(cycles);
//					vdp.tick();
//					vdp.syncVdpInterrupt(machine);
//					
//					vdp.work();
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
			break;
		default:
			break;	
		}

	}
}
