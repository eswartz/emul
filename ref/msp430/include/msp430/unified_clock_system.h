#ifndef __msp430_headers_unified_clock_system_h
#define __msp430_headers_unified_clock_system_h

/* unified_clock_system.h
 *
 * mspgcc project: MSP430 device headers
 * UNIFIED_CLOCK_SYSTEM module header
 *
 * (c) 2002 by S. Balling <praktikum@innoventis.de>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: unified_clock_system.h,v 1.1 2009/06/04 21:55:18 cliechti Exp $
 */

/* Switches:
__MSP430_HAS_UCS__
*/

#if defined(__MSP430_HAS_UCS__)
#define UCSCTL0_            0x0160
sfrw (UCSCTL0,UCSCTL0_);
#define UCSCTL0_L_          0x0160
sfrb (UCSCTL0_L,UCSCTL0_L_);
#define UCSCTL0_H_          0x0161
sfrb (UCSCTL0_H,UCSCTL0_H_);
#define UCSCTL1_            0x0162
sfrw (UCSCTL1,UCSCTL1_);
#define UCSCTL1_L_          0x0162
sfrb (UCSCTL1_L,UCSCTL1_L_);
#define UCSCTL1_H_          0x0163
sfrb (UCSCTL1_H,UCSCTL1_H_);
#define UCSCTL2_            0x0164
sfrw (UCSCTL2,UCSCTL2_);
#define UCSCTL2_L_          0x0164
sfrb (UCSCTL2_L,UCSCTL2_L_);
#define UCSCTL2_H_          0x0165
sfrb (UCSCTL2_H,UCSCTL2_H_);
#define UCSCTL3_            0x0166
sfrw (UCSCTL3,UCSCTL3_);
#define UCSCTL3_L_          0x0166
sfrb (UCSCTL3_L,UCSCTL3_L_);
#define UCSCTL3_H_          0x0167
sfrb (UCSCTL3_H,UCSCTL3_H_);
#define UCSCTL4_            0x0168
sfrw (UCSCTL4,UCSCTL4_);
#define UCSCTL4_L_          0x0168
sfrb (UCSCTL4_L,UCSCTL4_L_);
#define UCSCTL4_H_          0x0169
sfrb (UCSCTL4_H,UCSCTL4_H_);
#define UCSCTL5_            0x016A
sfrw (UCSCTL5,UCSCTL5_);
#define UCSCTL5_L_          0x016A
sfrb (UCSCTL5_L,UCSCTL5_L_);
#define UCSCTL5_H_          0x016B
sfrb (UCSCTL5_H,UCSCTL5_H_);
#define UCSCTL6_            0x016C
sfrw (UCSCTL6,UCSCTL6_);
#define UCSCTL6_L_          0x016C
sfrb (UCSCTL6_L,UCSCTL6_L_);
#define UCSCTL6_H_          0x016D
sfrb (UCSCTL6_H,UCSCTL6_H_);
#define UCSCTL7_            0x016E
sfrw (UCSCTL7,UCSCTL7_);
#define UCSCTL7_L_          0x016E
sfrb (UCSCTL7_L,UCSCTL7_L_);
#define UCSCTL7_H_          0x016F
sfrb (UCSCTL7_H,UCSCTL7_H_);
#define UCSCTL8_            0x0170
sfrw (UCSCTL8,UCSCTL8_);
#define UCSCTL8_L_          0x0170
sfrb (UCSCTL8_L,UCSCTL8_L_);
#define UCSCTL8_H_          0x0171
sfrb (UCSCTL8_H,UCSCTL8_H_);

/* UCSCTL0 Control Bits */
//#define RESERVED            (0x0001)    /* RESERVED */
//#define RESERVED            (0x0002)    /* RESERVED */
//#define RESERVED            (0x0004)    /* RESERVED */
#define MOD0                   (0x0008)       /* Modulation Bit Counter Bit : 0 */
#define MOD1                   (0x0010)       /* Modulation Bit Counter Bit : 1 */
#define MOD2                   (0x0020)       /* Modulation Bit Counter Bit : 2 */
#define MOD3                   (0x0040)       /* Modulation Bit Counter Bit : 3 */
#define MOD4                   (0x0080)       /* Modulation Bit Counter Bit : 4 */
#define DCO0                   (0x0100)       /* DCO TAP Bit : 0 */
#define DCO1                   (0x0200)       /* DCO TAP Bit : 1 */
#define DCO2                   (0x0400)       /* DCO TAP Bit : 2 */
#define DCO3                   (0x0800)       /* DCO TAP Bit : 3 */
#define DCO4                   (0x1000)       /* DCO TAP Bit : 4 */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL0 Control Bits */
//#define RESERVED            (0x0001)    /* RESERVED */
//#define RESERVED            (0x0002)    /* RESERVED */
//#define RESERVED            (0x0004)    /* RESERVED */
#define MOD0_L                 (0x0008)       /* Modulation Bit Counter Bit : 0 */
#define MOD1_L                 (0x0010)       /* Modulation Bit Counter Bit : 1 */
#define MOD2_L                 (0x0020)       /* Modulation Bit Counter Bit : 2 */
#define MOD3_L                 (0x0040)       /* Modulation Bit Counter Bit : 3 */
#define MOD4_L                 (0x0080)       /* Modulation Bit Counter Bit : 4 */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL0 Control Bits */
//#define RESERVED            (0x0001)    /* RESERVED */
//#define RESERVED            (0x0002)    /* RESERVED */
//#define RESERVED            (0x0004)    /* RESERVED */
#define DCO0_H                 (0x0001)       /* DCO TAP Bit : 0 */
#define DCO1_H                 (0x0002)       /* DCO TAP Bit : 1 */
#define DCO2_H                 (0x0004)       /* DCO TAP Bit : 2 */
#define DCO3_H                 (0x0008)       /* DCO TAP Bit : 3 */
#define DCO4_H                 (0x0010)       /* DCO TAP Bit : 4 */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL1 Control Bits */
#define DISMOD                 (0x0001)       /* Disable Modulation */
#define DCOR                   (0x0002)       /* DCO External Resistor Select */
//#define RESERVED            (0x0004)    /* RESERVED */
//#define RESERVED            (0x0008)    /* RESERVED */
#define DCORSEL0               (0x0010)       /* DCO Freq. Range Select Bit : 0 */
#define DCORSEL1               (0x0020)       /* DCO Freq. Range Select Bit : 1 */
#define DCORSEL2               (0x0040)       /* DCO Freq. Range Select Bit : 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0100)    /* RESERVED */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL1 Control Bits */
#define DISMOD_L               (0x0001)       /* Disable Modulation */
#define DCOR_L                 (0x0002)       /* DCO External Resistor Select */
//#define RESERVED            (0x0004)    /* RESERVED */
//#define RESERVED            (0x0008)    /* RESERVED */
#define DCORSEL0_L             (0x0010)       /* DCO Freq. Range Select Bit : 0 */
#define DCORSEL1_L             (0x0020)       /* DCO Freq. Range Select Bit : 1 */
#define DCORSEL2_L             (0x0040)       /* DCO Freq. Range Select Bit : 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0100)    /* RESERVED */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL1 Control Bits */
//#define RESERVED            (0x0004)    /* RESERVED */
//#define RESERVED            (0x0008)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0100)    /* RESERVED */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

#define DCORSEL_0              (0x0000)       /* DCO RSEL 0 */
#define DCORSEL_1              (0x0010)       /* DCO RSEL 1 */
#define DCORSEL_2              (0x0020)       /* DCO RSEL 2 */
#define DCORSEL_3              (0x0030)       /* DCO RSEL 3 */
#define DCORSEL_4              (0x0040)       /* DCO RSEL 4 */
#define DCORSEL_5              (0x0050)       /* DCO RSEL 5 */
#define DCORSEL_6              (0x0060)       /* DCO RSEL 6 */
#define DCORSEL_7              (0x0070)       /* DCO RSEL 7 */

/* UCSCTL2 Control Bits */
#define FLLN0                  (0x0001)       /* FLL Multipier Bit : 0 */
#define FLLN1                  (0x0002)       /* FLL Multipier Bit : 1 */
#define FLLN2                  (0x0004)       /* FLL Multipier Bit : 2 */
#define FLLN3                  (0x0008)       /* FLL Multipier Bit : 3 */
#define FLLN4                  (0x0010)       /* FLL Multipier Bit : 4 */
#define FLLN5                  (0x0020)       /* FLL Multipier Bit : 5 */
#define FLLN6                  (0x0040)       /* FLL Multipier Bit : 6 */
#define FLLN7                  (0x0080)       /* FLL Multipier Bit : 7 */
#define FLLN8                  (0x0100)       /* FLL Multipier Bit : 8 */
#define FLLN9                  (0x0200)       /* FLL Multipier Bit : 9 */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
#define FLLD0                  (0x1000)       /* Loop Divider Bit : 0 */
#define FLLD1                  (0x2000)       /* Loop Divider Bit : 1 */
#define FLLD2                  (0x4000)       /* Loop Divider Bit : 1 */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL2 Control Bits */
#define FLLN0_L                (0x0001)       /* FLL Multipier Bit : 0 */
#define FLLN1_L                (0x0002)       /* FLL Multipier Bit : 1 */
#define FLLN2_L                (0x0004)       /* FLL Multipier Bit : 2 */
#define FLLN3_L                (0x0008)       /* FLL Multipier Bit : 3 */
#define FLLN4_L                (0x0010)       /* FLL Multipier Bit : 4 */
#define FLLN5_L                (0x0020)       /* FLL Multipier Bit : 5 */
#define FLLN6_L                (0x0040)       /* FLL Multipier Bit : 6 */
#define FLLN7_L                (0x0080)       /* FLL Multipier Bit : 7 */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL2 Control Bits */
#define FLLN8_H                (0x0001)       /* FLL Multipier Bit : 8 */
#define FLLN9_H                (0x0002)       /* FLL Multipier Bit : 9 */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
#define FLLD0_H                (0x0010)       /* Loop Divider Bit : 0 */
#define FLLD1_H                (0x0020)       /* Loop Divider Bit : 1 */
#define FLLD2_H                (0x0040)       /* Loop Divider Bit : 1 */
//#define RESERVED            (0x8000)    /* RESERVED */

#define FLLD_0                 (0x0000)       /* Multiply Selected Loop Freq. 1 */
#define FLLD_1                 (0x1000)       /* Multiply Selected Loop Freq. 2 */
#define FLLD_2                 (0x2000)       /* Multiply Selected Loop Freq. 4 */
#define FLLD_3                 (0x3000)       /* Multiply Selected Loop Freq. 8 */
#define FLLD_4                 (0x4000)       /* Multiply Selected Loop Freq. 16 */
#define FLLD_5                 (0x5000)       /* Multiply Selected Loop Freq. 32 */
#define FLLD_6                 (0x6000)       /* Multiply Selected Loop Freq. 32 */
#define FLLD_7                 (0x7000)       /* Multiply Selected Loop Freq. 32 */
#define FLLD__1                (0x0000)       /* Multiply Selected Loop Freq. By 1 */
#define FLLD__2                (0x1000)       /* Multiply Selected Loop Freq. By 2 */
#define FLLD__4                (0x2000)       /* Multiply Selected Loop Freq. By 4 */
#define FLLD__8                (0x3000)       /* Multiply Selected Loop Freq. By 8 */
#define FLLD__16               (0x4000)       /* Multiply Selected Loop Freq. By 16 */
#define FLLD__32               (0x5000)       /* Multiply Selected Loop Freq. By 32 */

/* UCSCTL3 Control Bits */
#define FLLREFDIV0             (0x0001)       /* Reference Divider Bit : 0 */
#define FLLREFDIV1             (0x0002)       /* Reference Divider Bit : 1 */
#define FLLREFDIV2             (0x0004)       /* Reference Divider Bit : 2 */
//#define RESERVED            (0x0008)    /* RESERVED */
#define SELREF0                (0x0010)       /* FLL Reference Clock Select Bit : 0 */
#define SELREF1                (0x0020)       /* FLL Reference Clock Select Bit : 1 */
#define SELREF2                (0x0040)       /* FLL Reference Clock Select Bit : 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0100)    /* RESERVED */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL3 Control Bits */
#define FLLREFDIV0_L           (0x0001)       /* Reference Divider Bit : 0 */
#define FLLREFDIV1_L           (0x0002)       /* Reference Divider Bit : 1 */
#define FLLREFDIV2_L           (0x0004)       /* Reference Divider Bit : 2 */
//#define RESERVED            (0x0008)    /* RESERVED */
#define SELREF0_L              (0x0010)       /* FLL Reference Clock Select Bit : 0 */
#define SELREF1_L              (0x0020)       /* FLL Reference Clock Select Bit : 1 */
#define SELREF2_L              (0x0040)       /* FLL Reference Clock Select Bit : 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0100)    /* RESERVED */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL3 Control Bits */
//#define RESERVED            (0x0008)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0100)    /* RESERVED */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

#define FLLREFDIV_0            (0x0000)       /* Reference Divider: f(LFCLK)/1 */
#define FLLREFDIV_1            (0x0001)       /* Reference Divider: f(LFCLK)/2 */
#define FLLREFDIV_2            (0x0002)       /* Reference Divider: f(LFCLK)/4 */
#define FLLREFDIV_3            (0x0003)       /* Reference Divider: f(LFCLK)/8 */
#define FLLREFDIV_4            (0x0004)       /* Reference Divider: f(LFCLK)/12 */
#define FLLREFDIV_5            (0x0005)       /* Reference Divider: f(LFCLK)/16 */
#define FLLREFDIV_6            (0x0006)       /* Reference Divider: f(LFCLK)/16 */
#define FLLREFDIV_7            (0x0007)       /* Reference Divider: f(LFCLK)/16 */
#define FLLREFDIV__1           (0x0000)       /* Reference Divider: f(LFCLK)/1 */
#define FLLREFDIV__2           (0x0001)       /* Reference Divider: f(LFCLK)/2 */
#define FLLREFDIV__4           (0x0002)       /* Reference Divider: f(LFCLK)/4 */
#define FLLREFDIV__8           (0x0003)       /* Reference Divider: f(LFCLK)/8 */
#define FLLREFDIV__12          (0x0004)       /* Reference Divider: f(LFCLK)/12 */
#define FLLREFDIV__16          (0x0005)       /* Reference Divider: f(LFCLK)/16 */
#define SELREF_0               (0x0000)       /* FLL Reference Clock Select 0 */
#define SELREF_1               (0x0010)       /* FLL Reference Clock Select 1 */
#define SELREF_2               (0x0020)       /* FLL Reference Clock Select 2 */
#define SELREF_3               (0x0030)       /* FLL Reference Clock Select 3 */
#define SELREF_4               (0x0040)       /* FLL Reference Clock Select 4 */
#define SELREF_5               (0x0050)       /* FLL Reference Clock Select 5 */
#define SELREF_6               (0x0060)       /* FLL Reference Clock Select 6 */
#define SELREF_7               (0x0070)       /* FLL Reference Clock Select 7 */
#define SELREF__XT1CLK         (0x0000)       /* Multiply Selected Loop Freq. By XT1CLK */
#define SELREF__VLOCLK         (0x0010)       /* Multiply Selected Loop Freq. By VLOCLK */
#define SELREF__REFOCLK        (0x0020)       /* Multiply Selected Loop Freq. By REFOCLK */
#define SELREF__XT2CLK         (0x0050)       /* Multiply Selected Loop Freq. By XT2CLK */

/* UCSCTL4 Control Bits */
#define SELM0                  (0x0001)       /* MCLK Source Select Bit: 0 */
#define SELM1                  (0x0002)       /* MCLK Source Select Bit: 1 */
#define SELM2                  (0x0004)       /* MCLK Source Select Bit: 2 */
//#define RESERVED            (0x0008)    /* RESERVED */
#define SELS0                  (0x0010)       /* SMCLK Source Select Bit: 0 */
#define SELS1                  (0x0020)       /* SMCLK Source Select Bit: 1 */
#define SELS2                  (0x0040)       /* SMCLK Source Select Bit: 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
#define SELA0                  (0x0100)       /* ACLK Source Select Bit: 0 */
#define SELA1                  (0x0200)       /* ACLK Source Select Bit: 1 */
#define SELA2                  (0x0400)       /* ACLK Source Select Bit: 2 */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL4 Control Bits */
#define SELM0_L                (0x0001)       /* MCLK Source Select Bit: 0 */
#define SELM1_L                (0x0002)       /* MCLK Source Select Bit: 1 */
#define SELM2_L                (0x0004)       /* MCLK Source Select Bit: 2 */
//#define RESERVED            (0x0008)    /* RESERVED */
#define SELS0_L                (0x0010)       /* SMCLK Source Select Bit: 0 */
#define SELS1_L                (0x0020)       /* SMCLK Source Select Bit: 1 */
#define SELS2_L                (0x0040)       /* SMCLK Source Select Bit: 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL4 Control Bits */
//#define RESERVED            (0x0008)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
#define SELA0_H                (0x0001)       /* ACLK Source Select Bit: 0 */
#define SELA1_H                (0x0002)       /* ACLK Source Select Bit: 1 */
#define SELA2_H                (0x0004)       /* ACLK Source Select Bit: 2 */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

#define SELM_0                 (0x0000)       /* MCLK Source Select 0 */
#define SELM_1                 (0x0001)       /* MCLK Source Select 1 */
#define SELM_2                 (0x0002)       /* MCLK Source Select 2 */
#define SELM_3                 (0x0003)       /* MCLK Source Select 3 */
#define SELM_4                 (0x0004)       /* MCLK Source Select 4 */
#define SELM_5                 (0x0005)       /* MCLK Source Select 5 */
#define SELM_6                 (0x0006)       /* MCLK Source Select 6 */
#define SELM_7                 (0x0007)       /* MCLK Source Select 7 */
#define SELM__XT1CLK           (0x0000)       /* MCLK Source Select XT1CLK */
#define SELM__VLOCLK           (0x0001)       /* MCLK Source Select VLOCLK */
#define SELM__REFOCLK          (0x0002)       /* MCLK Source Select REFOCLK */
#define SELM__DCOCLK           (0x0003)       /* MCLK Source Select DCOCLK */
#define SELM__DCOCLKDIV        (0x0004)       /* MCLK Source Select DCOCLKDIV */
#define SELM__XT2CLK           (0x0005)       /* MCLK Source Select XT2CLK */

#define SELS_0                 (0x0000)       /* SMCLK Source Select 0 */
#define SELS_1                 (0x0010)       /* SMCLK Source Select 1 */
#define SELS_2                 (0x0020)       /* SMCLK Source Select 2 */
#define SELS_3                 (0x0030)       /* SMCLK Source Select 3 */
#define SELS_4                 (0x0040)       /* SMCLK Source Select 4 */
#define SELS_5                 (0x0050)       /* SMCLK Source Select 5 */
#define SELS_6                 (0x0060)       /* SMCLK Source Select 6 */
#define SELS_7                 (0x0070)       /* SMCLK Source Select 7 */
#define SELS__XT1CLK           (0x0000)       /* SMCLK Source Select XT1CLK */
#define SELS__VLOCLK           (0x0010)       /* SMCLK Source Select VLOCLK */
#define SELS__REFOCLK          (0x0020)       /* SMCLK Source Select REFOCLK */
#define SELS__DCOCLK           (0x0030)       /* SMCLK Source Select DCOCLK */
#define SELS__DCOCLKDIV        (0x0040)       /* SMCLK Source Select DCOCLKDIV */
#define SELS__XT2CLK           (0x0050)       /* SMCLK Source Select XT2CLK */

#define SELA_0                 (0x0000)       /* ACLK Source Select 0 */
#define SELA_1                 (0x0100)       /* ACLK Source Select 1 */
#define SELA_2                 (0x0200)       /* ACLK Source Select 2 */
#define SELA_3                 (0x0300)       /* ACLK Source Select 3 */
#define SELA_4                 (0x0400)       /* ACLK Source Select 4 */
#define SELA_5                 (0x0500)       /* ACLK Source Select 5 */
#define SELA_6                 (0x0600)       /* ACLK Source Select 6 */
#define SELA_7                 (0x0700)       /* ACLK Source Select 7 */
#define SELA__XT1CLK           (0x0000)       /* ACLK Source Select XT1CLK */
#define SELA__VLOCLK           (0x0100)       /* ACLK Source Select VLOCLK */
#define SELA__REFOCLK          (0x0200)       /* ACLK Source Select REFOCLK */
#define SELA__DCOCLK           (0x0300)       /* ACLK Source Select DCOCLK */
#define SELA__DCOCLKDIV        (0x0400)       /* ACLK Source Select DCOCLKDIV */
#define SELA__XT2CLK           (0x0500)       /* ACLK Source Select XT2CLK */

/* UCSCTL5 Control Bits */
#define DIVM0                  (0x0001)       /* MCLK Divider Bit: 0 */
#define DIVM1                  (0x0002)       /* MCLK Divider Bit: 1 */
#define DIVM2                  (0x0004)       /* MCLK Divider Bit: 2 */
//#define RESERVED            (0x0008)    /* RESERVED */
#define DIVS0                  (0x0010)       /* SMCLK Divider Bit: 0 */
#define DIVS1                  (0x0020)       /* SMCLK Divider Bit: 1 */
#define DIVS2                  (0x0040)       /* SMCLK Divider Bit: 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
#define DIVA0                  (0x0100)       /* ACLK Divider Bit: 0 */
#define DIVA1                  (0x0200)       /* ACLK Divider Bit: 1 */
#define DIVA2                  (0x0400)       /* ACLK Divider Bit: 2 */
//#define RESERVED            (0x0800)    /* RESERVED */
#define DIVPA0                 (0x1000)       /* ACLK from Pin Divider Bit: 0 */
#define DIVPA1                 (0x2000)       /* ACLK from Pin Divider Bit: 1 */
#define DIVPA2                 (0x4000)       /* ACLK from Pin Divider Bit: 2 */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL5 Control Bits */
#define DIVM0_L                (0x0001)       /* MCLK Divider Bit: 0 */
#define DIVM1_L                (0x0002)       /* MCLK Divider Bit: 1 */
#define DIVM2_L                (0x0004)       /* MCLK Divider Bit: 2 */
//#define RESERVED            (0x0008)    /* RESERVED */
#define DIVS0_L                (0x0010)       /* SMCLK Divider Bit: 0 */
#define DIVS1_L                (0x0020)       /* SMCLK Divider Bit: 1 */
#define DIVS2_L                (0x0040)       /* SMCLK Divider Bit: 2 */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL5 Control Bits */
//#define RESERVED            (0x0008)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
#define DIVA0_H                (0x0001)       /* ACLK Divider Bit: 0 */
#define DIVA1_H                (0x0002)       /* ACLK Divider Bit: 1 */
#define DIVA2_H                (0x0004)       /* ACLK Divider Bit: 2 */
//#define RESERVED            (0x0800)    /* RESERVED */
#define DIVPA0_H               (0x0010)       /* ACLK from Pin Divider Bit: 0 */
#define DIVPA1_H               (0x0020)       /* ACLK from Pin Divider Bit: 1 */
#define DIVPA2_H               (0x0040)       /* ACLK from Pin Divider Bit: 2 */
//#define RESERVED            (0x8000)    /* RESERVED */

#define DIVM_0                 (0x0000)       /* MCLK Source Divider 0 */
#define DIVM_1                 (0x0001)       /* MCLK Source Divider 1 */
#define DIVM_2                 (0x0002)       /* MCLK Source Divider 2 */
#define DIVM_3                 (0x0003)       /* MCLK Source Divider 3 */
#define DIVM_4                 (0x0004)       /* MCLK Source Divider 4 */
#define DIVM_5                 (0x0005)       /* MCLK Source Divider 5 */
#define DIVM_6                 (0x0006)       /* MCLK Source Divider 6 */
#define DIVM_7                 (0x0007)       /* MCLK Source Divider 7 */
#define DIVM__1                (0x0000)       /* MCLK Source Divider f(MCLK)/1 */
#define DIVM__2                (0x0001)       /* MCLK Source Divider f(MCLK)/2 */
#define DIVM__4                (0x0002)       /* MCLK Source Divider f(MCLK)/4 */
#define DIVM__8                (0x0003)       /* MCLK Source Divider f(MCLK)/8 */
#define DIVM__16               (0x0004)       /* MCLK Source Divider f(MCLK)/16 */
#define DIVM__32               (0x0005)       /* MCLK Source Divider f(MCLK)/32 */

#define DIVS_0                 (0x0000)       /* SMCLK Source Divider 0 */
#define DIVS_1                 (0x0010)       /* SMCLK Source Divider 1 */
#define DIVS_2                 (0x0020)       /* SMCLK Source Divider 2 */
#define DIVS_3                 (0x0030)       /* SMCLK Source Divider 3 */
#define DIVS_4                 (0x0040)       /* SMCLK Source Divider 4 */
#define DIVS_5                 (0x0050)       /* SMCLK Source Divider 5 */
#define DIVS_6                 (0x0060)       /* SMCLK Source Divider 6 */
#define DIVS_7                 (0x0070)       /* SMCLK Source Divider 7 */
#define DIVS__1                (0x0000)       /* SMCLK Source Divider f(SMCLK)/1 */
#define DIVS__2                (0x0010)       /* SMCLK Source Divider f(SMCLK)/2 */
#define DIVS__4                (0x0020)       /* SMCLK Source Divider f(SMCLK)/4 */
#define DIVS__8                (0x0030)       /* SMCLK Source Divider f(SMCLK)/8 */
#define DIVS__16               (0x0040)       /* SMCLK Source Divider f(SMCLK)/16 */
#define DIVS__32               (0x0050)       /* SMCLK Source Divider f(SMCLK)/32 */

#define DIVA_0                 (0x0000)       /* ACLK Source Divider 0 */
#define DIVA_1                 (0x0100)       /* ACLK Source Divider 1 */
#define DIVA_2                 (0x0200)       /* ACLK Source Divider 2 */
#define DIVA_3                 (0x0300)       /* ACLK Source Divider 3 */
#define DIVA_4                 (0x0400)       /* ACLK Source Divider 4 */
#define DIVA_5                 (0x0500)       /* ACLK Source Divider 5 */
#define DIVA_6                 (0x0600)       /* ACLK Source Divider 6 */
#define DIVA_7                 (0x0700)       /* ACLK Source Divider 7 */
#define DIVA__1                (0x0000)       /* ACLK Source Divider f(ACLK)/1 */
#define DIVA__2                (0x0100)       /* ACLK Source Divider f(ACLK)/2 */
#define DIVA__4                (0x0200)       /* ACLK Source Divider f(ACLK)/4 */
#define DIVA__8                (0x0300)       /* ACLK Source Divider f(ACLK)/8 */
#define DIVA__16               (0x0400)       /* ACLK Source Divider f(ACLK)/16 */
#define DIVA__32               (0x0500)       /* ACLK Source Divider f(ACLK)/32 */

#define DIVPA_0                (0x0000)       /* ACLK from Pin Source Divider 0 */
#define DIVPA_1                (0x1000)       /* ACLK from Pin Source Divider 1 */
#define DIVPA_2                (0x2000)       /* ACLK from Pin Source Divider 2 */
#define DIVPA_3                (0x3000)       /* ACLK from Pin Source Divider 3 */
#define DIVPA_4                (0x4000)       /* ACLK from Pin Source Divider 4 */
#define DIVPA_5                (0x5000)       /* ACLK from Pin Source Divider 5 */
#define DIVPA_6                (0x6000)       /* ACLK from Pin Source Divider 6 */
#define DIVPA_7                (0x7000)       /* ACLK from Pin Source Divider 7 */
#define DIVPA__1               (0x0000)       /* ACLK from Pin Source Divider f(ACLK)/1 */
#define DIVPA__2               (0x1000)       /* ACLK from Pin Source Divider f(ACLK)/2 */
#define DIVPA__4               (0x2000)       /* ACLK from Pin Source Divider f(ACLK)/4 */
#define DIVPA__8               (0x3000)       /* ACLK from Pin Source Divider f(ACLK)/8 */
#define DIVPA__16              (0x4000)       /* ACLK from Pin Source Divider f(ACLK)/16 */
#define DIVPA__32              (0x5000)       /* ACLK from Pin Source Divider f(ACLK)/32 */

/* UCSCTL6 Control Bits */
#define XT1OFF                 (0x0001)       /* High Frequency Oscillator 1 (XT1) disable */
#define SMCLKOFF               (0x0002)       /* SMCLK Off */
#define XCAP0                  (0x0004)       /* XIN/XOUT Cap Bit: 0 */
#define XCAP1                  (0x0008)       /* XIN/XOUT Cap Bit: 1 */
#define XT1BYPASS              (0x0010)       /* XT1 bypass mode : 0: internal 1:sourced from external pin */
#define XTS                    (0x0020)       /* 1: Selects high-freq. oscillator */
#define XT1DRIVE0              (0x0040)       /* XT1 Drive Level mode Bit 0 */
#define XT1DRIVE1              (0x0080)       /* XT1 Drive Level mode Bit 1 */
#define XT2OFF                 (0x0100)       /* High Frequency Oscillator 2 (XT2) disable */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
#define XT2BYPASS              (0x1000)       /* XT2 bypass mode : 0: internal 1:sourced from external pin */
//#define RESERVED            (0x2000)    /* RESERVED */
#define XT2DRIVE0              (0x4000)       /* XT2 Drive Level mode Bit 0 */
#define XT2DRIVE1              (0x8000)       /* XT2 Drive Level mode Bit 1 */

/* UCSCTL6 Control Bits */
#define XT1OFF_L               (0x0001)       /* High Frequency Oscillator 1 (XT1) disable */
#define SMCLKOFF_L             (0x0002)       /* SMCLK Off */
#define XCAP0_L                (0x0004)       /* XIN/XOUT Cap Bit: 0 */
#define XCAP1_L                (0x0008)       /* XIN/XOUT Cap Bit: 1 */
#define XT1BYPASS_L            (0x0010)       /* XT1 bypass mode : 0: internal 1:sourced from external pin */
#define XTS_L                  (0x0020)       /* 1: Selects high-freq. oscillator */
#define XT1DRIVE0_L            (0x0040)       /* XT1 Drive Level mode Bit 0 */
#define XT1DRIVE1_L            (0x0080)       /* XT1 Drive Level mode Bit 1 */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */

/* UCSCTL6 Control Bits */
#define XT2OFF_H               (0x0001)       /* High Frequency Oscillator 2 (XT2) disable */
//#define RESERVED            (0x0200)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
#define XT2BYPASS_H            (0x0010)       /* XT2 bypass mode : 0: internal 1:sourced from external pin */
//#define RESERVED            (0x2000)    /* RESERVED */
#define XT2DRIVE0_H            (0x0040)       /* XT2 Drive Level mode Bit 0 */
#define XT2DRIVE1_H            (0x0080)       /* XT2 Drive Level mode Bit 1 */

#define XCAP_0                 (0x0000)       /* XIN/XOUT Cap 0 */
#define XCAP_1                 (0x0004)       /* XIN/XOUT Cap 1 */
#define XCAP_2                 (0x0008)       /* XIN/XOUT Cap 2 */
#define XCAP_3                 (0x000C)       /* XIN/XOUT Cap 3 */
#define XT1DRIVE_0             (0x0000)       /* XT1 Drive Level mode: 0 */
#define XT1DRIVE_1             (0x0040)       /* XT1 Drive Level mode: 1 */
#define XT1DRIVE_2             (0x0080)       /* XT1 Drive Level mode: 2 */
#define XT1DRIVE_3             (0x00C0)       /* XT1 Drive Level mode: 3 */
#define XT2DRIVE_0             (0x0000)       /* XT2 Drive Level mode: 0 */
#define XT2DRIVE_1             (0x4000)       /* XT2 Drive Level mode: 1 */
#define XT2DRIVE_2             (0x8000)       /* XT2 Drive Level mode: 2 */
#define XT2DRIVE_3             (0xC000)       /* XT2 Drive Level mode: 3 */

/* UCSCTL7 Control Bits */
#define DCOFFG                 (0x0001)       /* DCO Fault Flag */
#define XT1LFOFFG              (0x0002)       /* XT1 Low Frequency Oscillator Fault Flag */
#define XT1HFOFFG              (0x0004)       /* XT1 High Frequency Oscillator 1 Fault Flag */
#define XT2OFFG                (0x0008)       /* High Frequency Oscillator 2 Fault Flag */
#define FLLULIFG               (0x0010)       /* FLL Unlock Interrupt Flag */
//#define RESERVED            (0x0020)    /* RESERVED */
//#define RESERVED            (0x0040)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
#define FLLUNLOCK0             (0x0100)       /* FLL Unlock Bit 0 */
#define FLLUNLOCK1             (0x0200)       /* FLL Unlock Bit 1 */
#define FLLUNLOCKHIS0          (0x0400)       /* FLL Unlock History Bit 0 */
#define FLLUNLOCKHIS1          (0x08000)      /* FLL Unlock History Bit 1 */
#define FLLULIE                (0x01000)      /* FLL Unlock Interrupt Enable */
#define FLLWARNEN              (0x02000)      /* FLL Warning Enable */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL7 Control Bits */
#define DCOFFG_L               (0x0001)       /* DCO Fault Flag */
#define XT1LFOFFG_L            (0x0002)       /* XT1 Low Frequency Oscillator Fault Flag */
#define XT1HFOFFG_L            (0x0004)       /* XT1 High Frequency Oscillator 1 Fault Flag */
#define XT2OFFG_L              (0x0008)       /* High Frequency Oscillator 2 Fault Flag */
#define FLLULIFG_L             (0x0010)       /* FLL Unlock Interrupt Flag */
//#define RESERVED            (0x0020)    /* RESERVED */
//#define RESERVED            (0x0040)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL7 Control Bits */
//#define RESERVED            (0x0020)    /* RESERVED */
//#define RESERVED            (0x0040)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
#define FLLUNLOCK0_H           (0x0001)       /* FLL Unlock Bit 0 */
#define FLLUNLOCK1_H           (0x0002)       /* FLL Unlock Bit 1 */
#define FLLUNLOCKHIS0_H        (0x0004)       /* FLL Unlock History Bit 0 */
#define FLLUNLOCKHIS1_H        (0x0080)       /* FLL Unlock History Bit 1 */
#define FLLULIE_H              (0x0010)       /* FLL Unlock Interrupt Enable */
#define FLLWARNEN_H            (0x0020)       /* FLL Warning Enable */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL8 Control Bits */
#define ACLKREQEN              (0x0001)       /* ACLK Clock Request Enable */
#define MCLKREQEN              (0x0002)       /* MCLK Clock Request Enable */
#define SMCLKREQEN             (0x0004)       /* SMCLK Clock Request Enable */
#define MODOSCREQEN            (0x0008)       /* MODOSC Clock Request Enable */
#define IFCLKSEN               (0x0010)       /* Enable Interface Clock slow down mechanism */
//#define RESERVED            (0x0020)    /* RESERVED */
//#define RESERVED            (0x0040)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
#define SMCLK_FSEN             (0x0100)       /* Enable fail safe enable for SMCLK source */
#define ACLK_FSEN              (0x0200)       /* Enable fail safe enable for ACLK source */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL8 Control Bits */
#define ACLKREQEN_L            (0x0001)       /* ACLK Clock Request Enable */
#define MCLKREQEN_L            (0x0002)       /* MCLK Clock Request Enable */
#define SMCLKREQEN_L           (0x0004)       /* SMCLK Clock Request Enable */
#define MODOSCREQEN_L          (0x0008)       /* MODOSC Clock Request Enable */
#define IFCLKSEN_L             (0x0010)       /* Enable Interface Clock slow down mechanism */
//#define RESERVED            (0x0020)    /* RESERVED */
//#define RESERVED            (0x0040)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */

/* UCSCTL8 Control Bits */
//#define RESERVED            (0x0020)    /* RESERVED */
//#define RESERVED            (0x0040)    /* RESERVED */
//#define RESERVED            (0x0080)    /* RESERVED */
#define SMCLK_FSEN_H           (0x0001)       /* Enable fail safe enable for SMCLK source */
#define ACLK_FSEN_H            (0x0002)       /* Enable fail safe enable for ACLK source */
//#define RESERVED            (0x0400)    /* RESERVED */
//#define RESERVED            (0x0800)    /* RESERVED */
//#define RESERVED            (0x1000)    /* RESERVED */
//#define RESERVED            (0x2000)    /* RESERVED */
//#define RESERVED            (0x4000)    /* RESERVED */
//#define RESERVED            (0x8000)    /* RESERVED */
#endif

#endif /*__msp430_headers_unified_clock_system_h */
