/**
 * 
 */
package v9t9.engine.files;


/**
 * @author ejs
 *
 */
public class CatalogEntry {
	static byte drcTrans[][] = new byte[][] { 
		{0, 1}, 
		{FDR.ff_program, 5},
		{FDR.ff_internal, 3}, 
		{(byte) FDR.ff_variable, 2},
		{(byte) (FDR.ff_variable + FDR.ff_internal), 4}
	};


	public final String fileName;
	public final int secs;
	public final String type;
	public final int recordLength;
	public final int typeCode;

	/**
	 * @param fileName
	 * @param sz
	 * @param type
	 * @param recordLength
	 */
	public CatalogEntry(String fileName, int sz, int flags, int recordLength) {
		this.fileName = fileName;
		this.secs = sz;
		
		String ttype = "???";
		if ((flags & IFDRFlags.ff_program) != 0)
			ttype = "PROGRAM";
		else {
			if ((flags & IFDRFlags.ff_internal) != 0)
				ttype = "INT";
			else
				ttype = "DIS";
			if ((flags & IFDRFlags.ff_variable) != 0)
				ttype += "/VAR";
			else
				ttype += "/FIX";
		}
		this.type = ttype;
		
		int idx;
		int code = 0;
		for (idx = 0; idx < drcTrans.length; idx++)
			if (drcTrans[idx][0] ==
				(flags & (FDR.ff_internal | FDR.ff_program | FDR.ff_variable))) {
				code = drcTrans[idx][1];
				break;
			}
		// no match == program
		if (idx >= drcTrans.length) {
			code = 1;
		}
		this.typeCode = code;
		
		this.recordLength = recordLength;
	}

}
