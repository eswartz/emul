#ifndef __msp430_headers_timer8_h
#define __msp430_headers_timer8_h

/* timer8.h
 *
 * mspgcc project: MSP430 device headers
 * 8 bit timer
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: timer8.h,v 1.2 2002/12/28 06:51:02 coppice Exp $
 */

/* Switches: none */

#define TCCTL_              0x0042  /* Timer/Counter Control */
sfrb(TCCTL,TCCTL_);
/* The bit names have been prefixed with "TC" */
#define TCRXD               0x01
#define TCTXD               0x02
#define TCRXACT             0x04
#define TCENCNT             0x08
#define TCTXE               0x10
#define TCISCTL             0x20
#define TCSSEL0             0x40
#define TCSSEL1             0x80
/* Source select of clock input coded with Bits 6-7 */
#define TCSSEL_P01          0                       /* source is signal at pin P0.1 (default) */
#define TCSSEL_ACLK         (TCSSEL0)               /* source is ACLK */
#define TCSSEL_MCLK         (TCSSEL1)               /* source is MCLK */
#define TCSSEL_P01_MCLK     (TCSSEL1|TCSSEL0)       /* source is signal pin P0.1 .AND. MCLK */

#define TCPLD               0x0043
#define TCDAT               0x0044

#endif
