/**
 * 
 */
package v9t9.tools.forthcomp.words;

import v9t9.tools.forthcomp.AbortException;
import v9t9.tools.forthcomp.HostContext;
import v9t9.tools.forthcomp.ITargetWord;
import v9t9.tools.forthcomp.IWord;
import v9t9.tools.forthcomp.RelocEntry.RelocType;

/**
 * @author ejs
 *
 */
public class Tick extends BaseStdWord {
	public Tick() {
	}

	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#execute(v9t9.forthcomp.IContext)
	 */
	public void execute(HostContext hostContext, TargetContext targetContext) throws AbortException {
		String name = hostContext.readToken();

		IWord word = targetContext.find(name);
		if (word == null) {
			word = targetContext.defineForward(name, hostContext.getStream().getLocation());
			System.out.println("*** Warning: tick'ing forward word; next word should be ','!");
			targetContext.addRelocation(targetContext.getDP(), RelocType.RELOC_ABS_ADDR_16, 
					((ITargetWord)word).getEntry().getContentAddr());
		}
		
		if (!(word instanceof ITargetWord))
			throw hostContext.abort("cannot take address of host word " + name);
		
		hostContext.pushData(((ITargetWord)word).getEntry().getContentAddr());
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.forthcomp.IWord#isImmediate()
	 */
	public boolean isImmediate() {
		return false;
	}
}
