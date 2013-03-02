/*
  BaseFifoLpcDataFetcher.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.engine.speech;

/**
 * @author ejs
 *
 */
public abstract class BaseFifoLpcDataFetcher extends BaseLpcDataFetcher implements IFifoLpcDataFetcher, ILPCByteFetcher {

	protected IFifoStatusListener listener;

	/**
	 * 
	 */
	public BaseFifoLpcDataFetcher() {
		super();
	}

	public void setListener(IFifoStatusListener listener) {
		this.listener = listener;
	}

}