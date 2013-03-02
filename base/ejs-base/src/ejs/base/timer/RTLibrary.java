/*
  RTLibrary.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.timer;

import com.sun.jna.Callback;
import com.sun.jna.PointerType;

/**
 * @author ejs
 *
 */
public interface RTLibrary extends com.sun.jna.Library  {

	public static final java.lang.String JNA_LIBRARY_NAME = "grt";
	public static final com.sun.jna.NativeLibrary JNA_NATIVE_LIB = com.sun.jna.NativeLibrary.getInstance(JNA_LIBRARY_NAME);
	public static final RTLibrary INSTANCE = (RTLibrary)com.sun.jna.Native.loadLibrary(JNA_LIBRARY_NAME, RTLibrary.class);

	public static interface TimerCallFunc extends Callback {
	    void invoke(RT rt, long current_time);
	}
	
	public static class RT extends PointerType {
	}
	
	/** Initialize realtime, returning 0 for success or an errno.
	 * @return errno
	 */
	int rt_init();
	
	RT rt_new();

	/** Get the current time in nanoseconds */
	long rt_get_time();
	/**
	 * Reschedule the function to execute once at the given time.
	 * Note that only ONE function is ever scheduled.
	 * @nanotime absolute time
	 * @func function to invoke (or NULL to reset)
	 * @return errno
	 */
	int rt_oneshot(RT rt, long nanotime, TimerCallFunc func);
	/**
	 * Reschedule the function to execute once at the given time.
	 * Note that only ONE function is ever scheduled.
	 * @nanotime absolute time
	 * @func function to invoke (or NULL to reset)
	 * @return errno
	 */
	int rt_periodic(RT rt, long nanotime, long interval, TimerCallFunc func);
	/** Pause realtime scheduling
	 * @return errno
	 */
	int rt_pause(RT rt);
	/** Resume realtime scheduling
	 * @return errno
	 */
	int rt_resume(RT rt);

	/** Shut down realtime */
	int rt_term(RT rt);
	
	void rt_free(RT rt);
}
