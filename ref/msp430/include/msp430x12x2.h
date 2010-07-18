#ifndef __msp430x12x2
#define __msp430x12x2

/* msp430x12x2.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x12x2 family header
 *
 * (c) 2002 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430x12x2.h,v 1.5 2005/08/17 14:28:46 coppice Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT1__
#define __MSP430_HAS_PORT2__
#define __MSP430_HAS_PORT3__
#define __MSP430_HAS_UART0__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_BASIC_CLOCK__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_ADC10__

#include <msp430/usart.h>
#include <msp430/gpio.h>
#include <msp430/timera.h>
#include <msp430/basic_clock.h>
#include <msp430/flash.h>
#include <msp430/adc10.h>

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
#define URXIE0              (1<<0)
#define UTXIE0              (1<<1)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define URXIFG0             (1<<0)
#define UTXIFG0             (1<<1)

#define ME2_                0x0005  /* Module Enable 2 */
sfrb(ME2,ME2_);
#define URXE0               (1<<0)
#define USPIE0              (1<<0)
#define UTXE0               (1<<1)

#define PORT1_VECTOR        4       /* 0xFFE4 Port 1 */
#define PORT2_VECTOR        6       /* 0xFFE6 Port 2 */
#define ADC10_VECTOR        10      /* 0xFFEA ADC 10 */
#define USART0TX_VECTOR     12      /* 0xFFEC USART 0 Transmit */
#define USART0RX_VECTOR     14      /* 0xFFEE USART 0 Receive */
#define TIMERA1_VECTOR      16      /* 0xFFF0 Timer A CC1-2, TA */
#define TIMERA0_VECTOR      18      /* 0xFFF2 Timer A CC0 */
#define WDT_VECTOR          20      /* 0xFFF4 Watchdog Timer */
#define NMI_VECTOR          28      /* 0xFFFC Non-maskable */

#define UART0TX_VECTOR      USART0TX_VECTOR
#define UART0RX_VECTOR      USART0RX_VECTOR

#endif /* #ifndef __msp430x12x2 */
