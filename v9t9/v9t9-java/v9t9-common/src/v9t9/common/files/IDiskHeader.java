/**
 * 
 */
package v9t9.common.files;

/**
 * @author ejs
 *
 */
public interface IDiskHeader {
	String getPath();
	
	/** tracks per side */
	int getTracks();		
	/** 1 or 2 */
	int getSides();		
	/** bytes per track */
	int getTrackSize();	
	/** offset for track 0 data */
	int getTrack0Offset();
	/** estimate image size */
	long getImageSize();
	int getTrackOffset(int num);

}
