/**
 * 
 */
package v9t9.common.sound;

import v9t9.common.hardware.ISoundChip;

/**
 * @author ejs
 *
 */
public interface IMultiSoundChip extends ISoundChip {

	ISoundChip getChip(int num);
	int getChipCount();
}
