#ifndef __msp430_headers_opamp_h
#define __msp430_headers_opamp_h

/* opamp.h
 *
 * mspgcc project: MSP430 device headers
 * Operational amplifier module header
 *
 * (c) 2003 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: opamp.h,v 1.6 2007/09/18 12:37:02 coppice Exp $
 */

/* Switches:
__msp430_have_opamp_1
__msp430_have_opamp_2
__msp430_have_opamp_feedback_taps
__msp430_have_opamp_switches
__msp430_have_opamp_output_select
__msp430_have_opamp_rail_to_rail
__msp430_have_opamp_offset_cal
*/

#define OA0CTL0_            0x00C0  /* OA0 Control register 0 */
sfrb(OA0CTL0, OA0CTL0_);
#define OA0CTL1_            0x00C1  /* OA0 Control register 1 */
sfrb(OA0CTL1, OA0CTL1_);

#if defined(__msp430_have_opamp_1)
#define OA1CTL0_            0x00C2  /* OA1 Control register 0 */
sfrb(OA1CTL0, OA1CTL0_);
#define OA1CTL1_            0x00C3  /* OA1 Control register 1 */
sfrb(OA1CTL1, OA1CTL1_);
#endif

#if defined(__msp430_have_opamp_2)
#define OA2CTL0_            0x00C4  /* OA2 Control register 0 */
sfrb(OA2CTL0, OA2CTL0_);
#define OA2CTL1_            0x00C5  /* OA2 Control register 1 */
sfrb(OA2CTL1, OA2CTL1_);
#endif

#if defined(__msp430_have_opamp_switches)
#define SWCTL_              0x00CF  /* OA  Analog Switches Control Register */
sfrb(SWCTL, SWCTL_)
#endif

#if defined(__msp430_have_opamp_output_select)
#define OAADC0              0x01      /* OAx output to ADC12 input channel select 0 */
#define OAADC1              0x02      /* OAx output to ADC12 input channel select 1 */
#endif
#define OAPM0               0x04      /* OAx Power mode select 0 */
#define OAPM1               0x08      /* OAx Power mode select 1 */
#define OAP0                0x10      /* OAx Inverting input select 0 */
#define OAP1                0x20      /* OAx Inverting input select 1 */
#define OAN0                0x40      /* OAx Non-inverting input select 0 */
#define OAN1                0x80      /* OAx Non-inverting input select 1 */

#define OAPM_0              (0<<2)    /* OAx Power mode select: off */
#define OAPM_1              (1<<2)    /* OAx Power mode select: slow */
#define OAPM_2              (2<<2)    /* OAx Power mode select: medium */
#define OAPM_3              (3<<2)    /* OAx Power mode select: fast */
#define OAP_0               (0<<4)    /* OAx Inverting input select 00 */
#define OAP_1               (1<<4)    /* OAx Inverting input select 01 */
#define OAP_2               (2<<4)    /* OAx Inverting input select 10 */
#define OAP_3               (3<<4)    /* OAx Inverting input select 11 */
#define OAN_0               (0<<6)    /* OAx Non-inverting input select 00 */
#define OAN_1               (1<<6)    /* OAx Non-inverting input select 01 */
#define OAN_2               (2<<6)    /* OAx Non-inverting input select 10 */
#define OAN_3               (3<<6)    /* OAx Non-inverting input select 11 */

#if defined(__msp430_have_opamp_rail_to_rail)
#define OARRIP              0x01      /* OAx Rail-to-Rail Input off */
#endif
#if defined(__msp430_have_opamp_offset_cal)
#define OACAL               0x02      /* OAx Offset Calibration */
#endif
#define OAFC0               0x04      /* OAx Function control 0 */
#define OAFC1               0x08      /* OAx Function control 1 */
#define OAFC2               0x10      /* OAx Function control 2 */
#if defined(__msp430_have_opamp_feedback_taps)
#define OAFBR0              0x20      /* OAx Feedback resistor select 0 */
#define OAFBR1              0x40      /* OAx Feedback resistor select 1 */
#define OAFBR2              0x80      /* OAx Feedback resistor select 2 */
#endif

#define OAFC_0              (0<<2)    /* OAx Function: Gen. Purpose */
#define OAFC_1              (1<<2)    /* OAx Function: Comparing */
#define OAFC_2              (2<<2)    /* OAx Function: Reserved */
#define OAFC_3              (3<<2)    /* OAx Function: Differential */
#define OAFC_4              (4<<2)    /* OAx Function: Non-Inverting, PGA */
#define OAFC_5              (5<<2)    /* OAx Function: Reserved */
#define OAFC_6              (6<<2)    /* OAx Function: Inverting */
#define OAFC_7              (7<<2)    /* OAx Function: Differential */

#if defined(__msp430_have_opamp_feedback_taps)
#define OAFBR_0             (0<<5)    /* OAx Feedback resistor: Tap 0 */
#define OAFBR_1             (1<<5)    /* OAx Feedback resistor: Tap 1 */
#define OAFBR_2             (2<<5)    /* OAx Feedback resistor: Tap 2 */
#define OAFBR_3             (3<<5)    /* OAx Feedback resistor: Tap 3 */
#define OAFBR_4             (4<<5)    /* OAx Feedback resistor: Tap 4 */
#define OAFBR_5             (5<<5)    /* OAx Feedback resistor: Tap 5 */
#define OAFBR_6             (6<<5)    /* OAx Feedback resistor: Tap 6 */
#define OAFBR_7             (7<<5)    /* OAx Feedback resistor: Tap 7 */
#endif

#if defined(__msp430_have_opamp_switches)
#define SWCTL0              0x01      /* OA  Analog Switch Control 0 */
#define SWCTL1              0x02      /* OA  Analog Switch Control 1 */
#define SWCTL2              0x04      /* OA  Analog Switch Control 2 */
#define SWCTL3              0x08      /* OA  Analog Switch Control 3 */
#define SWCTL4              0x10      /* OA  Analog Switch Control 4 */
#define SWCTL5              0x20      /* OA  Analog Switch Control 5 */
#define SWCTL6              0x40      /* OA  Analog Switch Control 6 */
#define SWCTL7              0x80      /* OA  Analog Switch Control 7 */
#endif

#endif
