/**
 * 
 */
package v9t9.common.speech;

/**
 * @author ejs
 *
 */
public class TMS5220Consts {
	/** talk status */
	final public static int SS_TS	= 0x80;
	/** buffer low */
	final public static int SS_BL	= 0x40;
	/** buffer empty */
	final public static int SS_BE	= 0x20;
	
	/** internal flag */
	final public static int SS_SPEAKING = 1;	

	/** write -> command */
	final public static int GT_WCMD	= 0x1;		
	/** write -> speech external data */
	final public static int GT_WDAT	= 0x2;		
	/** read -> status */
	final public static int GT_RSTAT  = 0x4;	
	/** read -> data */
	final public static int GT_RDAT	= 0x8;		
}
