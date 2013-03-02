/*
  SwtAwtJavaClient.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.gui.client.swt;

import v9t9.common.client.IClient;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.awt.AwtKeyboardHandler;

/**
 * This client does all its own dang work!
 * @author ejs
 */
public class SwtAwtJavaClient extends BaseSwtJavaClient implements IClient {
	public static String ID = "SWTAWT";
	
    public SwtAwtJavaClient(IMachine machine) {
    	super(machine);
    }

    @Override
    public String getIdentifier() {
    	return ID;
    }
	/**
	 * 
	 */
	protected void setupRenderer() {
		videoRenderer = new SwtAwtVideoRenderer(machine, videoTimer);
	
		keyboardHandler = new AwtKeyboardHandler(
    		machine.getKeyboardState(), machine);
	}
}

