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
import org.eclipse.tm.tcf.protocol.JSON.Binary;
import org.eclipse.tm.tcf.services.IMemory;

import v9t9.common.machine.IMachine;
import v9t9.common.memory.IMemoryDomain;
import v9t9.common.memory.IMemoryEntry;
import v9t9.server.tcf.services.IMemoryV2;

/**
 * @author ejs
 *
 */
public class MemoryService extends BaseServiceImpl {

	/**
	 * Standard commands 
	 */
	private static final String GET = "get";
	private static final String SET = "set";
	private static final String GET_CONTEXT = "getContext";
	private static final String GET_CHILDREN = "getChildren";
	
	/**
	 * @param machine
	 * @param channel
	 * @param serviceName
	 */
	public MemoryService(IMachine machine, IChannel channel) {
		this(machine, channel, IMemory.NAME);
		
	}
	
	/**
	 * @param machine
	 * @param channel
	 * @param serviceName
	 */
	public MemoryService(IMachine machine, IChannel channel, String name) {
		super(machine, channel, name);
		
		registerCommand(GET_CHILDREN, 1, 2);
		registerCommand(GET_CONTEXT, 1, 2);
		registerCommand(SET, 6, 2);
		registerCommand(GET, 5, 2);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.server.tcf.services.local.BaseServiceImpl#handleCommand(java.lang.String, java.lang.Object[])
	 */
	@Override
	protected Object[] handleCommand(String name, Object[] args)
			throws ErrorReport, Exception {
		if (GET_CHILDREN.equals(name)) {
			return doGetChildren(args);
		} else if (GET_CONTEXT.equals(name)) {
			return doGetContext(args);
		} else if (GET.equals(name)) {
			return doGet(args);
			
		} else if (SET.equals(name)) {
			return doSet(args);
		}
		return null;
	}

	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doSet(Object[] args) throws ErrorReport {
		// args: 0:contextId 1:addr 2:word_size 3:size 4:mode 5:content
		// ret: content error ranges
		
		IMemoryDomain domain = getDomainOrError(args[0]);
		int word_size = ((Number) args[2]).intValue();
		if (word_size < 0 || (word_size & (word_size - 1)) != 0)
			throw new ErrorReport("Bad word size " + word_size, 
					IErrorReport.TCF_ERROR_INV_DATA_SIZE);

		int mode = ((Number) args[4]).intValue();
		int addr = ((Number) args[1]).intValue();
		int size = ((Number) args[3]).intValue();
		if (size < 0 || size != ((size + word_size - 1) & ~(word_size - 1)))
			throw new ErrorReport("Bad size of " + size + " @ " + word_size, 
					IErrorReport.TCF_ERROR_INV_DATA_SIZE);

		if (addr < 0 || addr != ((addr) & ~(word_size - 1)))
			throw new ErrorReport("Bad alignment of " + addr + " @ " + word_size, 
					IErrorReport.TCF_ERROR_INV_ADDRESS);

		byte[] buf;
		if (args[5] instanceof List) {
			@SuppressWarnings("unchecked")
			List<Number> list = (List<Number>) args[5];
			buf = new byte[list.size()];
			for (int i = 0; i < buf.length; i++)
				buf[i] = ((Number) list.get(i)).byteValue();
		} else {
			buf = JSON.toByteArray(args[5]);
		}

		if ((mode & IMemoryV2.MODE_FLAT) != 0) {
			for (int i = 0; i < size; i++) {
				domain.flatWriteByte(addr + i, buf[i]);
			}
		} else {
			for (int i = 0; i < size; i++) {
				domain.writeByte(addr + i, buf[i]);
			}
		}

		
		return new Object[] { null, null };
	}

	/**
	 * @param args
	 * @return
	 */
	protected Object[] doGet(Object[] args) {
		// args: 0:contextId 1:addr 2:word_size 3:size 4:mode
		// ret: content error ranges
		
		try {
			IMemoryDomain domain = getDomainOrError(args[0]);
			int word_size = ((Number) args[2]).intValue();
			if (word_size <= 0 || (word_size & (word_size - 1)) != 0)
				throw new ErrorReport("Bad word size " + word_size, 
						IErrorReport.TCF_ERROR_INV_DATA_SIZE);

			
			int mode = ((Number) args[4]).intValue();
			int addr = ((Number) args[1]).intValue();
			int size = ((Number) args[3]).intValue();
			if (size < 0 || size != ((size + word_size - 1) & ~(word_size - 1)))
				throw new ErrorReport("Bad size of " + size + " @ " + word_size, 
						IErrorReport.TCF_ERROR_INV_DATA_SIZE);

			if (addr < 0 || addr != ((addr) & ~(word_size - 1)))
				throw new ErrorReport("Bad alignment of " + addr + " @ " + word_size, 
						IErrorReport.TCF_ERROR_INV_ADDRESS);

			byte[] buf = new byte[size];

			if ((mode & IMemoryV2.MODE_FLAT) != 0) {
				for (int i = 0; i < size; i++) {
					buf[i] = domain.flatReadByte(addr + i);
				}
			} else {
				for (int i = 0; i < size; i++) {
					buf[i] = domain.readByte(addr + i);
				}
			}
			
			return new Object[] { new Binary(buf, 0, buf.length), null, null };
		} catch (ErrorReport e) {
			return new Object[] { null, e, null };
		}
	}

	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doGetContext(Object[] args) throws ErrorReport {
		// args: contextId
		// args: object of attributes
		
		IMemoryDomain domain = getDomainOrError(args[0]);
		Map<String, Object> context = createContext(null, domain);
		if (context != null)
			return new Object[] { null, context };
		
		throw new ErrorReport("Unknown context " + args[0], IErrorReport.TCF_ERROR_INV_CONTEXT);
	}

	/**
	 * @param args
	 * @return
	 * @throws ErrorReport
	 */
	protected Object[] doGetChildren(Object[] args) throws ErrorReport {
		// args: contextId
		// ret: list of contextIds
		String id = args[0] != null ? args[0].toString() : null;
		if (id == null || id.length() == 0 || "root".equals(id)) {
			// get domain IDs
			id = "";
			List<String> contextIds = new ArrayList<String>();
			for (IMemoryDomain domain : machine.getMemory().getDomains()) {
				contextIds.add(domain.getIdentifier());
			}
			return new Object[] { null, contextIds };
		}
		throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
	}

	/**
	 * @param object
	 * @return
	 * @throws ErrorReport 
	 */
	protected IMemoryDomain getDomainOrError(Object object) throws ErrorReport {
		String id = object != null ? object.toString() : null;
		
		IMemoryDomain domain = machine.getMemory().getDomain(id);
		if (domain != null) 
			return domain;
		
		throw new ErrorReport("Unknown context " + id, IErrorReport.TCF_ERROR_INV_CONTEXT);
	}

	/**
	 * @param domain
	 * @return
	 */
	private Map<String, Object> createContext(String parent, IMemoryDomain domain) {
		Map<String, Object> ctx = new HashMap<String, Object>();
		String id = domain.getIdentifier();
		
		int minAddr = 0;
		int maxAddr = 0;
		for (IMemoryEntry entry : domain.getFlattenedMemoryEntries()) {
			if (entry.getName().equals(IMemoryDomain.UNMAPPED_MEMORY_ID))
				continue;
			if (entry.getAddr() < minAddr)
				minAddr = entry.getAddr();
			if (entry.getAddr() + entry.getSize() > maxAddr)
				maxAddr = entry.getAddr() + entry.getSize();
		}
		
		if (minAddr == 0 && maxAddr == 0)
			return null;
		
		ctx.put(IMemory.PROP_ID, id);
		ctx.put(IMemory.PROP_NAME, domain.getName());
		ctx.put(IMemory.PROP_START_BOUND, minAddr);
		ctx.put(IMemory.PROP_END_BOUND, maxAddr);
		ctx.put(IMemory.PROP_BIG_ENDIAN, true);
		ctx.put(IMemory.PROP_PARENT_ID, parent);
		ctx.put(IMemory.PROP_ADDRESS_SIZE, 2);
		ctx.put(IMemory.PROP_ACCESS_TYPES, new String[] { 
				IMemory.ACCESS_DATA, IMemory.ACCESS_PHYSICAL,
				IMemory.ACCESS_USER
		});
		return ctx;
	}
}
