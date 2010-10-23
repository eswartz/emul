/**
 * 
 */
package org.ejs.v9t9.forthcomp;

import java.io.IOException;
import java.util.Stack;

/**
 * @author ejs
 *
 */
public class HostContext extends Context {
	private Stack<Integer> dataStack;
	private TokenStream tokenStream;
	private Stack<Integer> returnStack;

	/**
	 * 
	 */
	public HostContext() {
		super();
		dataStack = new Stack<Integer>();
		returnStack = new Stack<Integer>();
		tokenStream = new TokenStream();

	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IContext#pushData(int)
	 */
	public void pushData(int value) {
		dataStack.push(value);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IContext#popData()
	 */
	public int popData() {
		return dataStack.pop();
	}
	/**
	 * @return
	 */
	public TokenStream getStream() {
		return tokenStream;
	}
	/**
	 * @return
	 * @throws AbortException 
	 * @throws AbortException 
	 */
	public String readToken() throws AbortException  {
		try {
			return tokenStream.read();
		} catch (IOException e) {
			throw abort("end of file");
		}
	}
	/**
	 * @param string
	 * @return
	 */
	public AbortException abort(String string) {
		return tokenStream.abort(string);
	}

	/**
	 * @param i
	 */
	public void pushReturn(int i) {
		returnStack.push(i);
	}
	
	public int popReturn() {
		return returnStack.pop();
	}

	/**
	 * @return
	 */
	public Stack<Integer> getDataStack() {
		return dataStack;
	}
	/**
	 * @return the returnStack
	 */
	public Stack<Integer> getReturnStack() {
		return returnStack;
	}

	/**
	 * @return 
	 * @throws AbortException 
	 * 
	 */
	public HostVariable assertCompiling() throws AbortException {
		HostVariable state = (HostVariable)require("state");
		if (state.getValue() == 0)
			throw abort("not defining");
		return state;

	}

	public void setCompiling(boolean b) throws AbortException {
		HostVariable state = (HostVariable)require("state");
		int val = b ? 1 : 0;
		if (state.getValue() == val)
			throw abort("already defining");
		state.setValue(val);
		
	}

	/**
	 * @throws AbortException 
	 * 
	 */
	public void stopCompiling() throws AbortException {
		assertCompiling().setValue(0);
	}

	/**
	 * @param i
	 * @throws AbortException 
	 */
	public void assertPairs(int i) throws AbortException {
		if (popData() != i)
			throw abort("mismatched conditional: " + i);
	}

	/**
	 * @param i
	 */
	public void pushPairs(int i) {
		pushData(i);
	}

	/**
	 * @return
	 */
	public int peekData() {
		return dataStack.peek();
	}

	public void setCSP() throws AbortException {
		HostVariable csp = (HostVariable)require("csp");
		csp.setValue(dataStack.size());
	}
	public void assertCSP() throws AbortException {
		HostVariable csp = (HostVariable)require("csp");
		if (csp.getValue() != dataStack.size())
			throw abort("mismatched conditionals");
	}
}
