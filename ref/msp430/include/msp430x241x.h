#ifndef __msp430x241x
#define __msp430x241x

/* msp430x241x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x14x family header
 *
 * (c) 2007 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430x241x.h,v 1.8 2009/07/26 05:53:27 pjansen Exp $
 *
 * 2008-03-06 - modifications by M Barnes
 * - changed to 241x series, in line with patch by G. Lemm
 * 
 * 2008-02-05 - modifications by G.Lemm <msp430@daqtix.com>
 * - defined __DisableCalData to get rid of assembler errors
 * - cut vector table to 32 byte length
 * - added usci.h include
 * - added __MSP430_HAS_USCI1__ define_
 * - commented out usart.h (didn't take a close look if we still need it)
 *
 * 2008-05-22 cliechti
 * - fix bugs with calibration data
 * - took out __DisableCalData
 */

//~ #define __DisableCalData

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_MPY__
#define __MSP430_HAS_PORT1_R__
#define __MSP430_HAS_PORT2_R__
#define __MSP430_HAS_PORT3_R__
#define __MSP430_HAS_PORT4_R__
#define __MSP430_HAS_PORT5_R__
#define __MSP430_HAS_PORT6_R__
#define __MSP430_HAS_SVS__
#define __MSP430_HAS_USCI__
#define __MSP430_HAS_USCI0__
#define __MSP430_HAS_USCI1__
#define __MSP430_HAS_USCI_AB0__
#define __MSP430_HAS_USCI_AB1__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_TB7__
#define __MSP430_HAS_BC2__
#define __MSP430_HAS_FLASH2__
#define __MSP430_HAS_CAPLUS__
#define __MSP430_HAS_ADC12__


#include <msp430/mpy.h>
#include <msp430/gpio.h>
#include <msp430/svs.h>
#include <msp430/usci.h>
#include <msp430/timera.h>
#include <msp430/timerb.h>
#include <msp430/basic_clock.h>
#include <msp430/flash.h>
#include <msp430/compa.h>
#include <msp430/adc12.h>
#include <msp430/common.h>

#define IE1_                0x0000  /* Interrupt Enable 1 */
sfrb(IE1,IE1_);
#define WDTIE               (1<<0)
#define OFIE                (1<<1)
#define NMIIE               (1<<4)
#define ACCVIE              (1<<5)

#define IFG1_               0x0002  /* Interrupt Flag 1 */
sfrb(IFG1,IFG1_);
#define WDTIFG              (1<<0)
#define OFIFG               (1<<1)
#define PORIFG              (1<<2)
#define RSTIFG              (1<<3)
#define NMIIFG              (1<<4)

#define IE2_                0x0001  /* Interrupt Enable 2 */
sfrb(IE2,IE2_);
#define UC0IE               IE2
#define UCA0RXIE            (1<<0)
#define UCA0TXIE            (1<<1)
#define UCB0RXIE            (1<<2)
#define UCB0TXIE            (1<<3)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define UC0IFG              IFG2
#define UCA0RXIFG           (1<<0)
#define UCA0TXIFG           (1<<1)
#define UCB0RXIFG           (1<<2)
#define UCB0TXIFG           (1<<3)

/************************************************************
* Calibration Data in Info Mem
************************************************************/

/* TLV Calibration Data Structure */
#define TAG_DCO_30             0x01      /* Tag for DCO30  Calibration Data */
#define TAG_ADC12_1            0x10      /* Tag for ADC12_1 Calibration Data */
#define TAG_EMPTY              0xFE      /* Tag for Empty Data Field in Calibration Data */


#ifndef __DisableCalData
#define TLV_CHECKSUM_          0x10C0    /* TLV CHECK SUM */
sfrw(TLV_CHECKSUM, TLV_CHECKSUM_);
#define TLV_DCO_30_TAG_        0x10F6    /* TLV TAG_DCO30 TAG */
sfrb(TLV_DCO_30_TAG, TLV_DCO_30_TAG_);
#define TLV_DCO_30_LEN_        0x10F7    /* TLV TAG_DCO30 LEN */
sfrb(TLV_DCO_30_LEN, TLV_DCO_30_LEN_);
#define TLV_ADC12_1_TAG_       0x10DA    /* TLV ADC12_1 TAG */
sfrb(TLV_ADC12_1_TAG, TLV_ADC12_1_TAG_);
#define TLV_ADC12_1_LEN_       0x10DB    /* TLV ADC12_1 LEN */
sfrb(TLV_ADC12_1_LEN, TLV_ADC12_1_LEN_);
#endif

#define CAL_ADC_25T85          0x0007    /* Index for 2.5V/85Deg Cal. Value */
#define CAL_ADC_25T30          0x0006    /* Index for 2.5V/30Deg Cal. Value */
#define CAL_ADC_25VREF_FACTOR  0x0005    /* Index for 2.5V Ref. Factor */
#define CAL_ADC_15T85          0x0004    /* Index for 1.5V/85Deg Cal. Value */
#define CAL_ADC_15T30          0x0003    /* Index for 1.5V/30Deg Cal. Value */
#define CAL_ADC_15VREF_FACTOR  0x0002    /* Index for ADC 1.5V Ref. Factor */
#define CAL_ADC_OFFSET         0x0001    /* Index for ADC Offset */
#define CAL_ADC_GAIN_FACTOR    0x0000    /* Index for ADC Gain Factor */

#define CAL_DCO_16MHZ          0x0000    /* Index for DCOCTL  Calibration Data for 16MHz */
#define CAL_BC1_16MHZ          0x0001    /* Index for BCSCTL1 Calibration Data for 16MHz */
#define CAL_DCO_12MHZ          0x0002    /* Index for DCOCTL  Calibration Data for 12MHz */
#define CAL_BC1_12MHZ          0x0003    /* Index for BCSCTL1 Calibration Data for 12MHz */
#define CAL_DCO_8MHZ           0x0004    /* Index for DCOCTL  Calibration Data for 8MHz */
#define CAL_BC1_8MHZ           0x0005    /* Index for BCSCTL1 Calibration Data for 8MHz */
#define CAL_DCO_1MHZ           0x0006    /* Index for DCOCTL  Calibration Data for 1MHz */
#define CAL_BC1_1MHZ           0x0007    /* Index for BCSCTL1 Calibration Data for 1MHz */


/************************************************************
* Calibration Data in Info Mem
************************************************************/
#ifndef __DisableCalData
#define CALDCO_16MHZ_         0x10F8    /* DCOCTL  Calibration Data for 16MHz */
sfrb(CALDCO_16MHZ, CALDCO_16MHZ_);
#define CALBC1_16MHZ_         0x10F9    /* BCSCTL1 Calibration Data for 16MHz */
sfrb(CALBC1_16MHZ, CALBC1_16MHZ_);
#define CALDCO_12MHZ_         0x10FA    /* DCOCTL  Calibration Data for 12MHz */
sfrb(CALDCO_12MHZ, CALDCO_12MHZ_);
#define CALBC1_12MHZ_         0x10FB    /* BCSCTL1 Calibration Data for 12MHz */
sfrb(CALBC1_12MHZ, CALBC1_12MHZ_);
#define CALDCO_8MHZ_          0x10FC    /* DCOCTL  Calibration Data for 8MHz */
sfrb(CALDCO_8MHZ, CALDCO_8MHZ_);
#define CALBC1_8MHZ_          0x10FD    /* BCSCTL1 Calibration Data for 8MHz */
sfrb(CALBC1_8MHZ, CALBC1_8MHZ_);
#define CALDCO_1MHZ_          0x10FE    /* DCOCTL  Calibration Data for 1MHz */
sfrb(CALDCO_1MHZ, CALDCO_1MHZ_);
#define CALBC1_1MHZ_          0x10FF    /* BCSCTL1 Calibration Data for 1MHz */
sfrb(CALBC1_1MHZ, CALBC1_1MHZ_);
#endif /* #ifndef __DisableCalData */

#define RESERVED0_VECTOR    0  /* 0xFFC0 Reserved Int. Vector 0 */
#define RESERVED1_VECTOR    2  /* 0xFFC2 Reserved Int. Vector 1 */
#define RESERVED2_VECTOR    4  /* 0xFFC4 Reserved Int. Vector 2 */
#define RESERVED3_VECTOR    6  /* 0xFFC6 Reserved Int. Vector 3 */
#define RESERVED4_VECTOR    8  /* 0xFFC8 Reserved Int. Vector 4 */
#define RESERVED5_VECTOR    10 /* 0xFFCA Reserved Int. Vector 5 */
#define RESERVED6_VECTOR    12 /* 0xFFCC Reserved Int. Vector 6 */
#define RESERVED7_VECTOR    14 /* 0xFFCE Reserved Int. Vector 7 */
#define RESERVED8_VECTOR    16 /* 0xFFD0 Reserved Int. Vector 8 */
#define RESERVED9_VECTOR    18 /* 0xFFD2 Reserved Int. Vector 9 */
#define RESERVED10_VECTOR   20 /* 0xFFD4 Reserved Int. Vector 10 */
#define RESERVED11_VECTOR   22 /* 0xFFD6 Reserved Int. Vector 11 */
#define RESERVED12_VECTOR   24 /* 0xFFD8 Reserved Int. Vector 12 */
#define RESERVED13_VECTOR   26 /* 0xFFDA Reserved Int. Vector 13 */
#define DAC12_VECTOR        28 /* 0xFFDC DAC12 Vector */
#define DMA_VECTOR          30 /* 0xFFDE DMA Vector */
#define USCIAB1TX_VECTOR    32 /* 0xFFE0 USCI A1/B1 Transmit */
#define USCIAB1RX_VECTOR    34 /* 0xFFE2 USCI A1/B1 Receive */
#define PORT1_VECTOR        36 /* 0xFFE4 Port 1 */
#define PORT2_VECTOR        38 /* 0xFFE6 Port 2 */
#define RESERVED20_VECTOR   40 /* 0xFFE8 Reserved Int. Vector 20 */
#define ADC12_VECTOR        42 /* 0xFFEA ADC */
#define USCIAB0TX_VECTOR    44 /* 0xFFEC USCI A0/B0 Transmit */
#define USCIAB0RX_VECTOR    46 /* 0xFFEE USCI A0/B0 Receive */
#define TIMERA1_VECTOR      48 /* 0xFFF0 Timer A CC1-2, TA */
#define TIMERA0_VECTOR      50 /* 0xFFF2 Timer A CC0 */
#define WDT_VECTOR          52 /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  54 /* 0xFFF6 Comparator A */
#define TIMERB1_VECTOR      56 /* 0xFFF8 Timer B CC1-6, TB */
#define TIMERB0_VECTOR      58 /* 0xFFFA Timer B CC0 */
#define NMI_VECTOR          60 /* 0xFFFC Non-maskable */

#define ADC_VECTOR          ADC12_VECTOR

#define BSLSKEY_ 0xFFbE         /* The address is used as bootstrap loader security key */
#define BSLSKEY_DISABLE  0xAA55 /* Disables the BSL completely */
#define BSLSKEY_NO_ERASE 0x0000 /* Disables the erasure of the flash if an invalid password is supplied */

#endif /* #ifndef __msp430x241x */
