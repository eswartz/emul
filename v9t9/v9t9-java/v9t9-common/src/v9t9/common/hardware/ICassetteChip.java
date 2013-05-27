/*
  ICassetteChip.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.hardware;

import ejs.base.properties.IPersistable;
import v9t9.common.machine.IRegisterAccess;
import v9t9.common.sound.ICassetteVoice;

/**
 * @author ejs
 *
 */
public interface ICassetteChip extends IPersistable, IRegisterAccess {
	ICassetteVoice getCassetteVoice();
	void reset();
}
