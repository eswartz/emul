/*
  IClockedVoice.java

  (c) 2011-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.sound;

import v9t9.common.machine.IRegisterBank;

/**
 * This represents the parameters controlling a single voice
 * which has a volume and a frequency
 * @author ejs
 *
 */
public interface IClockedVoice extends IRegisterBank {
	/** Set the voice frequency */
	void setPeriod(int hz);
	/** Get the voice frequency */
	int getPeriod();
	
	/** Set the voice volume (0=silent, 255=max) */
	void setAttenuation(int vol);
	/** Get the voice attenuation (0=silent, 255=max) */
	int getAttenuation();

}
