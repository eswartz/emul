#if !defined(__msp430x54xx)
#define __msp430x54xx

/* msp430x54xx.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x54xx family header
 *
 * (c) 2006 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * 2008-10-08 - sb-sf (sb-sf@users.sf.net)
 * - created, based on msp430xG461x.h
 *
 * $Id: msp430x54xx.h,v 1.5 2009/06/04 21:55:18 cliechti Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_T0A5__
#define __MSP430_HAS_T1A3__
#define __MSP430_HAS_UCS__
#define __MSP430_HAS_USCI0_5__
#define __MSP430_HAS_USCI1_5__
#define __MSP430_HAS_USCI2_5__
#define __MSP430_HAS_USCI2_5__
#define __MSP430_SYS_BASE__     0x180
#define __MSP430_WDT_A_BASE__   0x150
#define __MSP430_PORT1_BASE__   0x200
#define __MSP430_PORT2_BASE__   0x200
#define __MSP430_PORT3_BASE__   0x220
#define __MSP430_PORT4_BASE__   0x220
#define __MSP430_PORT5_BASE__   0x240
#define __MSP430_PORT6_BASE__   0x240
#define __MSP430_PORT7_BASE__   0x260
#define __MSP430_PORT8_BASE__   0x260
#define __MSP430_PORT9_BASE__   0x280
#define __MSP430_PORT10_BASE__  0x280
#define __MSP430_PORT11_BASE__  0x2A0
#define __MSP430_PORTJ_BASE__   0x320

#define __MSP430_MPY32_BASE__   0x4C0


#include <msp430/wdt_a.h>
#include <msp430/sys.h>
#include <msp430/gpio_5xxx.h>
#include <msp430/mpy32.h>
#include <msp430/timera.h>
#include <msp430/unified_clock_system.h>
#include <msp430/usci.h>

/*

  Empty yet :(

*/
#define GIE                 0x0008

#define SFRIE1_             0x0100  /* Interrupt Enable 1 */
#define SFRIE1_L_           SFRIE1_
#define SFRIE1_H_           SFRIE1_ + 0x01
sfrw(SFRIE1, SFRIE1_);
sfrb(SFRIE1_L, SFRIE1_L_);
sfrb(IE1, SFRIE1_L_);
sfrb(SFRIE1_H, SFRIE1_H_);
sfrb(IE2, SFRIE1_H_);
#define WDTIE               (1<<0)
#define OFIE                (1<<1)
/* RESERVED                 (1<<2)*/
#define VMAIE               (1<<3)
#define NMIIE               (1<<4)
#define ACCVIE              (1<<5)
#define JMBINIE             (1<<6)
#define JMBOUTIE            (1<<7)

#define SFRIFG1_            0x0102  /* Interrupt Flag 1 */
#define SFRIFG1_L_          SFRIFG1_
#define SFRIFG1_H_          SFRIFG1_ + 0x01
sfrw(SFRIFG1, SFRIFG1_);
sfrb(SFRIFG1_L, SFRIFG1_L_);
sfrb(IFG1, SFRIFG1_L_);
sfrb(SFRIFG1_H, SFRIFG1_H_);
sfrb(IFG2, SFRIFG1_H_);
#define WDTIFG              (1<<0)
#define OFIFG               (1<<1)
/* RESERVED                 (1<<2)*/
#define VMAIFG              (1<<3)
#define NMIIFG              (1<<4)
/* RESERVED                 (1<<5)*/
#define JMBINIFG            (1<<6)
#define JMBOUTIFG           (1<<7)

#define SFRRPCR_            0x0104  /* Reset pin control */
sfrw(SFRRPCR, SFRRPCR_);
#define SFRRPCR_L_          SFRRPCR_
#define SFRRPCR_H_          SFRRPCR_ + 0x01
sfrb(SFRRPCR_L, SFRRPCR_L_);
sfrb(SFRRPCR_H, SFRRPCR_H_);
#define SYSNMI              (1<<0)  /* RST/NMI pin (0:Reset, 1: NMI) */
#define SYSNMIIES           (1<<1)  /* NMI edge select (0:rising edge). Can trigger NMI */
#define SYSRSTUP            (1<<2)  /* Reset resistor pin pullup (0: pulldown, 1: pullup) */
#define SYSRSTRE            (1<<3)  /* Reset pin resistor enable (0: disabled, 1: enabled) */


#define RTC_A_VECTOR        0x52    /* 0xFFD2 Basic Timer / RTC */
#define PORT2_VECTOR        0x54    /* 0xFFD4 Port 2 */
#define USCIB3_RXTX_VECTOR  0x56    /* 0xFFD6 USCI B3 RX/TX */
#define USCIA3_RXTX_VECTOR  0x58    /* 0xFFD8 USCI A3 RX/TX */
#define USCIB1_RXTX_VECTOR  0x5A    /* 0xFFDA USCI B1 RX/TX */
#define USCIA1_RXTX_VECTOR  0x5C    /* 0xFFDC USCI A1 RX/TX */
#define PORT1_VECTOR        0x5E    /* 0xFFDE Port 1 */
#define TIMER1_A1_VECTOR    0x60    /* 0xFFE0 Timer1_A3 CC1-2, TA1 */
#define TIMER1_A0_VECTOR    0x62    /* 0xFFE2 Timer1_A3 CC0 */
#define DMA_VECTOR          0x64    /* 0xFFE4 DMA */
#define USCIB2_RXTX_VECTOR  0x66    /* 0xFFE6 USCI B2 RX/TX */
#define USCIA2_RXTX_VECTOR  0x68    /* 0xFFE8 USCI A2 RX/TX */
#define TIMER0_A1_VECTOR    0x6A    /* 0xFFEA Timer0_A5 CC1-4, TA0 */
#define TIMER0_A0_VECTOR    0x6C    /* 0xFFEC Timer0_A5 CC0 */
#define AD12_A_VECTOR       0x6E    /* 0xFFEE ADC */
#define USCIB0_RXTX_VECTOR  0x70    /* 0xFFF0 USCI B0 RX/TX */
#define USCIA0_RXTX_VECTOR  0x72    /* 0xFFF2 USCI A0 RX/TX */
#define WDT_VECTOR          0x74    /* 0xFFF4 Watchdog Timer */
#define TIMER0_B1_VECTOR    0x76    /* 0xFFF6 Timer_B7 CC1-6, TB */
#define TIMER0_B0_VECTOR    0x78    /* 0xFFF8 Timer_B7 CC0 */
#define USER_NMI_VECTOR     0x7A    /* 0xFFFA Non-maskable */
#define NMI_VECTOR          0x7C    /* 0xFFFC Non-maskable */

#endif /* #ifndef __msp430x54xx */
