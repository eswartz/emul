#ifndef __msp430_headers_scanif_h
#define __msp430_headers_scanif_h

/* scanif.h
 *
 * mspgcc project: MSP430 device headers
 * Scan interface module header
 *
 * (c) 2003 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: scanif.h,v 1.3 2008/10/09 15:00:14 sb-sf Exp $
 */

/* Switches: none */

#define SIFDEBUG_           0x01B0  /* SIF, Debug Register */
sfrw(SIFDEBUG, SIFDEBUG_);
#define SIFCNT_             0x01B2  /* SIF, Counter1/2 */
sfrw(SIFCNT, SIFCNT_);
#define SIFTPSMV_           0x01B4  /* SIF, Processing State Machine */
sfrw(SIFTPSMV, SIFTPSMV_);
#define SIFCTL0_            0x01B6  /* SIF, Control Register 0 */
sfrw(SIFCTL0, SIFCTL0_);
#define SIFCTL1_            0x01B8  /* SIF, Control Register 1 */
sfrw(SIFCTL1, SIFCTL1_);
#define SIFCTL2_            0x01BA  /* SIF, Control Register 2 */
sfrw(SIFCTL2, SIFCTL2_);
#define SIFCTL3_            0x01BC  /* SIF, Control Register 3 */
sfrw(SIFCTL3, SIFCTL3_);
#define SIFCTL4_            0x01BE  /* SIF, Control Register 4 */
sfrw(SIFCTL4, SIFCTL4_);
#define SIFDACR0_           0x01C0  /* SIF, Digital to Analog Conv. 0 */
sfrw(SIFDACR0, SIFDACR0_);
#define SIFDACR1_           0x01C2  /* SIF, Digital to Analog Conv. 1 */
sfrw(SIFDACR1, SIFDACR1_);
#define SIFDACR2_           0x01C4  /* SIF, Digital to Analog Conv. 2 */
sfrw(SIFDACR2, SIFDACR2_);
#define SIFDACR3_           0x01C6  /* SIF, Digital to Analog Conv. 3 */
sfrw(SIFDACR3, SIFDACR3_);
#define SIFDACR4_           0x01C8  /* SIF, Digital to Analog Conv. 4 */
sfrw(SIFDACR4, SIFDACR4_);
#define SIFDACR5_           0x01CA  /* SIF, Digital to Analog Conv. 5 */
sfrw(SIFDACR5, SIFDACR5_);
#define SIFDACR6_           0x01CC  /* SIF, Digital to Analog Conv. 6 */
sfrw(SIFDACR6, SIFDACR6_);
#define SIFDACR7_           0x01CE  /* SIF, Digital to Analog Conv. 7 */
sfrw(SIFDACR7, SIFDACR7_);

#define SIFTSM_             0x01D0  /* SIF, Timing State Machine 0 */
#ifdef __ASSEMBLER__
#define SIFTSM              SIFTSM_                 /* SIF, Timing State Machine (for assembler) */
#else
#define SIFTSM              ((char*) SIFTSM_)       /* SIF, Timing State Machine (for C) */
#endif
#define SIFTSM0_            0x01D0  /* SIF, Timing State Machine 0 */
sfrw(SIFTSM0, SIFTSM0_);
#define SIFTSM1_            0x01D2  /* SIF, Timing State Machine 1 */
sfrw(SIFTSM1, SIFTSM1_);
#define SIFTSM2_            0x01D4  /* SIF, Timing State Machine 2 */
sfrw(SIFTSM2, SIFTSM2_);
#define SIFTSM3_            0x01D6  /* SIF, Timing State Machine 3 */
sfrw(SIFTSM3, SIFTSM3_);
#define SIFTSM4_            0x01D8  /* SIF, Timing State Machine 4 */
sfrw(SIFTSM4, SIFTSM4_);
#define SIFTSM5_            0x01DA  /* SIF, Timing State Machine 5 */
sfrw(SIFTSM5, SIFTSM5_);
#define SIFTSM6_            0x01DC  /* SIF, Timing State Machine 6 */
sfrw(SIFTSM6, SIFTSM6_);
#define SIFTSM7_            0x01DE  /* SIF, Timing State Machine 7 */
sfrw(SIFTSM7, SIFTSM7_);
#define SIFTSM8_            0x01E0  /* SIF, Timing State Machine 8 */
sfrw(SIFTSM8, SIFTSM8_);
#define SIFTSM9_            0x01E2  /* SIF, Timing State Machine 9 */
sfrw(SIFTSM9, SIFTSM9_);
#define SIFTSM10_           0x01E4  /* SIF, Timing State Machine 10 */
sfrw(SIFTSM10, SIFTSM10_);
#define SIFTSM11_           0x01E6  /* SIF, Timing State Machine 11 */
sfrw(SIFTSM11, SIFTSM11_);
#define SIFTSM12_           0x01E8  /* SIF, Timing State Machine 12 */
sfrw(SIFTSM12, SIFTSM12_);
#define SIFTSM13_           0x01EA  /* SIF, Timing State Machine 13 */
sfrw(SIFTSM13, SIFTSM13_);
#define SIFTSM14_           0x01EC  /* SIF, Timing State Machine 14 */
sfrw(SIFTSM14, SIFTSM14_);
#define SIFTSM15_           0x01EE  /* SIF, Timing State Machine 15 */
sfrw(SIFTSM15, SIFTSM15_);
#define SIFTSM16_           0x01F0  /* SIF, Timing State Machine 16 */
sfrw(SIFTSM16, SIFTSM16_);
#define SIFTSM17_           0x01F2  /* SIF, Timing State Machine 17 */
sfrw(SIFTSM17, SIFTSM17_);
#define SIFTSM18_           0x01F4  /* SIF, Timing State Machine 18 */
sfrw(SIFTSM18, SIFTSM18_);
#define SIFTSM19_           0x01F6  /* SIF, Timing State Machine 19 */
sfrw(SIFTSM19, SIFTSM19_);
#define SIFTSM20_           0x01F8  /* SIF, Timing State Machine 20 */
sfrw(SIFTSM20, SIFTSM20_);
#define SIFTSM21_           0x01FA  /* SIF, Timing State Machine 21 */
sfrw(SIFTSM21, SIFTSM21_);
#define SIFTSM22_           0x01FC  /* SIF, Timing State Machine 22 */
sfrw(SIFTSM22, SIFTSM22_);
#define SIFTSM23_           0x01FE  /* SIF, Timing State Machine 23 */
sfrw(SIFTSM23, SIFTSM23_);

/* SIFCTL1 */
#define SIFEN               (0x0001)    /* SIF Enable */
#define SIFTESTD            (0x0002)    /* SIF 0:Normal / 1:Test Mode */
#define SIFIFG0             (0x0004)    /* SIF Interrupt Flag 0 */
#define SIFIFG1             (0x0008)    /* SIF Interrupt Flag 1 */
#define SIFIFG2             (0x0010)    /* SIF Interrupt Flag 2 */
#define SIFIFG3             (0x0020)    /* SIF Interrupt Flag 3 */
#define SIFIFG4             (0x0040)    /* SIF Interrupt Flag 4 */
#define SIFIFG5             (0x0080)    /* SIF Interrupt Flag 5 */
#define SIFIFG6             (0x0100)    /* SIF Interrupt Flag 6 */
#define SIFIE0              (0x0200)    /* SIF Interrupt Enable 0 */
#define SIFIE1              (0x0400)    /* SIF Interrupt Enable 1 */
#define SIFIE2              (0x0800)    /* SIF Interrupt Enable 2 */
#define SIFIE3              (0x1000)    /* SIF Interrupt Enable 3 */
#define SIFIE4              (0x2000)    /* SIF Interrupt Enable 4 */
#define SIFIE5              (0x4000)    /* SIF Interrupt Enable 5 */
#define SIFIE6              (0x8000)    /* SIF Interrupt Enable 6 */

/* SIFCTL2 */
#define SIFTCH0OUT          (0x0001)    /* SIF TCH0 result */
#define SIFTCH1OUT          (0x0002)    /* SIF TCH1 result */
#define SIFTCH00            (0x0004)    /* SIF 1. Channel select 0 */
#define SIFTCH01            (0x0008)    /* SIF 1. Channel select 1 */
#define SIFTCH10            (0x0010)    /* SIF 2. Channel select 0 */
#define SIFTCH11            (0x0020)    /* SIF 2. Channel select 1 */
#define SIFTEN              (0x0040)    /* SIF Enable Transistors */
#define SIFSH               (0x0080)    /* SIF Sample on/off */
#define SIFVCC2             (0x0100)    /* SIF VCC/2 Generator off/on */
#define SIFVSS              (0x0200)    /* SIF Select Terminal for sample Cap. */
#define SIFCACI3            (0x0400)    /* SIF Selection of SIFCI3 */
#define SIFCI3SEL           (0x0800)    /* SIF Select CI3 Source */
#define SIFCAX              (0x1000)    /* SIF Select CA Source */
#define SIFCAINV            (0x2000)    /* SIF Invert CA Output 0:off/1:on */
#define SIFCAON             (0x4000)    /* SIF Switch CA on */
#define SIFDACON            (0x8000)    /* SIF Switch DAC on */

/* SIFCTL3 */
#define SIF0OUT             (0x0001)    /* SIF Sensor 0 Out */
#define SIF1OUT             (0x0002)    /* SIF Sensor 1 Out */
#define SIF2OUT             (0x0004)    /* SIF Sensor 2 Out */
#define SIF3OUT             (0x0008)    /* SIF Sensor 3 Out */
#define SIFIFGSET0          (0x0010)    /* SIF SIFIFG0 level select */
#define SIFIFGSET1          (0x0020)    /* SIF SIFIFG1 level select */
#define SIFIFGSET2          (0x0040)    /* SIF SIFIFG2 level select */
#define SIFCS               (0x0080)    /* SIF Capture Select */
#define SIFIS10             (0x0100)    /* SIF Input Select SIFCNT1.0 */
#define SIFIS11             (0x0200)    /* SIF Input Select SIFCNT1.1 */
#define SIFIS20             (0x0400)    /* SIF Input Select SIFCNT2.0 */
#define SIFIS21             (0x0800)    /* SIF Input Select SIFCNT2.1 */
#define SIFIS30             (0x1000)    /* SIF Input Select SIFCNT3.0 */
#define SIFIS31             (0x2000)    /* SIF Input Select SIFCNT3.1 */
#define SIFIS40             (0x4000)    /* SIF Input Select SIFCNT4.0 */
#define SIFIS41             (0x8000)    /* SIF Input Select SIFCNT4.1 */

#define SIFIS1_0            (0x0000)    /* SIF Input Select SIFCNT1 0 */
#define SIFIS1_1            (0x0100)    /* SIF Input Select SIFCNT1 1 */
#define SIFIS1_2            (0x0200)    /* SIF Input Select SIFCNT1 2 */
#define SIFIS1_3            (0x0300)    /* SIF Input Select SIFCNT1 3 */
#define SIFIS2_0            (0x0000)    /* SIF Input Select SIFCNT2 0 */
#define SIFIS2_1            (0x0400)    /* SIF Input Select SIFCNT2 1 */
#define SIFIS2_2            (0x0800)    /* SIF Input Select SIFCNT2 2 */
#define SIFIS2_3            (0x0C00)    /* SIF Input Select SIFCNT2 3 */
#define SIFIS3_0            (0x0000)    /* SIF Input Select SIFCNT3 0 */
#define SIFIS3_1            (0x1000)    /* SIF Input Select SIFCNT3 1 */
#define SIFIS3_2            (0x2000)    /* SIF Input Select SIFCNT3 2 */
#define SIFIS3_3            (0x3000)    /* SIF Input Select SIFCNT3 3 */
#define SIFIS4_0            (0x0000)    /* SIF Input Select SIFCNT4 0 */
#define SIFIS4_1            (0x4000)    /* SIF Input Select SIFCNT4 1 */
#define SIFIS4_2            (0x8000)    /* SIF Input Select SIFCNT4 2 */
#define SIFIS4_3            (0xC000)    /* SIF Input Select SIFCNT4 3 */

/* SIFCTL4 */
#define SIFDIV10            (0x0001)    /* SIF Clock Divider 1.0 */
#define SIFDIV11            (0x0002)    /* SIF Clock Divider 1.1 */
#define SIFDIV20            (0x0004)    /* SIF Clock Divider 2.0 */
#define SIFDIV21            (0x0008)    /* SIF Clock Divider 2.1 */
#define SIFDIV30            (0x0010)    /* SIF Clock Divider 3.0 */
#define SIFDIV31            (0x0020)    /* SIF Clock Divider 3.1 */
#define SIFDIV32            (0x0040)    /* SIF Clock Divider 3.2 */
#define SIFDIV33            (0x0080)    /* SIF Clock Divider 3.3 */
#define SIFDIV34            (0x0100)    /* SIF Clock Divider 3.4 */
#define SIFDIV35            (0x0200)    /* SIF Clock Divider 3.5 */
#define SIFQ6EN             (0x0400)    /* SIF Feedback 6 Enable */
#define SIFQ7EN             (0x0800)    /* SIF Feedback 7 Enable */
#define SIFCNT1ENP          (0x1000)    /* SIF Enable SIFCNT1 up count */
#define SIFCNT1ENM          (0x2000)    /* SIF Enable SIFCNT1 down count */
#define SIFCNT2EN           (0x4000)    /* SIF Enable SIFCNT2 count */
#define SIFCNTRST           (0x8000)    /* SIF Enable Counter Reset on Read */

#define SIFDIV1_1           (0x0000)    /* SIF Clock Divider 1: /1 */
#define SIFDIV1_2           (0x0001)    /* SIF Clock Divider 1: /2 */
#define SIFDIV1_4           (0x0002)    /* SIF Clock Divider 1: /4 */
#define SIFDIV1_8           (0x0003)    /* SIF Clock Divider 1: /8 */
#define SIFDIV2_1           (0x0000)    /* SIF Clock Divider 2: /1 */
#define SIFDIV2_2           (0x0004)    /* SIF Clock Divider 2: /2 */
#define SIFDIV2_4           (0x0008)    /* SIF Clock Divider 2: /4 */
#define SIFDIV2_8           (0x000C)    /* SIF Clock Divider 2: /8 */

#define SIFDIV3_1           (0x0000)    /* SIF Clock Divider 3: /1 */
#define SIFDIV3_3           (0x0010)    /* SIF Clock Divider 3: /3 */
#define SIFDIV3_5           (0x0020)    /* SIF Clock Divider 3: /5 */
#define SIFDIV3_7           (0x0030)    /* SIF Clock Divider 3: /7 */
#define SIFDIV3_9           (0x0040)    /* SIF Clock Divider 3: /9 */
#define SIFDIV3_11          (0x0050)    /* SIF Clock Divider 3: /11 */
#define SIFDIV3_13          (0x0060)    /* SIF Clock Divider 3: /13 */
#define SIFDIV3_15          (0x0070)    /* SIF Clock Divider 3: /15 */
#define SIFDIV3_21          (0x00B0)    /* SIF Clock Divider 3: /21 */
#define SIFDIV3_25          (0x0120)    /* SIF Clock Divider 3: /25 */
#define SIFDIV3_27          (0x00C0)    /* SIF Clock Divider 3: /27 */
#define SIFDIV3_33          (0x00E0)    /* SIF Clock Divider 3: /33 */
#define SIFDIV3_35          (0x00D0)    /* SIF Clock Divider 3: /35 */
#define SIFDIV3_39          (0x00E0)    /* SIF Clock Divider 3: /39 */
#define SIFDIV3_45          (0x00F0)    /* SIF Clock Divider 3: /45 */
#define SIFDIV3_49          (0x01B0)    /* SIF Clock Divider 3: /49 */
#define SIFDIV3_55          (0x0150)    /* SIF Clock Divider 3: /55 */
#define SIFDIV3_63          (0x01C0)    /* SIF Clock Divider 3: /63 */
#define SIFDIV3_65          (0x0160)    /* SIF Clock Divider 3: /65 */
#define SIFDIV3_75          (0x0170)    /* SIF Clock Divider 3: /75 */
#define SIFDIV3_77          (0x01D0)    /* SIF Clock Divider 3: /77 */
#define SIFDIV3_81          (0x0240)    /* SIF Clock Divider 3: /81 */
#define SIFDIV3_91          (0x01E0)    /* SIF Clock Divider 3: /91 */
#define SIFDIV3_99          (0x0250)    /* SIF Clock Divider 3: /99 */
#define SIFDIV3_105         (0x01F0)    /* SIF Clock Divider 3: /105 */
#define SIFDIV3_117         (0x0260)    /* SIF Clock Divider 3: /117 */
#define SIFDIV3_121         (0x02D0)    /* SIF Clock Divider 3: /121 */
#define SIFDIV3_135         (0x01F0)    /* SIF Clock Divider 3: /135 */
#define SIFDIV3_143         (0x02E0)    /* SIF Clock Divider 3: /143 */
#define SIFDIV3_165         (0x02F0)    /* SIF Clock Divider 3: /165 */
#define SIFDIV3_169         (0x0360)    /* SIF Clock Divider 3: /169 */
#define SIFDIV3_195         (0x0370)    /* SIF Clock Divider 3: /195 */
#define SIFDIV3_225         (0x03F0)    /* SIF Clock Divider 3: /225 */

/* SIFCTL5 */
#define SIFCLKEN            (0x0001)    /* SIF 0:SMCLK for SIFCLK / 1:SIFCLKG for SIFCLK */
#define SIFCLKGON           (0x0002)    /* SIF Switch SIFCLKG on */
#define SIFFNOM             (0x0004)    /* SIF Select Nominal Frequ. 0:4MHz / 1:1MHz */
#define SIFCLKFQ0           (0x0008)    /* SIF Clock Generator frequency adjust 0 */
#define SIFCLKFQ1           (0x0010)    /* SIF Clock Generator frequency adjust 1 */
#define SIFCLKFQ2           (0x0020)    /* SIF Clock Generator frequency adjust 2 */
#define SIFCLKFQ3           (0x0040)    /* SIF Clock Generator frequency adjust 3 */
#define SIFTSMRP            (0x0080)    /* SIF Timing State Machine Repeat mode */
#define SIFCNT30            (0x0100)    /* SIF Counter 3.0 */
#define SIFCNT31            (0x0200)    /* SIF Counter 3.1 */
#define SIFCNT32            (0x0400)    /* SIF Counter 3.2 */
#define SIFCNT33            (0x0800)    /* SIF Counter 3.3 */
#define SIFCNT34            (0x1000)    /* SIF Counter 3.4 */
#define SIFCNT35            (0x2000)    /* SIF Counter 3.5 */
#define SIFCNT36            (0x4000)    /* SIF Counter 3.6 */
#define SIFCNT37            (0x8000)    /* SIF Counter 3.7 */

/* SIFTSM */
#define SIFTSMCH0           (0x0001)    /* SIF Select channel for tsm: 0 */
#define SIFTSMCH1           (0x0002)    /* SIF Select channel for tsm: 1 */
#define SIFTSMLCOFF         (0x0004)    /* SIF Switch LC off */
#define SIFTSMEX            (0x0008)    /* SIF  */
#define SIFTSMCA            (0x0010)    /* SIF  */
#define SIFTSMCLKON         (0x0020)    /* SIF  */
#define SIFTSMRSON          (0x0040)    /* SIF  */
#define SIFTSMTESTS1        (0x0080)    /* SIF  */
#define SIFTSMDAC           (0x0100)    /* SIF  */
#define SIFTSMSTOP          (0x0200)    /* SIF  */
#define SIFTSMACLK          (0x0400)    /* SIF  */
#define SIFTSMREPEAT0       (0x0800)    /* SIF  */
#define SIFTSMREPEAT1       (0x1000)    /* SIF  */
#define SIFTSMREPEAT2       (0x2000)    /* SIF  */
#define SIFTSMREPEAT3       (0x4000)    /* SIF  */
#define SIFTSMREPEAT4       (0x8000)    /* SIF  */

#endif
