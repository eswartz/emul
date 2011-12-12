/**
 * 
 */
package v9t9.server.tcf.services.local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.services.IRegisters;

import v9t9.common.machine.IMachine;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.memory.IMemoryDomain;

/**
 * @author ejs
 *
 */
public class RegisterService extends BaseServiceImpl {

	/**
	 * 
	 */
	private static final String ROOT = "root";
	private static final String SET = "set";
	private static final String GET = "get";
	private static final String GET_CHILDREN = "getChildren";
	private static final String GET_CONTEXT = "getContext";

	public RegisterService(IMachine machine, IChannel channel,
			String serviceName) {
		super(machine, channel, serviceName);
		
		registerCommand(GET_CONTEXT, 1, 2);
		registerCommand(GET_CHILDREN, 1, 2);
		registerCommand(GET, 1, 2);
		registerCommand(SET, 2, 2);
	}

	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (GET_CONTEXT.equals(name)) {
			return doGetContext(args);
		}
		else if (GET_CHILDREN.equals(name)) {
			return doGetChildren(args);
		}
		else if (GET.equals(name)) {
			return doGet(args);
		}
		else if (SET.equals(name)) {
			return doSet(args);
		}
		return null;
	}

	/**
	 * @param args
	 */
	private Object[] doGetContext(Object[] args) throws Exception {
		// args: contextId
		// args: object of attributes
		
		IRegisterAccess access = getAccessOrError(args[0]);
		Map<String, Object> context = null;
		if (access != null) {
			context = createContext(null, args[0].toString(), access);
		}
		return new Object[] { null, context };
	}

	protected Map<String, Object> createContext(String parent,
			String id,
			IRegisterAccess access) {
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put(IRegisters.PROP_ID, id);
		ctx.put(IRegisters.PROP_PARENT_ID, parent);
		ctx.put(IRegisters.PROP_NAME, access.getGroupName());
		return ctx;
	}

	/**
	 * @param object
	 * @return
	 */
	protected IRegisterAccess getAccessOrError(Object object) throws ErrorReport {
		if (object == null)
			return null;
		String id = object.toString();
		if (id.length() == 0 || id.equals(ROOT))
			return null;
		
		if (IMemoryDomain.NAME_CPU.equals(id)) {
			return machine.getCpu().getState();
		} else if (IMemoryDomain.NAME_VIDEO.equals(id)) {
			return machine.getVdp();
		}
		
		throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		
		
	}

	/**
	 * @param args
	 */
	private Object[] doGetChildren(Object[] args) throws Exception {
		// args:  context
		// ret: list
		IRegisterAccess access = getAccessOrError(args[0]);
		
		List<String> kids = new ArrayList<String>();
		for (int i = 0; i < access.getRegisterCount(); i++)
			kids.add(access.getRegisterInfo(i).id);
		return new Object[] { null, kids };
	}

	/**
	 * @param args
	 */
	private Object[] doGet(Object[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @param args
	 * @return 
	 */
	private Object[] doSet(Object[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
