/**
 * 
 */
package v9t9.emulator.hardware.dsrs.realdisk;

/**
 * @author ejs
 *
 */
public class RealDiskConsts {

	public static final int DSKbuffersize = (16384);		/* maximum track size */
	public static final int 
	FDC_restore			= 0x00;
	public static final int 
	FDC_seek			= 0x10;
	public static final int 
	fl_head_load	= 0x08;
	public static final int 
	fl_verify_track	= 0x04;	/* match track register with sector ID */
	public static final int 
	fl_step_rate	= 0x03;
	public static final int 
	FDC_step			= 0x20;
	public static final int 
	FDC_stepin			= 0x40;
	public static final int 
	FDC_stepout			= 0x60;
	public static final int 
	fl_update_track	= 0x10; /* for all step commands */
	// +fl_head_load, fl_verify_track, fl_step_rate
	public static final int 
		FDC_readsector		= 0x80;
	public static final int 
	FDC_writesector		= 0xA0;
	public static final int 
	fl_multiple		= 0x10;
	public static final int 
	fl_length_coding= 0x08;	// sector length coding in FDC1771
	public static final int 
	fl_side_number	= 0x08;	/* which side to match */	// FDC179x
	public static final int 
	fl_side_compare	= 0x02;
	public static final int 
	fl_deleted_dam	= 0x01;
	public static final int 
	FDC_readIDmarker= 0xC0;
	public static final int 
	FDC_readtrack	= 0xE0;
	public static final int 
	FDC_writetrack	= 0xF0;
	public static final int 
	fl_15ms_delay	= 0x04;	/* common to readsector...writetrack */
	public static final int 
		FDC_interrupt	= 0xD0
	;

}
