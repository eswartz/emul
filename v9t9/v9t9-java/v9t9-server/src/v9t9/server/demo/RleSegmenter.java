/**
 * 
 */
package v9t9.server.demo;

import java.util.Iterator;

/**
 * @author ejs
 *
 */
public class RleSegmenter implements Iterable<RleSegmenter.Segment> {

	public static class Segment {

		private boolean repeat;
		private int offset;
		private int length;
		
		public Segment(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}
		public Segment(int offset, int length, byte repeatByte) {
			this.offset = offset;
			this.length = length;
			this.repeat = true;
		}
		public boolean isRepeat() {
			return repeat;
		}

		public int getOffset() {
			return offset;
		}

		public int getLength() {
			return length;
		}
	}

	private final byte[] data;
	private final int offset;
	private final int end;
	private final int threshold;

	/**
	 * @param threshold minimum number of repeats
	 * @param data
	 * @param offset
	 * @param length
	 */
	public RleSegmenter(int threshold, byte[] data, int offset, int length) {
		this.threshold = threshold;
		this.data = data;
		this.offset = offset;
		this.end = offset + length;
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Segment> iterator() {
		return new Iterator<RleSegmenter.Segment>() {

			int idx = offset;
			
			@Override
			public boolean hasNext() {
				return idx < end;
			}

			@Override
			public Segment next() {
				int origIdx = idx;
				
				boolean isRepeat = false;
				if (idx + 1 < end && data[idx] == data[idx + 1]) {
					// possible repeat
					isRepeat = true;
					idx++;
					while (idx < end && data[origIdx] == data[idx]) {
						idx++;
					}
					
					if (idx - origIdx < threshold) {
						// oh, guess not
						isRepeat = false;
					}
				}
				
				if (!isRepeat) {
					// look for non-repeating data
					int repeatIdx = -1;
					while (idx < end) {
						if (idx + 1 < end && data[idx] == data[idx + 1]) {
							if (repeatIdx < 0) {
								repeatIdx = idx;
							}
						} else {
							if (repeatIdx >= 0) {
								// tracking a possible repeat?
								if (idx + 1 - repeatIdx >= threshold) {
									// ok, that'll work -- get it next time
									idx = repeatIdx;
									break;
								} else {
									repeatIdx = -1;
								}
							}
						}
						idx++;
					}
				}
				
				if (!isRepeat) {
					return new Segment(origIdx, idx - origIdx);
				} else {
					return new Segment(origIdx, idx - origIdx, data[origIdx]);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
