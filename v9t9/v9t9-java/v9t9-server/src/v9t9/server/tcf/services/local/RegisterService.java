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
import org.eclipse.tm.tcf.protocol.JSON;
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

	protected RegisterService(IMachine machine, IChannel channel, String name) {
		super(machine, channel, name);
		
		registerCommand(GET_CONTEXT, 1, 2);
		registerCommand(GET_CHILDREN, 1, 2);
		registerCommand(GET, 1, 2);
		registerCommand(SET, 2, 1);
	}
	
	public RegisterService(IMachine machine, IChannel channel) {
		this(machine, channel, IRegisters.NAME);
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
		
		String id = null;
		if (args[0] != null)
			id = args[0].toString();

		String group = getGroupContext(id);
		IRegisterAccess access = getAccessOrError(group);
		Map<String, Object> context = null;
		if (access != null) {
			if (group == null || group.equals(id))
				context = createGroupContext(group, access);
			else
				context = createRegisterContext(group, id.substring(group.length() + 1), access);
		}
		return new Object[] { null, context };
	}

	protected Map<String, Object> createGroupContext(
			String id,
			IRegisterAccess access) {
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put(IRegisters.PROP_ID, id);
		ctx.put(IRegisters.PROP_NAME, access.getGroupName());
		return ctx;
	}
	
	protected Map<String, Object> createRegisterContext(String parent,
			String id,
			IRegisterAccess access) throws ErrorReport {
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put(IRegisters.PROP_ID, parent + "." + id);
		ctx.put(IRegisters.PROP_PARENT_ID, parent);
		
		int reg = access.getRegisterNumber(id);
		if (reg == Integer.MIN_VALUE) {
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		
		}
		IRegisterAccess.RegisterInfo info = access.getRegisterInfo(reg);
		ctx.put(IRegisters.PROP_NAME, info.description);
		ctx.put(IRegisters.PROP_BIG_ENDIAN, true);
		ctx.put(IRegisters.PROP_SIZE, info.size);
		
		String role = IRegisters.ROLE_CORE;
		
		int roleNum = info.flags & IRegisterAccess.FLAG_ROLE_MASK;
		switch (roleNum) {
		case IRegisterAccess.FLAG_ROLE_FP:
			role = IRegisters.ROLE_FP;
			break;
		case IRegisterAccess.FLAG_ROLE_PC:
			role = IRegisters.ROLE_PC;
			break;
		case IRegisterAccess.FLAG_ROLE_RET:
			role = IRegisters.ROLE_RET;
			break;
		case IRegisterAccess.FLAG_ROLE_SP:
			role = IRegisters.ROLE_SP;
			break;
		}
		ctx.put(IRegisters.PROP_ROLE, role);
		
		if (info.domain != null) {
			ctx.put(IRegisters.PROP_MEMORY_CONTEXT, info.domain.getIdentifier());
			ctx.put(IRegisters.PROP_MEMORY_ADDRESS, info.addr);
			
		}
		return ctx;
	}

	/**
	 * @param object
	 * @return
	 */
	protected IRegisterAccess getAccessOrError(String id) throws ErrorReport {
		if (id == null || id.length() == 0 || id.equals(ROOT))
			throw new ErrorReport("Invalid context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
		
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
		String id = null;
		if (args[0] != null)
			id = args[0].toString();

		if (id == null || id.length() == 0 || id.equals(ROOT)) {
			return new Object[] { null, 
					new String[] { IMemoryDomain.NAME_CPU, IMemoryDomain.NAME_VIDEO } };
		}
		IRegisterAccess access = getAccessOrError(id);
		if (access == null)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		
			
		String pfx = args[0].toString() + ".";
		List<String> kids = new ArrayList<String>();
		for (int i = 0; i < access.getRegisterCount(); i++)
			kids.add(pfx + access.getRegisterInfo(i + access.getFirstRegister()).id);
		return new Object[] { null, kids };
	}

	/**
	 * @param args
	 */
	private Object[] doGet(Object[] args) throws Exception {
		String id = null;
		if (args[0] != null)
			id = args[0].toString();

		String[] parts = id.split("\\.");
		if (parts.length != 2)
			throw new ErrorReport("Bad context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
		
		IRegisterAccess access = getAccessOrError(parts[0]);
		if (access == null)
			throw new ErrorReport("Unknown group context " + parts[0], IErrorReport.TCF_ERROR_INV_CONTEXT);		
		
		int num = access.getRegisterNumber(parts[1]);
		if (num == Integer.MIN_VALUE)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		

		int value = access.getRegister(num);
		
		int size = access.getRegisterInfo(num).size;
		byte[] data = new byte[size];
		
		for (int i = 0; i < size; i++) {
			data[size - i - 1] = (byte) value;
			value >>= 8;
		}
		return new Object[] { null, new JSON.Binary(data, 0, size) };
		
		
	}

	/**
	 * @param args
	 * @return 
	 */
	private Object[] doSet(Object[] args) throws Exception {
		String id = null;
		if (args[0] != null)
			id = args[0].toString();

		String[] parts = id.split("\\.");
		if (parts.length != 2)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);

		IRegisterAccess access = getAccessOrError(parts[0]);
		if (access == null)
			throw new ErrorReport("Unknown group context " + parts[0], IErrorReport.TCF_ERROR_INV_CONTEXT);

		int num = access.getRegisterNumber(parts[1]);
		if (num == Integer.MIN_VALUE)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		

		byte[] data = toByteArray(args[1]);
		int value = 0;
		for (int i = 0; i < data.length; i++)
			value = (value << 8) | (data[i] & 0xff);
		
		access.setRegister(num, value);
		
		return new Object[] { null };
		
	}

	protected String getGroupContext(String id) {
		if (id == null)
			return null;
		int idx = id.indexOf('.');
		if (idx < 0)
			return id;
		return id.substring(0, idx);
	}

}
