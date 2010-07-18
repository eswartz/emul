#ifndef __msp430xG43x
#define __msp430xG43x

/* msp430xG43x.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430xG43x family header
 *
 * (c) 2003 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: msp430xG43x.h,v 1.8 2008/11/07 08:28:40 sb-sf Exp $
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
#define __MSP430_HAS_FLLPLUS__
#define __MSP430_HAS_SVS__
#define __MSP430_HAS_LCD4__
#define __MSP430_HAS_UART0__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_TB3__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_COMPA__
#define __MSP430_HAS_ADC12__
#define __MSP430_HAS_DAC12_3__
#define __MSP430_HAS_DMA_3__
#define __MSP430_HAS_OA_3__

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
#include <msp430/lcd.h>
#include <msp430/flash.h>
#include <msp430/timera.h>
#include <msp430/timerb.h>
#include <msp430/usart.h>
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
#define DAC12_DMA_VECTOR    6       /* 0xFFE6 DAC 12 */
#define PORT1_VECTOR        8       /* 0xFFE8 Port 1 */
#define TIMER0_A1_VECTOR    10      /* 0xFFEA Timer A CC1-2, TA */
#define TIMER0_A0_VECTOR    12      /* 0xFFEC Timer A CC0 */
#define ADC12_VECTOR        14      /* 0xFFEE ADC */
#define USART0TX_VECTOR     16      /* 0xFFF0 USART 0 Transmit */
#define USART0RX_VECTOR     18      /* 0xFFF2 USART 0 Receive */
#define WDT_VECTOR          20      /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  22      /* 0xFFF6 Comparator A */
#define TIMERB1_VECTOR      24      /* 0xFFF8 Timer B CC1-2, TB */
#define TIMERB0_VECTOR      26      /* 0xFFFA Timer B CC0 */
#define NMI_VECTOR          28      /* 0xFFFC Non-maskable */

#define UART0TX_VECTOR      USART0TX_VECTOR
#define UART0RX_VECTOR      USART0RX_VECTOR
#define TIMERA1_VECTOR      TIMER0_A1_VECTOR
#define TIMERA0_VECTOR      TIMER0_A0_VECTOR
#define ADC_VECTOR          ADC12_VECTOR

#endif /* #ifndef __msp430xG43x */
