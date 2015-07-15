/**
 * 
 */
package v9t9.common.files;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import v9t9.common.files.IMD5SumFilter.FilterSegment;
import ejs.base.utils.FileUtils;
import ejs.base.utils.TextUtils;

/**
 * This manages generating MD5 sums.
 * @author ejs
 *
 */
public class MD5SumEngine {
	private MD5SumEngine() {
	}

	/**
	 * Create an MD5 of the content with the filter applied.
	 * @param filter
	 * @param content
	 * @return string of hex-encoded MD5
	 */
	public static String createMD5(IMD5SumFilter filter, byte[] content) {
		try {
			return createMD5(filter, new ByteArrayInputStream(content), content.length);
		} catch (IOException e) {
			// shouldn't happen
			return "";
		}
		
	}
	
	/**
	 * Create an MD5 of the input stream's content with the filter applied.
	 * @param filter
	 * @param is stream (closed at end)
	 * @return string of hex-encoded MD5
	 */
	public static String createMD5(IMD5SumFilter filter, InputStream is, int contentLength) throws IOException {
		if (filter == null)
			return "";
			
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}

		List<FilterSegment> segments = new ArrayList<FilterSegment>();
		filter.fillSegments(contentLength, segments);
		try {
			long pos = 0;
			for (FilterSegment segment : segments) {
				long toSkip = segment.offset - pos;
				FileUtils.skipFully(is, toSkip);
				pos += toSkip;
				
				byte[] content = FileUtils.readInputStreamContents(is, segment.length);
				pos += content.length;
				
				digest.update(content);
			}
			return TextUtils.binaryToString(digest.digest());
		} finally {
			is.close();
		}
	}
	
}
