#ifndef __msp430_headers_adc10_h
#define __msp430_headers_adc10_h

/* adc10.h
 *
 * mspgcc project: MSP430 device headers
 * ADC10 module header
 *
 * (c) 2002 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: adc10.h,v 1.9 2008/10/09 15:00:14 sb-sf Exp $
 */

/* Switches: none */

#define ADC10DTC0_          0x0048      /* ADC10 Data Transfer Control 0 */
sfrb(ADC10DTC0, ADC10DTC0_);
#define ADC10DTC1_          0x0049      /* ADC10 Data Transfer Control 1 */
sfrb(ADC10DTC1, ADC10DTC1_);
#define ADC10AE0_           0x004A      /* ADC10 Analog Enable */
sfrb(ADC10AE0, ADC10AE0_);

#if defined(__msp430_have_adc10ae2)
#define ADC10AE1_           0x004B      /* ADC10 Analog Enable */
sfrb(ADC10AE1, ADC10AE1_);
#endif

/* Alternate register names */
#define ADC10AE_        ADC10AE0_
#define ADC10AE         ADC10AE0

#define ADC10CTL0_          0x01B0      /* ADC10 Control 0 */
sfrw(ADC10CTL0, ADC10CTL0_);
#define ADC10CTL1_          0x01B2      /* ADC10 Control 1 */
sfrw(ADC10CTL1, ADC10CTL1_);
#define ADC10MEM_           0x01B4      /* ADC10 Memory */
sfrw(ADC10MEM, ADC10MEM_);
#define ADC10SA_            0x01BC      /* ADC10 Data Transfer Start Address */
sfrw(ADC10SA, ADC10SA_);

#ifndef __ASSEMBLER__
/* Structured declaration */
typedef struct {
  volatile unsigned
    adc10sc:1,
    enc:1,
    adc10ifg:1,
    adc10ie:1,
    adc10on:1,
    refon:1,
    r2_5v:1,
    msc:1,
    adc10sr:4,
    adc10sht:4;
} __attribute__ ((packed)) adc10ctl0_t;

typedef struct {
  volatile unsigned
    adc10busy:1,
    conseq:2,
    adc10ssel:2,
    adc10div:3;
} __attribute__ ((packed)) adc10ctl1_t;

/* The adc10 declaration itself */
struct adc10_t {
  adc10ctl0_t ctl0;
  adc10ctl1_t ctl1;
  volatile unsigned mem;
  volatile unsigned sa;
};
#ifdef __cplusplus
extern "C" struct adc10_t adc10 asm("0x01B0");
#else //__cplusplus
struct adc10_t adc10 asm("0x01B0");
#endif //__cplusplus

#endif

#define ADC10SC             0x0001      /* ADC10CTL0 */
#define ENC                 0x0002
#define ADC10IFG            0x0004
#define ADC10IE             0x0008
#define ADC10ON             0x0010
#define REFON               0x0020
#define REF2_5V             0x0040
#define MSC                 0x0080
#define REFBURST            0x0100
#define REFOUT              0x0200
#define ADC10SR             0x0400

#define ADC10SHT_0          (0<<11)     /* 4 x ADC10CLKs */
#define ADC10SHT_1          (1<<11)     /* 8 x ADC10CLKs */
#define ADC10SHT_2          (2<<11)     /* 16 x ADC10CLKs */
#define ADC10SHT_3          (3<<11)     /* 64 x ADC10CLKs */

#define SREF_0              (0<<13)     /* VR+ = AVCC and VR- = AVSS */
#define SREF_1              (1<<13)     /* VR+ = VREF+ and VR- = AVSS */
#define SREF_2              (2<<13)     /* VR+ = VEREF+ and VR- = AVSS */
#define SREF_3              (3<<13)     /* VR+ = VEREF+ and VR- = AVSS */
#define SREF_4              (4<<13)     /* VR+ = AVCC and VR- = VREF-/VEREF- */
#define SREF_5              (5<<13)     /* VR+ = VREF+ and VR- = VREF-/VEREF- */
#define SREF_6              (6<<13)     /* VR+ = VEREF+ and VR- = VREF-/VEREF- */
#define SREF_7              (7<<13)     /* VR+ = VEREF+ and VR- = VREF-/VEREF- */

#define ADC10BUSY           0x0001      /* ADC10CTL1 */

#define CONSEQ_0            (0<<1)      /* Single channel single conversion */
#define CONSEQ_1            (1<<1)      /* Sequence of channels */
#define CONSEQ_2            (2<<1)      /* Repeat single channel */
#define CONSEQ_3            (3<<1)      /* Repeat sequence of channels */

#define ADC10SSEL_0         (0<<3)      /* ADC10OSC */
#define ADC10SSEL_1         (1<<3)      /* ACLK */
#define ADC10SSEL_2         (2<<3)      /* MCLK */
#define ADC10SSEL_3         (3<<3)      /* SMCLK */

#define ADC10DIV_0          (0<<5)
#define ADC10DIV_1          (1<<5)
#define ADC10DIV_2          (2<<5)
#define ADC10DIV_3          (3<<5)
#define ADC10DIV_4          (4<<5)
#define ADC10DIV_5          (5<<5)
#define ADC10DIV_6          (6<<5)
#define ADC10DIV_7          (7<<5)

#define ISSH                0x0100
#define ADC10DF             0x0200

#define SHS_0               (0<<10)     /* ADC10SC */
#define SHS_1               (1<<10)     /* TA3 OUT1 */
#define SHS_2               (2<<10)     /* TA3 OUT0 */
#define SHS_3               (3<<10)     /* TA3 OUT2 */

#define INCH_0              (0<<12)     /* A0 */
#define INCH_1              (1<<12)     /* A1 */
#define INCH_2              (2<<12)     /* A2 */
#define INCH_3              (3<<12)     /* A3 */
#define INCH_4              (4<<12)     /* A4 */
#define INCH_5              (5<<12)     /* A5 */
#define INCH_6              (6<<12)     /* A6 */
#define INCH_7              (7<<12)     /* A7 */
#define INCH_8              (8<<12)     /* VeREF+ */
#define INCH_9              (9<<12)     /* VREF-/VeREF- */
#define INCH_10             (10<<12)    /* Temperature sensor */
#define INCH_11             (11<<12)    /* (VCC - VSS) / 2 */
#define INCH_12             (12<<12)    /* Selects channel 11 */
#define INCH_13             (13<<12)    /* Selects channel 11 */
#define INCH_14             (14<<12)    /* Selects channel 11 */
#define INCH_15             (15<<12)    /* Selects channel 11 */

#define ADC10FETCH          0x0001      /* ADC10DTC0 */
#define ADC10B1             0x0002
#define ADC10CT             0x0004
#define ADC10TB             0x0008

#define ADC10DISABLE        0x0000      /* ADC10DTC1 */

/* Aliases by mspgcc */
#define ADC10SHT_DIV4       ADC10SHT_0  /* 4 x ADC10CLKs */
#define ADC10SHT_DIV8       ADC10SHT_1  /* 8 x ADC10CLKs */
#define ADC10SHT_DIV16      ADC10SHT_2  /* 16 x ADC10CLKs */
#define ADC10SHT_DIV64      ADC10SHT_3  /* 64 x ADC10CLKs */

#define SREF_AVCC_AVSS      SREF_0      /* VR+ = AVCC and VR- = AVSS */
#define SREF_VREF_AVSS      SREF_1      /* VR+ = VREF+ and VR- = AVSS */
#define SREF_VEREF_AVSS     SREF_2      /* VR+ = VEREF+ and VR- = AVSS */
//~ #define SREF_VEREF_AVSS     SREF_3      /* VR+ = VEREF+ and VR- = AVSS */
#define SREF_AVCC_VEREF     SREF_4      /* VR+ = AVCC and VR- = VREF-/VEREF- */
#define SREF_VREF_VEREF     SREF_5      /* VR+ = VREF+ and VR- = VREF-/VEREF- */
#define SREF_VEREF_VEREF    SREF_6      /* VR+ = VEREF+ and VR- = VREF-/VEREF- */
//~ #define SREF_VEREF_VEREF    SREF_7      /* VR+ = VEREF+ and VR- = VREF-/VEREF- */

#define ADC10SSEL_ADC10OSC  ADC10SSEL_0 /* ADC10OSC */
#define ADC10SSEL_ACLK      ADC10SSEL_1 /* ACLK */
#define ADC10SSEL_MCLK      ADC10SSEL_2 /* MCLK */
#define ADC10SSEL_SMCLK     ADC10SSEL_3 /* SMCLK */

#define INCH_A0             INCH_0      /* A0 */
#define INCH_A1             INCH_1      /* A1 */
#define INCH_A2             INCH_2      /* A2 */
#define INCH_A3             INCH_3      /* A3 */
#define INCH_A4             INCH_4      /* A4 */
#define INCH_A5             INCH_5      /* A5 */
#define INCH_A6             INCH_6      /* A6 */
#define INCH_A7             INCH_7      /* A7 */
#define INCH_VEREF_PLUS     INCH_8      /* VeREF+ */
#define INCH_VEREF_MINUS    INCH_9      /* VREF-/VeREF- */
#define INCH_TEMP           INCH_10     /* Temperature sensor */
#define INCH_VCC2           INCH_11     /* (VCC - VSS) / 2 */

#endif
