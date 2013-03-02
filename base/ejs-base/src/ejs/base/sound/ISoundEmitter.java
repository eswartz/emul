/*
  ISoundEmitter.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package ejs.base.sound;

import javax.sound.sampled.AudioFormat;

public interface ISoundEmitter {
	void started(AudioFormat format);
	void played(ISoundView view);
	void stopped();
	void waitUntilSilent();
	void setVolume(double loudness);
}