/**
 * 
 */
package v9t9.machine.ti99.machine;


import java.net.URL;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModuleManager;
import v9t9.common.settings.Settings;
import v9t9.engine.files.directory.DiskDirectoryMapper;
import v9t9.engine.modules.ModuleManager;
import v9t9.engine.sound.SoundTMS9919;
import v9t9.engine.video.tms9918a.VdpTMS9918A;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.ti99.dsr.emudisk.EmuDiskDsr;
import v9t9.machine.ti99.dsr.pcode.PCodeDsr;
import v9t9.machine.ti99.dsr.realdisk.RealDiskImageDsr;
import v9t9.machine.ti99.memory.TI994AStandardConsoleMemoryModel;

/**
 * @author ejs
 *
 */
public class StandardMachineModel extends BaseTI99MachineModel {

	public static final String ID = "StandardTI994A";

	public StandardMachineModel() {
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#createMachine()
	 */
	@Override
	public IMachine createMachine(ISettingsHandler settings) {
		return new TI994A(settings, this);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new TI994AStandardConsoleMemoryModel(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	public IVdpChip createVdp(IMachine machine) {
		VdpTMS9918A vdp = new VdpTMS9918A(machine);
		return vdp;
	}
	
	public void defineDevices(IMachine machine_) {
		Settings.get(machine_, IKeyboardState.settingBackspaceIsCtrlH).setBoolean(false);
		
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.setCru(new InternalCru9901(machine, machine.getKeyboardState()));
			
			EmuDiskDsr emudsr = new EmuDiskDsr(Settings.getSettings(machine_), 
					DiskDirectoryMapper.INSTANCE);
			machine.getDsrManager().registerDsr(emudsr);
			RealDiskImageDsr diskdsr = new RealDiskImageDsr(machine, (short) 0x1100);
			machine.getDsrManager().registerDsr(diskdsr);
			
			PCodeDsr pcodedsr = new PCodeDsr(machine);
			machine.getDsrManager().registerDsr(pcodedsr);
		}
	}

	public ISoundChip createSoundChip(IMachine machine) {
		return new SoundTMS9919(machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createModuleManager(v9t9.common.machine.IMachine)
	 */
	@Override
	public IModuleManager createModuleManager(IMachine machine) {
		return new ModuleManager(machine, "stock_modules.xml");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDataURL()
	 */
	@Override
	public URL getDataURL() {
		return EmulatorMachinesData.getDataURL("ti99/");
	}
}
