#ifndef __msp430_headers_svs_h
#define __msp430_headers_svs_h

/* svs.h
 *
 * mspgcc project: MSP430 device headers
 * Supply voltage supervisor
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: svs.h,v 1.6 2005/08/17 14:28:46 coppice Exp $
 */

/* Switches: __msp430_has_svs_at_0x55
             __msp430_has_non_variable_svs_threshold */

#if defined(__msp430_has_svs_at_0x55)
#define SVSCTL_             0x0055  /* SVS Control */
#else
#define SVSCTL_             0x0056  /* SVS Control */
#endif
sfrb(SVSCTL,SVSCTL_);
#define SVSFG               0x01
#define SVSOP               0x02
#define SVSON               0x04
#define PORON               0x08
#define VLDOFF              0x00
#define VLDON               0x10
#define VLD_1_8V            0x10

/* Some additional defines to round out the definitions in the same style
   as other peripherals. These are not in TI's header. */
#define VLD0                (1<<4)
#define VLD1                (2<<4)
#define VLD2                (4<<4)
#define VLD3                (8<<4)

#if !defined(__msp430_has_non_variable_svs_threshold)
#define VLD_OFF             (0<<4)
#define VLD_1               (1<<4)
#define VLD_2               (2<<4)
#define VLD_3               (3<<4)
#define VLD_4               (4<<4)
#define VLD_5               (5<<4)
#define VLD_6               (6<<4)
#define VLD_7               (7<<4)
#define VLD_8               (8<<4)
#define VLD_9               (9<<4)
#define VLD_10              (10<<4)
#define VLD_11              (11<<4)
#define VLD_12              (12<<4)
#define VLD_13              (13<<4)
#define VLD_14              (14<<4)
#define VLD_EXT             (15<<4)
#endif

#endif
