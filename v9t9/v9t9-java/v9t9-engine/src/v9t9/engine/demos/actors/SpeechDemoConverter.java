/*
  SpeechDemoConverter.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
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
