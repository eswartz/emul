/*
  IStatus.java

  (c) 2005-2011 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.common.cpu;

public interface IStatus {

	// Status setting flags
	public static final int stset_NONE = 0; // status not affected
	public static final int stset_ALL = 1; // all bits changed
	public static final int stset_INT = 2; // interrupt mask

	String toString();

	void copyTo(IStatus copy);

	short flatten();

	void expand(short stat);

	/** is arith greater */
	boolean isLT();

	/** is logical lower or equal */
	boolean isLE();

	/** is logical lower */
	boolean isL();

	/** is equal */
	boolean isEQ();

	/** is not equal */
	boolean isNE();

	/** is logical higher or equal */
	boolean isHE();

	/** is arith greater or equal */
	boolean isGT();

	/** is logical higher */
	boolean isH();

	/** is carry */
	boolean isC();

	int getIntMask();

}