/*
  VdpArea.java

  (c) 2008-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.video.common;

import ejs.base.utils.HexUtils;

public class VdpArea
{
	public int	base, size;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Base: " + Integer.toHexString(base) + "; Size: " + HexUtils.toHex4(size);
	}
}