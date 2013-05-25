/*
  FileImportHandler.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.modules;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import v9t9.common.client.IEmulatorContentSource;
import v9t9.common.client.IEmulatorContentSourceProvider;
import v9t9.common.machine.IMachine;
import v9t9.common.modules.IModule;
import v9t9.common.modules.ModuleContentSource;

/**
 * @author ejs
 *
 */
public class ModuleContentProvider implements IEmulatorContentSourceProvider {

	private static final Logger log = Logger.getLogger(ModuleContentProvider.class);
	
	private IMachine machine;

	public ModuleContentProvider(IMachine machine) {
		this.machine = machine;
	}

	public IEmulatorContentSource[] analyze(URI uri) {
		URI databaseURI;
	
		databaseURI = URI.create("temp_modules.xml");
		
		File file;
		try {
			file = new File(uri);
		} catch (IllegalArgumentException e) {
			return IEmulatorContentSource.EMPTY;
		}
		
		Collection<IModule> ents;
		ents = machine.scanModules(databaseURI, file);
		if (ents.isEmpty())
			return IEmulatorContentSource.EMPTY;
					
		List<IEmulatorContentSource> sources = new ArrayList<IEmulatorContentSource>(ents.size());
		for (IModule module : ents) {
			sources.add(new ModuleContentSource(machine, module));
		}
		return (IEmulatorContentSource[]) sources.toArray(new IEmulatorContentSource[sources.size()]);
//		IModule theMatch = null;
//		String matchPattern = ".*/?"+Pattern.quote(file.getName())+"/?.*";
//		for (IModule module : ents) {
//			for (File modFile : module.getUsedFiles(machine.getRomPathFileLocator())) {
//				if (modFile.getName().matches(matchPattern)) {
//					theMatch = module;
//					break;
//				}
//			}
//			if (theMatch != null)
//				break;
//		}
//		
//		if (theMatch == null)
//			return IEmulatorContentSource.EMPTY;
		
		/*
		boolean doit = MessageDialog.openQuestion(shell, "Load Module?",
				"V9t9 recognized the module '" + theMatch.getName() + "'.\n\n"+
				"Load this module and reset the emulator now?");
		
		if (doit) {
			machine.getModuleManager().addModules(Collections.singletonList(theMatch));
			
			machine.reset();
			machine.getModuleManager().unloadAllModules();
			machine.getModuleManager().loadModule(theMatch);
			
			return true;
		}
		
		return false;
		*/

	}
}
