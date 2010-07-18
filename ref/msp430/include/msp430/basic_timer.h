#ifndef __msp430_headers_basic_timer_h
#define __msp430_headers_basic_timer_h

/* basic_timer.h
 *
 * mspgcc project: MSP430 device headers
 * BASIC_TIMER module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: basic_timer.h,v 1.5 2006/06/07 13:01:30 coppice Exp $
 */

/* Switches:
__MSP430_HAS_BT_RTC__ - if device has the enhanced BT with RTC

*/

#define BTCTL_              0x0040      /* Basic Timer Control */
sfrb(BTCTL, BTCTL_);

/* The bit names have been prefixed with "BT" */
#define BTIP0               0x01
#define BTIP1               0x02
#define BTIP2               0x04
#define BTFRFQ0             0x08
#define BTFRFQ1             0x10
#define BTDIV               0x20                        /* fCLK2 = ACLK:256 */
#define BTRESET             0x40                        /* BT is reset and BTIFG is reset if this bit is set */
#define BTHOLD              0x40                        /* BT1 is held if this bit is set */
#define BTSSEL              0x80                        /* fBT = fMCLK (main clock) */

#define BTCNT1_             0x0046      /* Basic Timer Count 1 */
sfrb(BTCNT1, BTCNT1_);
#define BTCNT2_             0x0047      /* Basic Timer Count 2 */
sfrb(BTCNT2, BTCNT2_);

/* Frequency of the BTCNT2 coded with Bit 5 and 7 in BTCTL */
#define BT_fCLK2_ACLK               0x00
#define BT_fCLK2_ACLK_DIV256        BTDIV
#define BT_fCLK2_MCLK               BTSSEL

/* Interrupt interval time fINT coded with Bits 0-2 in BTCTL */
#define BT_fCLK2_DIV2       0                           /* fINT = fCLK2:2 (default) */
#define BT_fCLK2_DIV4       (BTIP0)                     /* fINT = fCLK2:4 */
#define BT_fCLK2_DIV8       (BTIP1)                     /* fINT = fCLK2:8 */
#define BT_fCLK2_DIV16      (BTIP1|BTIP0)               /* fINT = fCLK2:16 */
#define BT_fCLK2_DIV32      (BTIP2)                     /* fINT = fCLK2:32 */
#define BT_fCLK2_DIV64      (BTIP2|BTIP0)               /* fINT = fCLK2:64 */
#define BT_fCLK2_DIV128     (BTIP2|BTIP1)               /* fINT = fCLK2:128 */
#define BT_fCLK2_DIV256     (BTIP2|BTIP1|BTIP0)         /* fINT = fCLK2:256 */
/* Frequency of LCD coded with Bits 3-4 */
#define BT_fLCD_DIV32       0                           /* fLCD = fACLK:32 (default) */
#define BT_fLCD_DIV64       (BTFRFQ0)                   /* fLCD = fACLK:64 */
#define BT_fLCD_DIV128      (BTFRFQ1)                   /* fLCD = fACLK:128 */
#define BT_fLCD_DIV256      (BTFRFQ1|BTFRFQ0)           /* fLCD = fACLK:256 */
/* LCD frequency values with fBT=fACLK */
#define BT_fLCD_1K          0                           /* fACLK:32 (default) */
#define BT_fLCD_512         (BTFRFQ0)                   /* fACLK:64 */
#define BT_fLCD_256         (BTFRFQ1)                   /* fACLK:128 */
#define BT_fLCD_128         (BTFRFQ1|BTFRFQ0)           /* fACLK:256 */
/* LCD frequency values with fBT=fMCLK */
#define BT_fLCD_31K         (BTSSEL)                    /* fMCLK:32 */
#define BT_fLCD_15_5K       (BTSSEL|BTFRFQ0)            /* fMCLK:64 */
#define BT_fLCD_7_8K        (BTSSEL|BTFRFQ1|BTFRFQ0)    /* fMCLK:256 */
/* With assumed values of fACLK=32KHz, fMCLK=1MHz */
/* fBT=fACLK is thought for longer interval times */
#define BT_ADLY_0_064       0                           /* 0.064ms interval (default) */
#define BT_ADLY_0_125       (BTIP0)                     /* 0.125ms    " */
#define BT_ADLY_0_25        (BTIP1)                     /* 0.25ms     " */
#define BT_ADLY_0_5         (BTIP1|BTIP0)               /* 0.5ms      " */
#define BT_ADLY_1           (BTIP2)                     /* 1ms        " */
#define BT_ADLY_2           (BTIP2|BTIP0)               /* 2ms        " */
#define BT_ADLY_4           (BTIP2|BTIP1)               /* 4ms        " */
#define BT_ADLY_8           (BTIP2|BTIP1|BTIP0)         /* 8ms        " */
#define BT_ADLY_16          (BTDIV)                     /* 16ms       " */
#define BT_ADLY_32          (BTDIV|BTIP0)               /* 32ms       " */
#define BT_ADLY_64          (BTDIV|BTIP1)               /* 64ms       " */
#define BT_ADLY_125         (BTDIV|BTIP1|BTIP0)         /* 125ms      " */
#define BT_ADLY_250         (BTDIV|BTIP2)               /* 250ms      " */
#define BT_ADLY_500         (BTDIV|BTIP2|BTIP0)         /* 500ms      " */
#define BT_ADLY_1000        (BTDIV|BTIP2|BTIP1)         /* 1000ms     " */
#define BT_ADLY_2000        (BTDIV|BTIP2|BTIP1|BTIP0)   /* 2000ms     " */
/* fCLK2=fMCLK (1MHz) is thought for short interval times */
/* the timing for short intervals is more precise than ACLK */
/* NOTE */
/* Be sure that the SCFQCTL-Register is set to 01Fh so that fMCLK=1MHz */
/* Too low interval times result in too interrupts more frequent than the
   processor can handle! */
#define BT_MDLY_0_002       (BTSSEL)                    /* 0.002ms interval       *** interval times */
#define BT_MDLY_0_004       (BTSSEL|BTIP0)              /* 0.004ms    "           *** too short for */
#define BT_MDLY_0_008       (BTSSEL|BTIP1)              /* 0.008ms    "           *** interrupt */
#define BT_MDLY_0_016       (BTSSEL|BTIP1|BTIP0)        /* 0.016ms    "           *** handling */
#define BT_MDLY_0_032       (BTSSEL|BTIP2)              /* 0.032ms    " */
#define BT_MDLY_0_064       (BTSSEL|BTIP2|BTIP0)        /* 0.064ms    " */
#define BT_MDLY_0_125       (BTSSEL|BTIP2|BTIP1)        /* 0.125ms    " */
#define BT_MDLY_0_25        (BTSSEL|BTIP2|BTIP1|BTIP0)  /* 0.25ms     " */

/* Reset/Hold coded with Bits 6-7 in BT(1)CTL */
/* this is for BT */
#define BTRESET_CNT1        (BTRESET)                   /* BTCNT1 is reset while BTRESET is set */
#define BTRESET_CNT1_2      (BTRESET|BTDIV)             /* BTCNT1 .AND. BTCNT2 are reset while ~ is set */
/* this is for BT1 */
#define BTHOLD_CNT1         (BTHOLD)                    /* BTCNT1 is held while BTHOLD is set */
#define BTHOLD_CNT1_2       (BTHOLD|BTDIV)              /* BT1CNT1 .AND. BT1CNT2 are held while ~ is set */

#if defined(__MSP430_HAS_BT_RTC__)

#define RTCCTL_             0x0041      /* Real Time Clock Control */
sfrb(RTCCTL, RTCCTL_);
#define RTCNT1_             0x0042      /* Real Time Counter 1 */
sfrb(RTCNT1, RTCNT1_);
#define RTCNT2_             0x0043      /* Real Time Counter 2 */
sfrb(RTCNT2, RTCNT2_);
#define RTCNT3_             0x0044      /* Real Time Counter 3 */
sfrb(RTCNT3, RTCNT3_);
#define RTCNT4_             0x0045      /* Real Time Counter 4 */
sfrb(RTCNT4, RTCNT4_);
#define RTCDAY_             0x004C      /* Real Time Clock Day */
sfrb(RTCDAY, RTCDAY_);
#define RTCMON_             0x004D      /* Real Time Clock Month */
sfrb(RTCMON, RTCMON_);
#define RTCYEARL_           0x004E      /* Real Time Clock Year (Low Byte) */
sfrb(RTCYEARL, RTCYEARL_);
#define RTCYEARH_           0x004F      /* Real Time Clock Year (High Byte) */
sfrb(RTCYEARH, RTCYEARH_);

#define RTCSEC              RTCNT1
#define RTCMIN              RTCNT2
#define RTCHOUR             RTCNT3
#define RTCDOW              RTCNT4

#define RTCTL_              0x0040      /* Basic/Real Timer Control */
sfrb(RTCTL, RTCTL_);

#define RTCTIM0             RTCNT12
#define RTCTIM1             RTCNT34

#define RTCBCD              (0x80)                      /* RTC BCD Select */
#define RTCHOLD             (0x40)                      /* RTC Hold */
#define RTCMODE1            (0x20)                      /* RTC Mode 1 */
#define RTCMODE0            (0x10)                      /* RTC Mode 0 */
#define RTCTEV1             (0x08)                      /* RTC Time Event 1 */
#define RTCTEV0             (0x04)                      /* RTC Time Event 0 */
#define RTCIE               (0x02)                      /* RTC Interrupt Enable */
#define RTCFG               (0x01)                      /* RTC Event Flag */

#define RTCTEV_0            (0<<2)                      /* RTC Time Event: 0 */
#define RTCTEV_1            (1<<2)                      /* RTC Time Event: 1 */
#define RTCTEV_2            (2<<2)                      /* RTC Time Event: 2 */
#define RTCTEV_3            (3<<2)                      /* RTC Time Event: 3 */
#define RTCMODE_0           (0<<4)                      /* RTC Mode: 0 */
#define RTCMODE_1           (1<<4)                      /* RTC Mode: 1 */
#define RTCMODE_2           (2<<4)                      /* RTC Mode: 2 */
#define RTCMODE_3           (3<<4)                      /* RTC Mode: 3 */

#endif

/* INTERRUPT CONTROL BITS */
/* #define BTIE                0x80 */
/* #define BTIFG               0x80 */
/* #define BTME                0x80 */

#endif
