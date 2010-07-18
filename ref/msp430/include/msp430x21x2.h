#ifndef __msp430x21x2
#define __msp430x21x2

/* msp430x21x2.h
 *
 * mspgcc project: MSP430 device headers
 * MSP430x11x2 family header
 *
 * (c) 2005 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * 2008-09-17 - sb-sf (sb-sf@users.sf.net)
 * - created, based on msp430x21x1.h
 *
 * $Id: msp430x21x2.h,v 1.2 2009/02/27 02:16:38 cliechti Exp $
 */

#include <iomacros.h>

#define __MSP430_HAS_WDT__
#define __MSP430_HAS_PORT1_R__
#define __MSP430_HAS_PORT2_R__
#define __MSP430_HAS_PORT3_R__
#define __MSP430_HAS_USCI_AB0__
#define __MSP430_HAS_TA3__
#define __MSP430_HAS_T1A2__
#define __MSP430_HAS_BC2__
#define __MSP430_HAS_FLASH2__
#define __MSP430_HAS_CAPLUS__
#define __MSP430_HAS_ADC10__
#define __MSP430_HAS_TLV__

#include <msp430/basic_clock.h>
#include <msp430/flash.h>
#include <msp430/eprom.h>
#include <msp430/timera.h>
#include <msp430/gpio.h>
#include <msp430/compa.h>
#include <msp430/usci.h>
#include <msp430/adc10.h>
#include <msp430/tlv.h>

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
#define UC0IE               IE2
#define UCA0RXIE            (1<<0)
#define UCA0TXIE            (1<<1)
#define UCB0RXIE            (1<<2)
#define UCB0TXIE            (1<<3)

#define IFG2_               0x0003  /* Interrupt Flag 2 */
sfrb(IFG2,IFG2_);
#define UC0IFG              IFG2
#define UCA0RXIFG           (1<<0)
#define UCA0TXIFG           (1<<1)
#define UCB0RXIFG           (1<<2)
#define UCB0TXIFG           (1<<3)

#define PORT1_VECTOR        4       /* 0xFFE4 Port 1 */
#define PORT2_VECTOR        6       /* 0xFFE6 Port 2 */
#define ADC10_VECTOR        10      /* 0xFFEA ADC10 */
#define USCIAB0TX_VECTOR    12      /* 0xFFEC USCI A0/B0 Transmit */
#define USCIAB0RX_VECTOR    14      /* 0xFFEE USCI A0/B0 Receive */
#define TIMER0_A1_VECTOR    16      /* 0xFFF0 Timer A0 CC1-2, TA */
#define TIMER0_A0_VECTOR    18      /* 0xFFF2 Timer A0 CC0 */
#define WDT_VECTOR          20      /* 0xFFF4 Watchdog Timer */
#define COMPARATORA_VECTOR  22      /* 0xFFF6 Comparator A */
#define TIMER1_A1_VECTOR    24      /* 0xFFF8 Timer A1 CC1, TA */
#define TIMER1_A0_VECTOR    26      /* 0xFFFA Timer A1 CC0 */
#define NMI_VECTOR          28      /* 0xFFFC Non-maskable */ 

#define BSLSKEY_         0xFFDE /* The address is used as bootstrap loader security key */
#define BSLSKEY_DISABLE  0xAA55 /* Disables the BSL completely */
#define BSLSKEY_NO_ERASE 0x0000 /* Disables the erasure of the flash if an invalid password is supplied */

#endif /* #ifndef __msp430x21x2 */

