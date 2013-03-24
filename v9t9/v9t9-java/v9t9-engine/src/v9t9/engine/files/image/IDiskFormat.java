/**
 * 
 */
package v9t9.engine.files.image;

import java.util.List;

import v9t9.common.files.IdMarker;
import v9t9.engine.dsr.realdisk.ICRCAlgorithm;

/**
 * @author ejs
 *
 */
public interface IDiskFormat {

	/**
	 * Fetch the ID markers from the given track
	 * @param trackBuffer
	 * @param formatting if true, update CRCs
	 * @return list
	 */
	List<IdMarker> fetchIdMarkers(byte[] trackBuffer, int trackSize, boolean formatting);

	/**
	 * 
	 */
	ICRCAlgorithm getCRCAlgorithm();

	/**
	 * @return
	 */
	boolean doesFormatMatch(byte[] trackBuffer, int trackSize);
}
