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
import java.util.Collection;

import v9t9.common.files.PathFileLocator;
import ejs.base.logging.LoggingUtils;

/**
 * @author ejs
 *
 */
public class Help {


	public static void main(String[] args) throws IOException, URISyntaxException {
		LoggingUtils.setupNullLogging();
		
		System.out.println("V9t9 Tools\n");
		
		System.out.println("Run this JAR as:\n");
		System.out.println("\tjava -cp v9t9-tools.jar <class> [args]\n");
		System.out.println("where <class> is one of:\n");
		
		String className = Help.class.getName();
		int didx = className.lastIndexOf('.');
		String packageName = className.substring(0, didx+1);
		String helpDir = "/" + packageName.replace('.', '/');
		
		String classPath = helpDir + Help.class.getSimpleName() + ".class";
		//System.out.println("classPath=" +classPath);
		URL classURL = Help.class.getResource(classPath);
		//System.out.println("classURL=" +classURL);
		URL helpDirURL = new URL(classURL, ".");
		//System.out.println("helpDirURL=" +helpDirURL);
		
		PathFileLocator loc = new PathFileLocator();
		Collection<String> ents = loc.getDirectoryListing(helpDirURL.toURI());
		
		for (String ent : ents) {
			if (ent.endsWith(".class")) {
				String toolName = packageName + ent.substring(0, ent.length() - ".class".length());
				if (!toolName.equals(Help.class.getName())) {
					System.out.println("\t" + toolName);
				}
			}
		}
		
	}
	
}
