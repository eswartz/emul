/**
 * 
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
