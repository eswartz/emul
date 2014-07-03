/*
  TestQuote.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;
import v9t9.tools.forthcomp.TargetContext;
import v9t9.tools.forthcomp.UnitTests;

/**
 * @author ejs
 *
 */
public class TestQuote extends BaseWord {

	private UnitTests unitTests;

	public TestQuote() {
		setInterpretationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				
				int leng = hostContext.popData();
				int addr = hostContext.popData();
				
				StringBuilder bodysb = new StringBuilder();
				
				while (leng-- > 0) {
					bodysb.append((char) targetContext.readChar(addr++));
				}

				if (unitTests != null) {
					unitTests.addTest(bodysb.toString());
				}
			}
		});
		
	}

	
	public void setUnitTests(UnitTests unitTests) {
		this.unitTests = unitTests;

	}

}
