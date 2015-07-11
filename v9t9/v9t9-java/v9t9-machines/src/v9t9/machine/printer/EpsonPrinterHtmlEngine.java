/*
  EpsonPrinterHtmlEngine.java

  (c) 2014 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.printer;

import v9t9.common.dsr.IPrinterHtmlEngine;
import v9t9.common.dsr.IPrinterHtmlHandler;
import v9t9.common.dsr.IPrinterHtmlListener;
import ejs.base.utils.ListenerList;

/**
 * @author ejs
 *
 */
public class EpsonPrinterHtmlEngine implements IPrinterHtmlHandler, IPrinterHtmlEngine {
	
	private ListenerList<IPrinterHtmlListener> listeners = new ListenerList<IPrinterHtmlListener>();
	private boolean firstPage = true;
	private StringBuilder page = new StringBuilder();
	private int emptySize;
	private int column;
	private int tabSize = 8;
	private boolean atEsc;
	
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IRS232HtmlHandler#addListener(v9t9.machine.printer.IRS232HtmlListener)
	 */
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IPrinterHtmlEngine#addListener(v9t9.common.dsr.IPrinterHtmlListener)
	 */
	@Override
	public void addListener(IPrinterHtmlListener listener) {
		listeners.add(listener);
	}
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IRS232HtmlHandler#removeListener(v9t9.machine.printer.IRS232HtmlListener)
	 */
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IPrinterHtmlEngine#removeListener(v9t9.common.dsr.IPrinterHtmlListener)
	 */
	@Override
	public void removeListener(IPrinterHtmlListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IPrinterHtmlEngine#newPage()
	 */
	@Override
	public void newPage() {
		if (firstPage || page.length() > emptySize) {
			listeners.fire(new ListenerList.IFire<IPrinterHtmlListener>() {

				@Override
				public void fire(IPrinterHtmlListener listener) {
					termPage();
					listener.updated(page.toString());
					initPage();
				}
			});
			firstPage = false;
		}
		listeners.fire(new ListenerList.IFire<IPrinterHtmlListener>() {
			
			@Override
			public void fire(IPrinterHtmlListener listener) {
				listener.newPage();
			}
		});
	}
	
	/**
	 * 
	 */
	protected void initPage() {
		page.setLength(0);
		page.append("<html>\n");
		page.append("<head>\n");
		page.append("</head>\n");
		page.append("<body>\n");
		page.append("<pre>\n");
		home();
		emptySize = page.length();
	}
	protected void termPage() {
		page.append("</pre>\n");		
		page.append("</body>\n");		
		page.append("</html>\n");		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.machine.printer.IPrinterHtmlEngine#sendChar(char)
	 */
	@Override
	public void sendChar(char ch) {
		if (atEsc) {
			handleEsc(ch);
			return;
		}
		
		switch (ch) {
		case '\r':
			page.append('\r');
			home();
			break;
		case '\n':
			page.append("<br/>\n");
			break;
		case '\t':  {
			int nextTab = column + (tabSize - column % tabSize);
			while (column < nextTab) {
				page.append("&nbsp;");
				column++;
			}
			break;
		}
		case 27: {
			atEsc = true;
			break;
		}
			
		default:
			page.append(ch);
			column++;
			break;
		}
	}
	/**
	 * @param ch
	 */
	private void handleEsc(char ch) {
		switch (ch) {
		
		}
	}
	/**
	 * 
	 */
	private void home() {
		column = 0;
	}

}
