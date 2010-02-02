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

public class V9t9FDR extends FDR {

    public static final int FDRSIZE = 128;
    
    protected byte[] filenam;
    private byte[] res10;
    private byte[] dcpb;
    private byte[] rec20;

    /**
    char        filenam[10];// filename, padded with spaces
    u8          res10[2];   // reserved 
    u8          flags;      // filetype flags 
    u8          recspersec; // # records per sector, 
                               256/reclen for FIXED,
                               255/(reclen+1) for VAR,
                               0 for program 
    u16         secsused;   // [big-endian]:  # sectors in file 
    u8          byteoffs;   // last byte used in file 
                                   (0 = no last empty sector) 
    u8          reclen;     // record length, 0 for program 
    u16         numrecs;    // [little-endian]:  # records for FIXED file,
                               # sectors for VARIABLE file,
                               0 for program 
    u8          rec20[8];   // reserved 
    u8          dcpb[100];  // sector layout of file, ignored for v9t9 
     */

    public static FDR readFDR(File file) throws IOException, InvalidFDRException {
        V9t9FDR fdr = new V9t9FDR();
        fdr.size = FDRSIZE;
        FileInputStream stream = new FileInputStream(file);

        fdr.filenam = new byte[10];
        stream.read(fdr.filenam, 0, 10);
        
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < fdr.filenam.length; i++) {
            if (i == ' ') {
				break;
			}
            buffer.append((char)fdr.filenam[i]);
        }
        fdr.filename = buffer.toString();
        
        fdr.res10 = new byte[2];
        stream.read(fdr.res10, 0, 2);
        fdr.flags = (byte) stream.read();
        fdr.recspersec = (byte) stream.read();
        fdr.secsused = (short) (stream.read() << 8 | stream.read());
        fdr.byteoffs = (byte) stream.read();
        fdr.reclen = (byte) stream.read();
        fdr.numrecs = (short) (stream.read() | stream.read() << 8);
        fdr.rec20 = new byte[8];
        stream.read(fdr.rec20, 0, 8);
        fdr.dcpb = new byte[100];
        stream.read(fdr.dcpb, 0, 100);
        
        fdr.validate(file);
        
        return fdr;
    }

    private void validate(File file) throws InvalidFDRException {
        // check for invalid filetype flags
        if ((flags & ~FF_VALID_FLAGS) != 0) {
            throw new InvalidFDRException();
                    /*"FIAD server:  invalid flags %02x "
                            "for file '%s'\n"),
                            flags,
                            filename);*/
        }

        long filesize = file.length();
        
        // check for invalid file size:
        // do not allow file to be more than one sector larger than FDR says,
        // but allow it to be up to 64 sectors smaller:  
        // this is a concession for files copied with "direct output to file", 
        // which must write FDR changes before writing data.
        if (secsused < (filesize - FDRSIZE) / 256 - 1
                 || secsused > (filesize - FDRSIZE) / 256 + 64) {
                throw new InvalidFDRException();
                        /*_("FIAD server:  invalid number of sectors %d "
                            "for data size %d in file '%s'\n"),
                            TI2HOST(secsused), 
                            filesize - FDRSIZE,
                            filename);*/
        }

        // fixed files have 256/reclen records per sector
        if ((flags & ff_program) == 0
            && (flags & ff_variable) == 0) {
            if (reclen == 0 ||
                256 / reclen != recspersec) 
            {
                /*fiad_logger(_L | LOG_ERROR, _("FIAD server:  record length %d / records per sector %d invalid\n"
                            "for FIXED file '%s'\n"),
                            reclen,
                            recspersec,
                            filename);*/
                throw new InvalidFDRException();
            }
        }
        
        // variable files have 255/(reclen+1) records per sector
        if ((flags & ff_program) == 0) {
            if (reclen == 0 || 
                255 / (reclen + 1) != recspersec 
                 // known problem that older v9t9s used this calculation
                && 256 / reclen != recspersec)
            {
                /*fiad_logger(_L | LOG_ERROR, _("FIAD server:  record length %d / records per sector %d invalid\n"
                            "for VARIABLE file '%s'\n"),
                            reclen,
                            recspersec,
                            filename);*/
                throw new InvalidFDRException();
            }
        }

        // program files have 0
        if (reclen != 0 && recspersec != 0) {
            /*fiad_logger(_LL | LOG_ERROR, _("FIAD server:  record length %d / records per sector %d invalid\n"
                        "for PROGRAM file '%s'\n"),
                        reclen,
                        recspersec,
                        filename);
            return false;*/
            throw new InvalidFDRException();
        }

    }

}
