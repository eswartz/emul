/**
 * 
 */
package v9t9.common.video;


/**
 * This interface serves to access the sprite data written onto
 * a canvas, in the event the data is not directly blitted onto
 * an IVdpCanvas.
 * @author ejs
 *
 */
public interface ISpriteVdpCanvas extends ICanvas, ISpriteDrawingCanvas {

	byte getColorAtOffset(int offset);
	
	int getBitmapOffset(int x, int y);
}