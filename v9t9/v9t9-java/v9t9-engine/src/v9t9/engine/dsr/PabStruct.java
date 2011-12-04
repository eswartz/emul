/**
 * 
 */
package v9t9.engine.dsr;

import v9t9.common.dsr.IMemoryTransfer;
import v9t9.common.files.FDR;
import v9t9.common.files.V9t9FDR;

import static v9t9.engine.dsr.PabConstants.*;

public class PabStruct 
{
	/** 0 file operation (f_xxx) */
	public int	opcode;		
	/** 1 file access code (fp_xxx) + open mode (m_xxx) [IN]
	<br> error code [OUT] */
	public int	pflags;		
	/** 2 VDP record address */
	public int	bufaddr;	
	/** 4 file record length */
	public int	preclen;	
	/** 5 characters used in record */
	public int	charcount;	
	/** 6 current record (seek position), or byte count for load/save */
	public int	recnum;	
	/** 8 screen offset (for CSx) */
	public int	scrnoffs;	
	/** 9 length of filename following */
	public int	namelen;	
	/** device.filename */
	public String	path;
	public short pabaddr;		
	
	/**
	 * Read the pab contents from memory.
	 * @throws DsrException
	 */
	public void fetch(IMemoryTransfer xfer, short pabaddr) {
		this.pabaddr = pabaddr;
		
		this.opcode = xfer.readVdpByte(pabaddr) & 0xff;
		this.pflags = xfer.readVdpByte(pabaddr + 1) & 0xff;
		this.bufaddr = xfer.readVdpShort(pabaddr + 2) & 0xffff;
		this.preclen = xfer.readVdpByte(pabaddr + 4) & 0xff;
		this.charcount = xfer.readVdpByte(pabaddr + 5) & 0xff;
		this.recnum = xfer.readVdpShort(pabaddr + 6) & 0xffff;
		this.scrnoffs = xfer.readVdpByte(pabaddr + 8) & 0xff;
		this.namelen = xfer.readVdpByte(pabaddr + 9) & 0xff;
	}
	

	/**
	 * 	Copy PAB from file struct into VDP after a file operation
	 */
	public void store(IMemoryTransfer xfer) {
		xfer.writeVdpByte(pabaddr, (byte) this.opcode);
		xfer.writeVdpByte(pabaddr + 1, (byte) this.pflags);
		xfer.writeVdpByte(pabaddr + 2, (byte) (this.bufaddr >> 8));
		xfer.writeVdpByte(pabaddr + 3, (byte) (this.bufaddr & 0xff));
		xfer.writeVdpByte(pabaddr + 4, (byte) this.preclen);
		xfer.writeVdpByte(pabaddr + 5, (byte) this.charcount);
		xfer.writeVdpByte(pabaddr + 6, (byte) (this.recnum >> 8));
		xfer.writeVdpByte(pabaddr + 7, (byte) (this.recnum & 0xff));
		xfer.writeVdpByte(pabaddr + 8, (byte) this.scrnoffs);
		xfer.writeVdpByte(pabaddr + 9, (byte) this.namelen);
	}


	public int getAccess() {
		 return pflags & fp_access_mask;
	}
	
	public int getOpenMode() {
		return pflags & m_mode_mask;
	}
	
	public boolean isOverwriting() {
		return getOpenMode() == m_output;
	}
	
	public boolean canCreate() {
		int mode = getOpenMode();
		return mode == m_append || mode == m_output || mode == m_update; 
	}


	/**
	 * Compare the FDR with the PAB, making sure the PAB operation is compatile
	 * with the FDR, and that record sizes and file types match.
	 */
	public void checkCompatibleFDR(FDR fdr) throws DsrException {
		int fflags;

		if (fdr instanceof V9t9FDR) {
			int index = path.lastIndexOf('.');
			String fdrFileName = ((V9t9FDR) fdr).getFileName();
			String pabFileName = path.substring(index + 1);
			if (fdrFileName.equals(pabFileName)) {
				System.err.println("Filename in FDR does not match filename: " + pabFileName + " vs. " + fdrFileName);
			}
		}
		
		fflags = fdr.getFlags();

		/* program files are easy */
		if (opcode == op_load || opcode == op_save)
			if ((fflags & FDR.ff_program) != 0)
				return;

		/* both must be fixed or variable */
		if (((pflags & fp_variable) != 0) == ((fflags & FDR.ff_variable) != 0)) {
			/*  fixup the PAB if it doesn't know record size */
			if (preclen == 0)
				preclen = fdr.getRecordLength();

			/* both must have same record length */
			if (preclen != fdr.getRecordLength())
				throw new DsrException(PabConstants.e_badopenmode, "record length differs: " + preclen + " vs " + fdr.getRecordLength());

			/* and no "var,relative" files */
			if ((pflags & (fp_relative | fp_variable)) == (fp_relative | fp_variable))
				throw new DsrException(PabConstants.e_badopenmode, "var vs relative file");
		} else {
			throw new DsrException(PabConstants.e_badopenmode, "DSKcomparefdrandpab: fixed/variable flag differs");
		}
	}


	/**
	 * @return
	 */
	public boolean isDisVar() {
		return isVariable() && (pflags & PabConstants.fp_internal) == 0;
	}


	/**
	 * @return
	 */
	public boolean isVariable() {
		return (pflags & PabConstants.fp_variable) != 0;
	}


	/**
	 * @return
	 */
	public boolean isReading() {
		int mode = (pflags & PabConstants.m_mode_mask);
		return mode != PabConstants.m_output && mode != PabConstants.m_append;
	}


	/**
	 * @return
	 */
	public boolean isRelative() {
		return (pflags & PabConstants.fp_relative + PabConstants.fp_variable) == PabConstants.fp_relative;
	}


	/**
	 * @return
	 */
	public boolean isWriting() {
		return (pflags & PabConstants.m_mode_mask) != PabConstants.m_input;
	}
}