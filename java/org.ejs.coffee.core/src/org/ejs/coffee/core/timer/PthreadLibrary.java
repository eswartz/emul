/**
 * 
 */
package org.ejs.coffee.core.timer;

import com.sun.jna.*;

/**
 * @author ejs
 *
 */
public interface PthreadLibrary extends com.sun.jna.Library  {

	public static final java.lang.String JNA_LIBRARY_NAME = "pthread";
	public static final com.sun.jna.NativeLibrary JNA_NATIVE_LIB = com.sun.jna.NativeLibrary.getInstance(JNA_LIBRARY_NAME);
	public static final PthreadLibrary INSTANCE = (PthreadLibrary)com.sun.jna.Native.loadLibrary(JNA_LIBRARY_NAME, PthreadLibrary.class);

	int pthread_self();
}
