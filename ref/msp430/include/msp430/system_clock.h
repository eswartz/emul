#ifndef __msp430_headers_system_clock_h
#define __msp430_headers_system_clock_h

/* system_clock.h
 *
 * mspgcc project: MSP430 device headers
 * SYSTEM_CLOCK module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: system_clock.h,v 1.7 2006/06/07 13:01:30 coppice Exp $
 */

/* Switches:

__MSP430_HAS_FLL__          - if device has the original FLL
__MSP430_HAS_FLLPLUS__      - if device has both an FLL+ and a Xtal oscillator 2
__MSP430_HAS_FLLPLUS_SMALL__- if device has the smaller FLL+

*/

#define SCFI0_              0x0050  /* System Clock Frequency Integrator 0 */
sfrb(SCFI0,SCFI0_);
#define FN_2                0x04
#define FN_3                0x08
#define FN_4                0x10
#if defined(__MSP430_HAS_FLLPLUS__)  ||  defined(__MSP430_HAS_FLLPLUS_SMALL__)
#define FN_8                0x20            /* fDCOCLK = 10*fNominal */
#define FLLD0               0x40            /* Loop Divider Bit : 0 */
#define FLLD1               0x80            /* Loop Divider Bit : 1 */

#define FLLD_1              0               /* Multiply Selected Loop Freq. By 1 */
#define FLLD_2              (FLLD0)         /* Multiply Selected Loop Freq. By 2 */
#define FLLD_4              (FLLD1)         /* Multiply Selected Loop Freq. By 4 */
#define FLLD_8              (FLLD1|FLLD0)   /* Multiply Selected Loop Freq. By 8 */
#endif

#define SCFI1_              0x0051  /* System Clock Frequency Integrator 1 */
sfrb(SCFI1,SCFI1_);
#define SCFQCTL_            0x0052  /* System Clock Frequency Control */
sfrb(SCFQCTL,SCFQCTL_);
/* System clock frequency values fMCLK coded with Bits 0-6 in SCFQCTL */
/* #define SCFQ_32K            0x00               fMCLK=1*fACLK          only a range from */
/* #define SCFQ_64K            0x01               fMCLK=2*fACLK          3+1 to 127+1 is possible */
#define SCFQ_128K           0x03            /* fMCLK=4*fACLK */
#define SCFQ_256K           0x07            /* fMCLK=8*fACLK */
#define SCFQ_512K           0x0F            /* fMCLK=16*fACLK */
#define SCFQ_1M             0x1F            /* fMCLK=32*fACLK */
#define SCFQ_2M             0x3F            /* fMCLK=64*fACLK */
#define SCFQ_4M             0x7F            /* fMCLK=128*fACLK        not possible for ICE */

#if defined(__MSP430_HAS_FLLPLUS__)  ||  defined(__MSP430_HAS_FLLPLUS_SMALL__)

#define SCFQ_M              0x80                     /* Modulation Disable */

#define FLL_CTL0_           0x0053  /* FLL+ Control 0 */
sfrb(FLL_CTL0,FLL_CTL0_);
#define DCOF                0x01            /* DCO Fault Flag */
#define LFOF                0x02            /* Low Frequency Oscillator Fault Flag */
#define XT1OF               0x04            /* High Frequency Oscillator Fault Flag */
#if defined(__MSP430_HAS_FLLPLUS__)
#define XT2OF               0x08            /* XT2 Oscillator Fault Flag */
#endif
#define XCAP0PF             0x00            /* XIN Cap = XOUT Cap = 0pf */
#define XCAP10PF            0x10            /* XIN Cap = XOUT Cap = 10pf */
#define XCAP14PF            0x20            /* XIN Cap = XOUT Cap = 14pf */
#define XCAP18PF            0x30            /* XIN Cap = XOUT Cap = 18pf */
#define XTS_FLL             0x40            /* 1: Selects high-freq. oscillator */
#define DCOPLUS             0x80            /* DCO+ Enable */

#define FLL_CTL1_           0x0054  /* FLL+ Control 1 */
sfrb(FLL_CTL1,FLL_CTL1_);
#define FLL_DIV0            0x01            /* FLL+ Divide Px.x/ACLK 0 */
#define FLL_DIV1            0x02            /* FLL+ Divide Px.x/ACLK 1 */

#define FLL_DIV_1           0x00            /* FLL+ Divide Px.x/ACLK By 1 */
#define FLL_DIV_2           0x01            /* FLL+ Divide Px.x/ACLK By 2 */
#define FLL_DIV_4           0x02            /* FLL+ Divide Px.x/ACLK By 4 */
#define FLL_DIV_8           0x03            /* FLL+ Divide Px.x/ACLK By 8 */

#if defined(__MSP430_HAS_FLLPLUS__)
#define SELS                0x04            /* Peripheral Module Clock Source (0: DCO, 1: XT2) */
#define SELM0               0x08            /* MCLK Source Select 0 */
#define SELM1               0x10            /* MCLK Source Select 1 */
#define XT2OFF              0x20            /* High Frequency Oscillator 2 (XT2) disable */

#define SELM_DCO            0x00            /* Select DCO for CPU MCLK */
#define SELM_XT2            0x10            /* Select XT2 for CPU MCLK */
#define SELM_A              0x18            /* Select A (from LFXT1) for CPU MCLK */
#define SMCLKOFF            0x40            /* Peripheral Module Clock (SMCLK) disable */
#endif

#else

#define CBCTL_              0x0053  /* Crystal Buffer Control *** WRITE-ONLY *** */
sfrb(CBCTL,CBCTL_);
#define CBE                 0x01
#define CBSEL0              0x02
#define CBSEL1              0x04
/* Source select of frequency at output pin XBUF coded with Bits 1-2 in CBCTL */
#define CBSEL_ACLK          0               /* source is ACLK (default after POR) */
#define CBSEL_ACLK_DIV2     (CBSEL0)        /* source is ACLK/2 */
#define CBSEL_ACLK_DIV4     (CBSEL1)        /* source is ACLK/4 */
#define CBSEL_MCLK          (CBSEL1|CBSEL0) /* source is MCLK */

#endif

/* INTERRUPT CONTROL BITS */
/* These two bits are defined in the Special Function Registers */
/* #define OFIFG               0x02 */
/* #define OFIE                0x02 */

#endif
