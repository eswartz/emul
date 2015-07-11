/*
  CharacterMatrix.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.printer;

import java.util.Arrays;

/**
 * @author ejs
 *
 */
public class CharacterMatrix {

	private char ch;
	private int height;
	private int width;
	private boolean[][] set;

	/**
	 * @param ch
	 */
	public CharacterMatrix(char ch, int width, int height) {
		this.ch = ch;
		this.width = width;
		this.height = height;
		set = new boolean[width][height];
		for (int i = 0; i < width; i++)
			set[i] = new boolean[height];
	}

	
	
	@Override
	public String toString() {
		return "CharacterMatrix [ch=" + ch + ", height=" + height + ", width="
				+ width + ", set=" + Arrays.toString(set) + "]";
	}



	/**
	 * @param row
	 * @param col
	 * @param b
	 */
	public void set(int row, int col, boolean b) {
		this.set[col][row] = b;
	}
	public boolean isSet(int row, int col) {
		if (col >= width || row >= height) return false; 
		return set[col][row];
	}



	/**
	 * @param cx
	 * @return
	 */
	public int getColumn(int cx) {
		int by = 0;
		int mask = 1 << height;
		for (int cy = 0; cy < height; cy++) {
			boolean s = isSet(cy, cx);
			if (s) {
				by |= mask;
			}
			mask >>= 1;
		}
		return by;
	}
}
