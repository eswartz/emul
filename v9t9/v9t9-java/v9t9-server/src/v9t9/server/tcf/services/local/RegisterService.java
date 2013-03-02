/*
  RegisterService.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.server.tcf.services.local;

import java.io.ByteArrayOutputStream;
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
import v9t9.common.machine.IRegisterAccess.RegisterInfo;
import v9t9.server.tcf.services.IRegistersV2;

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
	private static final String SETM = "setm";
	private static final String GETM = "getm";
	private static final String GET_CHILDREN = "getChildren";
	private static final String GET_CONTEXT = "getContext";

	protected RegisterService(IMachine machine, IChannel channel, String name) {
		super(machine, channel, name);
		
		registerCommand(GET_CONTEXT, 1, 2);
		registerCommand(GET_CHILDREN, 1, 2);
		registerCommand(GET, 1, 2);
		registerCommand(SET, 2, 1);
		registerCommand(GETM, 1, 2);
		registerCommand(SETM, 2, 1);
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
		else if (GETM.equals(name)) {
			return doGetm(args);
		}
		else if (SETM.equals(name)) {
			return doSetm(args);
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
		
		if ((info.flags & IRegisterAccess.FLAG_VOLATILE) != 0) {
			ctx.put(IRegisters.PROP_VOLATILE, true);
		}
		if ((info.flags & IRegisterAccess.FLAG_SIDE_EFFECTS) != 0) {
			ctx.put(IRegisters.PROP_SIDE_EFFECTS, true);
		}
		
		// IRegistersV2 special
		ctx.put(IRegistersV2.PROP_NUMBER, reg);
		
		return ctx;
	}

	/**
	 * @param object
	 * @return
	 */
	protected IRegisterAccess getAccessOrError(String id) throws ErrorReport {
		if (id == null || id.length() == 0 || id.equals(ROOT))
			throw new ErrorReport("Invalid context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
		
		if (IRegisterAccess.ID_CPU.equals(id)) {
			return machine.getCpu().getState();
		} else if (IRegisterAccess.ID_VIDEO.equals(id)) {
			return machine.getVdp();
		} else if (IRegisterAccess.ID_SOUND.equals(id)) {
			return machine.getSound();
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
					new String[] { 
					IRegisterAccess.ID_CPU, 
					IRegisterAccess.ID_VIDEO,
					IRegisterAccess.ID_SOUND 
				} 
			};
		}
		IRegisterAccess access = getAccessOrError(id);
		if (access == null)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		
			
		String pfx = args[0].toString() + ".";
		List<String> kids = new ArrayList<String>();
		for (int i = 0; i < access.getRegisterCount(); i++) {
			RegisterInfo registerInfo = access.getRegisterInfo(i + access.getFirstRegister());
			assert registerInfo != null && registerInfo.id != null;
			kids.add(pfx + registerInfo.id);
		}
		return new Object[] { null, kids };
	}

	/**
	 * @param args
	 */
	private Object[] doGet(Object[] args) throws Exception {
		String id = null;
		if (args[0] != null)
			id = args[0].toString();

		ByteArrayOutputStream bos = new ByteArrayOutputStream(4);
		
		doGetReg(id, 0, 0, bos);
		
		return new Object[] { null, new JSON.Binary(bos.toByteArray(), 0, bos.size()) };
		
		
	}

	/**
	 * @param id
	 * @param bos
	 * @return
	 * @throws ErrorReport
	 */
	protected void doGetReg(String id, int offs, int size, ByteArrayOutputStream bos)
			throws ErrorReport {
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
		
		if (size == 0)
			size = access.getRegisterInfo(num).size;
		
		for (int i = offs; i < size; i++) {
			bos.write(value >> (8 * (size - i - 1)));
		}
	}

	private Object[] doSet(Object[] args) throws Exception {
		String id = null;
		if (args[0] != null)
			id = args[0].toString();

		byte[] data = toByteArray(args[1]);
		int size = data.length;
		
		int idx = 0;
		
		doSetReg(id, data, idx, 0, size);
		
		return new Object[] { null };
		
	}

	/**
	 * @param id
	 * @param data
	 * @param idx
	 * @param size
	 * @throws ErrorReport
	 */
	protected int doSetReg(String id, byte[] data, int idx, int offs, int size)
			throws ErrorReport {
		String[] parts = id.split("\\.");
		if (parts.length != 2)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);

		IRegisterAccess access = getAccessOrError(parts[0]);
		if (access == null)
			throw new ErrorReport("Unknown group context " + parts[0], IErrorReport.TCF_ERROR_INV_CONTEXT);

		int num = access.getRegisterNumber(parts[1]);
		if (num == Integer.MIN_VALUE)
			throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);		

		int value = 0;
		for (int i = offs; i < size; i++) {
			value |= (data[idx++] & 0xff) << (8 * (size - i - 1));
		}
		
		access.setRegister(num, value);
		
		return idx;
	}


	private Object[] doGetm(Object[] args) throws Exception {
		@SuppressWarnings("unchecked")
		List<List<Object>> locMap = (List<List<Object>>) args[0];

		ByteArrayOutputStream bos = new ByteArrayOutputStream(4 * locMap.size());

		for (List<Object> loc : locMap) {
			if (loc.size() != 3) {
				throw new ErrorReport("Invalid location object", IErrorReport.TCF_ERROR_INV_FORMAT);
				
			}
			String id = (String) loc.get(0);
			int offs = ((Number) loc.get(1)).intValue();
			int size = ((Number) loc.get(2)).intValue();
			
			doGetReg(id, offs, size, bos);
		}
		
		return new Object[] { null, new JSON.Binary(bos.toByteArray(), 0, bos.size()) };
	}

	private Object[] doSetm(Object[] args) throws Exception {
		@SuppressWarnings("unchecked")
		List<List<Object>> locMap = (List<List<Object>>) args[0];
		byte[] data = JSON.toByteArray(args[1]);

		int idx = 0;
		for (List<Object> loc : locMap) {
			if (loc.size() != 3) {
				throw new ErrorReport("Invalid location object", IErrorReport.TCF_ERROR_INV_FORMAT);
				
			}
			String id = (String) loc.get(0);
			int offs = ((Number) loc.get(1)).intValue();
			int size = ((Number) loc.get(2)).intValue();
			
			idx = doSetReg(id, data, idx, offs, size);
		}
		
		if (idx != data.length)
			throw new ErrorReport("Did not consume all register data", IErrorReport.TCF_ERROR_INV_FORMAT);
			
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
