/**
 * 
 */
package org.ejs.v9t9.forthcomp;

/**
 * @author ejs
 *
 */
public class QuestionDoParser implements IWord {
	public QuestionDoParser() {
	}

	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#execute(org.ejs.v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		hostContext.assertCompiling();
		
		ITargetWord doWord = (ITargetWord) targetContext.find("(do)");
		if (doWord == null)
			throw hostContext.abort("no (do)");
		
		ITargetWord twoDup = (ITargetWord) targetContext.find("2dup");
		if (twoDup== null)
			throw hostContext.abort("no 2dup");

		ITargetWord sub = (ITargetWord) targetContext.find("-");
		if (sub == null)
			throw hostContext.abort("no -");
		ITargetWord zeroEqu = (ITargetWord) targetContext.find("0=");
		if (zeroEqu == null)
			throw hostContext.abort("no 0=");

		targetContext.compile(twoDup);
		targetContext.compile(doWord);
		
		targetContext.compile(sub);
		targetContext.compile(zeroEqu);
		
		new IfParser().execute(hostContext, targetContext);
		new LeaveParser().execute(hostContext, targetContext);
		new ThenParser().execute(hostContext, targetContext);
		
		//hostContext.leaves().add(targetContext.pushFixup(hostContext));
		
		hostContext.pushData(targetContext.getDP());
		hostContext.pushPairs(3);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return true;
	}
}
