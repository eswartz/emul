/*
  INoiseVoice.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

/**
 * This represents the extra parameters influencing a noise voice
 * @author ejs
 *
 */
public interface INoiseVoice extends IClockedVoice {
	/** The control nybble for the noise generator (xyy where x is 0=periodic, 1=noise and yy is freq selector) */
	void setControl(int value);
	/** The control nybble for the noise generator (xyy where x is 0=periodic, 1=noise and yy is freq selector)  */
	int getControl();
}
