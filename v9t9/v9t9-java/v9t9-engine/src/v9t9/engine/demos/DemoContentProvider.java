/*
  DemoContentProvider.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.demos;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.demos.DemoContentSource;
import v9t9.common.demos.IDemo;
import v9t9.common.files.IPathFileLocator;
import v9t9.common.files.IPathFileLocator.FileInfo;
import v9t9.common.machine.IMachine;
import ejs.base.utils.FileUtils;
import ejs.base.utils.Pair;

/**
 * @author ejs
 *
 */
public class DemoContentProvider implements IEmulatorContentSourceProvider {

	private static final Logger log = Logger.getLogger(DemoContentProvider.class);
	
	private IMachine machine;

	private IPathFileLocator locator;

	public DemoContentProvider(IMachine machine) {
		this.machine = machine;
		locator = machine.getDemoManager().getDemoLocator();
	}

	/* (non-Javadoc)
	 * @see v9t9.common.client.IEmulatorContentProvider#analyze(java.net.URI)
	 */
	@Override
	public IEmulatorContentSource[] analyze(final URI uri) {
		List<IEmulatorContentSource> sources = new ArrayList<IEmulatorContentSource>();
		if (uri.toString().endsWith(".dem")) {
			addDemo(sources, uri);
		}
		else {
			if (!uri.toString().endsWith("/"))
				return IEmulatorContentSource.EMPTY;
			
			final URI dirURI = uri;
			try {
				log.debug("scanning demos in " + dirURI);
				Map<String, FileInfo> ents = locator.getDirectoryListing(dirURI);
				for (Map.Entry<String, FileInfo> ent : ents.entrySet()) {
					String name = ent.getKey();
					log.debug("\t" + name);
					if (name.endsWith(".dem")) {
						FileInfo info = ent.getValue();
						final URI demoURI = info.uri;
						addDemo(sources, demoURI);
					}
				}
			} catch (IOException e) {
				log.error("failed to scan directory", e);
				// ignore
			}

		}
		
		return sources.toArray(new IEmulatorContentSource[sources.size()]);
	}
	
	private void addDemo(List<IEmulatorContentSource> sources, URI uri) {
		Pair<URI, String> info = locator.splitFileName(uri);
		String name = info.second;
		String descrName = name.substring(0, name.length() - 4) + ".txt";
		URI descrURI = locator.resolveInsideURI(uri, descrName);
		
		String description;
		try {
			description = FileUtils.readInputStreamTextAndClose(
					locator.createInputStream(descrURI));
		} catch (IOException e) {
			description = "";
		}
		
		IDemo demo = new Demo(info.first, uri, name, description);
		sources.add(new DemoContentSource(machine, demo));
		log.debug("\tfound demo: " + demo);
	}
}
