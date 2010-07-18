#ifndef __msp430x14x
#define __msp430x14x

/* msp430x14x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x14x family header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430x14x.h,v 1.9 2005/10/18 13:29:21 coppice Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_MPY__
#define __MSP430_HAS_PORT1__
#define __MSP430_HAS_PORT2__
#define __MSP430_HAS_PORT3__
#define __MSP430_HAS_PORT4__
#define __MSP430_HAS_PORT5__
#define __MSP430_HAS_PORT6__
#define __MSP430_HAS_UART0__
#define __MSP430_HAS_UART1__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_TB7__
#define __MSP430_HAS_BASIC_CLOCK__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_COMPA__
#define __MSP430_HAS_ADC12__

#include <msp430/mpy.h>
#include <msp430/gpio.h>
#include <msp430/usart.h>
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
#define URXIE0              (1<<6)
#define UTXIE0              (1<<7)

#define IFG1_               0x0002  /* Interrupt Flag 1 */
sfrb(IFG1,IFG1_);
#define WDTIFG              (1<<0)
#define OFIFG               (1<<1)
#define NMIIFG              (1<<4)
#define URXIFG0             (1<<6)
#define UTXIFG0             (1<<7)

#define ME1_                0x0004  /* Module Enable 1 */
sfrb(ME1,ME1_);
#define URXE0               (1<<6)
#define USPIE0              (1<<6)
#define UTXE0               (1<<7)

#define IE2_                0x0001  /* Interrupt Enable 2 */
sfrb(IE2,IE2_);
#define URXIE1              (1<<4)
#define UTXIE1              (1<<5)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define URXIFG1             (1<<4)
#define UTXIFG1             (1<<5)

#define ME2_                0x0005  /* Module Enable 2 */
sfrb(ME2,ME2_);
#define URXE1               (1<<4)
#define USPIE1              (1<<4)
#define UTXE1               (1<<5)

#define PORT2_VECTOR        2  /* 0xFFE2 Port 2 */
#define USART1TX_VECTOR     4  /* 0xFFE4 USART 1 Transmit */
#define USART1RX_VECTOR     6  /* 0xFFE6 USART 1 Receive */
#define PORT1_VECTOR        8  /* 0xFFE8 Port 1 */
#define TIMERA1_VECTOR      10 /* 0xFFEA Timer A CC1-2, TA */
#define TIMERA0_VECTOR      12 /* 0xFFEC Timer A CC0 */   
#define ADC12_VECTOR        14 /* 0xFFEE ADC */
#define USART0TX_VECTOR     16 /* 0xFFF0 USART 0 Transmit */
#define USART0RX_VECTOR     18 /* 0xFFF2 USART 0 Receive */
#define WDT_VECTOR          20 /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  22 /* 0xFFF6 Comparator A */
#define TIMERB1_VECTOR      24 /* 0xFFF8 Timer B 1-7 */
#define TIMERB0_VECTOR      26 /* 0xFFFA Timer B 0 */
#define NMI_VECTOR          28 /* 0xFFFC Non-maskable */

#define UART0TX_VECTOR      USART0TX_VECTOR
#define UART0RX_VECTOR      USART0RX_VECTOR
#define UART1TX_VECTOR      USART1TX_VECTOR
#define UART1RX_VECTOR      USART1RX_VECTOR
#define ADC_VECTOR          ADC12_VECTOR

#endif /* #ifndef __msp430x14x */
