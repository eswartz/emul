#ifndef __msp430xE42x
#define __msp430xE42x

/* msp430xE42x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430xE42x family header
 *
 * (c) 2003 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430xE42x.h,v 1.6 2009/01/11 23:11:48 sb-sf Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT1__
#define __MSP430_HAS_PORT2__
#define __MSP430_HAS_BT__
#define __MSP430_HAS_FLLPLUS_SMALL__
#define __MSP430_HAS_SVS__
#define __MSP430_HAS_LCD4__
#define __MSP430_HAS_UART0__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_MPY__
#define __MSP430_HAS_SD16_CH1__
#define __MSP430_HAS_SD16_CH2__
#define __MSP430_SD16IV_BASE__    0x110
#define __MSP430_SD16MEM_BASE__   0x112

#if defined(__MSP430_E423__) || defined(__MSP430_E425__) || defined(__MSP430_E427__)
#define __MSP430_HAS_ESP430E__  // for backward compability
#define __MSP430_HAS_ESP430E1A__
#elif defined(__MSP430_E4232__) || defined(__MSP430_E4242__) ||  defined(__MSP430_E4252__) || defined(__MSP430_E4272__)
#define __MSP430_HAS_ESP430E1B__
#endif

#define __msp430_have_lcd_16_20
#define LCD_BASE 0x90

#include <msp430/basic_timer.h>
#include <msp430/system_clock.h>
#include <msp430/svs.h>
#include <msp430/lcd.h>
#include <msp430/flash.h>
#include <msp430/timera.h>
#include <msp430/usart.h>
#include <msp430/mpy.h>
#include <msp430/gpio.h>
#include <msp430/sd16.h>
#include <msp430/esp430e.h>

#include <msp430/common.h>

#define IE1_                0x0000  /* Interrupt Enable 1 */
sfrb(IE1,IE1_);
#define WDTIE               (1<<0)
#define OFIE                (1<<1)
#define NMIIE               (1<<4)
#define ACCVIE              (1<<5)
#define URXIE0              (1<<6)
#define UTXIE0              (1<<7)

#define IFG1_               0x0002  /* Interrupt Flag 1 */
sfrb(IFG1,IFG1_);
#define WDTIFG              (1<<0)
#define OFIFG               (1<<1)
#define NMIIFG              (1<<4)
#define URXIFG0             (1<<6)
#define UTXIFG0             (1<<7)

#define IE2_                0x0001  /* Interrupt Enable 2 */
sfrb(IE2,IE2_);
#define BTIE                (1<<7)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define BTIFG               (1<<7)

#define ME1_                0x0004  /* Module Enable 1 */
sfrb(ME1,ME1_);
#define URXE0               (1<<6)
#define USPIE0              (1<<6)
#define UTXE0               (1<<7)

#define BASICTIMER_VECTOR   0       /* 0xFFE0 Basic Timer */
#define PORT2_VECTOR        2       /* 0xFFE2 Port 2 */
#define PORT1_VECTOR        8       /* 0xFFE8 Port 1 */
#define TIMERA1_VECTOR      10      /* 0xFFEA Timer A CC1-2, TA */
#define TIMERA0_VECTOR      12      /* 0xFFEC Timer A CC0 */
#define USART0TX_VECTOR     16      /* 0xFFF0 USART 0 Transmit */
#define USART0RX_VECTOR     18      /* 0xFFF2 USART 0 Receive */
#define WDT_VECTOR          20      /* 0xFFF4 Watchdog Timer */
#define SD16_VECTOR         24      /* 0xFFF8 Sigma Delta ADC */
#define ESP430_VECTOR       26      /* 0xFFFA ESP430 Mailbox */
#define NMI_VECTOR          28      /* 0xFFFC Non-maskable */ 

#define UART0TX_VECTOR      USART0TX_VECTOR
#define UART0RX_VECTOR      USART0RX_VECTOR

#endif /* #ifndef __msp430xE42x */
