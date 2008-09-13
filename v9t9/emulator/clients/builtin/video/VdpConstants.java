/**
 * 
 */
package v9t9.emulator.clients.builtin.video;

/**
 * @author ejs
 *
 */
public class VdpConstants {

	final static int VDP_INTERRUPT = 0x80;
	final static int VDP_COINC = 0x40;
	final static int VDP_FIVE_SPRITES = 0x20;
	final static int VDP_FIFTH_SPRITE = 0x1f;
	final static int R0_BITMAP = 2;
	final static int R0_EXTERNAL = 1;
	final static int R1_RAMSIZE = 128;
	final static int R1_NOBLANK = 64;
	final static int R1_INT = 32;
	final static int R1_TEXT = 16;
	final static int R1_MULTI = 8;
	final static int R1_SPR4 = 2;
	final static int R1_SPRMAG = 1;

}
