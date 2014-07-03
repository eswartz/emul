/*
  TestQuote.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp.words;

import java.util.ArrayList;
import java.util.List;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.ForthComp;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ISemantics;

/**
 * @author ejs
 *
 */
public class TestQuote extends BaseWord {

	protected int testNum;
	protected List<String> testWords = new ArrayList<String>();
	private ForthComp compiler;

	public TestQuote() {
		setInterpretationSemantics(new ISemantics() {
			
			@Override
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {

				new SQuote().getInterpretationSemantics().execute(hostContext, targetContext);
				
				int leng = hostContext.popData();
				int addr = hostContext.popData();
				
				if (targetContext.isTestMode()) {
					StringBuilder bodysb = new StringBuilder();
					
					while (leng-- > 0) {
						bodysb.append((char) targetContext.readChar(addr++));
					}
					
					String[] bodyText = bodysb.toString().trim().split("\\s", 2);
					
					String name = "$test" + testNum++ + "-" + bodyText[0];
					
					StringBuilder sb = new StringBuilder(); 
					sb.append("| : ").append(name).append(" ");
					sb.append(bodyText[1]);
					sb.append(" ;");
					
					System.out.println("TEST: " + sb + " \\ " + Integer.toHexString(targetContext.getDP()));
					
					compiler.parseString(
							hostContext.getStream().getLocation() + " > " + name, 
							sb.toString());
					
					testWords.add(name);
				}
			}
		});
		
	}

	public void setCompiler(ForthComp compiler) {
		this.compiler = compiler;
	}

	public void finish(HostContext hostContext, TargetContext targetContext) throws AbortException {
		if (!targetContext.isTestMode())
			return;
		
		StringBuilder sb = new StringBuilder();

		sb.append("| : RUNTEST ( addr -- ) ");
		sb.append(" EXECUTE  0= IF ABORT\" failed\" THEN \n");
		sb.append(" ;\n");
		
		sb.append("| : RUNTESTS ");
		for (String testWord : testWords) {
			sb.append("['] ").append(testWord).append(" RUNTEST regs-init ");
		}
		sb.append(" tests-completed ;\n");
		
		compiler.parseString(
				"RUNTESTS", 
				sb.toString());

		testWords.clear();
	}
}
