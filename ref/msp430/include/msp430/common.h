
#ifndef __msp430_headers_common_h
#define __msp430_headers_common_h

/* common.h
 *
 * mspgcc project: MSP430 device headers
 * Common register definitions
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: common.h,v 1.6 2008/10/09 15:00:14 sb-sf Exp $
 */

/* Switches: none */

#define BIT0                0x0001
#define BIT1                0x0002
#define BIT2                0x0004
#define BIT3                0x0008
#define BIT4                0x0010
#define BIT5                0x0020
#define BIT6                0x0040
#define BIT7                0x0080
#define BIT8                0x0100
#define BIT9                0x0200
#define BITA                0x0400
#define BITB                0x0800
#define BITC                0x1000
#define BITD                0x2000
#define BITE                0x4000
#define BITF                0x8000

#define C                   0x0001
#define Z                   0x0002
#define N                   0x0004
#define V                   0x0100
#define GIE                 0x0008
#define CPUOFF              0x0010
#define OSCOFF              0x0020
#define SCG0                0x0040
#define SCG1                0x0080

#ifdef __ASSEMBLER__ /* Begin #defines for assembler */
#define LPM0                CPUOFF
#define LPM1                SCG0+CPUOFF
#define LPM2                SCG1+CPUOFF
#define LPM3                SCG1+SCG0+CPUOFF
#define LPM4                SCG1+SCG0+OSCOFF+CPUOFF
#else /* Begin #defines for C */
#define LPM0_bits           CPUOFF
#define LPM1_bits           SCG0+CPUOFF
#define LPM2_bits           SCG1+CPUOFF
#define LPM3_bits           SCG1+SCG0+CPUOFF
#define LPM4_bits           SCG1+SCG0+OSCOFF+CPUOFF

#define LPM0      _BIS_SR(LPM0_bits) /* Enter Low Power Mode 0 */
#define LPM0_EXIT _BIC_SR_IRQ(LPM0_bits) /* Exit Low Power Mode 0 */
#define LPM1      _BIS_SR(LPM1_bits) /* Enter Low Power Mode 1 */
#define LPM1_EXIT _BIC_SR_IRQ(LPM1_bits) /* Exit Low Power Mode 1 */
#define LPM2      _BIS_SR(LPM2_bits) /* Enter Low Power Mode 2 */
#define LPM2_EXIT _BIC_SR_IRQ(LPM2_bits) /* Exit Low Power Mode 2 */
#define LPM3      _BIS_SR(LPM3_bits) /* Enter Low Power Mode 3 */
#define LPM3_EXIT _BIC_SR_IRQ(LPM3_bits) /* Exit Low Power Mode 3 */
#define LPM4      _BIS_SR(LPM4_bits) /* Enter Low Power Mode 4 */
#define LPM4_EXIT _BIC_SR_IRQ(LPM4_bits) /* Exit Low Power Mode 4 */
#endif /* End #defines for C */

#define WDTCTL_             0x0120  /* Watchdog Timer Control */
sfrw (WDTCTL,WDTCTL_);
/* The bit names have been prefixed with "WDT" */
#define WDTIS0              0x0001
#define WDTIS1              0x0002
#define WDTSSEL             0x0004
#define WDTCNTCL            0x0008
#define WDTTMSEL            0x0010
#define WDTNMI              0x0020
#define WDTNMIES            0x0040
#define WDTHOLD             0x0080

#define WDTPW               0x5A00

/* WDT-interval times [1ms] coded with Bits 0-2 */
/* WDT is clocked by fMCLK (assumed 1MHz) */
#define WDT_MDLY_32         (WDTPW|WDTTMSEL|WDTCNTCL)                           /* 32ms interval (default) */
#define WDT_MDLY_8          (WDTPW|WDTTMSEL|WDTCNTCL|WDTIS0)                    /* 8ms     " */
#define WDT_MDLY_0_5        (WDTPW|WDTTMSEL|WDTCNTCL|WDTIS1)                    /* 0.5ms   " */
#define WDT_MDLY_0_064      (WDTPW|WDTTMSEL|WDTCNTCL|WDTIS1|WDTIS0)             /* 0.064ms " */
/* WDT is clocked by fACLK (assumed 32KHz) */
#define WDT_ADLY_1000       (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL)                   /* 1000ms  " */
#define WDT_ADLY_250        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL|WDTIS0)            /* 250ms   " */
#define WDT_ADLY_16         (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL|WDTIS1)            /* 16ms    " */
#define WDT_ADLY_1_9        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL|WDTIS1|WDTIS0)     /* 1.9ms   " */
/* Watchdog mode -> reset after expired time */
/* WDT is clocked by fMCLK (assumed 1MHz) */
#define WDT_MRST_32         (WDTPW|WDTCNTCL)                                    /* 32ms interval (default) */
#define WDT_MRST_8          (WDTPW|WDTCNTCL|WDTIS0)                             /* 8ms     " */
#define WDT_MRST_0_5        (WDTPW|WDTCNTCL|WDTIS1)                             /* 0.5ms   " */
#define WDT_MRST_0_064      (WDTPW|WDTCNTCL|WDTIS1|WDTIS0)                      /* 0.064ms " */
/* WDT is clocked by fACLK (assumed 32KHz) */
#define WDT_ARST_1000       (WDTPW|WDTCNTCL|WDTSSEL)                            /* 1000ms  " */
#define WDT_ARST_250        (WDTPW|WDTCNTCL|WDTSSEL|WDTIS0)                     /* 250ms   " */
#define WDT_ARST_16         (WDTPW|WDTCNTCL|WDTSSEL|WDTIS1)                     /* 16ms    " */
#define WDT_ARST_1_9        (WDTPW|WDTCNTCL|WDTSSEL|WDTIS1|WDTIS0)              /* 1.9ms   " */

/* INTERRUPT CONTROL */
/* These two bits are defined in the Special Function Registers */
/* #define WDTIE               0x01 */
/* #define WDTIFG              0x01 */

/* Aliases by mspgcc */
#define WDTIS_0              0x0000
#define WDTIS_1              0x0001
#define WDTIS_2              0x0002
#define WDTIS_3              0x0003


/* Backwards compatibility to older versions of the header files.
   Please consider using the new names.
 */
#ifdef __MSP430_HAS_PORT1__
    #define __msp430_have_port1
#endif

#ifdef __MSP430_HAS_PORT2__
    #define __msp430_have_port2
#endif

#ifdef __MSP430_HAS_PORT3__
    #define __msp430_have_port3
#endif

#ifdef __MSP430_HAS_PORT4__
    #define __msp430_have_port4
#endif

#ifdef __MSP430_HAS_PORT5__
    #define __msp430_have_port5
#endif

#ifdef __MSP430_HAS_PORT6__
    #define __msp430_have_port6
#endif

#ifdef __MSP430_HAS_UART1__
    #define __msp430_have_usart1
#endif

#ifdef __MSP430_HAS_TB7__
    #define __msp430_have_timerb7
#endif

#endif
