#if !defined(__msp430_headers_lcd_a_h)
#define __msp430_headers_lcd_a_h

/* lcd_a.h
 *
 * mspgcc project: MSP430 device headers
 * LCD_A module header
 *
 * (c) 2005 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: lcd_a.h,v 1.4 2008/10/09 15:00:14 sb-sf Exp $
 */

/* Switches: none */

#define LCDACTL_            0x0090      /* LCD_A Control Register */
sfrb(LCDACTL, LCDACTL_);
#define LCDON               0x01
#define LCDSON              0x04
#define LCDMX0              0x08
#define LCDMX1              0x10
#define LCDFREQ0            0x20
#define LCDFREQ1            0x40
#define LCDFREQ2            0x80

/* Display modes coded with Bits 2-4 */
#define LCDSTATIC           (LCDSON)
#define LCD2MUX             (LCDMX0|LCDSON)
#define LCD3MUX             (LCDMX1|LCDSON)
#define LCD4MUX             (LCDMX1|LCDMX0|LCDSON)

/* Frequency select code with Bits 5-7 */
#define LCDFREQ_32          (0<<5)      /* LCD Freq: ACLK divided by 32 */
#define LCDFREQ_64          (1<<5)      /* LCD Freq: ACLK divided by 64 */
#define LCDFREQ_96          (2<<5)      /* LCD Freq: ACLK divided by 96 */
#define LCDFREQ_128         (3<<5)      /* LCD Freq: ACLK divided by 128 */
#define LCDFREQ_192         (4<<5)      /* LCD Freq: ACLK divided by 192 */
#define LCDFREQ_256         (5<<5)      /* LCD Freq: ACLK divided by 256 */
#define LCDFREQ_384         (6<<5)      /* LCD Freq: ACLK divided by 384 */
#define LCDFREQ_512         (7<<5)      /* LCD Freq: ACLK divided by 512 */

#define LCDAPCTL0_          0x00AC      /* LCD_A Port Control Register 0 */
sfrb(LCDAPCTL0, LCDAPCTL0_);
#define LCDS0               0x01        /* LCD Segment  0 to  3 Enable. */
#define LCDS4               0x02        /* LCD Segment  4 to  7 Enable. */
#define LCDS8               0x04        /* LCD Segment  8 to 11 Enable. */
#define LCDS12              0x08        /* LCD Segment 12 to 15 Enable. */
#define LCDS16              0x10        /* LCD Segment 16 to 19 Enable. */
#define LCDS20              0x20        /* LCD Segment 20 to 23 Enable. */
#define LCDS24              0x40        /* LCD Segment 24 to 27 Enable. */
#define LCDS28              0x80        /* LCD Segment 28 to 31 Enable. */

#define LCDAPCTL1_          0x00AD      /* LCD_A Port Control Register 1 */
sfrb(LCDAPCTL1, LCDAPCTL1_);
#define LCDS32              0x01        /* LCD Segment 32 to 35 Enable. */
#define LCDS36              0x02        /* LCD Segment 36 to 39 Enable. */

#define LCDAVCTL0_          0x00AE      /* LCD_A Voltage Control Register 0 */
sfrb(LCDAVCTL0, LCDAVCTL0_);
#define LCD2B               0x01        /* Selects 1/2 bias. */
#define VLCDREF0            0x02        /* Selects reference voltage for regulated charge pump: 0 */
#define VLCDREF1            0x04        /* Selects reference voltage for regulated charge pump: 1 */
#define LCDCPEN             0x08        /* LCD Voltage Charge Pump Enable. */
#define VLCDEXT             0x10        /* Select external source for VLCD. */
#define LCDREXT             0x20        /* Selects external connections for LCD mid voltages. */
#define LCDR03EXT           0x40        /* Selects external connection for lowest LCD voltage. */

/* Reference voltage source select for the regulated charge pump */
#define VLCDREF_0           (0<<1)      /* Internal */
#define VLCDREF_1           (1<<1)      /* External */
#define VLCDREF_2           (2<<1)      /* Reserved */
#define VLCDREF_3           (3<<1)      /* Reserved */

#define LCDAVCTL1_          0x00AF      /* LCD_A Voltage Control Register 1 */
sfrb(LCDAVCTL1, LCDAVCTL1_);
#define VLCD0               0x02        /* VLCD select: 0 */
#define VLCD1               0x04        /* VLCD select: 1 */
#define VLCD2               0x08        /* VLCD select: 2 */
#define VLCD3               0x10        /* VLCD select: 3 */

/* Charge pump voltage selections */
#define VLCD_0              (0<<1)      /* Charge pump disabled */
#define VLCD_1              (1<<1)      /* VLCD = 2.60V */
#define VLCD_2              (2<<1)      /* VLCD = 2.66V */
#define VLCD_3              (3<<1)      /* VLCD = 2.72V */
#define VLCD_4              (4<<1)      /* VLCD = 2.78V */
#define VLCD_5              (5<<1)      /* VLCD = 2.84V */
#define VLCD_6              (6<<1)      /* VLCD = 2.90V */
#define VLCD_7              (7<<1)      /* VLCD = 2.96V */
#define VLCD_8              (8<<1)      /* VLCD = 3.02V */
#define VLCD_9              (9<<1)      /* VLCD = 3.08V */
#define VLCD_10             (10<<1)     /* VLCD = 3.14V */
#define VLCD_11             (11<<1)     /* VLCD = 3.20V */
#define VLCD_12             (12<<1)     /* VLCD = 3.26V */
#define VLCD_13             (12<<1)     /* VLCD = 3.32V */
#define VLCD_14             (13<<1)     /* VLCD = 3.38V */
#define VLCD_15             (15<<1)     /* VLCD = 3.44V */

#define VLCD_DISABLED       (0<<1)      /* Charge pump disabled */
#define VLCD_2_60           (1<<1)      /* VLCD = 2.60V */
#define VLCD_2_66           (2<<1)      /* VLCD = 2.66V */
#define VLCD_2_72           (3<<1)      /* VLCD = 2.72V */
#define VLCD_2_78           (4<<1)      /* VLCD = 2.78V */
#define VLCD_2_84           (5<<1)      /* VLCD = 2.84V */
#define VLCD_2_90           (6<<1)      /* VLCD = 2.90V */
#define VLCD_2_96           (7<<1)      /* VLCD = 2.96V */
#define VLCD_3_02           (8<<1)      /* VLCD = 3.02V */
#define VLCD_3_08           (9<<1)      /* VLCD = 3.08V */
#define VLCD_3_14           (10<<1)     /* VLCD = 3.14V */
#define VLCD_3_20           (11<<1)     /* VLCD = 3.20V */
#define VLCD_3_26           (12<<1)     /* VLCD = 3.26V */
#define VLCD_3_32           (12<<1)     /* VLCD = 3.32V */
#define VLCD_3_38           (13<<1)     /* VLCD = 3.38V */
#define VLCD_3_44           (15<<1)     /* VLCD = 3.44V */

#define LCDMEM_             LCD_BASE+1              /* LCD memory */
#ifdef __ASSEMBLER__
#define LCDMEM              LCDMEM_                 /* LCD memory (for assembler) */
#else
#define LCDMEM              ((char*) LCDMEM_)       /* LCD memory (for C) */
#endif
#define LCDM1_              LCDMEM_                 /* LCD memory 1 */
sfrb(LCDM1,LCDM1_);
#define LCDM2_              LCD_BASE+0x2            /* LCD memory 2 */
sfrb(LCDM2,LCDM2_);
#define LCDM3_              LCD_BASE+0x3            /* LCD memory 3 */
sfrb(LCDM3,LCDM3_);
#define LCDM4_              LCD_BASE+0x4            /* LCD memory 4 */
sfrb(LCDM4,LCDM4_);
#define LCDM5_              LCD_BASE+0x5            /* LCD memory 5 */
sfrb(LCDM5,LCDM5_);
#define LCDM6_              LCD_BASE+0x6            /* LCD memory 6 */
sfrb(LCDM6,LCDM6_);
#define LCDM7_              LCD_BASE+0x7            /* LCD memory 7 */
sfrb(LCDM7,LCDM7_);
#define LCDM8_              LCD_BASE+0x8            /* LCD memory 8 */
sfrb(LCDM8,LCDM8_);
#define LCDM9_              LCD_BASE+0x9            /* LCD memory 9 */
sfrb(LCDM9,LCDM9_);
#define LCDM10_             LCD_BASE+0xA            /* LCD memory 10 */
sfrb(LCDM10,LCDM10_);
#define LCDM11_             LCD_BASE+0xB            /* LCD memory 11 */
sfrb(LCDM11,LCDM11_);
#define LCDM12_             LCD_BASE+0xC            /* LCD memory 12 */
sfrb(LCDM12,LCDM12_);
#define LCDM13_             LCD_BASE+0xD            /* LCD memory 13 */
sfrb(LCDM13,LCDM13_);
#define LCDM14_             LCD_BASE+0xE            /* LCD memory 14 */
sfrb(LCDM14,LCDM14_);
#define LCDM15_             LCD_BASE+0xF            /* LCD memory 15 */
sfrb(LCDM15,LCDM15_);

#define LCDMA_              LCDM10_                 /* LCD memory A */
sfrb(LCDMA,LCDMA_);
#define LCDMB_              LCDM11_                 /* LCD memory B */
sfrb(LCDMB,LCDMB_);
#define LCDMC_              LCDM12_                 /* LCD memory C */
sfrb(LCDMC,LCDMC_);
#define LCDMD_              LCDM13_                 /* LCD memory D */
sfrb(LCDMD,LCDMD_);
#define LCDME_              LCDM14_                 /* LCD memory E */
sfrb(LCDME,LCDME_);
#define LCDMF_              LCDM15_                 /* LCD memory F */
sfrb(LCDMF,LCDMF_);

#if defined(__msp430_have_lcd_16_20)
#define LCDM16_             LCD_BASE+0x10           /* LCD memory 16 */
sfrb(LCDM16,LCDM16_);
#define LCDM17_             LCD_BASE+0x11           /* LCD memory 17 */
sfrb(LCDM17,LCDM17_);
#define LCDM18_             LCD_BASE+0x12           /* LCD memory 18 */
sfrb(LCDM18,LCDM18_);
#define LCDM19_             LCD_BASE+0x13           /* LCD memory 19 */
sfrb(LCDM19,LCDM19_);
#define LCDM20_             LCD_BASE+0x14           /* LCD memory 20 */
sfrb(LCDM20,LCDM20_);
#endif

#endif
