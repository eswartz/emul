/*
  Help.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import v9t9.common.files.IPathFileLocator.FileInfo;
import v9t9.common.files.PathFileLocator;
import ejs.base.logging.LoggingUtils;

/**
 * @author ejs
 *
 */
public class ToolHelp {

	public static void main(String[] args) throws IOException, URISyntaxException {
		LoggingUtils.setupNullLogging();
		
		System.out.println("V9t9 Tools\n");
		
		System.out.println("Run as:\n");
		System.out.println("v9t9[.bat|.sh] -tool <name> [args]\n");
		System.out.println("where <name> is one of:\n");
		
		String className = ToolHelp.class.getName();
		int didx = className.lastIndexOf('.');
		String packageName = className.substring(0, didx+1);
		String helpDir = "/" + packageName.replace('.', '/');
		
		String classPath = helpDir + ToolHelp.class.getSimpleName() + ".class";
		URL classURL = ToolHelp.class.getResource(classPath);
		URL helpDirURL = new URL(classURL, ".");
		
		PathFileLocator loc = new PathFileLocator();
		Map<String, FileInfo> ents = loc.getDirectoryListing(helpDirURL.toURI());
		
		for (String ent : ents.keySet()) {
			if (ent.endsWith(".class")) {
				String toolName = packageName + ent.substring(0, ent.length() - ".class".length());
				if (!toolName.equals(ToolHelp.class.getName())) {
					if (toolName.startsWith(packageName))
						toolName = toolName.substring(packageName.length());
					System.out.println("\t" + toolName);
				}
			}
		}
		
		System.out.println("\nRun a tool without arguments to get help.");
	}
	
}
