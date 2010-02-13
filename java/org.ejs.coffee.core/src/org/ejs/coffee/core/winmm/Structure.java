package org.ejs.coffee.core.winmm;

import java.lang.reflect.Array;

public class Structure<Base, Foo, Bar> extends com.sun.jna.Structure {

	  public static<Base> Base[] newArray(
			Class<Base> class1, int arrayLength) {
		Object arr = Array.newInstance(class1, arrayLength);
		Base[] ret = (Base[]) arr;
		for (int i = 0; i < arrayLength; i++)
			try {
				ret[i] = (Base) class1.newInstance();
			} catch (Exception e) {
			}
		return ret;
	}

}
