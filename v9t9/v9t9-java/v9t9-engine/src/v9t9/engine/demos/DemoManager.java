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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import v9t9.common.demo.DemoHeader;
import v9t9.common.demo.IDemo;
import v9t9.common.demo.IDemoActor;
import v9t9.common.demo.IDemoEventFormatter;
import v9t9.common.demo.IDemoInputStream;
import v9t9.common.demo.IDemoManager;
import v9t9.common.demo.IDemoOutputStream;
import v9t9.common.events.NotifyException;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.PathFileLocator;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.engine.demos.actors.TimerTickActor;
import v9t9.engine.demos.events.TimerTick;
import v9t9.engine.demos.format.DemoFormat;
import v9t9.engine.demos.format.DemoFormatInputStream;
import v9t9.engine.demos.format.DemoFormatOutputStream;
import v9t9.engine.demos.format.old.OldDemoFormat;
import v9t9.engine.demos.format.old.OldDemoFormatInputStream;
import v9t9.engine.demos.format.old.OldDemoFormatOutputStream;
import ejs.base.utils.CountingOutputStream;
import ejs.base.utils.FileUtils;

/**
 * @author ejs
 *
 */
public class DemoManager implements IDemoManager {

	private List<IDemo> demos = new ArrayList<IDemo>();
	private IPathFileLocator locator;
	private final IMachine machine;
	private Map<String, IDemoActor> actors = new LinkedHashMap<String, IDemoActor>();
	
	public DemoManager(IMachine machine) {
		this.machine = machine;
		this.locator = new PathFileLocator();
		
		locator.addReadOnlyPathProperty(Settings.get(machine, IDemoManager.settingBootDemosPath));
		locator.addReadOnlyPathProperty(Settings.get(machine, IDemoManager.settingUserDemosPath));
		locator.setReadWritePathProperty(Settings.get(machine, IDemoManager.settingRecordedDemosPath));
		
		registerActor(new TimerTickActor());
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#registerContributor(v9t9.common.demo.IDemoContributor)
	 */
	@Override
	public void registerActor(IDemoActor actor) {
		actors.put(actor.getEventIdentifier(), actor);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#getActors()
	 */
	@Override
	public IDemoActor[] getActors() {
		return actors.values().toArray(new IDemoActor[actors.size()]);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoManager#findActor(java.lang.String)
	 */
	@Override
	public IDemoActor findActor(String id) {
		return actors.get(id);
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
			DemoFormatInputStream inputStream = new DemoFormatInputStream(machine.getModel(), is);
			return inputStream;
		} else if (Arrays.equals(header, OldDemoFormat.DEMO_MAGIC_HEADER_V910)) {
			return new OldDemoFormatInputStream(is);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemoHandler#createDemoWriter(java.net.URI)
	 */
	@Override
	public IDemoOutputStream createDemoWriter(URI uri) throws IOException,
			NotifyException {
		if (true) {
			DemoHeader header = new DemoHeader();
			header.setMachineModel(machine.getModel().getIdentifier());
			for (IDemoActor actor : actors.values()) {
				if (actor.getEventIdentifier().equals(TimerTick.ID))
					continue;
				
				IDemoEventFormatter formatter = DemoFormat.FORMATTER_REGISTRY.findFormatterByEvent(
						actor.getEventIdentifier());
				if (formatter != null) {
					header.findOrAllocateIdentifier(formatter.getBufferIdentifer());
				}
			}
			DemoFormatOutputStream demoStream = new DemoFormatOutputStream(header, 
					new CountingOutputStream(new BufferedOutputStream(locator.createOutputStream(uri))));
			
			return demoStream;
		} else {
			return new OldDemoFormatOutputStream( 
					new CountingOutputStream(new BufferedOutputStream(locator.createOutputStream(uri))));
		}
	}
}
