/**
 * 
 */
package v9t9.engine.demos.actors;

import java.io.IOException;
import java.util.Map;

import v9t9.common.demo.IDemoActor;
import v9t9.common.demo.IDemoEvent;
import v9t9.common.demo.IDemoPlayer;
import v9t9.common.demo.IDemoRecorder;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.machine.SimpleRegisterWriteTracker;
import v9t9.engine.demos.events.VideoWriteRegisterEvent;

/**
 * @author ejs
 *
 */
public class VdpRegisterDemoActor implements IDemoActor {

	private IVdpChip vdp;
	private SimpleRegisterWriteTracker vdpRegisterListener;

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#getEventIdentifier()
	 */
	@Override
	public String getEventIdentifier() {
		return VideoWriteRegisterEvent.ID;
	}
	
	@Override
	public void setup(IMachine machine) {
		vdp = machine.getVdp();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#connectForRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void connectForRecording(IDemoRecorder recorder) throws IOException {
		vdpRegisterListener = new SimpleRegisterWriteTracker(vdp,
				vdp.getFirstRegister(),
				vdp.getRecordableRegs());
		vdpRegisterListener.addRegisterListener();		
		

		// send video regs
		IRegisterAccess vra = vdp;
		int lastReg = vra.getFirstRegister() + vra.getRegisterCount();
		for (int i = vra.getFirstRegister(); i < lastReg; i++) {
			recorder.getOutputStream().writeEvent(
					new VideoWriteRegisterEvent(i, vra.getRegister(i)));
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#flushRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void flushRecording(IDemoRecorder recorder) throws IOException {
		synchronized (vdpRegisterListener) {
			Map<Integer, Integer> changes = vdpRegisterListener.getChanges();
			synchronized (changes) {
				for (Map.Entry<Integer, Integer> chg : changes.entrySet()) {
					if (chg.getKey() >= 0) {
						recorder.getOutputStream().writeEvent(
								new VideoWriteRegisterEvent(chg.getKey(), chg.getValue()));
					}
				}
				vdpRegisterListener.clearChanges();
			}
		}		
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#disconnectFromRecording(v9t9.common.demo.IDemoRecorder)
	 */
	@Override
	public void disconnectFromRecording(IDemoRecorder recorder) {
		vdpRegisterListener.removeRegisterListener();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoActor#executeEvent(v9t9.common.demo.IDemoPlayer, v9t9.common.demo.IDemoEvent)
	 */
	@Override
	public void executeEvent(IDemoPlayer player, IDemoEvent event)
			throws IOException {
		VideoWriteRegisterEvent ev = (VideoWriteRegisterEvent) event;
		vdp.setRegister(ev.getReg(), ev.getVal());		
	}

}
