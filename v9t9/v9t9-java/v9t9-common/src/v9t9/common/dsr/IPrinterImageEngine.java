/*
  IPrinterImageEngine.java

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
public interface IPrinterImageEngine {

	/**
	 * Get the id of the printer (e.g. make/model emulated)
	 */
	String getPrinterId();

	void addListener(IPrinterImageListener listener);

	void removeListener(IPrinterImageListener listener);

	void newPage();

	void print(byte ch);
	void print(char ch);
	void print(String text);

	/**
	 * 
	 */
	void flushPage();

	/**
	 * @param horizDpi
	 * @param vertDpi
	 */
	void setDpi(int horizDpi, int vertDpi);

	/**
	 * Get the location of the print head vertically on the page, from 0.0 to 1.0
	 * @return
	 */
	double getPageRowPercentage();
	/**
	 * Get the location of the print head horizontally on the page, from 0.0 to 1.0
	 * @return
	 */
	double getPageColumnPercentage();

}