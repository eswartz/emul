/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package ejs.base.utils;

public class Check {
    public static void checkArg(Object o) {
        if (o == null) {
			throw new IllegalArgumentException();
		}
    }
    public static void checkArg(boolean state) {
        if (!state) {
			throw new IllegalArgumentException();
		}
    }
    public static void checkState(boolean b) {
        if (!b) {
			throw new IllegalStateException();
		}
    }
    public static void failedArg(Throwable t) {
        throw new IllegalArgumentException(t);
    }
}
