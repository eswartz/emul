/*
  IVdpTMS9918A.java

  (c) 2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;

/**
 * @author ejs
 *
 */
public interface IVdpTMS9918A extends IVdpChip {
	int getVdpRegisterCount();

	int getScreenTableBase();
	int getScreenTableSize();
	int getPatternTableBase();
	int getPatternTableSize();
	int getColorTableBase();
	int getColorTableSize();
	int getSpriteTableBase();
	int getSpriteTableSize();
	int getSpritePatternTableBase();
	int getSpritePatternTableSize();
	
	int getBitmapModeColorMask();
	int getBitmapModePatternMask();
	
	boolean isBitmapMonoMode();

}
