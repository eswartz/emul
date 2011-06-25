/**
 * 
 */
package v9t9.forthcomp.words;

import v9t9.forthcomp.AbortException;
import v9t9.forthcomp.DictEntry;
import v9t9.forthcomp.HostContext;
import v9t9.forthcomp.ISemantics;
import v9t9.forthcomp.ITargetWord;

/**
 * @author ejs
 *
 */
public class TargetConstant extends TargetWord implements ITargetWord {

	private final int value;
	private final int width;

	/**
	 * @param entry
	 */
	public TargetConstant(String name, int value_, int width_) {
		super(new DictEntry(0, 0, name));
		this.value = value_;
		this.width = width_;
		
		setCompilationSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				if (getEntry().canInline()) {
					if (getWidth() == 1)
						targetContext.compileLiteral(getValue(), false, true);
					else if (getWidth() == 2 && targetContext.getCellSize() == 2)
						targetContext.compileDoubleLiteral(getValue() & 0xffff, getValue() >> 16, false, true);
					else
						assert false;
				} else {
					targetContext.compile(TargetConstant.this);
				}
			}
		});
		setExecutionSemantics(new ISemantics() {
			
			public void execute(HostContext hostContext, TargetContext targetContext)
					throws AbortException {
				hostContext.pushData(value & 0xffff);
				if (width == 2)
					hostContext.pushData(value >> 16);
				
			}
		});
	}

	/**
	 * @return
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
}
