/**
 * 
 */
package v9t9.engine.demos;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.demo.IDemo;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.demo.IDemoManager;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.PathFileLocator;
import v9t9.common.machine.IMachineModel;
import v9t9.engine.demos.format.CountingOutputStream;
import v9t9.engine.demos.format.DemoFormat;
import v9t9.engine.demos.format.NewDemoFormatReader;
import v9t9.engine.demos.format.NewDemoFormatWriter;
import v9t9.engine.demos.format.OldDemoFormatReader;
import ejs.base.utils.FileUtils;

/**
 * @author ejs
 *
 */
public class DemoManager implements IDemoManager {

	private List<IDemo> demos = new ArrayList<IDemo>();
	private IPathFileLocator locator;
	private final IMachineModel machineModel;
	
	public DemoManager(ISettingsHandler settings, IMachineModel machineModel) {
		this.machineModel = machineModel;
		this.locator = new PathFileLocator();
		
		locator.addReadOnlyPathProperty(settings.get(IDemoHandler.settingBootDemosPath));
		locator.addReadOnlyPathProperty(settings.get(IDemoHandler.settingUserDemosPath));
		locator.setReadWritePathProperty(settings.get(IDemoHandler.settingRecordedDemosPath));
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#getDemoLocator()
	 */
	@Override
	public IPathFileLocator getDemoLocator() {
		return locator;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#getDemos()
	 */
	@Override
	public synchronized IDemo[] getDemos() {
		return demos.toArray(new IDemo[demos.size()]);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#reload()
	 */
	@Override
	public synchronized void reload() {
		
		demos.clear();
		
		for (final URI dirURI : locator.getSearchURIs()) {
			try {
				Collection<String> ents = locator.getDirectoryListing(dirURI);
				for (String ent : ents) {
					if (ent.endsWith(".dem")) {
						final URI demoURI = locator.resolveInsideURI(dirURI, ent);
						String descrName = ent.substring(0, ent.length() - 4) + ".txt";
						URI descrURI = locator.resolveInsideURI(dirURI, descrName);
						
						String description;
						try {
							description = FileUtils.readInputStreamTextAndClose(
									locator.createInputStream(descrURI));
						} catch (IOException e) {
							description = "";
						}
						demos.add(new Demo(dirURI, demoURI, ent, description));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				// ignore
			}
		}
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#addDemo(v9t9.common.demo.IDemo)
	 */
	@Override
	public void addDemo(IDemo demo) {
		if (!demos.contains(demo))
			demos.add(demo);
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#removeDemo(v9t9.common.demo.IDemo)
	 */
	@Override
	public void removeDemo(IDemo demo) {
		demos.remove(demo);
	}


	/**
	 * @param uri
	 * @return
	 * @throws IOException
	 * @throws NotifyException
	 */
	public IDemoInputStream createDemoReader(URI uri) throws IOException,
			NotifyException {
		InputStream is = locator.createInputStream(uri);
		byte[] header = new byte[4];
		is.read(header);
		
		if (Arrays.equals(header, DemoFormat.DEMO_MAGIC_HEADER_V9t9)) {
			return new NewDemoFormatReader(machineModel, is);
		} else if (Arrays.equals(header, DemoFormat.DEMO_MAGIC_HEADER_TI60)
				|| Arrays.equals(header, DemoFormat.DEMO_MAGIC_HEADER_V910)) {
			return new OldDemoFormatReader(is);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoHandler#createDemoWriter(java.net.URI)
	 */
	@Override
	public IDemoOutputStream createDemoWriter(URI uri) throws IOException,
			NotifyException {
		return new NewDemoFormatWriter(machineModel, 
				new CountingOutputStream(new BufferedOutputStream(locator.createOutputStream(uri))));
	}
}
