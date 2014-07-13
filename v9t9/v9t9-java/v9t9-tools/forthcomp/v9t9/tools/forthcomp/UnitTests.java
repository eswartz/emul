/*
  TestQuote.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import v9t9.tools.forthcomp.words.BaseWord;

/**
 * @author ejs
 *
 */
public class UnitTests extends BaseWord {

	protected int testNum;
	protected StringBuilder testDefinitionText = new StringBuilder();
	protected StringBuilder testWordText = new StringBuilder();
	private ForthComp compiler;

	public void setCompiler(ForthComp compiler) {
		this.compiler = compiler;
	}

	public void addTest(String text) throws AbortException {
		String[] bodyText = text.trim().split("\\s", 2);
		
		String name = "$test" + testNum++ + "-" + bodyText[0];
		
		StringBuilder sb = new StringBuilder(); 
		sb.append("| : ").append(name).append(" ");
		sb.append(bodyText[1]);
		sb.append(" ;");
		
		ITargetContext targetContext = compiler.getTargetContext();
		System.out.println("TEST: " + sb + " \\ " + Integer.toHexString(targetContext.getDP()));
		
		testDefinitionText.append(sb.toString()).append("\n");
		
//		compiler.parseString(
//				hostContext.getStream().getLocation() + " > " + name, 
//				sb.toString());
//
		
		testWordText.append("['] ").append(name).append(" RUNTEST regs-init\n");
	}


	public void addText(String text) throws AbortException {
		testDefinitionText.append(" DECIMAL \n").append(text).append("\n");
//		HostContext hostContext = compiler.getHostContext();
//		
//		compiler.parseString(
//				hostContext.getStream().getLocation() + " > " + text, 
//				text);
	}

	public void finish() throws AbortException {
		if (!compiler.getTargetContext().isTestMode())
			return;
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(testDefinitionText);

		sb.append("DECIMAL  | : RUNTEST ( addr -- ) ");
		sb.append(" $abcd swap ");
		sb.append(" EXECUTE  0= IF ~FAILURE~ THEN \n");
		sb.append(" $abcd  <> IF ABORT\" stack damage\" ~FAILURE~ THEN \n");
		sb.append(" ;\n");
		
		sb.append("| : RUNTESTS ");
		sb.append(testWordText);
		sb.append(" ~SUCCESS~ ;\n");
		
		compiler.getHostContext().getLog().println(sb);
		compiler.getHostContext().getLog().flush();
		
		compiler.parseString(
				"RUNTESTS", 
				sb.toString());

	}
}
