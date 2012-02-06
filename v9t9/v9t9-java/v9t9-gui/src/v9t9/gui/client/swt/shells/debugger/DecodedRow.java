/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

/**
 * @author ejs
 *
 */
public class DecodedRow {

	private final IDecodedContent content;
	private final MemoryRange range;

	/**
	 * @param addr
	 * @param range
	 * @param decode
	 */
	public DecodedRow(IDecodedContent content, MemoryRange range) {
		this.range = range;
		this.content = content;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((range == null) ? 0 : range.hashCode());
		result = prime * result + content.getAddr();
		result = prime * result + content.getSize();
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DecodedRow other = (DecodedRow) obj;
		if (range == null) {
			if (other.range != null)
				return false;
		} else if (!range.equals(other.range))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else {
			if (content.getAddr() != other.content.getAddr())
				return false;
			if (content.getSize() != other.content.getSize())
				return false;
		}
		return true;
	}



	/**
	 * @return the content
	 */
	public IDecodedContent getContent() {
		return content;
	}
	/**
	 * @return the range
	 */
	public MemoryRange getRange() {
		return range;
	}
	
	

}
