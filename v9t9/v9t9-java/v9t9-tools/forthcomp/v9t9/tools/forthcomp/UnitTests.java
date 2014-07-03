/*
  TestQuote.java

  (c) 2010-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.forthcomp;

import java.util.ArrayList;
import java.util.List;

import v9t9.tools.forthcomp.words.BaseWord;

/**
 * @author ejs
 *
 */
public class UnitTests extends BaseWord {

	protected int testNum;
	protected List<String> testWords = new ArrayList<String>();
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
		
		HostContext hostContext = compiler.getHostContext();
		ITargetContext targetContext = compiler.getTargetContext();
		System.out.println("TEST: " + sb + " \\ " + Integer.toHexString(targetContext.getDP()));
		
		testWords.add(name);
		
		compiler.parseString(
				hostContext.getStream().getLocation() + " > " + name, 
				sb.toString());
		
	}


	public void addText(String text) throws AbortException {
		HostContext hostContext = compiler.getHostContext();
		
		compiler.parseString(
				hostContext.getStream().getLocation() + " > " + text, 
				text);
	}

	public void finish() throws AbortException {
		if (!compiler.getTargetContext().isTestMode())
			return;
		
		StringBuilder sb = new StringBuilder();

		sb.append("| : RUNTEST ( addr -- ) ");
		sb.append(" $abcd swap ");
		sb.append(" EXECUTE  0= IF ABORT\" failed\" THEN \n");
		sb.append(" $abcd  <> IF ABORT\" stack damage\" THEN \n");
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
