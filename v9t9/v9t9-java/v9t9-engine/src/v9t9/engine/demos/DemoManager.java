/**
 * 
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import v9t9.common.demo.IDemo;
import v9t9.common.demo.IDemoHandler;
import v9t9.common.demo.IDemoManager;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.PathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import ejs.base.utils.FileUtils;

/**
 * @author ejs
 *
 */
public class DemoManager implements IDemoManager {

	private List<IDemo> demos = new ArrayList<IDemo>();
	private IPathFileLocator locator;
	
	public DemoManager(IMachine machine) {
		this.locator = new PathFileLocator();
		
		locator.addReadOnlyPathProperty(Settings.get(machine, 
				IDemoHandler.settingBootDemosPath));
		locator.addReadOnlyPathProperty(Settings.get(machine, 
				IDemoHandler.settingUserDemosPath));
		locator.setReadWritePathProperty(Settings.get(machine, 
				IDemoHandler.settingRecordedDemosPath));
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

}
