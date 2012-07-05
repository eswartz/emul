/**
 * 
 */
package ejs.base.sound;

import org.eclipse.jface.viewers.ISelection;


/**
 * @author ejs
 *
 */
public interface ISoundRangeSelection extends ISelection, ISoundView {

	ISoundView getSoundView();
	
	int getSelectedStartFrame();
	int getSelectedFrameCount();
}
