#ifndef __msp430_headers_lcd_h
#define __msp430_headers_lcd_h

/* lcd.h
 *
 * mspgcc project: MSP430 device headers
 * LCD module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: lcd.h,v 1.7 2008/10/09 15:00:14 sb-sf Exp $
 */

/* Switches:

LCD_BASE - base address of LCD module
__msp430_have_lcdlowr - if LCD controller supports the LCDLOWR bit
__msp430_have_lcd_16_20 - if LCD controller supports memory locations 16 to 20

*/

#define LCDCTL_             LCD_BASE                /* LCD control */
sfrb(LCDCTL,LCDCTL_);

/* the names of the mode bits are different from the spec */
#define LCDON               0x01
#if defined(__msp430_have_lcdlowr)
#define LCDLOWR             0x02
#endif
#define LCDSON              0x04
#define LCDMX0              0x08
#define LCDMX1              0x10
#define LCDP0               0x20
#define LCDP1               0x40
#define LCDP2               0x80
/* Display modes coded with Bits 2-4 */
#define LCDSTATIC           (LCDSON)
#define LCD2MUX             (LCDMX0|LCDSON)
#define LCD3MUX             (LCDMX1|LCDSON)
#define LCD4MUX             (LCDMX1|LCDMX0|LCDSON)
/* Group select code with Bits 5-7                     Seg.lines */
#define LCDSG0              0x00                    /* --------- */
#define LCDSG0_1            (LCDP0)                 /* S0  - S15 */
#define LCDSG0_2            (LCDP1)                 /* S0  - S19 */
#define LCDSG0_3            (LCDP1|LCDP0)           /* S0  - S23 */
#define LCDSG0_4            (LCDP2)                 /* S0  - S27 */
#define LCDSG0_5            (LCDP2|LCDP0)           /* S0  - S31 */
#define LCDSG0_6            (LCDP2|LCDP1)           /* S0  - S35 */
#define LCDSG0_7            (LCDP2|LCDP1|LCDP0)     /* S0  - S39 */

/* NOTE: YOU CAN ONLY USE THE 'S' OR 'G' DECLARATIONS FOR A COMMAND */
/* MOV  #LCDSG0_3+LCDOG2_7,&LCDCTL ACTUALLY MEANS MOV  #LCDP1,&LCDCTL! */
#define LCDOG1_7            0x00                    /* --------- */
#define LCDOG2_7            (LCDP0)                 /* S0  - S15 */
#define LCDOG3_7            (LCDP1)                 /* S0  - S19 */
#define LCDOG4_7            (LCDP1|LCDP0)           /* S0  - S23 */
#define LCDOG5_7            (LCDP2)                 /* S0  - S27 */
#define LCDOG6_7            (LCDP2|LCDP0)           /* S0  - S31 */
#define LCDOG7              (LCDP2|LCDP1)           /* S0  - S35 */
#define LCDOGOFF            (LCDP2|LCDP1|LCDP0)     /* S0  - S39 */

#define LCDMEM_             LCD_BASE+1              /* LCD memory */
#if defined(__ASSEMBLER__)
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
