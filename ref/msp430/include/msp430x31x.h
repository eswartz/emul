#ifndef __msp430x31x
#define __msp430x31x

/* msp430x31x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x31x family header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430x31x.h,v 1.7 2005/08/17 14:28:46 coppice Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT0__
#define __MSP430_HAS_LCD__
#define __MSP430_HAS_BT__
#define __MSP430_HAS_FLL__
#define __MSP430_HAS_8BTC__
#define __MSP430_HAS_TP__
#define __MSP430_HAS_EPROM__

#define LCD_BASE 0x30
#define __msp430_have_lcdlowr

#include <msp430/gpio.h>
#include <msp430/lcd.h>
#include <msp430/basic_timer.h>
#include <msp430/eprom.h>
#include <msp430/timer8.h>
#include <msp430/timerport.h>
#include <msp430/system_clock.h>

#include <msp430/common.h>

#define IE1_                0x0000  /* Interrupt Enable 1 */
sfrb(IE1,IE1_);
#define WDTIE               (1<<0)
#define OFIE                (1<<1)
#define P0IE_0              (1<<2)
#define P0IE_1              (1<<3)

#define IFG1_               0x0002  /* Interrupt Flag 1 */
sfrb(IFG1,IFG1_);
#define WDTIFG              (1<<0)
#define OFIFG               (1<<1)
#define P0IFG_0             (1<<2)
#define P0IFG_1             (1<<3)
#define NMIIFG              (1<<4)

#define IE2_                0x0001  /* Interrupt Enable 2 */
sfrb(IE2,IE2_);
#define TPIE                (1<<2)
#define BTIE                (1<<7)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define BTIFG               (1<<7)

#define PORT0_VECTOR        0	    /* 0xFFE0 Port 0 Bits 2-7 [Lowest Priority] */
#define BASICTIMER_VECTOR   2	    /* 0xFFE2 Basic Timer */
#define TIMERPORT_VECTOR    10	    /* 0xFFE8 Timer/Port */   
#define WDT_VECTOR          20	    /* 0xFFF4 Watchdog Timer */
#define IO1_VECTOR          24	    /* 0xFFF8 Dedicated IO (P0.1) */
#define IO0_VECTOR          26	    /* 0xFFFA Dedicated IO (P0.0) */
#define NMI_VECTOR          28	    /* 0xFFFC Non-maskable */

#endif /* #ifndef __msp430x31x */
