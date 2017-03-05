/*
  ISoundChip.java

  (c) 2009-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;


import ejs.base.properties.IPersistable;
import v9t9.common.machine.IRegisterAccess;

/**
 * This interface is used to hook up a sound chip to a SoundHandler.
 * @author ejs
 *
 */
public interface ISoundChip extends IPersistable, IRegisterAccess {
	/** Write a byte to the sound chip(s) */
	void writeSound(int addr, byte val);

	void setAudioGate(int addr, boolean b);
	void reset();
}
