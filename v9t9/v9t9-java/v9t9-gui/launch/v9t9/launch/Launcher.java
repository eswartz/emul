/*
  Launcher.java

  (c) 2013-2015 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.launch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author ejs
 *
 */
public class Launcher {

	public static final String TOOL_PREFIX = "v9t9.tools.";
	/**
	 * 
	 */
	private static final String LIBS = "libs";

	public static void main(String[] args) {
		Launcher launcher = new Launcher();
		try {
			launcher.launch(args);
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			System.err.println("\nV9t9 crashed!");
			System.exit(4);
		} catch (Throwable e1) {
			e1.printStackTrace();
			System.err.println("\nCould not launch V9t9!" );
			System.exit(3);
		}

	}
	private byte[] buffer = new byte[8192];
	private File libDir;
	private Set<String> javaLibPaths = new LinkedHashSet<String>();
	
	protected void launch(String[] args) throws InvocationTargetException, Throwable {
		File tmpDir = getExtractDir();
		libDir = new File(tmpDir, LIBS);
		
		for (int idx = 0; idx < args.length; idx++) {
			if ("-clean".equals(args[idx])) {
				args = remove(args, idx);
				recursiveDelete(tmpDir);
				break;
			}
		}
		
		URL jarURL = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
		
		JarFile jarFile = JarUtils.getJarFile(jarURL);

		String oldJavaLibPath = System.getProperty("java.library.path");
		if (oldJavaLibPath != null && !oldJavaLibPath.isEmpty()) {
			javaLibPaths.addAll(Arrays.asList(oldJavaLibPath.split(System.getProperty("path.separator"))));
		}
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (jarFile != null) {
			List<URL> jarURLs = new ArrayList<URL>();
			
			extractZipFileAndClose("Extracting V9t9 to " + tmpDir + "...",
					tmpDir, jarFile, jarURLs);
			
			classLoader = replaceClassLoader(jarURLs);
			
			try {
				jarFile.close();
			} catch (IOException e) {
				
			}
		}

		// run alternate main class
		String mainClass = "v9t9.gui.Emulator";
		for (int idx = 0; idx < args.length; idx++) {
			if ("-help".equals(args[idx]) || "-h".equals(args[idx]) 
					|| "-?".equals(args[idx])) {
				System.out.println("V9t9 Launcher\n"
					+"\n"
					+"To emulate a machine:\n"
					+"\n"
					+"v9t9.[sh|bat] [--machine MachineName | --list-machines] [--client ClientName | --list-clients]\n"
					+"\n"
					+"To use tools:\n"
					+"\n"
					+"v9t9.[sh|bat] -tool [class args...] \n"
					);
				return;
			}
			if ("-tool".equals(args[idx])) {
				mainClass = "ToolHelp";
				args = remove(args, idx);
				if (idx < args.length) {
					mainClass = args[idx];
					args = remove(args, idx);
				}
				if (mainClass.indexOf('.') < 0)
					mainClass = TOOL_PREFIX + mainClass;
				break;
			}
		}
		
		StringBuilder newJavaLibPath = new StringBuilder();
		
		for (String path : javaLibPaths) {
			if (newJavaLibPath.length() > 0)
				newJavaLibPath.append(System.getProperty("path.separator"));
			newJavaLibPath.append(path);
		}
		
		System.err.println("Updating Java library path to: " + newJavaLibPath);
		System.setProperty("java.library.path", newJavaLibPath.toString());

		try {
			// ugly hack to force re-loading the path
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (Throwable t) {
			t.printStackTrace();
			System.err.println("\nCould not modify java.library.path: please invoke:\n\n\texport V9T9_VMARGS=\"-Djava.library.path=" + newJavaLibPath + "\"'\n\nand try again.");
		}
		
		Class<?> klass = classLoader.loadClass(mainClass);
		Method main = klass.getMethod("main", String[].class);
		main.invoke(null, new Object[] { args });
	}

	private void recursiveDelete(File tmpDir) {
		File[] files = tmpDir.listFiles();
		if (files == null)
			return;
		for (File file : files) {
			if (file.isDirectory())
				recursiveDelete(file);
			else
				file.delete();
		}
	}

	private String[] remove(String[] args, int idx) {
		String[] newArgs = new String[args.length - 1];
		System.arraycopy(args, 0, newArgs, 0, idx);
		System.arraycopy(args, idx + 1, newArgs, idx, args.length - idx - 1);
		return newArgs;
	}

	protected URLClassLoader replaceClassLoader(List<URL> jarURLs) {
		ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

		URLClassLoader urlClassLoader = new URLClassLoader(
				jarURLs.toArray(new URL[jarURLs.size()]),
              currentThreadClassLoader) {
			protected String findLibrary(String libname) {
				File libfile = new File(libDir, System.mapLibraryName(libname));
				if (libfile.exists())
					return libfile.getAbsolutePath();
				return super.findLibrary(libname);
			}
		};
		
		Thread.currentThread().setContextClassLoader(urlClassLoader);
		return urlClassLoader;
	}

	protected File getExtractDir() {
		File tmpDir;
		String v9t9Path = System.getProperty("v9t9.dir");
		if (v9t9Path != null && v9t9Path.length() > 0) {
			tmpDir = new File(v9t9Path);
		} else {
			String tmpPath;
			tmpPath = System.getProperty("java.io.tmpdir");
			tmpDir = new File(tmpPath, ".v9t9j/exec");
			
			tmpDir.mkdirs();
			if (!tmpDir.isDirectory()) {
				System.err.println("Cannot create " + tmpDir + " for extraction.");
				
				tmpDir = new File(System.getProperty("user.home"), ".v9t9j/exec");
			}
		}
		
		tmpDir.mkdirs();
		if (!tmpDir.isDirectory()) {
			System.err.println("Cannot create " + tmpDir + " for extraction.\n\n"
					+"Please use -Dv9t9.dir=... option to unpack V9t9 elsewhere.");
			System.exit(1);
		}
		return tmpDir;
	}

	protected void extractZipFileAndClose(String label, File targetDir, ZipFile zipFile, List<URL> jarURLs) {
		boolean any = false;
		
		Enumeration<? extends ZipEntry> enm = zipFile.entries();
		
		String myOS = System.getProperty("os.name");
		if (myOS.equals("Linux"))
			myOS = "linux";
		else if (myOS.startsWith("Windows "))
			myOS = "win32";
		else if (myOS.equals("Mac OS X"))
			myOS = "macosx";
		
		String myArch = System.getProperty("os.arch");
		String genericArch = myArch;
		if (myArch.contains("64") && (myArch.contains("amd") || myArch.contains("x86"))) {
			myArch = "x86_64";
			genericArch = "intel";
		} else if (myArch.endsWith("86")) {
			myArch = "x86";
			genericArch = "intel";
		}
		
		//System.out.println("myOS="+myOS+"; myArch="+myArch);
		
		while (enm.hasMoreElements()) {
			ZipEntry entry = enm.nextElement();
			
			String path = entry.getName();
			if (path.startsWith("META-INF"))
				continue;
				
			File target = new File(targetDir, path);
			
			//System.out.println(entry.getName() + " => " + target);
			
			if (entry.isDirectory()) {
				target.mkdirs();
				continue;
			} 
				
			boolean doExtract = false;
			boolean doCopy = true;
			
			String name = target.getName();
			
			target.getParentFile().mkdirs();
			
			// filter OS-specific ones
			if (name.startsWith("org.eclipse.swt") && !name.equals("org.eclipse.swt.jar")) {
				if (!name.matches("org.eclipse.swt..*" + myOS + "." + myArch + ".jar")) {
					continue;
				}
			}
			if (name.startsWith("v9t9j-natives-")) {
				if (!name.matches("v9t9j-natives-" + myOS + "-" + genericArch + ".jar")) {
					continue;
				}
				
				// extract directly
				doExtract = true;
			}
			
			// don't re-copy unless changed
			long entTime = entry.getTime();
			long fileTime = target.lastModified();
			if (target.exists() && fileTime != 0 && entTime > 0) {
				if (Math.abs(fileTime - entTime) <= 2000 
						&& entry.getSize() == target.length()) {
					doCopy = false;
				}
			}

			//System.out.println(name);
			if (name.matches("(?i).*\\.(so|dylib|jnilib|dll)")) {
				target.setExecutable(true, false);
				javaLibPaths.add(target.getParent());
			}
			
			if (doCopy) {
				if (!any) {
					System.out.println(label);
					any = true;
				}
					
				try {
					InputStream is = zipFile.getInputStream(entry);
					extractFileAndClose(is, target);
					
					target.setLastModified(entTime);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(3);
				}
			}
			
			if (doExtract) {
				try {
					File libDir = new File(targetDir, LIBS);
					extractZipFileAndClose("Extracting native libraries to " + targetDir + "...",
							libDir, new ZipFile(target), jarURLs);
					jarURLs.add(libDir.toURI().toURL());
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(5);
				}
			}
			else {
				if (path.endsWith(".jar")) {
					try {
						jarURLs.add(target.toURI().toURL());
					} catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		try {
			zipFile.close();
		} catch (IOException e) {
		}
	}

	/**
	 * @param jarFile
	 * @param buffer
	 * @param entry
	 * @param target
	 */
	protected void extractFileAndClose(InputStream is,
			File target) throws IOException {
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(target));
			int len;
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer, 0, len);
			}
		} finally {
			if (is != null) {
				try { is.close(); } catch (IOException e) { }
			}
			if (os != null) {
				try { os.close(); } catch (IOException e) { }
			}
		}
	}
}
