/*
  ICassetteVoice.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.sound;

/**
 * @author ejs
 *
 */
public interface ICassetteVoice {

	void setState(boolean state);
	boolean getState();
	
	void setMotor1(boolean motor);
	void setMotor2(boolean motor);
	/**
	 * @param secs
	 */
	void setClock(float secs);
}
