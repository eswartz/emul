/**
 * 
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.graphics.Point;


public interface IImageBar extends IImageCanvas {
	

	public interface IPaintOffsetListener {
		void offsetChanged(Point pt);
	}
	

	
	void addPaintOffsetListener(IPaintOffsetListener listener);
}