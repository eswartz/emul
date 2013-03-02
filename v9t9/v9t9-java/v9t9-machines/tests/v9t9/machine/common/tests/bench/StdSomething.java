/*
  StdSomething.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.common.tests.bench;

/**
 * @author ejs
 *
 */
public class StdSomething implements ISomething {

	/* (non-Javadoc)
	 * @see v9t9.machine.common.tests.bench.ISomething#run()
	 */
	@Override
	public void run() throws Exception {
		if (System.currentTimeMillis() % 2 == 0)
			throw new Exception();
	}

}
