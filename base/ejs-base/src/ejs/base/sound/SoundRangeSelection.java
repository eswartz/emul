/*
  SoundRangeSelection.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;


/**
 * @author ejs
 *
 */
public class SoundRangeSelection extends BaseSoundView implements ISoundRangeSelection {

	private int startFrame;
	private int frameCount;
	private ISoundView view;

	/**
	 * 
	 */
	public SoundRangeSelection(ISoundView view, int startFrame, int frameCount) {
		super(startFrame * view.getChannelCount(), frameCount * view.getChannelCount(), view.getFormat());
		this.view = view;
		this.startFrame = startFrame;
		this.frameCount = frameCount;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return startFrame + " @ " +frameCount;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + frameCount;
		result = prime * result + startFrame;
		return result;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SoundRangeSelection other = (SoundRangeSelection) obj;
		if (view != other.view)
			return false;
		if (frameCount != other.frameCount)
			return false;
		if (startFrame != other.startFrame)
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return frameCount == 0;
	}

	/* (non-Javadoc)
	 * @see audiomonkey.engine.ISoundRangeSelection#getSoundView()
	 */
	@Override
	public ISoundView getSoundView() {
		return view;
	}
	/* (non-Javadoc)
	 * @see audiomonkey.gui.common.ISoundRangeSelection#getStartSample()
	 */
	@Override
	public int getSelectedStartFrame() {
		return startFrame;
	}

	/* (non-Javadoc)
	 * @see audiomonkey.gui.common.ISoundRangeSelection#getSampleCount()
	 */
	@Override
	public int getSelectedFrameCount() {
		return frameCount;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.BaseSoundView#isSilent()
	 */
	@Override
	public boolean isSilent() {
		return view.isSilent() || frameCount == 0;
	}

	/* (non-Javadoc)
	 * @see ejs.base.sound.BaseSoundView#at(int)
	 */
	@Override
	public float at(int absOffs) {
		return view.at(absOffs + startFrame * view.getChannelCount());
	}
	

	/* (non-Javadoc)
	 * @see ejs.base.sound.BaseSoundView#getSoundView(int, int)
	 */
	@Override
	public ISoundView getSoundView(int fromSample, int count) {
		return new SoundRangeSelection(view, fromSample + startFrame * view.getChannelCount(), 
				count * view.getChannelCount());
	}


}
