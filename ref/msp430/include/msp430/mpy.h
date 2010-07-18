#ifndef __msp430_headers_mpy_h
#define __msp430_headers_mpy_h

/* mpy.h
 *
 * mspgcc project: MSP430 device headers
 * Hardware multiplier
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: mpy.h,v 1.2 2002/12/28 06:52:37 coppice Exp $
 */

/* Switches: none */

#define MPY_                0x0130  /* Multiply Unsigned/Operand 1 */
sfrw(MPY,MPY_);
#define MPYS_               0x0132  /* Multiply Signed/Operand 1 */
sfrw(MPYS,MPYS_);
#define MAC_                0x0134  /* Multiply Unsigned and Accumulate/Operand 1 */
sfrw(MAC,MAC_);
#define MACS_               0x0136  /* Multiply Signed and Accumulate/Operand 1 */
sfrw(MACS,MACS_);
#define OP2_                0x0138  /* Operand 2 */
sfrw(OP2,OP2_);
#define RESLO_              0x013A  /* Result Low Word */
sfrw(RESLO,RESLO_);
#define RESHI_              0x013C  /* Result High Word */
sfrw(RESHI,RESHI_);
#define SUMEXT_             0x013E  /* Sum Extend */
sfrw(SUMEXT,SUMEXT_);

#endif
