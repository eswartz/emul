
#ifndef __msp430_headers_eprom_h
#define __msp430_headers_eprom_h

/* eprom.h
 *
 * mspgcc project: MSP430 device headers
 * EPROM control registers
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: eprom.h,v 1.1 2002/03/20 01:54:44 data Exp $
 */

/* Switches: none */

#define EPCTL_              0x0054  /* EPROM Control */
sfrb (EPCTL,EPCTL_);
#define EPEXE               0x01
#define EPVPPS              0x02

#endif
