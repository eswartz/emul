/*
 * (c) Ed Swartz, 2005
 * 
 * Created on Feb 21, 2006
 *
 */
package v9t9.engine.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class TIFILESFDR extends FDR {

    private byte[] sig;

    private byte[] unused;

    /**
    u8          sig[8];     // '\007TIFILES'
    u16         secsused;   // [big-endian]:  # sectors in file
    u8          flags;      // filetype flags
    u8          recspersec; // # records per sector, 
                               256/reclen for FIXED,
                               255/(reclen+1) for VAR,
                               0 for program 
    u8          byteoffs;   // last byte used in file 
                               (0 = no last empty sector)
    u8          reclen;     // record length, 0 for program
    u16         numrecs;    // [little-endian]:  # records for FIXED file,
                               # sectors for VARIABLE file,
                               0 for program
    u8          unused[112];    // zero 
     */

    public static final byte[] SIGNATURE = { 7, 'T', 'I', 'F', 'I', 'L', 'E', 'S' };
    
    public static FDR readFDR(File file) throws IOException, InvalidFDRException {
        TIFILESFDR fdr = new TIFILESFDR();
        // TODO: translate
        fdr.filename = file.getName();
        fdr.size = 128;
        FileInputStream stream = new FileInputStream(file);
        fdr.sig = new byte[8];
        stream.read(fdr.sig, 0, 8);
        if (!Arrays.equals(fdr.sig, SIGNATURE)) {
			throw new InvalidFDRException();
		}
        fdr.secsused = (short) (stream.read() << 8 | stream.read());
        fdr.flags = (byte) stream.read();
        fdr.recspersec = (byte) stream.read();
        fdr.byteoffs = (byte) stream.read();
        fdr.reclen = (byte) stream.read();
        fdr.numrecs = (short) (stream.read() | stream.read() << 8);
        fdr.unused = new byte[112];
        stream.read(fdr.unused, 0, 112);
        return fdr;
    }

}
