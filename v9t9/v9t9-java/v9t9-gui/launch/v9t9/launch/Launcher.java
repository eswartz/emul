/**
 * 
 */
package v9t9.launch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author ejs
 *
 */
public class Launcher {
	
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

	protected void launch(String[] args) throws InvocationTargetException, Throwable {
		File tmpDir = getExtractDir();
		libDir = new File(tmpDir, LIBS);
		
		URL jarURL = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
		
		JarFile jarFile = JarUtils.getJarFile(jarURL);

		List<URL> jarURLs = new ArrayList<URL>();
		if (jarFile != null) {
			System.out.println("Extracting V9t9 to " + tmpDir + "...");

			extractZipFile(tmpDir, jarFile, jarURLs);
		}

		URLClassLoader urlClassLoader = replaceClassLoader(jarURLs);
		
		Class<?> klass = urlClassLoader.loadClass("v9t9.gui.Emulator");
		Method main = klass.getMethod("main", String[].class);
		main.invoke(null, new Object[] { args });
	}

	/**
	 * @param jarURLs
	 * @return
	 */
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

	/**
	 * @return
	 */
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

	/**
	 * @param targetDir
	 * @param jarURLs 
	 * @return 
	 */
	protected void extractZipFile(File targetDir, ZipFile zipFile, List<URL> jarURLs) {
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
		
		
		while (enm.hasMoreElements()) {
			ZipEntry entry = enm.nextElement();
			
			String path = entry.getName();
			if (path.startsWith("META-INF"))
				continue;
				
			File target = new File(targetDir, path);

			//System.out.println(entry.getName() + " => " + target);
			
			if (entry.isDirectory()) {
				target.mkdirs();
			} else {
				
				boolean doExtract = false;
				String name = target.getName();
				
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
				
				try {
					InputStream is = zipFile.getInputStream(entry);
					extractFileAndClose(is, target);
					
					if (name.matches("(?i).*\\.(so|dylib|dll)")) {
						target.setExecutable(true, false);
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(3);
				}
				
				if (doExtract) {
					try {
						System.out.println("Extracting native libraries to " + targetDir + "...");
						File libDir = new File(targetDir, LIBS);
						extractZipFile(libDir, new ZipFile(target), jarURLs);
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
