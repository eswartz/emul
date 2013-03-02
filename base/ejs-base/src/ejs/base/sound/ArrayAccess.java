/*
  ArrayAccess.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

/**
 * @author ejs
 *
 */
public class ArrayAccess implements IWriteArrayAccess {

	private float[] array;
	private int count;
	/**
	 * 
	 */
	public ArrayAccess(float[] array) {
		this.array = array;
		this.count = array.length;
	}
	
	/**
	 * @param array
	 * @param count
	 */
	public ArrayAccess(float[] array, int count) {
		this.array = array;
		this.count = count;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.IArrayAccess#size()
	 */
	@Override
	public int size() {
		return count;
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.IArrayAccess#at(int)
	 */
	@Override
	public float at(int absOffs) {
		if (absOffs < 0 || absOffs >= array.length)
			return 0f;
		return array[absOffs];
	}
	/* (non-Javadoc)
	 * @see ejs.base.sound.IWriteArrayAccess#set(int, float)
	 */
	@Override
	public void set(int sampleOffs, float value) {
		if (sampleOffs < array.length)
			array[sampleOffs] = value;
	}

}
