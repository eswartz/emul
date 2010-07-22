#if !defined(__msp430xG461x)
#define __msp430xG461x

/* msp430xG461x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430xG461x family header
 *
 * (c) 2006 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430xG461x.h,v 1.11 2009/07/26 05:53:27 pjansen Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT1__
#define __MSP430_HAS_PORT2__
#define __MSP430_HAS_PORT3__
#define __MSP430_HAS_PORT4__
#define __MSP430_HAS_PORT5__
#define __MSP430_HAS_PORT6__
#define __MSP430_HAS_PORT7__
#define __MSP430_HAS_PORT8__
#define __MSP430_HAS_PORTA__
#define __MSP430_HAS_PORT9__
#define __MSP430_HAS_PORT10__
#define __MSP430_HAS_PORTB__
#define __MSP430_HAS_FLLPLUS__
#define __MSP430_HAS_BT_RTC__
#define __MSP430_HAS_LCD_A__
#define __MSP430_HAS_UART0__
#define __MSP430_HAS_USCI__
#define __MSP430_HAS_USCI0__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_TB7__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_COMPA__
#define __MSP430_HAS_SVS__
#define __MSP430_HAS_ADC12__
#define __MSP430_HAS_DAC12__
#define __MSP430_HAS_DMAX_3__
#define __MSP430_HAS_DMAIV__
#define __MSP430_HAS_OA_3__
#define __MSP430_HAS_UART1__

#define __msp430_have_lcd_16_20
#define __msp430_have_dac12_op_amp
#define LCD_BASE 0x90
#define __msp430_have_opamp_1
#define __msp430_have_opamp_2
#define __msp430_have_opamp_feedback_taps
#define __msp430_have_opamp_output_select
#define __msp430_have_opamp_rail_to_rail

#include <msp430/basic_timer.h>
#include <msp430/system_clock.h>
#include <msp430/svs.h>
#include <msp430/lcd_a.h>
#include <msp430/flash.h>
#include <msp430/compa.h>
#include <msp430/timera.h>
#include <msp430/timerb.h>
#include <msp430/usart.h>
#include <msp430/usci.h>
#include <msp430/adc12.h>
#include <msp430/dac12.h>
#include <msp430/dma.h>
#include <msp430/gpio.h>
#include <msp430/opamp.h>

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
#define UCA0RXIE            (1<<0)
#define UCA0TXIE            (1<<1)
#define UCB0RXIE            (1<<2)
#define UCB0TXIE            (1<<3)
#define URXIE1              (1<<4)
#define UTXIE1              (1<<5)
#define BTIE                (1<<7)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define UCA0RXIFG           (1<<0)
#define UCA0TXIFG           (1<<1)
#define UCB0RXIFG           (1<<2)
#define UCB0TXIFG           (1<<3)
#define URXIFG1             (1<<4)
#define UTXIFG1             (1<<5)
#define BTIFG               (1<<7)

#define ME1_                0x0004  /* Module Enable 1 */
sfrb(ME1,ME1_);

#define ME2_                0x0005  /* Module Enable 2 */
sfrb(ME2,ME2_);
#define USPIE1              (1<<4)
#define URXE1               (1<<4)
#define UTXE1               (1<<5)

#define DAC12_VECTOR        28      /* 0xFFDC DAC 12 */
#define DMA_VECTOR          30      /* 0xFFDE DMA */
#define BASICTIMER_VECTOR   32      /* 0xFFE0 Basic Timer / RTC */
#define PORT2_VECTOR        34      /* 0xFFE2 Port 2 */
#define USART1TX_VECTOR     36      /* 0xFFE4 USART 1 Transmit */
#define USART1RX_VECTOR     38      /* 0xFFE6 USART 1 Receive */
#define PORT1_VECTOR        40      /* 0xFFE8 Port 1 */
#define TIMER0_A1_VECTOR    42      /* 0xFFEA Timer A CC1-2, TA */
#define TIMER0_A0_VECTOR    44      /* 0xFFEC Timer A CC0 */
#define ADC12_VECTOR        46      /* 0xFFEE ADC */
#define USCITX_VECTOR       48      /* 0xFFF0 USCI A0/B0 Transmit */
#define USCIRX_VECTOR       50      /* 0xFFF2 USCI A0/B0 Receive */
#define WDT_VECTOR          52      /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  54      /* 0xFFF6 Comparator A */
#define TIMER0_B1_VECTOR    56      /* 0xFFF8 Timer B CC1-2, TB */
#define TIMER0_B0_VECTOR    58      /* 0xFFFA Timer B CC0 */
#define NMI_VECTOR          60      /* 0xFFFC Non-maskable */

#define UART0TX_VECTOR      USART0TX_VECTOR
#define UART0RX_VECTOR      USART0RX_VECTOR
#define TIMERA1_VECTOR      TIMER0_A1_VECTOR
#define TIMERA0_VECTOR      TIMER0_A0_VECTOR
#define TIMERB1_VECTOR      TIMER0_B1_VECTOR
#define TIMERB0_VECTOR      TIMER0_B0_VECTOR

#endif /* #ifndef __msp430xG461x */
