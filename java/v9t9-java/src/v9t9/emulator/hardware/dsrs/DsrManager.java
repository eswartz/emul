package v9t9.emulator.hardware.dsrs;

import java.util.ArrayList;
import java.util.List;

import org.ejs.coffee.core.properties.IPersistable;
import org.ejs.coffee.core.settings.ISettingSection;
import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.common.Machine;
import v9t9.emulator.hardware.TI99Machine;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand9900;

public abstract class DsrManager implements IDsrManager {

	protected final Machine machine;
	protected List<DsrHandler> dsrs;
	protected DsrHandler activeDsr;

	public DsrManager(Machine machine) {
		super();
		this.machine = machine;
		dsrs = new ArrayList<DsrHandler>();

	}

	public void dispose() {
		for (DsrHandler dsr : dsrs) {
			dsr.dispose();
		}
	}

	public void saveState(ISettingSection section) {
		for (DsrHandler handler : dsrs) {
			handler.saveState(section.addSection(handler.getName()));
		}
	}

	public void loadState(ISettingSection section) {
		if (section == null) return;
		for (DsrHandler handler : dsrs) {
			handler.loadState(section.getSection(handler.getName()));
		}
	}

	public List<DsrHandler> getDsrs() {
		return dsrs;
	}

	public void registerDsr(DsrHandler dsr) {
		this.dsrs.add(dsr);
	}
}