/*
  Structure.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.winmm;

import java.lang.reflect.Array;

public class Structure<Base, Foo, Bar> extends com.sun.jna.Structure {

	  @SuppressWarnings("unchecked")
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
