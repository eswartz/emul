#if !defined(__msp430x47xx)
#define __msp430x47xx

/* msp430x47xx.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x47xx family header
 *
 * (c) 2006 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * 2008-10-08 - sb-sf (sb-sf@users.sf.net)
 * - created, based on msp430xG461x.h
 *
 * 2009-06-04 - THLN
 * - for msp430x47xx
 *       - __MSP430_HAS_PORT??_R__ updated
 *
 * $Id: msp430x47xx.h,v 1.7 2009/07/26 05:53:27 pjansen Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_MPY32_BASE__   0x140
#define __MSP430_HAS_PORT1_R__
#define __MSP430_HAS_PORT2_R__
#define __MSP430_HAS_PORT3_R__
#define __MSP430_HAS_PORT4_R__
#define __MSP430_HAS_PORT5_R__
#define __MSP430_HAS_PORT7_R__
#define __MSP430_HAS_PORT8_R__
#define __MSP430_HAS_PORT9_R__
#define __MSP430_HAS_PORT10_R__
#define __MSP430_HAS_PORTA__
#define __MSP430_HAS_PORTB__
#define __MSP430_HAS_FLLPLUS__
#define __MSP430_HAS_BT_RTC__
#define __MSP430_HAS_LCD_A__
#define __MSP430_HAS_USCI__
#define __MSP430_HAS_USCI0__
#define __MSP430_HAS_USCI1__
#define __MSP430_HAS_USCI_AB0__
#define __MSP430_HAS_USCI_AB1__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_TB3__
#define __MSP430_HAS_FLASH__
#define __MSP430_HAS_COMPA__
#define __MSP430_HAS_SVS__
#define __MSP430_HAS_SD16_A__
#define __MSP430_HAS_SD16_CH1__
#define __MSP430_HAS_SD16_CH2__
#if defined(__MSP430_4783__) || defined(__MSP430_4793__)
#define __MSP430_HAS_SD16_CH3__
#endif  /* __MSP430_47x3__ */

#if defined(__MSP430_47166__) || defined(__MSP430_47176__) || \
defined(__MSP430_47186__) || defined(__MSP430_47196__)
#define __MSP430_HAS_SD16_CH3__
#define __MSP430_HAS_SD16_CH4__
#define __MSP430_HAS_SD16_CH5__
#endif
#if defined(__MSP430_47167__) || defined(__MSP430_47177__) || \
defined(__MSP430_47187__) || defined(__MSP430_47197__)
#define __MSP430_HAS_SD16_CH3__
#define __MSP430_HAS_SD16_CH4__
#define __MSP430_HAS_SD16_CH5__
#define __MSP430_HAS_SD16_CH6__
#endif

#if defined(__MSP430_4784__) || defined(__MSP430_4794__) \
    || defined(__MSP430_4783__) || defined(__MSP430_4793__)
#define __MSP430_SD16IV_BASE__    0x110
#define __MSP430_SD16MEM_BASE__   0x112
#elif defined(__MSP430_47166__) || defined(__MSP430_47176__) || 
defined(__MSP430_47186__) || defined(__MSP430_47196__) \
    || defined(__MSP430_47167__) || defined(__MSP430_47177__) || 
defined(__MSP430_47187__) || defined(__MSP430_47197__)
#define __MSP430_SD16IV_BASE__    0x1AE
#define __MSP430_SD16MEM_BASE__   0x110
#endif




#define __msp430_have_lcd_16_20
#define LCD_BASE 0x90

#include <msp430/basic_timer.h>
#include <msp430/system_clock.h>
#include <msp430/svs.h>
#include <msp430/lcd_a.h>
#include <msp430/flash.h>
#include <msp430/compa.h>
#include <msp430/timera.h>
#include <msp430/timerb.h>
#include <msp430/usci.h>
#include <msp430/gpio.h>
#include <msp430/mpy32.h>
#include <msp430/sd16.h>

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
#define BTIE                (1<<7)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define UCA0RXIFG           (1<<0)
#define UCA0TXIFG           (1<<1)
#define UCB0RXIFG           (1<<2)
#define UCB0TXIFG           (1<<3)
#define BTIFG               (1<<7)


#if !defined(__MSP430X__)
/* __MSP430_47x3__, __MSP430_47x4__ */
#define BASICTIMER_VECTOR   0       /* 0xFFE0 Basic Timer / RTC */
#define PORT2_VECTOR        2       /* 0xFFE2 Port 2 */
#define USCIAB1TX_VECTOR    4       /* 0xFFE4 USCI A1/B1 Transmit */
#define USCIAB1RX_VECTOR    6       /* 0xFFE6 USCI A1/B1 Receive */
#define PORT1_VECTOR        8       /* 0xFFE8 Port 1 */
#define TIMER0_A1_VECTOR    10      /* 0xFFEA Timer A CC1-2, TA */
#define TIMER0_A0_VECTOR    12      /* 0xFFEC Timer A CC0 */
#define SD16_VECTOR         14      /* 0xFFEE ADC */
#define USCIAB0TX_VECTOR    16      /* 0xFFF0 USCI A0/B0 Transmit */
#define USCIAB0RX_VECTOR    18      /* 0xFFF2 USCI A0/B0 Receive */
#define WDT_VECTOR          20      /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  22      /* 0xFFF6 Comparator A */
#define TIMER0_B1_VECTOR    24      /* 0xFFF8 Timer B CC1-2, TB */
#define TIMER0_B0_VECTOR    26      /* 0xFFFA Timer B CC0 */
#define NMI_VECTOR          28      /* 0xFFFC Non-maskable */
#else
/* __MSP430_471x6__, __MSP430_471x7__ */
#define DMA_VECTOR          30      /* 0xFFDE DMA */
#define BASICTIMER_VECTOR   32      /* 0xFFE0 Basic Timer / RTC */
#define PORT2_VECTOR        34      /* 0xFFE2 Port 2 */
#define USCIAB1TX_VECTOR    36      /* 0xFFE4 USCI A1/B1 Transmit */
#define USCIAB1RX_VECTOR    38      /* 0xFFE6 USCI A1/B1 Receive */
#define PORT1_VECTOR        40      /* 0xFFE8 Port 1 */
#define TIMER0_A1_VECTOR    42      /* 0xFFEA Timer A CC1-2, TA */
#define TIMER0_A0_VECTOR    44      /* 0xFFEC Timer A CC0 */
#define SD16_VECTOR         46      /* 0xFFEE ADC */
#define USCIAB0TX_VECTOR    48      /* 0xFFF0 USCI A0/B0 Transmit */
#define USCIAB0RX_VECTOR    50      /* 0xFFF2 USCI A0/B0 Receive */
#define WDT_VECTOR          52      /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  54      /* 0xFFF6 Comparator A */
#define TIMER0_B1_VECTOR    56      /* 0xFFF8 Timer B CC1-2, TB */
#define TIMER0_B0_VECTOR    58      /* 0xFFFA Timer B CC0 */
#define NMI_VECTOR          60      /* 0xFFFC Non-maskable */
#endif

#define TIMERA1_VECTOR      TIMER0_A1_VECTOR
#define TIMERA0_VECTOR      TIMER0_A0_VECTOR
#define TIMERB1_VECTOR      TIMER0_B1_VECTOR
#define TIMERB0_VECTOR      TIMER0_B0_VECTOR

#endif /* #ifndef __msp430x47xx */
