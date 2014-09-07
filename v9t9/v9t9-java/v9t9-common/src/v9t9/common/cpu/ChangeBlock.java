/**
 * 
 */
package v9t9.common.cpu;

import java.util.Arrays;

/**
 * This represents the changes made by an instruction,
 * which may be applied and reverted from the CPU.
 * @author ejs
 *
 */
public abstract class ChangeBlock {
    private static final IChangeElement[] NONE = new IChangeElement[0];
    
    /** cycles consumed by decoding instruction & operands (others from IChangeElements) */
    public int cycles;
    
    private IChangeElement[] elements;
    private int elementIdx;
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + elementIdx;
		result = prime * result + cycles;
		for (int i = 0; i < elementIdx; i++)
			result = prime * result + elements[i].hashCode();
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeBlock other = (ChangeBlock) obj;
		if (elementIdx != other.elementIdx)
			return false;
		if (cycles != other.cycles)
			return false;
		for (int i = 0; i < elementIdx; i++)
			if (!elements[i].equals(other.elements[i]))
				return false;
		return true;
	}

	public void push(IChangeElement element) {
		if (elements == null) {
			elements = new IChangeElement[8];
//			element.setParent(getParent());
		} else {
//			element.setParent(elements[elementIdx - 1]);
		}
		elements[elementIdx++] = element;
	}
	
	public IChangeElement last() {
		return elementIdx > 0 ? elements[elementIdx - 1] : null;
	}
	
	public void apply(ICpuState cpuState) {
		for (int i = 0; i < elementIdx; i++)
			elements[i].apply(cpuState);
	}
	public void revert(ICpuState cpuState) {
		for (int i = elementIdx - 1; i >= 0; i--)
			elements[i].revert(cpuState);
	}


	/**
	 * @return
	 */
	public IChangeElement[] copyElements() {
		if (elements != null && elementIdx == elements.length)
			return elements;
		if (elements == null)
			return NONE;
		return Arrays.copyOf(elements, elementIdx);
	}

	public int getCount() {
		return elementIdx;
	}
	public IChangeElement getElement(int index) {
		return elements[index];
	}
}
