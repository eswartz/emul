package v9t9.engine.dsr;

import java.util.ArrayList;
import java.util.List;


import v9t9.base.settings.ISettingSection;
import v9t9.common.dsr.IDsrHandler;
import v9t9.common.dsr.IDsrManager;
import v9t9.common.machine.IMachine;

public abstract class DsrManager implements IDsrManager {

	protected final IMachine machine;
	protected List<IDsrHandler> dsrs;
	protected IDsrHandler activeDsr;

	public DsrManager(IMachine machine) {
		super();
		this.machine = machine;
		dsrs = new ArrayList<IDsrHandler>();

	}

	public void dispose() {
		for (IDsrHandler dsr : dsrs) {
			dsr.dispose();
		}
	}

	public void saveState(ISettingSection section) {
		for (IDsrHandler handler : dsrs) {
			handler.saveState(section.addSection(handler.getName()));
		}
	}

	public void loadState(ISettingSection section) {
		if (section == null) return;
		for (IDsrHandler handler : dsrs) {
			handler.loadState(section.getSection(handler.getName()));
		}
	}

	public List<IDsrHandler> getDsrs() {
		return dsrs;
	}

	public void registerDsr(IDsrHandler dsr) {
		this.dsrs.add(dsr);
	}
}