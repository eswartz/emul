#ifndef __MSP430_HEADERS_WDT_A_H
#define __MSP430_HEADERS_WDT_A_H

/* wdt_a.h
 *
 * mspgcc project: MSP430 device headers
 * watchdog timer module
 *
 * (c) 2008 by Sergey A. Borshch <sb-sf@users.sf.net>
 * Originally based in MSP430F543x datasheet (slas609)
 *    and MSP430x5xx Family User's Guide (slau208).
 *
 * $Id: wdt_a.h,v 1.6 2009/02/28 12:14:53 sb-sf Exp $
 */

/* Switches:

__MSP430_WDT_A_BASE__ - base address of WDT_A module

*/

#define WDTCTL_             __MSP430_WDT_A_BASE__ + 0x0C  /* Watchdog timer control register */
sfrw(WDTCTL, WDTCTL_);

/* WDTCTL */
#define WDTPW               (0x5A<<8)   /* Watchdog timer password. Always read as 069h. Must be written as 05Ah, or a PUC will be generated */
#define WDTHOLD             (1<<7)      /* Watchdog timer hold */
#define WDTSSEL1            (1<<6)      /* Watchdog timer clock source select */
#define WDTSSEL0            (1<<5)      /* Watchdog timer clock source select */
#define WDTTMSEL            (1<<4)      /* Watchdog timer mode select */
#define WDTCNTCL            (1<<3)      /* Watchdog timer counter clear */
#define WDTIS2              (1<<2)      /* Watchdog timer interval select */
#define WDTIS1              (1<<1)      /* Watchdog timer interval select */
#define WDTIS0              (1<<0)      /* Watchdog timer interval select */

/* Aliases by mspgcc */
#define WDTIS_0             (0<<0)      /* Watchdog timer /2G */
#define WDTIS_1             (1<<0)      /* Watchdog timer /128M */
#define WDTIS_2             (2<<0)      /* Watchdog timer /8192K */
#define WDTIS_3             (3<<0)      /* Watchdog timer /512K */
#define WDTIS_4             (4<<0)      /* Watchdog timer /32K */
#define WDTIS_5             (5<<0)      /* Watchdog timer /8192 */
#define WDTIS_6             (6<<0)      /* Watchdog timer /512 */
#define WDTIS_7             (7<<0)      /* Watchdog timer /64 */

#define WDTSSEL_0           (0<<5)      /* Watchdog clock SMCLK */
#define WDTSSEL_1           (1<<5)      /* Watchdog clock ACLK */
#define WDTSSEL_2           (2<<5)      /* Watchdog clock VLOCLK */
#define WDTSSEL_3           (3<<5)      /* Watchdog clock X_CLK */

/* WDT-interval times [1ms] coded with Bits 0-2 */
/* WDT is clocked by fMCLK (assumed 1MHz) */
#define WDT_SMDLY_2147S     (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_0)         /* 2147s   */
#define WDT_SMDLY_134S      (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_1)         /* 134s    */
#define WDT_SMDLY_8S        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_2)         /* 8.38s   */
#define WDT_SMDLY_500MS     (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_3)         /* 524ms   */
#define WDT_SMDLY_32        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_4)         /* 32ms interval (default) */
#define WDT_SMDLY_8         (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_5)         /* 8ms     */
#define WDT_SMDLY_0_5       (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_6)         /* 0.5ms   */
#define WDT_SMDLY_0_064     (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_0|WDTIS_7)         /* 0.064ms */
/* WDT is clocked by fACLK (assumed 32KHz) */
#define WDT_ADLY_65536S     (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_0)         /* 65536s  */
#define WDT_ADLY_4096S      (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_1)         /* 4096s   */
#define WDT_ADLY_256S       (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_2)         /* 256s    */
#define WDT_ADLY_16S        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_3)         /* 16s     */
#define WDT_ADLY_1000       (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_4)         /* 1000ms  */
#define WDT_ADLY_250        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_5)         /* 250ms   */
#define WDT_ADLY_16         (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_6)         /* 16ms    */
#define WDT_ADLY_1_9        (WDTPW|WDTTMSEL|WDTCNTCL|WDTSSEL_1|WDTIS_7)         /* 1.9ms   */
/* Watchdog mode -> reset after expired time */
/* WDT is clocked by fSMCLK (assumed 1MHz) */
#define WDT_SMRST_2147S     (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_0)                  /* 2147s   */
#define WDT_SMRST_134S      (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_1)                  /* 134s    */
#define WDT_SMRST_8S        (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_2)                  /* 8.38s   */
#define WDT_SMRST_500MS     (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_3)                  /* 524ms   */
#define WDT_SMRST_32        (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_4)                  /* 32ms interval (default) */
#define WDT_SMRST_8         (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_5)                  /* 8ms     */
#define WDT_SMRST_0_5       (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_6)                  /* 0.5ms   */
#define WDT_SMRST_0_064     (WDTPW|WDTCNTCL|WDTSSEL_0|WDTIS_7)                  /* 0.064ms */
/* WDT is clocked by fACLK (assumed 32KHz) */
#define WDT_ARST_65536S     (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_0)                  /* 65536s  */
#define WDT_ARST_4096S      (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_1)                  /* 4096s   */
#define WDT_ARST_256S       (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_2)                  /* 256s    */
#define WDT_ARST_16S        (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_3)                  /* 16s     */
#define WDT_ARST_1000       (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_4)                  /* 1s      */
#define WDT_ARST_250        (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_5)                  /* 250ms   */
#define WDT_ARST_16         (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_6)                  /* 16ms    */
#define WDT_ARST_1_9        (WDTPW|WDTCNTCL|WDTSSEL_1|WDTIS_7)                  /* 1.9ms   */

#ifndef __ASSEMBLER__
/* Structured declaration */

#undef  __xstr
#undef  __str
#define __xstr(x)     __str(x)
#define __str(x)      #x
struct
{
    volatile unsigned
    IS0:3,
    CNTCL:1,
    TMSEL:1,
    SSEL:2,
    HOLD:1,
    PW:8
} const WDTCTL_bits asm(__xstr(__MSP430_WDT_A_BASE__ + 0x0C));  /* Watchdog timer control register */

#endif  /* __ASSEMBLER__ */

#endif /* __MSP430_HEADERS_SYS_H */
