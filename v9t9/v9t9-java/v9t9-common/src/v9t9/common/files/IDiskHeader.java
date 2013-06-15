/*
  IDiskHeader.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.files;

/**
 * @author ejs
 *
 */
public interface IDiskHeader {
	String getPath();
	
	int getSecsPerTrack();
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
	int getTrackOffset(int num, int side);

	void setSides(int sides);
	void setTracks(int tracks);
	void setSecsPerTrack(int secsPerTrack);
	void setTrackSize(int tracksize);
	void setTrack0Offset(int track0offs);
	void setPath(String path);

	boolean isInvertedSide2();
	void setInvertedSide2(boolean invertedSide2);

	/**
	 * @return
	 */
	boolean isSide2DirectionKnown();

	/**
	 * @param b
	 */
	void setSide2DirectionKnown(boolean b);

}
