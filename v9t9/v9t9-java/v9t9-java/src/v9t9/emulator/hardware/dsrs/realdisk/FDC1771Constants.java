/**
 * Mar 1, 2011
 */
package v9t9.emulator.hardware.dsrs.realdisk;

/**
 * @author ejs
 *
 */
public interface FDC1771Constants {

	static final int DSKbuffersize = (16384);		/* maximum track size */

	static final int 
		FDC_restore			= 0x00,
		FDC_seek			= 0x10,
			fl_head_load	= 0x08,
			fl_verify_track	= 0x04,	/* match track register with sector ID */
			fl_step_rate	= 0x03,
		
		FDC_step			= 0x20,
		FDC_stepin			= 0x40,
		FDC_stepout			= 0x60,
			fl_update_track	= 0x10, /* for all step commands */	

			// +fl_head_load, fl_verify_track, fl_step_rate

		FDC_readsector		= 0x80,
		FDC_writesector		= 0xA0,
			fl_multiple		= 0x10,
			fl_length_coding= 0x08,	// sector length coding in FDC1771 
			fl_side_number	= 0x08,	/* which side to match */	// FDC179x
			fl_side_compare	= 0x02,
			fl_deleted_dam	= 0x01,

		FDC_readIDmarker= 0xC0,
		FDC_readtrack	= 0xE0,
		FDC_writetrack	= 0xF0,

			fl_15ms_delay	= 0x04,	/* common to readsector...writetrack */

		FDC_interrupt	= 0xD0
	;

}
