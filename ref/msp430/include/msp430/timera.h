#if !defined(__msp430_headers_timera_h__)
#define __msp430_headers_timera_h__

/* timera.h
 *
 * mspgcc project: MSP430 device headers
 * TIMERA module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * 2009-05-19 - modifications by S. Balling <praktikum@innoventis.de>
 * - added T0A5
 * - added T1A3
 *
 * $Id: timera.h,v 1.15 2009/06/04 21:55:18 cliechti Exp $
 */

/* Switches: 

__MSP430_HAS_TA2__  - if the device has a timer0 A with 2 channels
__MSP430_HAS_TA3__  - if the device has a timer0 A with 3 channels
__MSP430_HAS_T1A2__ - if the device has a timer1 A with 2 channels, as well as timer0 A
__MSP430_HAS_T1A5__ - if the device has a timer1 A with 5 channels, as well as timer0 A
__MSP430_HAS_T0A5__ - if the device has a timer0 A with 5 channels
__MSP430_HAS_T1A3__ - if the device has a timer1 A with 3 channels

*/

#if defined(__MSP430_HAS_T0A5__)
#define TA0CTL_             0x0340
sfrw (TA0CTL,TA0CTL_);
#define TA0CTL_L_           0x0340
sfrb (TA0CTL_L,TA0CTL_L_);
#define TA0CTL_H_           0x0341
sfrb (TA0CTL_H,TA0CTL_H_);
#define TA0CCTL0_           0x0342
sfrw (TA0CCTL0,TA0CCTL0_);
#define TA0CCTL0_L_         0x0342
sfrb (TA0CCTL0_L,TA0CCTL0_L_);
#define TA0CCTL0_H_         0x0343
sfrb (TA0CCTL0_H,TA0CCTL0_H_);
#define TA0CCTL1_           0x0344
sfrw (TA0CCTL1,TA0CCTL1_);
#define TA0CCTL1_L_         0x0344
sfrb (TA0CCTL1_L,TA0CCTL1_L_);
#define TA0CCTL1_H_         0x0345
sfrb (TA0CCTL1_H,TA0CCTL1_H_);
#define TA0CCTL2_           0x0346
sfrw (TA0CCTL2,TA0CCTL2_);
#define TA0CCTL2_L_         0x0346
sfrb (TA0CCTL2_L,TA0CCTL2_L_);
#define TA0CCTL2_H_         0x0347
sfrb (TA0CCTL2_H,TA0CCTL2_H_);
#define TA0CCTL3_           0x0348
sfrw (TA0CCTL3,TA0CCTL3_);
#define TA0CCTL3_L_         0x0348
sfrb (TA0CCTL3_L,TA0CCTL3_L_);
#define TA0CCTL3_H_         0x0349
sfrb (TA0CCTL3_H,TA0CCTL3_H_);
#define TA0CCTL4_           0x034A
sfrw (TA0CCTL4,TA0CCTL4_);
#define TA0CCTL4_L_         0x034A
sfrb (TA0CCTL4_L,TA0CCTL4_L_);
#define TA0CCTL4_H_         0x034B
sfrb (TA0CCTL4_H,TA0CCTL4_H_);
#define TA0R_               0x0350
sfrw (TA0R,TA0R_);
#define TA0R_L_             0x0350
sfrb (TA0R_L,TA0R_L_);
#define TA0R_H_             0x0351
sfrb (TA0R_H,TA0R_H_);
#define TA0CCR0_            0x0352
sfrw (TA0CCR0,TA0CCR0_);
#define TA0CCR0_L_          0x0352
sfrb (TA0CCR0_L,TA0CCR0_L_);
#define TA0CCR0_H_          0x0353
sfrb (TA0CCR0_H,TA0CCR0_H_);
#define TA0CCR1_            0x0354
sfrw (TA0CCR1,TA0CCR1_);
#define TA0CCR1_L_          0x0354
sfrb (TA0CCR1_L,TA0CCR1_L_);
#define TA0CCR1_H_          0x0355
sfrb (TA0CCR1_H,TA0CCR1_H_);
#define TA0CCR2_            0x0356
sfrw (TA0CCR2,TA0CCR2_);
#define TA0CCR2_L_          0x0356
sfrb (TA0CCR2_L,TA0CCR2_L_);
#define TA0CCR2_H_          0x0357
sfrb (TA0CCR2_H,TA0CCR2_H_);
#define TA0CCR3_            0x0358
sfrw (TA0CCR3,TA0CCR3_);
#define TA0CCR3_L_          0x0358
sfrb (TA0CCR3_L,TA0CCR3_L_);
#define TA0CCR3_H_          0x0359
sfrb (TA0CCR3_H,TA0CCR3_H_);
#define TA0CCR4_            0x035A
sfrw (TA0CCR4,TA0CCR4_);
#define TA0CCR4_L_          0x035A
sfrb (TA0CCR4_L,TA0CCR4_L_);
#define TA0CCR4_H_          0x035B
sfrb (TA0CCR4_H,TA0CCR4_H_);
#define TA0IV_              0x036E
sfrw (TA0IV,TA0IV_);
#define TA0IV_L_            0x036E
sfrb (TA0IV_L,TA0IV_L_);
#define TA0IV_H_            0x036F
sfrb (TA0IV_H,TA0IV_H_);
#define TA0EX0_             0x0360
sfrw (TA0EX0,TA0EX0_);
#define TA0EX0_L_           0x0360
sfrb (TA0EX0_L,TA0EX0_L_);
#define TA0EX0_H_           0x0361
sfrb (TA0EX0_H,TA0EX0_H_);
#endif /* __MSP430_HAS_T0A5__ */

#if defined(__MSP430_HAS_T1A3__)
#define TA1CTL_             0x0380
sfrw (TA1CTL,TA1CTL_);
#define TA1CTL_L_           0x0380
sfrb (TA1CTL_L,TA1CTL_L_);
#define TA1CTL_H_           0x0381
sfrb (TA1CTL_H,TA1CTL_H_);
#define TA1CCTL0_           0x0382
sfrw (TA1CCTL0,TA1CCTL0_);
#define TA1CCTL0_L_         0x0382
sfrb (TA1CCTL0_L,TA1CCTL0_L_);
#define TA1CCTL0_H_         0x0383
sfrb (TA1CCTL0_H,TA1CCTL0_H_);
#define TA1CCTL1_           0x0384
sfrw (TA1CCTL1,TA1CCTL1_);
#define TA1CCTL1_L_         0x0384
sfrb (TA1CCTL1_L,TA1CCTL1_L_);
#define TA1CCTL1_H_         0x0385
sfrb (TA1CCTL1_H,TA1CCTL1_H_);
#define TA1CCTL2_           0x0386
sfrw (TA1CCTL2,TA1CCTL2_);
#define TA1CCTL2_L_         0x0386
sfrb (TA1CCTL2_L,TA1CCTL2_L_);
#define TA1CCTL2_H_         0x0387
sfrb (TA1CCTL2_H,TA1CCTL2_H_);
#define TA1R_               0x0390
sfrw (TA1R,TA1R_);
#define TA1R_L_             0x0390
sfrb (TA1R_L,TA1R_L_);
#define TA1R_H_             0x0391
sfrb (TA1R_H,TA1R_H_);
#define TA1CCR0_            0x0392
sfrw (TA1CCR0,TA1CCR0_);
#define TA1CCR0_L_          0x0392
sfrb (TA1CCR0_L,TA1CCR0_L_);
#define TA1CCR0_H_          0x0393
sfrb (TA1CCR0_H,TA1CCR0_H_);
#define TA1CCR1_            0x0394
sfrw (TA1CCR1,TA1CCR1_);
#define TA1CCR1_L_          0x0394
sfrb (TA1CCR1_L,TA1CCR1_L_);
#define TA1CCR1_H_          0x0395
sfrb (TA1CCR1_H,TA1CCR1_H_);
#define TA1CCR2_            0x0396
sfrw (TA1CCR2,TA1CCR2_);
#define TA1CCR2_L_          0x0396
sfrb (TA1CCR2_L,TA1CCR2_L_);
#define TA1CCR2_H_          0x0397
sfrb (TA1CCR2_H,TA1CCR2_H_);
#define TA1IV_              0x03AE
sfrw (TA1IV,TA1IV_);
#define TA1IV_L_            0x03AE
sfrb (TA1IV_L,TA1IV_L_);
#define TA1IV_H_            0x03AF
sfrb (TA1IV_H,TA1IV_H_);
#define TA1EX0_             0x03A0
sfrw (TA1EX0,TA1EX0_);
#define TA1EX0_L_           0x03A0
sfrb (TA1EX0_L,TA1EX0_L_);
#define TA1EX0_H_           0x03A1
sfrb (TA1EX0_H,TA1EX0_H_);
#endif /* __MSP430_HAS_T1A3__ */

#if defined(__MSP430_HAS_TA2__)  ||  defined(__MSP430_HAS_TA3__)
#define TA0IV_              0x012E  /* Timer A 0 Interrupt Vector Word */
sfrw (TA0IV,TA0IV_);
#define TA0CTL_             0x0160  /* Timer A 0 Control */
sfrw (TA0CTL,TA0CTL_);
#define TA0R_               0x0170  /* Timer A 0 */
sfrw (TA0R,TA0R_);

#define TA0CCTL0_           0x0162  /* Timer A 0 Capture/Compare Control 0 */
sfrw (TA0CCTL0,TA0CCTL0_);
#define TA0CCTL1_           0x0164  /* Timer A 0 Capture/Compare Control 1 */
sfrw (TA0CCTL1,TA0CCTL1_);
#define TA0CCR0_            0x0172  /* Timer A 0 Capture/Compare 0 */
sfrw (TA0CCR0,TA0CCR0_);
#define TA0CCR1_            0x0174  /* Timer A 0 Capture/Compare 1 */
sfrw (TA0CCR1,TA0CCR1_);

/* Alternate register names */
#define TAIV                TA0IV
#define TAIV_               TA0IV_
#define TACTL               TA0CTL
#define TACTL_              TA0CTL_
#define TAR                 TA0R
#define TAR_                TA0R_

#define TACCTL0             TA0CCTL0
#define TACCTL0_            TA0CCTL0_
#define TACCTL1             TA0CCTL1
#define TACCTL1_            TA0CCTL1_
#define TACCR0              TA0CCR0
#define TACCR0_             TA0CCR0_
#define TACCR1              TA0CCR1
#define TACCR1_             TA0CCR1_

/* Further alternate register names */
#define CCTL0               TA0CCTL0
#define CCTL0_              TA0CCTL0_
#define CCTL1               TA0CCTL1
#define CCTL1_              TA0CCTL1_
#define CCR0                TA0CCR0
#define CCR0_               TA0CCR0_
#define CCR1                TA0CCR1
#define CCR1_               TA0CCR1_
#endif

#if defined(__MSP430_HAS_TA3__)
#define TA0CCTL2_           0x0166  /* Timer A 0 Capture/Compare Control 2 */
sfrw (TA0CCTL2,TA0CCTL2_);
#define TA0CCR2_            0x0176  /* Timer A 0 Capture/Compare 2 */
sfrw (TA0CCR2,TA0CCR2_);

/* Alternate register names */
#define TACCTL2             TA0CCTL2
#define TACCTL2_            TA0CCTL2_
#define TACCR2              TA0CCR2
#define TACCR2_             TA0CCR2_

/* Further alternate register names */
#define CCTL2               TA0CCTL2
#define CCTL2_              TA0CCTL2_
#define CCR2                TA0CCR2
#define CCR2_               TA0CCR2_
#endif

#if defined(__MSP430_HAS_T1A2__) ||  defined(__MSP430_HAS_T1A5__)
#define TA1IV_              0x011E  /* Timer A 1 Interrupt Vector Word */
sfrw (TA1IV, TA1IV_);
#define TA1CTL_             0x0180  /* Timer A 1 Control */
sfrw (TA1CTL, TA1CTL_);
#define TA1CCTL0_           0x0182  /* Timer A 1 Capture/Compare Control 0 */
sfrw (TA1CCTL0, TA1CCTL0_);
#define TA1CCTL1_           0x0184  /* Timer A 1 Capture/Compare Control 1 */
sfrw (TA1CCTL1, TA1CCTL1_);
#if defined(__MSP430_HAS_T1A5__)
#define TA1CCTL2_           0x0186  /* Timer A 1 Capture/Compare Control 2 */
sfrw (TA1CCTL2, TA1CCTL2_);
#define TA1CCTL3_           0x0188  /* Timer A 1 Capture/Compare Control 3 */
sfrw (TA1CCTL3, TA1CCTL3_);
#define TA1CCTL4_           0x018A  /* Timer A 1 Capture/Compare Control 4 */
sfrw (TA1CCTL4, TA1CCTL4_);
#endif
#define TAR1_               0x0190  /* Timer A 1 */
sfrw (TAR1, TAR1_);
#define TA1CCR0_            0x0192  /* Timer A 1 Capture/Compare 0 */
sfrw (TA1CCR0, TA1CCR0_);
#define TA1CCR1_            0x0194  /* Timer A 1 Capture/Compare 1 */
sfrw (TA1CCR1, TA1CCR1_);
#if defined(__MSP430_HAS_T1A5__)
#define TA1CCR2_            0x0196  /* Timer A 1 Capture/Compare 2 */
sfrw (TA1CCR2, TA1CCR2_);
#define TA1CCR3_            0x0198  /* Timer A 1 Capture/Compare 3 */
sfrw (TA1CCR3, TA1CCR3_);
#define TA1CCR4_            0x019A  /* Timer A 1 Capture/Compare 4 */
sfrw (TA1CCR4, TA1CCR4_);
#endif
#endif

#if !defined(__ASSEMBLER__)
/* Structured declaration */
typedef struct {
  volatile unsigned
    taifg:1,
    taie:1,
    taclr:1,
    dummy:1,
    tamc:2,
    taid:2,
    tassel:2;
} __attribute__ ((packed)) tactl_t;

typedef struct {
  volatile unsigned
    ccifg:1,
    cov:1,
    out:1,
    cci:1,
    ccie:1,
    outmod:3,
    cap:1,
    dummy:1,
    scci:1,
    scs:1,
    ccis:2,
    cm:2;
} __attribute__ ((packed)) tacctl_t;

#if defined(__MSP430_HAS_TA2__) ||  defined(__MSP430_HAS_TA3__)
/* The timer A declaration itself */
struct timera_t {
  tactl_t ctl;
  tacctl_t cctl0;
  tacctl_t cctl1;
#if defined(__MSP430_HAS_TA3__)
  tacctl_t cctl2;
#else
  volatile unsigned dummy1[1];   /* Pad to the next group of registers */
#endif
  volatile unsigned dummy2[4];   /* Pad to the next group of registers */
  volatile unsigned tar;
  volatile unsigned taccr0;
  volatile unsigned taccr1;
#if defined(__MSP430_HAS_TA3__)
  volatile unsigned taccr2;
#endif
};
#ifdef __cplusplus
extern "C" struct timera_t timera asm("0x0160");
#else //__cplusplus
struct timera_t timera asm("0x0160");
#endif //__cplusplus
#endif  // __MSP430_HAS_TA2__ || __MSP430_HAS_TA3__

#if defined(__MSP430_HAS_T1A2__) ||  defined(__MSP430_HAS_T1A5__)
/* The timer A1 declaration itself */
struct timera1_t {
  tactl_t ctl;
  tacctl_t cctl0;
  tacctl_t cctl1;
#if defined(__MSP430_HAS_T1A5__)
  tacctl_t cctl2;
  tacctl_t cctl3;
  tacctl_t cctl4;
#else
  volatile unsigned dummy1[3];   /* Pad to the next group of registers */
#endif
  volatile unsigned dummy2[2];   /* Pad to the next group of registers */
  volatile unsigned tar;
  volatile unsigned taccr0;
  volatile unsigned taccr1;
#if defined(__MSP430_HAS_T1A5__)
  volatile unsigned taccr2;
  volatile unsigned taccr3;
  volatile unsigned taccr4;
#endif
};
#ifdef __cplusplus
extern "C" struct timera1_t timera1 asm("0x0180");
#else //__cplusplus
struct timera1_t timera1 asm("0x0180");
#endif //__cplusplus

#endif
#endif

#define TASSEL2             0x0400  /* unused */        /* to distinguish from UART SSELx */
#define TASSEL1             0x0200  /* Timer A clock source select 1 */
#define TASSEL0             0x0100  /* Timer A clock source select 0 */
#define ID1                 0x0080  /* Timer A clock input divider 1 */
#define ID0                 0x0040  /* Timer A clock input divider 0 */
#define MC1                 0x0020  /* Timer A mode control 1 */
#define MC0                 0x0010  /* Timer A mode control 0 */
#define TACLR               0x0004  /* Timer A counter clear */
#define TAIE                0x0002  /* Timer A counter interrupt enable */
#define TAIFG               0x0001  /* Timer A counter interrupt flag */

#define MC_0                (0<<4)  /* Timer A mode control: 0 - Stop */
#define MC_1                (1<<4)  /* Timer A mode control: 1 - Up to CCR0 */
#define MC_2                (2<<4)  /* Timer A mode control: 2 - Continous up */
#define MC_3                (3<<4)  /* Timer A mode control: 3 - Up/Down */
#define ID_0                (0<<6)  /* Timer A input divider: 0 - /1 */
#define ID_1                (1<<6)  /* Timer A input divider: 1 - /2 */
#define ID_2                (2<<6)  /* Timer A input divider: 2 - /4 */
#define ID_3                (3<<6)  /* Timer A input divider: 3 - /8 */
#define TASSEL_0            (0<<8)  /* Timer A clock source select: 0 - TACLK */
#define TASSEL_1            (1<<8)  /* Timer A clock source select: 1 - ACLK  */
#define TASSEL_2            (2<<8)  /* Timer A clock source select: 2 - SMCLK */
#define TASSEL_3            (3<<8)  /* Timer A clock source select: 3 - INCLK */

#define CM1                 0x8000  /* Capture mode 1 */
#define CM0                 0x4000  /* Capture mode 0 */
#define CCIS1               0x2000  /* Capture input select 1 */
#define CCIS0               0x1000  /* Capture input select 0 */
#define SCS                 0x0800  /* Capture sychronize */
#define SCCI                0x0400  /* Latched capture signal (read) */
#define CAP                 0x0100  /* Capture mode: 1 /Compare mode : 0 */
#define OUTMOD2             0x0080  /* Output mode 2 */
#define OUTMOD1             0x0040  /* Output mode 1 */
#define OUTMOD0             0x0020  /* Output mode 0 */
#define CCIE                0x0010  /* Capture/compare interrupt enable */
#define CCI                 0x0008  /* Capture input signal (read) */
#define OUT                 0x0004  /* PWM Output signal if output mode 0 */
#define COV                 0x0002  /* Capture/compare overflow flag */
#define CCIFG               0x0001  /* Capture/compare interrupt flag */

#define OUTMOD_0            (0<<5)  /* PWM output mode: 0 - output only */
#define OUTMOD_1            (1<<5)  /* PWM output mode: 1 - set */
#define OUTMOD_2            (2<<5)  /* PWM output mode: 2 - PWM toggle/reset */
#define OUTMOD_3            (3<<5)  /* PWM output mode: 3 - PWM set/reset */
#define OUTMOD_4            (4<<5)  /* PWM output mode: 4 - toggle */
#define OUTMOD_5            (5<<5)  /* PWM output mode: 5 - Reset */
#define OUTMOD_6            (6<<5)  /* PWM output mode: 6 - PWM toggle/set */
#define OUTMOD_7            (7<<5)  /* PWM output mode: 7 - PWM reset/set */
#define CCIS_0              (0<<12) /* Capture input select: 0 - CCIxA */
#define CCIS_1              (1<<12) /* Capture input select: 1 - CCIxB */
#define CCIS_2              (2<<12) /* Capture input select: 2 - GND */
#define CCIS_3              (3<<12) /* Capture input select: 3 - Vcc */
#define CM_0                (0<<14) /* Capture mode: 0 - disabled */
#define CM_1                (1<<14) /* Capture mode: 1 - pos. edge */
#define CM_2                (2<<14) /* Capture mode: 1 - neg. edge */
#define CM_3                (3<<14) /* Capture mode: 1 - both edges */

/* Aliases by mspgcc */
#define MC_STOP             MC_0
#define MC_UPTO_CCR0        MC_1
#define MC_CONT             MC_2
#define MC_UPDOWN           MC_3

#define ID_DIV1             ID_0
#define ID_DIV2             ID_1
#define ID_DIV4             ID_2
#define ID_DIV8             ID_3

#define TASSEL_TACLK        TASSEL_0
#define TASSEL_ACLK         TASSEL_1
#define TASSEL_SMCLK        TASSEL_2
#define TASSEL_INCLK        TASSEL_3

#define OUTMOD_OUT          OUTMOD_0
#define OUTMOD_SET          OUTMOD_1
#define OUTMOD_TOGGLE_RESET OUTMOD_2
#define OUTMOD_SET_RESET    OUTMOD_3
#define OUTMOD_TOGGLE       OUTMOD_4
#define OUTMOD_RESET        OUTMOD_5
#define OUTMOD_TOGGLE_SET   OUTMOD_6
#define OUTMOD_RESET_SET    OUTMOD_7

#define CM_DISABLE          CM_0
#define CM_POS              CM_1
#define CM_NEG              CM_2
#define CM_BOTH             CM_3

/* TimerA IV names */
#if defined(__MSP430_HAS_TA3__) || defined(__MSP430_HAS_TA2____)
    #define TAIV_NONE       0x00   /* No interrupt pending */
    #define TAIV_CCR1       0x02   /* Capture/compare 1 TACCR1 CCIFG Highest */
    #if defined(__MSP430_HAS_TA3__)
        #define TAIV_CCR2   0x04   /* Capture/compare 2 TACCR2 CCIFG */
    #endif /*__MSP430_HAS_TA3__*/
    #define TAIV_OVERFLOW   0x0A   /* Timer overflow TAIFG Lowest */
#endif /*__MSP430_HAS_TA3__ || __MSP430_HAS_TA2__*/

#endif
