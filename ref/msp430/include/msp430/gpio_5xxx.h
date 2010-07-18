#ifndef __MSP430_HEADERS_GPIO_5XXX_H
#define __MSP430_HEADERS_GPIO_5XXX_H

/* gpio_5xxx.h
 *
 * mspgcc project: MSP430 device headers
 * Digital I/O 
 *
 * (c) 2008  by Sergey A. Borshch <sb-sf@sourceforge.net>
 * Originally based in MSP430F543x datasheet (slas609)
 *    and MSP430x5xx Family User's Guide (slau208).
 *
 * $Id: gpio_5xxx.h,v 1.2 2008/12/07 23:00:38 sb-sf Exp $
 */

/* Switches:

__MSP430_PORT1_BASE__ - base address of PORT1 module. PORT1 present in device if defined 
__MSP430_PORT2_BASE__ - base address of PORT2 module. PORT2 present in device if defined
__MSP430_PORT3_BASE__ - base address of PORT3 module. PORT3 present in device if defined
__MSP430_PORT4_BASE__ - base address of PORT4 module. PORT4 present in device if defined
__MSP430_PORT5_BASE__ - base address of PORT5 module. PORT5 present in device if defined
__MSP430_PORT6_BASE__ - base address of PORT6 module. PORT6 present in device if defined
__MSP430_PORT7_BASE__ - base address of PORT7 module. PORT7 present in device if defined
__MSP430_PORT8_BASE__ - base address of PORT8 module. PORT8 present in device if defined
__MSP430_PORT9_BASE__ - base address of PORT9 module. PORT9 present in device if defined
__MSP430_PORT10_BASE__ - base address of PORT10 module. PORT10 present in device if defined
__MSP430_PORT11_BASE__ - base address of PORT11 module. PORT11 present in device if defined
__MSP430_PORTJ_BASE__ - base address of PORTJ module. PORTJ present in device if defined

*/

#if defined(__MSP430_PORT1_BASE__)
#define P1IN_               __MSP430_PORT1_BASE__ + 0x00    /* Port 1 Input */
sfrb(P1IN, P1IN_);
#define P1OUT_              __MSP430_PORT1_BASE__ + 0x02    /* Port 1 Output */
sfrb(P1OUT, P1OUT_);
#define P1DIR_              __MSP430_PORT1_BASE__ + 0x04    /* Port 1 Direction */
sfrb(P1DIR, P1DIR_);
#define P1REN_              __MSP430_PORT1_BASE__ + 0x06    /* Port 1 Resistor enable */
sfrb(P1REN, P1REN_);
#define P1DS_               __MSP430_PORT1_BASE__ + 0x08    /* Port 1 Drive strength */
sfrb(P1DS, P1DS_);
#define P1SEL_              __MSP430_PORT1_BASE__ + 0x0A    /* Port 1 Selection */
sfrb(P1SEL, P1SEL_);
#define P1IV_               __MSP430_PORT1_BASE__ + 0x0E    /* Port 1 Interrupt vector word */
sfrb(P1IV, P1IV_);
#define P1IES_              __MSP430_PORT1_BASE__ + 0x18    /* Port 1 Interrupt Edge Select */
sfrb(P1IES, P1IES_);
#define P1IE_               __MSP430_PORT1_BASE__ + 0x1A    /* Port 1 Interrupt Enable */
sfrb(P1IE, P1IE_);
#define P1IFG_              __MSP430_PORT1_BASE__ + 0x1C    /* Port 1 Interrupt Flag */
sfrb(P1IFG, P1IFG_);
#endif

#if defined(__MSP430_PORT2_BASE__)
#define P2IN_               __MSP430_PORT2_BASE__ + 0x01    /* Port 2 Input */
sfrb(P2IN, P2IN_);
#define P2OUT_              __MSP430_PORT2_BASE__ + 0x03    /* Port 2 Output */
sfrb(P2OUT, P2OUT_);
#define P2DIR_              __MSP430_PORT2_BASE__ + 0x05    /* Port 2 Direction */
sfrb(P2DIR, P2DIR_);
#define P2REN_              __MSP430_PORT2_BASE__ + 0x07    /* Port 2 Resistor enable */
sfrb(P2REN, P2REN_);
#define P2DS_               __MSP430_PORT2_BASE__ + 0x09    /* Port 2 Drive strength */
sfrb(P2DS, P2DS_);
#define P2SEL_              __MSP430_PORT2_BASE__ + 0x0B    /* Port 2 Selection */
sfrb(P2SEL, P2SEL_);
#define P2IV_               __MSP430_PORT2_BASE__ + 0x1E    /* Port 2 Interrupt vector word */
sfrb(P2IV, P2IV_);
#define P2IES_              __MSP430_PORT2_BASE__ + 0x19    /* Port 2 Interrupt Edge Select */
sfrb(P2IES, P2IES_);
#define P2IE_               __MSP430_PORT2_BASE__ + 0x1B    /* Port 2 Interrupt Enable */
sfrb(P2IE, P2IE_);
#define P2IFG_              __MSP430_PORT2_BASE__ + 0x1D    /* Port 2 Interrupt Flag */
sfrb(P2IFG, P2IFG_);
#endif

#if defined(__MSP430_PORT3_BASE__)
#define P3IN_               __MSP430_PORT3_BASE__ + 0x00    /* Port 3 Input */
sfrb(P3IN, P3IN_);
#define P3OUT_              __MSP430_PORT3_BASE__ + 0x02    /* Port 3 Output */
sfrb(P3OUT, P3OUT_);
#define P3DIR_              __MSP430_PORT3_BASE__ + 0x04    /* Port 3 Direction */
sfrb(P3DIR, P3DIR_);
#define P3REN_              __MSP430_PORT3_BASE__ + 0x06    /* Port 3 Resistor enable */
sfrb(P3REN, P3REN_);
#define P3DS_               __MSP430_PORT3_BASE__ + 0x08    /* Port 3 Drive strength */
sfrb(P3DS, P3DS_);
#define P3SEL_              __MSP430_PORT3_BASE__ + 0x0A    /* Port 3 Selection */
sfrb(P3SEL, P3SEL_);
#endif

#if defined(__MSP430_PORT4_BASE__)
#define P4IN_               __MSP430_PORT4_BASE__ + 0x01    /* Port 4 Input */
sfrb(P4IN, P4IN_);
#define P4OUT_              __MSP430_PORT4_BASE__ + 0x03    /* Port 4 Output */
sfrb(P4OUT, P4OUT_);
#define P4DIR_              __MSP430_PORT4_BASE__ + 0x05    /* Port 4 Direction */
sfrb(P4DIR, P4DIR_);
#define P4REN_              __MSP430_PORT4_BASE__ + 0x07    /* Port 4 Resistor enable */
sfrb(P4REN, P4REN_);
#define P4DS_               __MSP430_PORT4_BASE__ + 0x09    /* Port 4 Drive strength */
sfrb(P4DS, P4DS_);
#define P4SEL_              __MSP430_PORT4_BASE__ + 0x0B    /* Port 4 Selection */
sfrb(P4SEL, P4SEL_);
#endif

#if defined(__MSP430_PORT5_BASE__)
#define P5IN_               __MSP430_PORT5_BASE__ + 0x00    /* Port 5 Input */
sfrb(P5IN, P5IN_);
#define P5OUT_              __MSP430_PORT5_BASE__ + 0x02    /* Port 5 Output */
sfrb(P5OUT, P5OUT_);
#define P5DIR_              __MSP430_PORT5_BASE__ + 0x04    /* Port 5 Direction */
sfrb(P5DIR, P5DIR_);
#define P5REN_              __MSP430_PORT5_BASE__ + 0x06    /* Port 5 Resistor enable */
sfrb(P5REN, P5REN_);
#define P5DS_               __MSP430_PORT5_BASE__ + 0x08    /* Port 5 Drive strength */
sfrb(P5DS, P5DS_);
#define P5SEL_              __MSP430_PORT5_BASE__ + 0x0A    /* Port 5 Selection */
sfrb(P5SEL, P5SEL_);
#endif

#if defined(__MSP430_PORT6_BASE__)
#define P6IN_               __MSP430_PORT6_BASE__ + 0x01    /* Port 6 Input */
sfrb(P6IN, P6IN_);
#define P6OUT_              __MSP430_PORT6_BASE__ + 0x03    /* Port 6 Output */
sfrb(P6OUT, P6OUT_);
#define P6DIR_              __MSP430_PORT6_BASE__ + 0x05    /* Port 6 Direction */
sfrb(P6DIR, P6DIR_);
#define P6REN_              __MSP430_PORT6_BASE__ + 0x07    /* Port 6 Resistor enable */
sfrb(P6REN, P6REN_);
#define P6DS_               __MSP430_PORT6_BASE__ + 0x09    /* Port 6 Drive strength */
sfrb(P6DS, P6DS_);
#define P6SEL_              __MSP430_PORT6_BASE__ + 0x0B    /* Port 6 Selection */
sfrb(P6SEL, P6SEL_);
#endif

#if defined(__MSP430_PORT7_BASE__)
#define P7IN_               __MSP430_PORT7_BASE__ + 0x00    /* Port 7 Input */
sfrb(P7IN, P7IN_);
#define P7OUT_              __MSP430_PORT7_BASE__ + 0x02    /* Port 7 Output */
sfrb(P7OUT, P7OUT_);
#define P7DIR_              __MSP430_PORT7_BASE__ + 0x04    /* Port 7 Direction */
sfrb(P7DIR, P7DIR_);
#define P7REN_              __MSP430_PORT7_BASE__ + 0x06    /* Port 7 Resistor enable */
sfrb(P7REN, P7REN_);
#define P7DS_               __MSP430_PORT7_BASE__ + 0x08    /* Port 7 Drive strength */
sfrb(P7DS, P7DS_);
#define P7SEL_              __MSP430_PORT7_BASE__ + 0x0A    /* Port 7 Selection */
sfrb(P7SEL, P7SEL_);
#endif

#if defined(__MSP430_PORT8_BASE__)
#define P8IN_               __MSP430_PORT8_BASE__ + 0x01    /* Port 8 Input */
sfrb(P8IN, P8IN_);
#define P8OUT_              __MSP430_PORT8_BASE__ + 0x03    /* Port 8 Output */
sfrb(P8OUT, P8OUT_);
#define P8DIR_              __MSP430_PORT8_BASE__ + 0x05    /* Port 8 Direction */
sfrb(P8DIR, P8DIR_);
#define P8REN_              __MSP430_PORT8_BASE__ + 0x07    /* Port 8 Resistor enable */
sfrb(P8REN, P8REN_);
#define P8DS_               __MSP430_PORT8_BASE__ + 0x09    /* Port 8 Drive strength */
sfrb(P8DS, P8DS_);
#define P8SEL_              __MSP430_PORT8_BASE__ + 0x0B    /* Port 8 Selection */
sfrb(P8SEL, P8SEL_);
#endif

#if defined(__MSP430_PORT9_BASE__)
#define P9IN_               __MSP430_PORT9_BASE__ + 0x00    /* Port 9 Input */
sfrb(P9IN, P9IN_);
#define P9OUT_              __MSP430_PORT9_BASE__ + 0x02    /* Port 9 Output */
sfrb(P9OUT, P9OUT_);
#define P9DIR_              __MSP430_PORT9_BASE__ + 0x04    /* Port 9 Direction */
sfrb(P9DIR, P9DIR_);
#define P9REN_              __MSP430_PORT9_BASE__ + 0x06    /* Port 9 Resistor enable */
sfrb(P9REN, P9REN_);
#define P9DS_               __MSP430_PORT9_BASE__ + 0x08    /* Port 9 Drive strength */
sfrb(P9DS, P9DS_);
#define P9SEL_              __MSP430_PORT9_BASE__ + 0x0A    /* Port 9 Selection */
sfrb(P9SEL, P9SEL_);
#endif

#if defined(__MSP430_PORT10_BASE__)
#define P10IN_               __MSP430_PORT10_BASE__ + 0x01  /* Port 10 Input */
sfrb(P10IN, P10IN_);
#define P10OUT_              __MSP430_PORT10_BASE__ + 0x03  /* Port 10 Output */
sfrb(P10OUT, P10OUT_);
#define P10DIR_              __MSP430_PORT10_BASE__ + 0x05  /* Port 10 Direction */
sfrb(P10DIR, P10DIR_);
#define P10REN_              __MSP430_PORT10_BASE__ + 0x07  /* Port 10 Resistor enable */
sfrb(P10REN, P10REN_);
#define P10DS_               __MSP430_PORT10_BASE__ + 0x09  /* Port 10 Drive strength */
sfrb(P10DS, P10DS_);
#define P10SEL_              __MSP430_PORT10_BASE__ + 0x0B  /* Port 10 Selection */
sfrb(P10SEL, P10SEL_);
#endif

#if defined(__MSP430_PORT11_BASE__)
#define P11IN_               __MSP430_PORT11_BASE__ + 0x00  /* Port 11 Input */
sfrb(P11IN, P11IN_);
#define P11OUT_              __MSP430_PORT11_BASE__ + 0x02  /* Port 11 Output */
sfrb(P11OUT, P11OUT_);
#define P11DIR_              __MSP430_PORT11_BASE__ + 0x04  /* Port 11 Direction */
sfrb(P11DIR, P11DIR_);
#define P11REN_              __MSP430_PORT11_BASE__ + 0x06  /* Port 11 Resistor enable */
sfrb(P11REN, P11REN_);
#define P11DS_               __MSP430_PORT11_BASE__ + 0x08  /* Port 11 Drive strength */
sfrb(P11DS, P11DS_);
#define P11SEL_              __MSP430_PORT11_BASE__ + 0x0A  /* Port 11 Selection */
sfrb(P11SEL, P11SEL_);
#endif

#if defined(__MSP430_PORTJ_BASE__)
#define PJIN_               __MSP430_PORTJ_BASE__ + 0x00    /* Port J Input */
sfrb(PJIN, PJIN_);
#define PJOUT_              __MSP430_PORTJ_BASE__ + 0x02    /* Port J Output */
sfrb(PJOUT, PJOUT_);
#define PJDIR_              __MSP430_PORTJ_BASE__ + 0x04    /* Port J Direction */
sfrb(PJDIR, PJDIR_);
#define PJREN_              __MSP430_PORTJ_BASE__ + 0x06    /* Port J Resistor enable */
sfrb(PJREN, PJREN_);
#define PJDS_               __MSP430_PORTJ_BASE__ + 0x08    /* Port J Drive strength */
sfrb(PJDS, PJDS_);
#endif

#endif  /* __MSP430_HEADERS_GPIO5_XXX_H */
