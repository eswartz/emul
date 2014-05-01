/**
 * 
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
}
