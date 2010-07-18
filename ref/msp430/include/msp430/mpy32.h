#ifndef __MSP430_HEADERS_MPY32_H
#define __MSP430_HEADERS_MPY32_H

/* mpy.h
 *
 * mspgcc project: MSP430 device headers
 * Hardware 32-bit multiplier
 *
 * (c) 2008  by Sergey A. Borshch <sb-sf@sourceforge.net>
 * Originally based in MSP430F543x datasheet (slas609)
 *    and MSP430x5xx Family User's Guide (slau208).
 *
 * $Id: mpy32.h,v 1.3 2008/12/15 12:48:20 sb-sf Exp $
 */

/* Switches:

__MSP430_MPY32_BASE__ - base address of MPY32 module

*/
#if defined(__MSP430_MPY32_BASE__)
#define MPY_                __MSP430_MPY32_BASE__ + 0x00    /* 16-bit operand 1 - multiply */
sfrw(MPY, MPY_);
#define MPYS_               __MSP430_MPY32_BASE__ + 0x02    /* 16-bit operand 1 - signed multiply */
sfrw(MPYS, MPYS_);
#define MAC_                __MSP430_MPY32_BASE__ + 0x04    /* 16-bit operand 1 - multiply accumulate */
sfrw(MAC, MAC_);
#define MACS_               __MSP430_MPY32_BASE__ + 0x06    /* 16-bit operand 1 - signed multiply accumulate */
sfrw(MACS, MACS_);
#define OP2_                __MSP430_MPY32_BASE__ + 0x08    /* 16-bit operand 2 */
sfrw(OP2, OP2_);
#define RESLO_              __MSP430_MPY32_BASE__ + 0x0A    /* 16x16 result low word */
sfrw(RESLO, RESLO_);
#define RESHI_              __MSP430_MPY32_BASE__ + 0x0C    /* 16x16 result high word */
sfrw(RESHI, RESHI_);
#define SUMEXT_             __MSP430_MPY32_BASE__ + 0x0E    /* 16x16 sum extension */
sfrw(SUMEXT, SUMEXT_);
#define MPY32L_             __MSP430_MPY32_BASE__ + 0x10    /* 32-bit operand 1 - multiply low word */
sfrw(MPY32L, MPY32L_);
#define MPY32H_             __MSP430_MPY32_BASE__ + 0x12    /* 32-bit operand 1 - multiply high word */
sfrw(MPY32H, MPY32H_);
#define MPYS32L_            __MSP430_MPY32_BASE__ + 0x14    /* 32-bit operand 1 - signed multiply low word */
sfrw(MPYS32L, MPYS32L_);
#define MPYS32H_            __MSP430_MPY32_BASE__ + 0x16    /* 32-bit operand 1 - signed multiply high word */
sfrw(MPYS32H, MPYS32H_);
#define MAC32L_             __MSP430_MPY32_BASE__ + 0x18    /* 32-bit operand 1 - multiply accumulate low word */
sfrw(MAC32L, MAC32L_);
#define MAC32H_             __MSP430_MPY32_BASE__ + 0x1A    /* 32-bit operand 1 - multiply accumulate high word */
sfrw(MAC32H, MAC32H_);
#define MACS32L_            __MSP430_MPY32_BASE__ + 0x1C    /* 32-bit operand 1 - signed multiply accumulate low word */
sfrw(MACS32L, MACS32L_);
#define MACS32H_            __MSP430_MPY32_BASE__ + 0x1E    /* 32-bit operand 1 - signed multiply accumulate high word */
sfrw(MACS32H, MACS32H_);
#define OP2L_               __MSP430_MPY32_BASE__ + 0x20    /* 32-bit operand 2 - low word */
sfrw(OP2L, OP2L_);
#define OP2H_               __MSP430_MPY32_BASE__ + 0x22    /* 32-bit operand 2 - high word */
sfrw(OP2H, OP2H_);
#define RES0_               __MSP430_MPY32_BASE__ + 0x24    /* 32x32 result 3 - least significant word */
sfrw(RES0,RES0_);
#define RES1_               __MSP430_MPY32_BASE__ + 0x26
sfrw(RES1,RES1_);
#define RES2_               __MSP430_MPY32_BASE__ + 0x28
sfrw(RES2,RES2_);
#define RES3_               __MSP430_MPY32_BASE__ + 0x2A    /* 32x32 result 3 - most significant word */
sfrw(RES3,RES3_);
#define MPY32CTL0_          __MSP430_MPY32_BASE__ + 0x2C    /* MPY32 control register 0 */
sfrw(MPY32CTL0,MPY32CTL0_);
#endif  /* defined(__MSP430_MPY32_BASE__) */

#define MPYDLY32            (1<<9)      /* Delayed write mode */
#define MPYDLYWRTEN         (1<<8)      /* Delayed write enable */
#define MPYPO2_32           (1<<7)      /* Multiplier bit width of operand 2 */
#define MPYPO1_32           (1<<6)      /* Multiplier bit width of operand 1 */
#define MPYM1               (1<<5)      /* Multiplier mode */
#define MPYM0               (1<<4)      /*  -- // -- */
#define MPYSAT              (1<<3)      /* Saturation mode */
#define MPYFRAC             (1<<2)      /* Fractional mode */
#define MPYC                (1<<0)      /* Carry of the multiplier */

#define MPYM_0              (0<<4)      /* Multiply */
#define MPYM_1              (1<<4)      /* Signed multiply */
#define MPYM_2              (2<<4)      /* Multiply accumulate */
#define MPYM_3              (3<<4)      /* Signed multiply accumulate */


#ifndef __ASSEMBLER__
/* Structured declaration */

#undef  __xstr
#undef  __str
#define __xstr(x)     __str(x)
#define __str(x)      #x

typedef struct
{
    volatile unsigned int MPY;          /* 16-bit operand 1 - multiply */
    volatile unsigned int MPYS;         /* 16-bit operand 1 - signed multiply */
    volatile unsigned int MAC;          /* 16-bit operand 1 - multiply accumulate */
    volatile unsigned int MACS;         /* 16-bit operand 1 - signed multiply accumulate */
    volatile unsigned int OP2;          /* 16-bit operand 2 */
    volatile unsigned int RESLO;        /* 16x16 result low word */
    volatile unsigned int RESHI;        /* 16x16 result high word */
    volatile unsigned int SUMEXT; 		/* 16x16 sum extension */
    volatile unsigned int MPY32L;       /* 32-bit operand 1 - multiply low word */
    volatile unsigned int MPY32H;       /* 32-bit operand 1 - multiply high word */
    volatile unsigned int MPYS32L;      /* 32-bit operand 1 - signed multiply low word */
    volatile unsigned int MPYS32H;      /* 32-bit operand 1 - signed multiply high word */
    volatile unsigned int MAC32L;       /* 32-bit operand 1 - multiply accumulate low word */
    volatile unsigned int MAC32H;       /* 32-bit operand 1 - multiply accumulate high word */
    volatile unsigned int MACS32L;      /* 32-bit operand 1 - signed multiply accumulate low word */
    volatile unsigned int MACS32H;      /* 32-bit operand 1 - signed multiply accumulate high word */
    volatile unsigned int OP2L;         /* 32-bit operand 2 - low word */
    volatile unsigned int OP2H;         /* 32-bit operand 2 - high word */
    volatile unsigned int RES0;         /* 32x32 result 3 - least significant word */
    volatile unsigned int RES1;
    volatile unsigned int RES2;
    volatile unsigned int RES3;         /* 32x32 result 3 - most significant word */
    volatile unsigned int MPY32CTL0;    /* MPY32 control register 0 */
} mpy32_t;
mpy32_t mpy32 asm(__xstr(__MSP430_MPY32_BASE__));
#undef  __str
#undef  __xstr
#endif  /* __ASSEMBLER__ */

#endif  /* __MSP430_HEADERS_MPY32_H */
