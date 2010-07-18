#include <msp430/iostructures.h>

#if !defined(__msp430_headers_gpio_h)
#define __msp430_headers_gpio_h

/* gpio.h
 *
 * mspgcc project: MSP430 device headers
 * GPIO module header
 *
 * 2009-06-04 - THLN
 * - for msp430x47xx
 *     - __MSP430_HAS_PORT9_R__ and __MSP430_HAS_PORT10_R__ added
 *
 * 2008-06-04 - TonyB (tony.borries@gmail.com)
 * - for msp430x2618 (and possibly others)
 *     - define __MSP430_HAS_PORT7_R__ and __MSP430_HAS_PORT7_R__
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: gpio.h,v 1.8 2009/06/04 21:34:29 cliechti Exp $
 */

/* Switches:

__MSP430_HAS_PORT0__        - if device has port 0
__MSP430_HAS_PORT1__        - if device has port 1
__MSP430_HAS_PORT1_R__      - if device has port 1 with pull-downs
__MSP430_HAS_PORT2__        - if device has port 2
__MSP430_HAS_PORT2_R__      - if device has port 2 with pull-downs
__MSP430_HAS_PORT3__        - if device has port 3
__MSP430_HAS_PORT3_R__      - if device has port 3 with pull-downs
__MSP430_HAS_PORT4__        - if device has port 4
__MSP430_HAS_PORT4_R__      - if device has port 4 with pull-downs
__MSP430_HAS_PORT5__        - if device has port 5
__MSP430_HAS_PORT5_R__      - if device has port 5 with pull-downs
__MSP430_HAS_PORT6__        - if device has port 6
__MSP430_HAS_PORT6_R__      - if device has port 6 with pull-downs
__MSP430_HAS_PORT7__        - if device has port 7
__MSP430_HAS_PORT7_R__      - if device has port 7 with pull-downs
__MSP430_HAS_PORT8__        - if device has port 8
__MSP430_HAS_PORT8_R__      - if device has port 8 with pull-downs
__MSP430_HAS_PORTA__        - if device has port A (16 bit view of ports 
7 & 8)
__MSP430_HAS_PORT9__        - if device has port 9
__MSP430_HAS_PORT9_R__      - if device has port 9 with pull-downs
__MSP430_HAS_PORT10__       - if device has port 10
__MSP430_HAS_PORT10_R__      - if device has port 10 with pull-downs
__MSP430_HAS_PORTB__        - if device has port B (16 bit view of ports 
9 & 10)

Note: these only make sense if the port itself is present. Also note that
the port resistor enable registers for ports 3-6 overlap with port 0 
registers,
so any device that has these resistors will not have port 0.
*/

#if defined(__MSP430_HAS_PORT0__)
#define P0IN_               0x0010  /* Port 0 Input */
sfrb(P0IN, P0IN_);
#define P0IN_0              0x01
#define P0IN_1              0x02
#define P0IN_2              0x04
#define P0IN_3              0x08
#define P0IN_4              0x10
#define P0IN_5              0x20
#define P0IN_6              0x40
#define P0IN_7              0x80

#define P0OUT_              0x0011  /* Port 0 Output */
sfrb(P0OUT, P0OUT_);
#define P0OUT_0             0x01
#define P0OUT_1             0x02
#define P0OUT_2             0x04
#define P0OUT_3             0x08
#define P0OUT_4             0x10
#define P0OUT_5             0x20
#define P0OUT_6             0x40
#define P0OUT_7             0x80

#define P0DIR_              0x0012  /* Port 0 Direction */
sfrb(P0DIR, P0DIR_);
#define P0DIR_0             0x01
#define P0DIR_1             0x02
#define P0DIR_2             0x04
#define P0DIR_3             0x08
#define P0DIR_4             0x10
#define P0DIR_5             0x20
#define P0DIR_6             0x40
#define P0DIR_7             0x80

#define P0IFG_              0x0013  /* Port 0 Interrupt Flag */
sfrb(P0IFG, P0IFG_);
/* These two bits are defined in Interrupt Flag 1 */
/* #define P0IFG_0             0x01 */
/* #define P0IFG_1             0x02 */
#define P0IFG_2             0x04
#define P0IFG_3             0x08
#define P0IFG_4             0x10
#define P0IFG_5             0x20
#define P0IFG_6             0x40
#define P0IFG_7             0x80

#define P0IES_              0x0014  /* Port 0 Interrupt Edge Select */
sfrb(P0IES, P0IES_);
#define P0IES_0             0x01
#define P0IES_1             0x02
#define P0IES_2             0x04
#define P0IES_3             0x08
#define P0IES_4             0x10
#define P0IES_5             0x20
#define P0IES_6             0x40
#define P0IES_7             0x80

#define P0IE_               0x0015  /* Port 0 Interrupt Enable */
sfrb(P0IE, P0IE_);
/* These two bits are defined in Interrupt Enable 1 */
/* #define P0IE_0              0x01 */
/* #define P0IE_1              0x02 */
#define P0IE_2              0x04
#define P0IE_3              0x08
#define P0IE_4              0x10
#define P0IE_5              0x20
#define P0IE_6              0x40
#define P0IE_7              0x80
#endif

#if defined(__MSP430_HAS_PORT1__)  ||  defined(__MSP430_HAS_PORT1_R__)
#define P1IN_               0x0020  /* Port 1 Input */
sfrb(P1IN, P1IN_);
#define P1OUT_              0x0021  /* Port 1 Output */
sfrb(P1OUT, P1OUT_);
#define P1DIR_              0x0022  /* Port 1 Direction */
sfrb(P1DIR, P1DIR_);
#define P1IFG_              0x0023  /* Port 1 Interrupt Flag */
sfrb(P1IFG, P1IFG_);
#define P1IES_              0x0024  /* Port 1 Interrupt Edge Select */
sfrb(P1IES, P1IES_);
#define P1IE_               0x0025  /* Port 1 Interrupt Enable */
sfrb(P1IE, P1IE_);
#define P1SEL_              0x0026  /* Port 1 Selection */
sfrb(P1SEL, P1SEL_);
#if defined(__MSP430_HAS_PORT1_R__)
#define P1REN_              0x0027  /* Port 1 Resistor enable */
sfrb(P1REN, P1REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT2__)  ||  defined(__MSP430_HAS_PORT2_R__)
#define P2IN_               0x0028  /* Port 2 Input */
sfrb(P2IN, P2IN_);
#define P2OUT_              0x0029  /* Port 2 Output */
sfrb(P2OUT, P2OUT_);
#define P2DIR_              0x002A  /* Port 2 Direction */
sfrb(P2DIR, P2DIR_);
#define P2IFG_              0x002B  /* Port 2 Interrupt Flag */
sfrb(P2IFG, P2IFG_);
#define P2IES_              0x002C  /* Port 2 Interrupt Edge Select */
sfrb(P2IES, P2IES_);
#define P2IE_               0x002D  /* Port 2 Interrupt Enable */
sfrb(P2IE, P2IE_);
#define P2SEL_              0x002E  /* Port 2 Selection */
sfrb(P2SEL, P2SEL_);
#if defined(__MSP430_HAS_PORT2_R__)
#define P2REN_              0x002F  /* Port 2 Resistor enable */
sfrb(P2REN, P2REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT3__)  ||  defined(__MSP430_HAS_PORT3_R__)
#define P3IN_               0x0018  /* Port 3 Input */
sfrb(P3IN, P3IN_);
#define P3OUT_              0x0019  /* Port 3 Output */
sfrb(P3OUT, P3OUT_);
#define P3DIR_              0x001A  /* Port 3 Direction */
sfrb(P3DIR, P3DIR_);
#define P3SEL_              0x001B  /* Port 3 Selection */
sfrb(P3SEL, P3SEL_);
#if defined(__MSP430_HAS_PORT3_R__)
#define P3REN_              0x0010  /* Port 3 Resistor enable */
sfrb(P3REN, P3REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT4__)  ||  defined(__MSP430_HAS_PORT4_R__)
#define P4IN_               0x001C  /* Port 4 Input */
sfrb(P4IN, P4IN_);
#define P4OUT_              0x001D  /* Port 4 Output */
sfrb(P4OUT, P4OUT_);
#define P4DIR_              0x001E  /* Port 4 Direction */
sfrb(P4DIR, P4DIR_);
#define P4SEL_              0x001F  /* Port 4 Selection */
sfrb(P4SEL, P4SEL_);
#if defined(__MSP430_HAS_PORT4_R__)
#define P4REN_              0x0011  /* Port 4 Resistor enable */
sfrb(P4REN, P4REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT5__)  ||  defined(__MSP430_HAS_PORT5_R__)
#define P5IN_               0x0030  /* Port 5 Input */
sfrb(P5IN, P5IN_);
#define P5OUT_              0x0031  /* Port 5 Output */
sfrb(P5OUT, P5OUT_);
#define P5DIR_              0x0032  /* Port 5 Direction */
sfrb(P5DIR, P5DIR_);
#define P5SEL_              0x0033  /* Port 5 Selection */
sfrb(P5SEL, P5SEL_);
#if defined(__MSP430_HAS_PORT5_R__)
#define P5REN_              0x0012  /* Port 5 Resistor enable */
sfrb(P5REN, P5REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT6__)  ||  defined(__MSP430_HAS_PORT6_R__)
#define P6IN_               0x0034  /* Port 6 Input */
sfrb(P6IN, P6IN_);
#define P6OUT_              0x0035  /* Port 6 Output */
sfrb(P6OUT, P6OUT_);
#define P6DIR_              0x0036  /* Port 6 Direction */
sfrb(P6DIR, P6DIR_);
#define P6SEL_              0x0037  /* Port 6 Selection */
sfrb(P6SEL, P6SEL_);
#if defined(__MSP430_HAS_PORT6_R__)
#define P6REN_              0x0013  /* Port 6 Resistor enable */
sfrb(P6REN, P6REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT7__)  ||  defined(__MSP430_HAS_PORT7_R__)
#define P7IN_               0x0038  /* Port 7 Input */
sfrb(P7IN, P7IN_);
#define P7OUT_              0x003A  /* Port 7 Output */
sfrb(P7OUT, P7OUT_);
#define P7DIR_              0x003C  /* Port 7 Direction */
sfrb(P7DIR, P7DIR_);
#define P7SEL_              0x003E  /* Port 7 Selection */
sfrb(P7SEL, P7SEL_);
#if defined(__MSP430_HAS_PORT7_R__)
#define P7REN_              0x0014  /* Port 7 Resistor enable */
sfrb(P7REN, P7REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORT8__)  ||  defined(__MSP430_HAS_PORT8_R__)
#define P8IN_               0x0039  /* Port 8 Input */
sfrb(P8IN, P8IN_);
#define P8OUT_              0x003B  /* Port 8 Output */
sfrb(P8OUT, P8OUT_);
#define P8DIR_              0x003D  /* Port 8 Direction */
sfrb(P8DIR, P8DIR_);
#define P8SEL_              0x003F  /* Port 8 Selection */
sfrb(P8SEL, P8SEL_);
#if defined(__MSP430_HAS_PORT8_R__)
#define P8REN_              0x0015  /* Port 8 Resistor enable */
sfrb(P8REN, P8REN_);
#endif
#endif

#if defined(__MSP430_HAS_PORTA__)
#define PAIN_               0x0038  /* Port A Input */
sfrb(PAIN, PAIN_);
#endif

#if defined(__MSP430_HAS_PORT9__) ||  defined(__MSP430_HAS_PORT9_R__)
#define P9IN_               0x0008  /* Port 9 Input */
sfrb(P9IN, P9IN_);
#define P9OUT_              0x000A  /* Port 9 Output */
sfrb(P9OUT, P9OUT_);
#define P9DIR_              0x000C  /* Port 9 Direction */
sfrb(P9DIR, P9DIR_);
#define P9SEL_              0x000E  /* Port 9 Selection */
sfrb(P9SEL, P9SEL_);

#if defined(__MSP430_HAS_PORT9_R__)
#define P9REN_              0x0016  /* Port 9 Resistor enable */
sfrb(P9REN, P9REN_);
#endif

#endif

#if defined(__MSP430_HAS_PORT10__) ||  defined(__MSP430_HAS_PORT10_R__)
#define P10IN_              0x0009  /* Port 10 Input */
sfrb(P10IN, P10IN_);
#define P10OUT_             0x000B  /* Port 10 Output */
sfrb(P10OUT, P10OUT_);
#define P10DIR_             0x000D  /* Port 10 Direction */
sfrb(P10DIR, P10DIR_);
#define P10SEL_             0x000F  /* Port 10 Selection */
sfrb(P10SEL, P10SEL_);

#if defined(__MSP430_HAS_PORT10_R__)
#define P10REN_             0x0017  /* Port 10 Resistor enable */
sfrb(P10REN, P10REN_);
#endif

#endif

#if defined(__MSP430_HAS_PORTB__)
#define PBIN_               0x0008  /* Port B Input */
sfrb(PBIN, PBIN_);
#endif

#endif
