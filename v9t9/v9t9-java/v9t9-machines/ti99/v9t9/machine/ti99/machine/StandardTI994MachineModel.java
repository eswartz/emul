/*
  StandardMachineModel.java

  (c) 2017 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.machine;


import java.net.URL;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.dsr.ISelectableDsrHandler;
import v9t9.common.hardware.IGplChip;
import v9t9.common.hardware.ISoundChip;
import v9t9.common.hardware.IVdpChip;
import v9t9.common.keyboard.IKeyboardState;
import v9t9.common.keyboard.KeyboardConstants;
import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryModel;
import v9t9.common.modules.IModuleManager;
import v9t9.engine.dsr.rs232.RS232Controllers;
import v9t9.engine.files.image.FDCControllers;
import v9t9.engine.files.image.RealDiskSettings;
import v9t9.engine.memory.GplChip;
import v9t9.engine.modules.ModuleManager;
import v9t9.engine.sound.SoundTMS9919;
import v9t9.engine.video.tms9918a.VdpTMS9918A;
import v9t9.machine.EmulatorMachinesData;
import v9t9.machine.dsr.rs232.RS232Settings;
import v9t9.machine.printer.EpsonPrinterImageEngine;
import v9t9.machine.printer.PIOPrinterImageHandler;
import v9t9.machine.printer.PrinterImageActorProvider;
import v9t9.machine.printer.RS232PrinterImageHandler;
import v9t9.machine.ti99.dsr.emudisk.EmuDiskDsr;
import v9t9.machine.ti99.dsr.pcode.PCodeDsr;
import v9t9.machine.ti99.dsr.realdisk.CorcompDiskImageDsr;
import v9t9.machine.ti99.dsr.realdisk.TIDiskImageDsr;
import v9t9.machine.ti99.dsr.rs232.RS232Regs;
import v9t9.machine.ti99.dsr.rs232.TIRS232Dsr;
import v9t9.machine.ti99.dsr.rs232.TIRS232PIODsr;
import v9t9.machine.ti99.memory.TI994AStandardConsoleMemoryModel;
import v9t9.machine.ti99.memory.TI994StandardConsoleMemoryModel;

/**
 * @author ejs
 *
 */
public class StandardTI994MachineModel extends BaseTI99MachineModel {

	public static final String ID = "StandardTI994";

	public StandardTI994MachineModel() {
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getIdentifier()
	 */
	@Override
	public String getIdentifier() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getName()
	 */
	@Override
	public String getName() {
		return "TI-99/4";
	}
	
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#createMachine()
	 */
	@Override
	public IMachine createMachine(ISettingsHandler settings) {
		return new TI994(settings, this);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getMemoryModel()
	 */
	@Override
	public IMemoryModel createMemoryModel(IMachine machine) {
		return new TI994StandardConsoleMemoryModel(machine);
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.MachineModel#getVdp()
	 */
	@Override
	public IVdpChip createVdp(IMachine machine) {
		VdpTMS9918A vdp = new VdpTMS9918A(machine);
		return vdp;
	}

	@Override
	public IGplChip createGpl(IMachine machine) {
		return new GplChip(machine, machine.getMemory().getDomain(IMemoryDomain.NAME_GRAPHICS));
	}
	
	@Override
	public void defineDevices(IMachine machine_) {
		machine_.getKeyboardState().registerMapping(KeyboardConstants.KEY_BACKSPACE,
				new int[] { KeyboardConstants.KEY_ALT, 'S' });
		
		machine_.setKeyboardMapping(new StandardTI994AKeyboardMapping());
		
		if (machine_ instanceof TI99Machine) {
			TI99Machine machine = (TI99Machine) machine_;
			machine.setCru(new InternalCru9901(machine));
			
			ISelectableDsrHandler diskDsr = new Selectable9900Dsr(machine, 
					machine.getSettings().get(RealDiskSettings.diskController),
					FDCControllers.WDC1771, new TIDiskImageDsr(machine, (short) 0x1100),
					FDCControllers.WDC1791, new CorcompDiskImageDsr(machine, (short) 0x1100));
			machine.getDsrManager().registerDsr(diskDsr);
			
			EmuDiskDsr emuDsr = new EmuDiskDsr(machine, 0x1000);
			machine.getDsrManager().registerDsr(emuDsr);
			
			TIRS232Dsr rs232Dsr = new TIRS232Dsr(machine, RS232Regs.CRU_BASE);
			TIRS232PIODsr rs232PioDsr = new TIRS232PIODsr(machine, RS232Regs.CRU_BASE);

			ISelectableDsrHandler rsDsr = new Selectable9900Dsr(machine, 
					machine.getSettings().get(RS232Settings.rs232Controller),
					RS232Controllers.RS232_ONLY, rs232Dsr,
					RS232Controllers.RS232_PIO, rs232PioDsr);
			machine.getDsrManager().registerDsr(rsDsr);
			
			// printer works on PIO or RS232/1 for either DSR
			EpsonPrinterImageEngine engine = new EpsonPrinterImageEngine(machine.getSettings());
			machine.getDemoManager().registerActorProvider(new PrinterImageActorProvider(engine.getPrinterId()));
			
			PIOPrinterImageHandler handler = new PIOPrinterImageHandler(machine, engine);
			machine.addPrinterImageHandler(handler);
			rs232PioDsr.getPIODevice(1).getPIO().getHandler().addListener(handler);
			
			RS232PrinterImageHandler rsHandler = new RS232PrinterImageHandler(machine, engine); 
			machine.addPrinterImageHandler(rsHandler);
			rs232PioDsr.getRS232Device(1).getRS232().getHandler().addListener(rsHandler);
			
			rs232Dsr.getRS232Device(1).getRS232().getHandler().addListener(rsHandler);
			
			PCodeDsr pcodeDsr = new PCodeDsr(machine);
			machine.getDsrManager().registerDsr(pcodeDsr);
			
		}
	}

	@Override
	public ISoundChip createSoundChip(IMachine machine) {
		return new SoundTMS9919(machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#createModuleManager(v9t9.common.machine.IMachine)
	 */
	@Override
	public IModuleManager createModuleManager(IMachine machine) {
		return new ModuleManager(machine,
				new String[] { "stock_modules.xml", "problem_modules.xml" });
	}

	@Override
	public IKeyboardState createKeyboardState(IMachine machine) {
		return new KeyboardState994(machine);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#getDataURL()
	 */
	@Override
	public URL getDataURL() {
		return EmulatorMachinesData.getDataURL("ti99/");
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.machine.IMachineModel#isModelCompatible(java.lang.String)
	 */
	@Override
	public boolean isModelCompatible(String machineModel) {
		return machineModel.equals(ID);
	}
}
