#ifndef __msp430_headers_slopeadc_h
#define __msp430_headers_slopeadc_h

/* slopeadc.h
 *
 * mspgcc project: MSP430 device headers
 * Slope ADC header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: slopeadc.h,v 1.2 2002/12/28 06:52:18 coppice Exp $
 */

/* Switches: none */

#define AIN_                0x0110  /* ADC Input */
sfrw(AIN,AIN_);
#define AEN_                0x0112  /* ADC Input Enable */
sfrw(AEN,AEN_);

#define ACTL_               0x0114  /* ADC Control */
sfrw(ACTL,ACTL_);
/* the names of the mode bits are different from the spec */
#define ADSOC               0x0001
#define ADSVCC              0x0002
#define ADIN0               0x0004
#define ADIN1               0x0008
#define ADIN2               0x0010
#define ADINOFF             0x0020
#define ADCSRC0             0x0040
#define ADCSRC1             0x0080
#define ADCSRCOFF           0x0100
#define ADRNG0              0x0200
#define ADRNG1              0x0400
#define ADAUTO              0x0800
#define ADPD                0x1000
/* Channel select coded with Bits 2-5 */
#define ADIN_A0             0                       /* (default) */
#define ADIN_A1             (ADIN0)
#define ADIN_A2             (ADIN1)
#define ADIN_A3             (ADIN1|ADIN0)
#define ADIN_A4             (ADIN2)
#define ADIN_A5             (ADIN2|ADIN0)
#define ADIN_A6             (ADIN2|ADIN1)
#define ADIN_A7             (ADIN2|ADIN1|ADIN0)
/* Current source output select coded with Bits 6-8 */
#define ADCSRC_A0           0                       /* (default) */
#define ADCSRC_A1           (ADCSRC0)
#define ADCSRC_A2           (ADCSRC1)
#define ADCSRC_A3           (ADCSRC1|ADCSRC0)
/* Range select coded with Bits 9-11 */
#define ADRNG_A             0                       /* 0<=Vin<1/4Vref  (default) */
#define ADRNG_B             (ADRNG0)                /* 1/4 Vref<=Vin<1/2 Vref */
#define ADRNG_C             (ADRNG1)                /* 1/2 Vref<=Vin<3/4 Vref */
#define ADRNG_D             (ADRNG1|ADRNG0)         /* 3/4 Vref<=Vin<1   Vref */
#define ADRNG_AUTO          (ADAUTO)                /* 0<=Vin<1   Vref auto detect range */

/* DATA REGISTER ADDRESS */
#define ADAT_               0x0118  /* ADC Data */
sfrw(ADAT,ADAT_);

#endif
