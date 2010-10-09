/**
 * 
 */
package v9t9.emulator.hardware.dsrs.pcode;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.ejs.coffee.core.properties.SettingProperty;
import org.ejs.coffee.core.settings.ISettingSection;

import v9t9.emulator.common.EmulatorSettings;
import v9t9.emulator.common.WorkspaceSettings;
import v9t9.emulator.common.IEventNotifier.Level;
import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.hardware.dsrs.DsrHandler9900;
import v9t9.emulator.hardware.dsrs.MemoryTransfer;
import v9t9.emulator.hardware.memory.mmio.ConsoleGramWriteArea;
import v9t9.emulator.hardware.memory.mmio.ConsoleGromReadArea;
import v9t9.emulator.hardware.memory.mmio.GplMmio;
import v9t9.engine.memory.DiskMemoryEntry;
import v9t9.engine.memory.Memory;
import v9t9.engine.memory.MemoryDomain;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class PCodeDsr implements DsrHandler9900 {

	static public final SettingProperty settingPCodeCardEnabled = new SettingProperty(
			"PCodeCardEnabled", "Enable P-Code Card", 
			"Enables the UCSD Pascal P-Code card.",
			new Boolean(false));
	private PCodeDsrRomBankedMemoryEntry dsrMemoryEntry;
	private TI99Machine machine;
	private MemoryDomain pcodeDomain;
	private GplMmio pcodeGromMmio;
	private MemoryEntry readMmioEntry;
	private MemoryEntry writeMmioEntry;
	private DiskMemoryEntry gromMemoryEntry;

	public static final String PCODE = "PCODE";
	/**
	 * @param machine
	 */
	public PCodeDsr(TI99Machine machine) {
		WorkspaceSettings.CURRENT.register(settingPCodeCardEnabled);
		this.machine = machine;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler9900#getCruBase()
	 */
	@Override
	public short getCruBase() {
		return 0x1f00;
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#activate(v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public void activate(MemoryDomain console) throws IOException {
		// DSR ROM
		if (!settingPCodeCardEnabled.getBoolean())
			return;
		
		Memory memory = console.memory;

		ensureSetup();
		
		// pCode GROMs are accessed specially
		memory.addAndMap(dsrMemoryEntry);
		memory.addAndMap(readMmioEntry);
		memory.addAndMap(writeMmioEntry);
		
		memory.addAndMap(gromMemoryEntry);
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void ensureSetup() throws IOException {
		Memory memory = machine.getMemory();
		MemoryDomain console = machine.getConsole();

		if (console.getEntryAt(0x4000) instanceof PCodeDsrRomBankedMemoryEntry)
			dsrMemoryEntry = (PCodeDsrRomBankedMemoryEntry) console.getEntryAt(0x4000);
		
		if (dsrMemoryEntry == null) {
			this.dsrMemoryEntry = (PCodeDsrRomBankedMemoryEntry) DiskMemoryEntry.newBankedWordMemoryFromFile(
					PCodeDsrRomBankedMemoryEntry.class,
					0x4000, 0x2000, memory, 
					"P-Code DSR ROM", console,
					"pCodeRomA.bin", 0, "pCodeRomB.bin", 0);
		}
		pcodeDomain = memory.getDomain(PCODE);
		if (pcodeDomain == null) {
			// P-Code GROMs are completely private to the card
			pcodeDomain = new MemoryDomain(PCODE);
			
			memory.addDomain(PCODE, pcodeDomain);
		}
		if (gromMemoryEntry == null) {
			gromMemoryEntry = DiskMemoryEntry.newByteMemoryFromFile(0, 0x10000, "PCode GROM",
					pcodeDomain, "pCodeGroms.bin", 0, false);
		}
		
		if (pcodeGromMmio == null) {
			pcodeGromMmio = new GplMmio(pcodeDomain);
			readMmioEntry = null;
			writeMmioEntry = null;
		}
		
		dsrMemoryEntry.setup(machine, pcodeGromMmio);
		
		if (readMmioEntry == null) {
			readMmioEntry = new MemoryEntry("PCode Read MMIO", pcodeDomain, 0x5800, 0x0400,
					new ConsoleGromReadArea(pcodeGromMmio));
	        writeMmioEntry = new MemoryEntry("PCode Write MMIO", pcodeDomain, 0x5C00, 0x0400,
	                new ConsoleGramWriteArea(pcodeGromMmio));
		}
		
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#deactivate(v9t9.engine.memory.MemoryDomain)
	 */
	@Override
	public void deactivate(MemoryDomain console) {
		Memory memory = console.memory;
		
		if (dsrMemoryEntry != null) {
			memory.removeAndUnmap(gromMemoryEntry);
			memory.removeAndUnmap(dsrMemoryEntry);
			memory.removeAndUnmap(readMmioEntry);
			memory.removeAndUnmap(writeMmioEntry);
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#dispose()
	 */
	@Override
	public void dispose() {

	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getEditableSettingGroups()
	 */
	@Override
	public Map<String, Collection<SettingProperty>> getEditableSettingGroups() {
		return Collections.<String, Collection<SettingProperty>>singletonMap("P-System",
				Collections.singletonList(settingPCodeCardEnabled));
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#getName()
	 */
	@Override
	public String getName() {
		return "UCSD P-System";
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.hardware.dsrs.DsrHandler#handleDSR(v9t9.emulator.hardware.dsrs.MemoryTransfer, short)
	 */
	@Override
	public boolean handleDSR(MemoryTransfer xfer, short code) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPersistable#loadState(org.ejs.coffee.core.settings.ISettingSection)
	 */
	@Override
	public void loadState(ISettingSection section) {
		if (section == null)
			return;
		
		ISettingSection sub = section.getSection("P-Code");
		if (sub == null)
			return;
		
		settingPCodeCardEnabled.loadState(sub);
		
		try {
			ensureSetup();
		} catch (IOException e) {
			machine.notifyEvent(Level.ERROR, e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.coffee.core.properties.IPersistable#saveState(org.ejs.coffee.core.settings.ISettingSection)
	 */
	@Override
	public void saveState(ISettingSection section) {
		ISettingSection sub = section.addSection("P-Code");
		settingPCodeCardEnabled.saveState(sub);
	}

}
