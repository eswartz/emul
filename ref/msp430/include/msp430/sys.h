#ifndef __MSP430_HEADERS_SYS_H
#define __MSP430_HEADERS_SYS_H

/* sys.h
 *
 * mspgcc project: MSP430 device headers
 * system control module
 *
 * (c) 2008 by Sergey A. Borshch <sb-sf@users.sf.net>
 * Originally based in MSP430F543x datasheet (slas609)
 *    and MSP430x5xx Family User's Guide (slau208).
 *
 * $Id: sys.h,v 1.3 2009/02/28 12:14:53 sb-sf Exp $
 */

/* Switches:

__MSP430_SYS_BASE__ - base address of SYS module

*/

#define SYSCTL_             __MSP430_SYS_BASE__ + 0x00  /* System control register */
sfrw(SYSCTL, SYSCTL_);
#define SYSCTL_L_           __MSP430_SYS_BASE__ + 0x00  /* low byte */
sfrb(SYSCTL_L, SYSCTL_L_);
#define SYSCTL_H_           __MSP430_SYS_BASE__ + 0x01  /* high byte */
sfrb(SYSCTL_H, SYSCTL_H_);

#define SYSBSLC_            __MSP430_SYS_BASE__ + 0x02  /* Bootstrap loader configuration register */
sfrw(SYSBSLC, SYSBSLC_);
#define SYSBSLC_L_          __MSP430_SYS_BASE__ + 0x02  /* low byte */
sfrb(SYSBSLC_L, SYSBSLC_L_);
#define SYSBSLC_H_          __MSP430_SYS_BASE__ + 0x03  /* high byte */
sfrb(SYSBSLC_H, SYSBSLC_H_);

#define SYSARB_             __MSP430_SYS_BASE__ + 0x04  /* Arbitration configuration register */
sfrw(SYSARB, SYSARB_);
#define SYSARB_L_           __MSP430_SYS_BASE__ + 0x04  /* low byte */
sfrb(SYSARB_L, SYSARB_L_);
#define SYSARB_H_           __MSP430_SYS_BASE__ + 0x05  /* high byte */
sfrb(SYSARB_H, SYSARB_H_);

#define SYSJMBC_            __MSP430_SYS_BASE__ + 0x06  /* JTAG Mailbox control register */
sfrw(SYSJMBC, SYSJMBC_);
#define SYSJMBC_L_          __MSP430_SYS_BASE__ + 0x06  /* low byte */
sfrb(SYSJMBC_L, SYSJMBC_L_);
#define SYSJMBC_H_          __MSP430_SYS_BASE__ + 0x07  /* high byte */
sfrb(SYSJMBC_H, SYSJMBC_H_);

#define SYSJMBI0_           __MSP430_SYS_BASE__ + 0x08  /* JTAG Mailbox input register #0 */
sfrw(SYSJMBI0, SYSJMBI0_);
#define SYSJMBI0_L_         __MSP430_SYS_BASE__ + 0x08  /* low byte */
sfrb(SYSJMBI0_L, SYSJMBI0_L_);
#define SYSJMBI0_H_         __MSP430_SYS_BASE__ + 0x09  /* high byte */
sfrb(SYSJMBI0_H, SYSJMBI0_H_);

#define SYSJMBI1_           __MSP430_SYS_BASE__ + 0x0A  /* JTAG Mailbox input register #1 */
sfrw(SYSJMBI1, SYSJMBI1_);
#define SYSJMBI1_L_         __MSP430_SYS_BASE__ + 0x0A  /* low byte */
sfrb(SYSJMBI1_L, SYSJMBI1_L_);
#define SYSJMBI1_H_         __MSP430_SYS_BASE__ + 0x0B  /* high byte */
sfrb(SYSJMBI1_H, SYSJMBI1_H_);

#define SYSJMBO0_           __MSP430_SYS_BASE__ + 0x0C  /* JTAG Mailbox output register #0 */
sfrw(SYSJMBO0, SYSJMBO0_);
#define SYSJMBO0_L_         __MSP430_SYS_BASE__ + 0x0C  /* low byte */
sfrb(SYSJMBO0_L, SYSJMBO0_L_);
#define SYSJMBO0_H_         __MSP430_SYS_BASE__ + 0x0D  /* high byte */
sfrb(SYSJMBO0_H, SYSJMBO0_H_);

#define SYSJMBO1_           __MSP430_SYS_BASE__ + 0x0E  /* JTAG Mailbox output register #1 */
sfrw(SYSJMBO1, SYSJMBO1_);
#define SYSJMBO1_L_         __MSP430_SYS_BASE__ + 0x0E  /* low byte */
sfrb(SYSJMBO1_L, SYSJMBO1_L_);
#define SYSJMBO1_H_         __MSP430_SYS_BASE__ + 0x0F  /* high byte */
sfrb(SYSJMBO1_H, SYSJMBO1_H_);

#define SYSBERRIV_          __MSP430_SYS_BASE__ + 0x18  /* Bus error vector generator */
sfrw(SYSBERRIV, SYSBERRIV_);
#define SYSUNIV_            __MSP430_SYS_BASE__ + 0x1A  /* User NMI vector generator */
sfrw(SYSUNIV, SYSUNIV_);
#define SYSSNIV_            __MSP430_SYS_BASE__ + 0x1C  /* System NMI vector generator */
sfrw(SYSSNIV, SYSSNIV_);
#define SYSRSTIV_           __MSP430_SYS_BASE__ + 0x1E  /* System reset vector generator */
sfrw(SYSRSTIV, SYSRSTIV_);

/* SYSCTL, SYSCTL_L */
#define SYSJTAGPIN          (1<<5)  /* Dedicated JTAG pins enable */
#define SYSBSLIND           (1<<4)  /* TCK/RST entry BSL indication detected */
#define SYSPMMPE            (1<<2)  /* PMM access protect */
#define SYSRIVECT           (1<<0)  /* RAM based interrupt vectors */

/* SYSBSLC, SYSBSLC_L, SYSBSLC_H */
#define SYSBSLPE            (1<<15) /* BSL memory protection enable */
#define SYSBSLOFF           (1<<14) /* BSL memory disable for size covered in SYSBSLSIZE */
#define SYSBSLR             (1<<2)  /* RAM assigned to BSL */
#define SYSBSLSIZE1         (1<<1)  /* BSL size */
#define SYSBSLSIZE0         (1<<0)  /* BSL size */

#define SYSBSLSIZE_0        (0<<0)  /* size 512 bytes, BSL_SEG_3 */
#define SYSBSLSIZE_1        (1<<0)  /* size 1024 bytes, BSL_SEG_2,3 */
#define SYSBSLSIZE_2        (2<<0)  /* size 1536 bytes, BSL_SEG_1,2,3 */
#define SYSBSLSIZE_3        (3<<0)  /* size 2048 bytes, BSL_SEG_0,1,2,3 */

/* SYSJMBC, SYSJMBC_L */
#define JMBCLR1OFF          (1<<7)  /* Incomming JTAG mailbox 1 flag auto-clear disable */
#define MBCLR0OFF           (1<<6)  /* Incomming JTAG mailbox 0 flag auto-clear disable */
#define JMBMODE             (1<<4)  /* Operation mode of JMB for JMBI0/1 and JMBO0/1 */
#define JMBOUT1FG           (1<<3)  /* Outgoing JTAG mailbox 1 flag */
#define JMBOUT0FG           (1<<2)  /* Outgoing JTAG mailbox 0 flag */
#define JMBIN1FG            (1<<1)  /* Incoming JTAG mailbox 1 flag */
#define JMBIN0FG            (1<<0)  /* Incoming JTAG mailbox 0 flag */


#ifndef __ASSEMBLER__
/* Structured declaration */

#undef  __xstr
#undef  __str
#define __xstr(x)     __str(x)
#define __str(x)      #x

typedef struct
{
    union
    {
        volatile unsigned int CTL;              /* System control register */
        struct
        {
            volatile unsigned char CTL_L;       /* low byte */
            volatile unsigned char CTL_H;       /* high byte */
        };
    };
    union
    {
        volatile unsigned int BSLC;             /* Bootstrap loader configuration register */
        struct
        {
            volatile unsigned char BSLC_L;      /* low byte */
            volatile unsigned char BSLC_H;      /* high byte */
        };
    };
    union
    {
        volatile unsigned int ARB;              /* Arbitration configuration register */
        struct
        {
            volatile unsigned char ARB_L;       /* low byte */
            volatile unsigned char ARB_H;       /* high byte */
        };
    };
    union
    {
        volatile unsigned int JMBC;             /* JTAG Mailbox control register */
        struct
        {
            volatile unsigned char JMBC_L;      /* low byte */
            volatile unsigned char JMBC_H;      /* high byte */
        };
    };
    union
    {
        volatile unsigned int JMBI0;            /* JTAG Mailbox input register #0 */
        struct
        {
            volatile unsigned char JMBI0_L;     /* low byte */
            volatile unsigned char JMBI0_H;     /* high byte */
        };
    };
    union
    {
        volatile unsigned int JMBI1;            /* JTAG Mailbox input register #1 */
        struct
        {
            volatile unsigned char JMBI1_L;     /* low byte */
            volatile unsigned char JMBI1_H;     /* high byte */
        };
    };
    union
    {
        volatile unsigned int JMBO0;            /* JTAG Mailbox output register #0 */
        struct
        {
            volatile unsigned char JMBO0_L;     /* low byte */
            volatile unsigned char JMBO0_H;     /* high byte */
        };
    };
    union
    {
        volatile unsigned int JMBO1;            /* JTAG Mailbox output register #1 */
        struct
        {
            volatile unsigned char JMBO1_L;     /* low byte */
            volatile unsigned char JMBO1_H;     /* high byte */
        };
    };
    unsigned int dummy[4];
    volatile unsigned int BERRIV;               /* Bus error vector generator */
    volatile unsigned int UNIV;                 /* User NMI vector generator */
    volatile unsigned int SNIV;                 /* System NMI vector generator */
    volatile unsigned int RSTIV;                /* System reset vector generator */
} sys_t;
sys_t SYS asm(__xstr(__MSP430_SYS_BASE__));

#endif  /* __ASSEMBLER__ */

#endif /* __MSP430_HEADERS_SYS_H */
