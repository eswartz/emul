#ifndef __msp430_headers_dac12_h
#define __msp430_headers_dac12_h

/* dac12.h
 *
 * mspgcc project: MSP430 device headers
 * DAC12 module header
 *
 * (c) 2002 by Steve Udnerwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: dac12.h,v 1.5 2004/11/03 14:29:10 coppice Exp $
 */

/* Switches: __msp430_have_dac12_op_amp */

#define DAC12_0CTL_         0x01C0  /* DAC12 control 0 */
sfrw(DAC12_0CTL,DAC12_0CTL_);
#define DAC12_1CTL_         0x01C2  /* DAC12 control 1 */
sfrw(DAC12_1CTL,DAC12_1CTL_);
#define DAC12_0DAT_         0x01C8  /* DAC12 data 0 */
sfrw(DAC12_0DAT,DAC12_0DAT_);
#define DAC12_1DAT_         0x01CA  /* DAC12 data 1 */
sfrw(DAC12_1DAT,DAC12_1DAT_);

#define DAC12GRP            0x0001  /* DAC12 group */
#define DAC12ENC            0x0002  /* DAC12 enable conversion */
#define DAC12IFG            0x0004  /* DAC12 interrupt flag */
#define DAC12IE             0x0008  /* DAC12 interrupt enable */
#define DAC12DF             0x0010  /* DAC12 data format */
#define DAC12AMP0           0x0020  /* DAC12 amplifier bit 0 */
#define DAC12AMP1           0x0040  /* DAC12 amplifier bit 1 */
#define DAC12AMP2           0x0080  /* DAC12 amplifier bit 2 */
#define DAC12IR             0x0100  /* DAC12 input reference and output range */
#define DAC12CALON          0x0200  /* DAC12 calibration */
#define DAC12LSEL0          0x0400  /* DAC12 load select bit 0 */
#define DAC12LSEL1          0x0800  /* DAC12 load select bit 1 */
#define DAC12RES            0x1000  /* DAC12 resolution */
#define DAC12SREF0          0x2000  /* DAC12 reference bit 0 */
#define DAC12SREF1          0x4000  /* DAC12 reference bit 1 */
#if defined(__msp430_have_dac12_op_amp)
#define DAC12OPS            0x8000  /* DAC12 Operation Amp. */
#endif

#define DAC12AMP_0          (0<<5)  /* DAC12 amplifier 0: off,    3-state */
#define DAC12AMP_1          (1<<5)  /* DAC12 amplifier 1: off,    off */
#define DAC12AMP_2          (2<<5)  /* DAC12 amplifier 2: low,    low */
#define DAC12AMP_3          (3<<5)  /* DAC12 amplifier 3: low,    medium */
#define DAC12AMP_4          (4<<5)  /* DAC12 amplifier 4: low,    high */
#define DAC12AMP_5          (5<<5)  /* DAC12 amplifier 5: medium, medium */
#define DAC12AMP_6          (6<<5)  /* DAC12 amplifier 6: medium, high */
#define DAC12AMP_7          (7<<5)  /* DAC12 amplifier 7: high,   high */

#define DAC12LSEL_0         (0<<10) /* DAC12 load select 0: direct */
#define DAC12LSEL_1         (1<<10) /* DAC12 load select 1: latched with DAT */
#define DAC12LSEL_2         (2<<10) /* DAC12 load select 2: latched with pos. Timer_A3.OUT1 */
#define DAC12LSEL_3         (3<<10) /* DAC12 load select 3: latched with pos. Timer_B7.OUT1 */

#define DAC12SREF_0         (0<<13) /* DAC12 reference 0: Vref+ */
#define DAC12SREF_1         (1<<13) /* DAC12 reference 1: Vref+ */
#define DAC12SREF_2         (2<<13) /* DAC12 reference 2: Veref+ */
#define DAC12SREF_3         (3<<13) /* DAC12 reference 3: Veref+ */

#endif
