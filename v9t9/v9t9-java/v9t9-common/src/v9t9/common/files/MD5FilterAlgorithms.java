/**
 * 
 */
package v9t9.common.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import v9t9.common.files.IMD5SumFilter.FilterSegment;
import ejs.base.utils.HexUtils;

/**
 * @author ejs
 *
 */
public class MD5FilterAlgorithms {
	private MD5FilterAlgorithms() { }
	
	public static String ALGORITHM_FULL = "full";
	public static String ALGORITHM_SEGMENT = "segment";
	public static String ALGORITHM_GROM = "grom";
	
	public static class FullContentFilter implements IMD5SumFilter {
		public static final FullContentFilter INSTANCE = new FullContentFilter();

		private FullContentFilter() { }
		
		@Override
		public String getId() {
			return ALGORITHM_FULL;
		}
		
		@Override
		public void fillSegments(int contentLength, Collection<FilterSegment> segments) {
			int fragment = contentLength & 0x0fff;
			if (fragment > 0 && fragment < 0x400) {
				// remove cruft bolted to the end
				contentLength -= fragment;
			}
			segments.add(new FilterSegment(0, contentLength));
		}
		
	}
	

	public static class FileSegmentFilter implements IMD5SumFilter {
		private String id;
		private FilterSegment segment;

		public FileSegmentFilter(int offset, int length) {
			this(new FilterSegment(offset, length));
		}
		public FileSegmentFilter(FilterSegment segment) {
			this.segment = segment;
			this.id = ALGORITHM_SEGMENT + ":" 
					+ HexUtils.toHex4(segment.offset)
					+ (segment.length >= 0 ? "+" + HexUtils.toHex4(segment.length) : "");

		}
		
		@Override
		public String getId() {
			return this.id;
		}
		
		@Override
		public void fillSegments(int contentLength, Collection<FilterSegment> segments) {
			segments.add(segment);
		}
		
		public int getLength() {
			return segment.length;
		}
		
		public int getOffset() {
			return segment.offset;
		}
	}

	public static class MultiFileSegmentFilter implements IMD5SumFilter {
		private String id;
		private Collection<FilterSegment> segments;

		public MultiFileSegmentFilter(Collection<FilterSegment> segments) {
			this.segments = segments;
			StringBuilder sb = new StringBuilder();
			for (FilterSegment segment : segments) {
				if (sb.length() > 0)
					sb.append(':');
				sb.append(HexUtils.toHex4(segment.offset)).append('+').append(HexUtils.toHex4(segment.length));
			}
			this.id = sb.toString();
		}
		
		@Override
		public String getId() {
			return id;
		}
		
		@Override
		public void fillSegments(int contentLength, Collection<FilterSegment> segments) {
			segments.addAll(this.segments);
		}
	}

	
	/**
	 * Consider only every 6k of each 8k segment of GROM
	 */
	public static class GromFilter implements IMD5SumFilter {
		public static final GromFilter INSTANCE = new GromFilter();
		
		private GromFilter() { }
		
		@Override
		public String getId() {
			return ALGORITHM_GROM;
		}
		
		@Override
		public void fillSegments(int contentLength, Collection<FilterSegment> segments) {
			int fragment = contentLength & 0x0fff;
			if (fragment > 0 && fragment < 0x400) {
				// remove cruft bolted to the end
				contentLength -= fragment;
			}
			
			for (int offs = 0; offs < contentLength; offs += 0x2000) {
				int limit = Math.min(offs + 0x1800, contentLength);
				segments.add(new FilterSegment(offs, limit - offs));
			}
		}
		
	}

	/**
	 * @param algorithm
	 * @return
	 */
	public static IMD5SumFilter create(String algorithm) {
		if (algorithm == null)
			return null;
		
		if (ALGORITHM_FULL.equals(algorithm))
			return FullContentFilter.INSTANCE;
		
		if (ALGORITHM_GROM.equals(algorithm))
			return GromFilter.INSTANCE;
		
		if (ALGORITHM_SEGMENT.equals(algorithm)) {
			assert false;
		}
		
		if (algorithm.startsWith(ALGORITHM_SEGMENT + ':')) {
			String[] pieces = algorithm.substring(ALGORITHM_SEGMENT.length()+1).split(":");
			return createFromSegments(pieces);
		}
		
		return null;
	}
	

	private static FilterSegment parse(String segment) {
		int offset = 0;
		int length = -1;
		int colIdx = segment.indexOf('+');
		if (colIdx < 0)
			return null;
		offset = Integer.parseInt(segment.substring(0, colIdx), 16);
		length = Integer.parseInt(segment.substring(colIdx+1), 16);
		return new FilterSegment(offset, length);
	}

	public static IMD5SumFilter createFromSegments(String... segments) {
		if (segments.length == 0)
			return FullContentFilter.INSTANCE;
		
		if (segments.length == 1) {
			// simple version
			FilterSegment segment = parse(segments[0]);
			if (segment == null)
				return null;
			return new FileSegmentFilter(segment);
		} else {
			// multiple entries
			List<FilterSegment> segs = new ArrayList<FilterSegment>();
			for (String segment : segments) {
				FilterSegment seg = parse(segment);
				if (seg == null)
					return null;
				segs.add(seg);
			}
			return new MultiFileSegmentFilter(segs);
		}
	}
	
}
