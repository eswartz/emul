/*
  ILPCEquationFetcher.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech;

import v9t9.common.speech.ILPCParameters;

/**
 * Provide LPC equation(s) for the LPC engine.
 * @author ejs
 *
 */
public interface ILPCEquationFetcher {

	/**
	 * Fetch an equation  
	 * @param fetcher TODO
	 * @param params parameters to fill in
	 */
	void fetchEquation(ILPCDataFetcher fetcher, ILPCParameters params);
}
