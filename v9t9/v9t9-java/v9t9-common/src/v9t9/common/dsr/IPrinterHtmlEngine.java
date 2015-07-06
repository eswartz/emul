/*
  IPrinterHtmlEngine.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.dsr;


/**
 * @author ejs
 *
 */
public interface IPrinterHtmlEngine {

	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IRS232HtmlHandler#addListener(v9t9.machine.printer.IRS232HtmlListener)
	 */
	void addListener(IPrinterHtmlListener listener);

	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IRS232HtmlHandler#removeListener(v9t9.machine.printer.IRS232HtmlListener)
	 */
	void removeListener(IPrinterHtmlListener listener);

	/**
	 * 
	 */
	void newPage();

	void sendChar(char ch);

}