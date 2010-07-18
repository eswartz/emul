#if !defined(__msp430xW42x)
#define __msp430xW42x

/* msp430xW42x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430xW42x family header
 *
 * (c) 2003 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430xW42x.h,v 1.6 2006/11/15 14:34:57 coppice Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT1__
#define __MSP430_HAS_PORT2__
#define __MSP430_HAS_PORT3__
#define __MSP430_HAS_PORT4__
#define __MSP430_HAS_PORT5__
#define __MSP430_HAS_PORT6__
#define __MSP430_HAS_BT__
#define __MSP430_HAS_FLLPLUS_SMALL__
#define __MSP430_HAS_SVS__
#define __MSP430_HAS_LCD4__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_T1A5__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_COMPA__
#define __MSP430_HAS_SCANIF__

#define __msp430_have_lcd_16_20
#define LCD_BASE 0x90

#include <msp430/basic_timer.h>
#include <msp430/system_clock.h>
#include <msp430/svs.h>
#include <msp430/lcd.h>
#include <msp430/flash.h>
#include <msp430/timera.h>
#include <msp430/gpio.h>
#include <msp430/scanif.h>
#include <msp430/compa.h>

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

#define IE2_                0x0001  /* Interrupt Enable 2 */
sfrb(IE2,IE2_);
#define BTIE                (1<<7)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define BTIFG               (1<<7)

#define BASICTIMER_VECTOR   0       /* 0xFFE0 Basic Timer */
#define PORT2_VECTOR        2       /* 0xFFE2 Port 2 */
#define PORT1_VECTOR        8       /* 0xFFE8 Port 1 */
#define TIMER0_A1_VECTOR    10      /* 0xFFEA Timer0_A CC1-2, TA */
#define TIMER0_A0_VECTOR    12      /* 0xFFEC Timer0_A CC0 */
#define SCANIF_VECTOR       18      /* 0xFFF2 Scan Inteface */
#define WDT_VECTOR          20      /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  22      /* 0xFFF6 Comparator A */
#define TIMER1_A1_VECTOR    24      /* 0xFFF8 Timer1_A CC1-4, TA1 */
#define TIMER1_A0_VECTOR    26      /* 0xFFFA Timer1_A CC0 */
#define NMI_VECTOR          28      /* 0xFFFC Non-maskable */ 

/* Alternate Names */
#define TIMERA1_VECTOR      TIMER0_A1_VECTOR
#define TIMERA0_VECTOR      TIMER0_A0_VECTOR

#endif /* #ifndef __msp430xW42x */
