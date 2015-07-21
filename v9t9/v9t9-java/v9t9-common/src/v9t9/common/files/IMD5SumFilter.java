/**
 * 
 */
package v9t9.common.files;

import java.util.Collection;

import ejs.base.utils.HexUtils;

/**
 * This interface defines which content from a candidate file 
 * is summed to make MD5 matches.
 * 
 * This should define #equals and #hashCode for use in 
 * repeated lookups. 
 * @author ejs
 *
 */
public interface IMD5SumFilter {
	public class FilterSegment {
		public FilterSegment(int offs, int count) {
			this.offset = offs;
			this.length = count;
		}
		public int offset;
		public int length;
		
		@Override
		public String toString() {
			return HexUtils.toHex4(offset) + "+" + HexUtils.toHex4(length);
		}
	}

	/** Get the identifier for referencing the filter */
	String getId();
	
	/** Fill in the segments that should be considered from the given URI */
	void fillSegments(int contentLength, Collection<IMD5SumFilter.FilterSegment> segments);

}