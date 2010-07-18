#if !defined(__msp430x20x2)
#define __msp430x20x2

/* msp430x20x2.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x20x2 family header
 *
 * (c) 2005 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430x20x2.h,v 1.5 2006/11/16 01:19:37 coppice Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT1_R__
#define __MSP430_HAS_PORT2_R__
#define __MSP430_HAS_TA2__
#define __MSP430_HAS_BC2__
#define __MSP430_HAS_FLASH2__
#define __MSP430_HAS_ADC10__
#define __MSP430_HAS_USI__

#include <msp430/basic_clock.h>
#include <msp430/flash.h>
#include <msp430/eprom.h>
#include <msp430/timera.h>
#include <msp430/gpio.h>
#include <msp430/adc10.h>
#include <msp430/usi.h>

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
#define NMIIFG              (1<<4)

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

#define PORT1_VECTOR        4           /* 0xFFE4 Port 1 */
#define PORT2_VECTOR        6           /* 0xFFE6 Port 2 */
#define USI_VECTOR          8           /* 0xFFE8 USI */
#define ADC10_VECTOR        10          /* 0xFFEA ADC10 */
#define TIMERA1_VECTOR      16          /* 0xFFF0 Timer A CC1-2, TA */
#define TIMERA0_VECTOR      18          /* 0xFFF2 Timer A CC0 */
#define WDT_VECTOR          20          /* 0xFFF4 Watchdog Timer */
#define NMI_VECTOR          28          /* 0xFFFC Non-maskable */

#endif /* #ifndef __msp430x20x2 */
