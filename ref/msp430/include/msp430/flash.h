#ifndef __msp430_headers_flash_h
#define __msp430_headers_flash_h

/* flash.h
 *
 * mspgcc project: MSP430 device headers
 * FLASH control registers
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: flash.h,v 1.5 2005/08/17 14:28:46 coppice Exp $
 */

/* Switches:
__MSP430_HAS_FLASH2__
*/

#define FCTL1_              0x0128  /* FLASH Control 1 */
sfrw (FCTL1,FCTL1_);
#define FCTL2_              0x012A  /* FLASH Control 2 */
sfrw (FCTL2,FCTL2_);
#define FCTL3_              0x012C  /* FLASH Control 3 */
sfrw (FCTL3,FCTL3_);

#define FRKEY               0x9600  /* Flash key returned by read */
#define FWKEY               0xA500  /* Flash key for write */
#define FXKEY               0x3300  /* for use with XOR instruction */

#define ERASE               0x0002  /* Enable bit for flash segment erase */
#define MERAS               0x0004  /* Enable bit for flash mass erase */
#if defined(__MSP430_HAS_FLASH2__)
#define EEI                 0x0008  /* Enable Erase Interrupts */
#define EEIEX               0x0010  /* Enable Emergency Interrupt Exit */
#endif
#define WRT                 0x0040  /* Enable bit for flash write */
#define BLKWRT              0x0080  /* Enable bit for flash segment write */
#define SEGWRT              0x0080  /* old definition */ /* Enable bit for Flash segment write */

#define FN0                 0x0001  /* Divide flash clock by: 2^0 */
#define FN1                 0x0002  /* Divide flash clock by: 2^1 */
#define FN2                 0x0004  /* Divide flash clock by: 2^2 */
#define FN3                 0x0008  /* Divide flash clock by: 2^3 */
#define FN4                 0x0010  /* Divide flash clock by: 2^4 */
#define FN5                 0x0020  /* Divide flash clock by: 2^5 */
#define FSSEL0              0x0040  /* Flash clock select 0 */        /* to distinguish from UART SSELx */
#define FSSEL1              0x0080  /* Flash clock select 1 */

#define FSSEL_0             0x0000  /* Flash clock select: 0 - ACLK */
#define FSSEL_1             0x0040  /* Flash clock select: 1 - MCLK */
#define FSSEL_2             0x0080  /* Flash clock select: 2 - SMCLK */
#define FSSEL_3             0x00C0  /* Flash clock select: 3 - SMCLK */

#define BUSY                0x0001  /* Flash busy: 1 */
#define KEYV                0x0002  /* Flash Key violation flag */
#define ACCVIFG             0x0004  /* Flash Access violation flag */
#define WAIT                0x0008  /* Wait flag for segment write */
#define LOCK                0x0010  /* Lock bit: 1 - Flash is locked (read only) */
#define EMEX                0x0020  /* Flash Emergency Exit */
#if defined(__MSP430_HAS_FLASH2__)
#define LOCKA               0x0040  /* Segment A Lock bit: read = 1 - Segment is locked (read only) */
#define FAIL                0x0080  /* Last Program or Erase failed */
#endif

/* Aliases by mspgcc */
#define FSSEL_ACLK          FSSEL_0
#define FSSEL_MCLK          FSSEL_1
#define FSSEL_SMCLK         FSSEL_2
/*#define FSSEL_SMCLK         FSSEL_3*/

#endif
