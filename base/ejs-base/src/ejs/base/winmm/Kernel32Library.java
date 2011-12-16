package ejs.base.winmm;

import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;

/**
 */
public interface Kernel32Library extends com.sun.jna.Library {
	public static class HANDLE extends PointerType {} 
	public static class HWAVEOUT extends PointerType {} 
	public static final java.lang.String JNA_LIBRARY_NAME = "kernel32";
	public static final com.sun.jna.NativeLibrary JNA_NATIVE_LIB = com.sun.jna.NativeLibrary.getInstance(Kernel32Library.JNA_LIBRARY_NAME);
	public static final Kernel32Library INSTANCE = (Kernel32Library)com.sun.jna.Native.loadLibrary(Kernel32Library.JNA_LIBRARY_NAME, Kernel32Library.class);

	int CreateSemaphoreW(Object security,int x,int y,String name);
	int ReleaseSemaphore(int handle,int x,IntByReference cnt);
	void CloseHandle(int h);

	void SleepEx(int msecs, boolean whatever);
}
