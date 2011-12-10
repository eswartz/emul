/**
 * 
 */
package v9t9.machine.ti99.machine;

import java.util.ArrayList;
import java.util.List;

import v9t9.common.client.ISoundHandler;
import v9t9.common.cpu.ICpu;
import v9t9.common.dsr.IDeviceIndicatorProvider;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IDeviceSettings;
import v9t9.common.dsr.IDsrManager;
import v9t9.common.hardware.ISpeechChip;
import v9t9.common.machine.IMachine;
import v9t9.common.machine.IMachineModel;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.settings.Settings;
import v9t9.engine.speech.ISpeechDataSender;
import v9t9.engine.speech.SpeechVoice;
import v9t9.engine.speech.TMS5220;
import v9t9.machine.ti99.cpu.Cpu9900;

/**
 * @author ejs
 *
 */
public abstract class BaseTI99MachineModel implements IMachineModel {
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getCPU()
	 */
	@Override
	public ICpu createCPU(IMachine machine) {
		return new Cpu9900(machine, 1000 / machine.getCpuTicksPerSec(), machine.getVdp());
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDsrSettings()
	 */
	@Override
	public List<IDeviceSettings> getDeviceSettings(IMachine machine) {
		List<IDeviceSettings> settings = new ArrayList<IDeviceSettings>();
		IDsrManager dsrManager = machine instanceof TI99Machine ? ((TI99Machine) machine).getDsrManager() : null;
		if (dsrManager != null)  {
			for (IDsrHandler handler : dsrManager.getDsrs()) {
				settings.add(handler);
			}
		}
		return settings;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getDeviceIndicatorProviders()
	 */
	@Override
	public List<IDeviceIndicatorProvider> getDeviceIndicatorProviders(IMachine machine) {
		List<IDeviceIndicatorProvider> list = new ArrayList<IDeviceIndicatorProvider>();
		IDsrManager dsrManager = machine instanceof TI99Machine ? ((TI99Machine) machine).getDsrManager() : null;
		if (dsrManager != null)  {
			for (IDsrHandler handler : dsrManager.getDsrs()) {
				list.addAll(handler.getDeviceIndicatorProviders());
			}
		}
		return list;
	}
	

	/* (non-Javadoc)
	 * @see v9t9.engine.machine.MachineModel#createSpeechChip(v9t9.engine.machine.IMachine)
	 */
	@Override
	public ISpeechChip createSpeechChip(final IMachine machine) {
		IMemoryDomain domain = machine.getMemory().getDomain(IMemoryDomain.NAME_SPEECH);
		if (domain == null)
			return null;
		final TMS5220 speech = new TMS5220(Settings.getSettings(machine), domain);
		
		speech.setSender(new ISpeechDataSender() {

			private ISoundHandler soundHandler;

			public void send(short val, int pos, int length) {
				((SpeechVoice) speech.getSpeechVoices()[0]).setSample(val);
				
				if (soundHandler == null)
					soundHandler = machine.getSound().getSoundHandler();
				if (soundHandler != null)
					soundHandler.speech();
			}
		});
		return speech;
	}
}