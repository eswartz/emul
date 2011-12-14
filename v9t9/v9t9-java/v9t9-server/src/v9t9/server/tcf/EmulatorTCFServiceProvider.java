/**
 * 
 */
package v9t9.server.tcf;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.ErrorReport;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.JSON.ObjectWriter;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IRegisters;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.SettingSchema;
import v9t9.server.tcf.services.IMemoryV2;
import v9t9.server.tcf.services.IRegistersV2;
import v9t9.server.tcf.services.ISettings;
import v9t9.server.tcf.services.local.MemoryService;
import v9t9.server.tcf.services.local.MemoryV2Service;
import v9t9.server.tcf.services.local.RegisterService;
import v9t9.server.tcf.services.local.RegistersV2Service;
import v9t9.server.tcf.services.local.SettingsService;
import v9t9.server.tcf.services.remote.SettingsProxy;

/**
 * This class manages TCF servers for remote access and control of a running
 * emulator.
 * @author ejs
 *
 */
public class EmulatorTCFServiceProvider implements IServiceProvider {

	public static final SettingSchema settingTCFLog = new SettingSchema(
			ISettingsHandler.TRANSIENT,
			"TCFLog",
			Boolean.FALSE);
	
	static {
        JSON.addObjectWriter(ErrorReport.class,
        		new ObjectWriter<ErrorReport>() {

					@Override
					public void write(ErrorReport o) throws IOException {
						JSON.writeObject(o.getAttributes());
					}
        	}
        );
	}
	
	private final Map<String, Class<? extends IService>> serviceMap = 
		new HashMap<String, Class<? extends IService>>();
	
	private final IMachine machine;

	/**
	 * 
	 */
	public EmulatorTCFServiceProvider(IMachine machine) {
		this.machine = machine;
		serviceMap.put("ZeroCopy", null);
		
		registerService(ISettings.NAME, SettingsService.class);
        registerService(IMemory.NAME, MemoryService.class);
        registerService(IMemoryV2.NAME, MemoryV2Service.class);
        registerService(IRegisters.NAME, RegisterService.class);
        registerService(IRegistersV2.NAME, RegistersV2Service.class);
	}
	
	public void registerService(String name, Class<? extends IService> serviceKlass) {
		assert !serviceMap.containsKey(name);
		serviceMap.put(name, serviceKlass);
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IServiceProvider#getLocalService(org.eclipse.tm.tcf.protocol.IChannel)
	 */
	@Override
	public IService[] getLocalService(IChannel channel) {
		if (machine == null)
			return null;
		
		final List<IService> services = new ArrayList<IService>(serviceMap.size());
		for (Map.Entry<String, Class<? extends IService>> entry : serviceMap.entrySet()) {
			if (entry.getValue() != null) {
				try {
					IService service = entry.getValue().getConstructor(
							IMachine.class, IChannel.class).newInstance(
									machine, channel);
					services.add(service);
				} catch (Exception e) {
					Protocol.log("Failed to instantiate local service " + entry.getKey(), e);
				}
			}
		}
		return services.toArray(new IService[services.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.tcf.protocol.IServiceProvider#getServiceProxy(org.eclipse.tm.tcf.protocol.IChannel, java.lang.String)
	 */
	@Override
	public IService getServiceProxy(IChannel channel, String service_name) {
		 IService service = null;
		 if (serviceMap.get(service_name) != null) {
	         try {
	             String packageName = SettingsProxy.class.getPackage().getName();
	             Class<?> cls = Class.forName(MessageFormat.format("{0}.{1}Proxy", packageName, service_name));
	             service = (IService)cls.getConstructor(IChannel.class).newInstance(channel);
	             assert service_name.equals(service.getName());
	         }
	         catch (Exception x) {
	        	 Protocol.log("Failed to fetch service proxy for " + service_name, x);
	         }
		 }
         return service;
	}
}
