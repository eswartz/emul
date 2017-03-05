/*
  ILPCParametersListener.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.speech;

/**
 * Listener for the mathematical details of speech, from the
 * LPC decoding point of view.  Each callback indicates that
 * the given parameters were converted to speech.
 * @author ejs
 *
 */
public interface ILPCParametersListener {

	void parametersAdded(ILPCParameters params);
}
