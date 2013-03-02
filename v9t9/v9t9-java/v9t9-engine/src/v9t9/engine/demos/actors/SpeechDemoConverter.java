/*
  SpeechDemoConverter.java

  (c) 2012 Edward Swartz

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
package v9t9.engine.demos.actors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import v9t9.common.speech.ILPCParametersListener;
import v9t9.engine.speech.LPCParameters;
import ejs.base.utils.BitInputStream;
import ejs.base.utils.ListenerList;
import ejs.base.utils.ListenerList.IFire;

/**
 * @author ejs
 *
 */
public class SpeechDemoConverter {

	private ListenerList<ILPCParametersListener> listeners = new ListenerList<ILPCParametersListener>();

	private ByteArrayOutputStream bis = new ByteArrayOutputStream();
	
	/**
	 * 
	 */
	public SpeechDemoConverter() {
	}
	
	public void addEquationListener(ILPCParametersListener listener) {
		listeners.add(listener);
		
	}

	public void removeEquationListener(
			ILPCParametersListener listener) {
		listeners.remove(listener);
	}

	/**
	 * 
	 */
	public void startPhrase() {
		bis.reset();
	}

	/**
	 * 
	 */
	public void stopPhrase() {
		flush();
	}

	/**
	 * 
	 */
	public void terminatePhrase() {
		flush();
	}

	/**
	 * 
	 */
	private void flush() {
		if (bis.size() > 0) {
			BitInputStream bs = new BitInputStream(new ByteArrayInputStream(bis.toByteArray()));
			
			while (true) {
				final LPCParameters params = new LPCParameters();
				try {
					params.fromBytes(bs);
					listeners.fire(new IFire<ILPCParametersListener>() {
	
						@Override
						public void fire(ILPCParametersListener listener) {
							listener.parametersAdded(params);
						}
					});
				} catch (IOException e) {
					break;
				}
			}
			try {
				bs.close();
			} catch (IOException e) {
			}
			bis.reset();
		}
	}

	/**
	 * @param data
	 */
	public void pushByte(byte data) {
		bis.write(data);
	}

}
