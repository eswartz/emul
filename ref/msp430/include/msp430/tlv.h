#ifndef __msp430_headers_tlv_h
#define __msp430_headers_tlv_h

/* tlv.h
 *
 * mspgcc project: MSP430 device headers
 * tlv structures
 *
 * (c) 2008 by Sergey A. Borshch <sb-sf@users.sf.net>
 *
 * $Id: tlv.h,v 1.2 2009/01/11 23:11:48 sb-sf Exp $
 */

/* Switches: none */


/* TLV Calibration Data Structure */
#define TAG_DCO_30             0x01      /* Tag for DCO30  Calibration Data */
#define TAG_ADC12_1            0x10      /* Tag for ADC12_1 Calibration Data */
#define TAG_EMPTY              0xFE      /* Tag for Empty Data Field in Calibration Data */

#define TLV_CHECKSUM_          0x10C0    /* TLV CHECK SUM */
sfrw(TLV_CHECKSUM, TLV_CHECKSUM_);
#define TLV_DCO_30_TAG_        0x10F6    /* TLV TAG_DCO30 TAG */
sfrb(TLV_DCO_30_TAG, TLV_DCO_30_TAG_);
#define TLV_DCO_30_LEN_        0x10F7    /* TLV TAG_DCO30 LEN */
sfrb(TLV_DCO_30_LEN, TLV_DCO_30_LEN_);
#define TLV_ADC12_1_TAG_       0x10DA    /* TLV ADC12_1 TAG */
sfrb(TLV_ADC12_1_TAG, TLV_ADC12_1_TAG_);
#define TLV_ADC12_1_LEN_       0x10DB    /* TLV ADC12_1 LEN */
sfrb(TLV_ADC12_1_LEN, TLV_ADC12_1_LEN_);

#define CAL_ADC_25T85          0x0007    /* Index for 2.5V/85Deg Cal. Value */
#define CAL_ADC_25T30          0x0006    /* Index for 2.5V/30Deg Cal. Value */
#define CAL_ADC_25VREF_FACTOR  0x0005    /* Index for 2.5V Ref. Factor */
#define CAL_ADC_15T85          0x0004    /* Index for 1.5V/85Deg Cal. Value */
#define CAL_ADC_15T30          0x0003    /* Index for 1.5V/30Deg Cal. Value */
#define CAL_ADC_15VREF_FACTOR  0x0002    /* Index for ADC 1.5V Ref. Factor */
#define CAL_ADC_OFFSET         0x0001    /* Index for ADC Offset */
#define CAL_ADC_GAIN_FACTOR    0x0000    /* Index for ADC Gain Factor */

#define CALDCO_16MHZ_         0x10F8    /* DCOCTL  Calibration Data for 16MHz */
sfrb(CALDCO_16MHZ, CALDCO_16MHZ_);
#define CALBC1_16MHZ_         0x10F9    /* BCSCTL1 Calibration Data for 16MHz */
sfrb(CALBC1_16MHZ, CALBC1_16MHZ_);
#define CALDCO_12MHZ_         0x10FA    /* DCOCTL  Calibration Data for 12MHz */
sfrb(CALDCO_12MHZ, CALDCO_12MHZ_);
#define CALBC1_12MHZ_         0x10FB    /* BCSCTL1 Calibration Data for 12MHz */
sfrb(CALBC1_12MHZ, CALBC1_12MHZ_);
#define CALDCO_8MHZ_          0x10FC    /* DCOCTL  Calibration Data for 8MHz */
sfrb(CALDCO_8MHZ, CALDCO_8MHZ_);
#define CALBC1_8MHZ_          0x10FD    /* BCSCTL1 Calibration Data for 8MHz */
sfrb(CALBC1_8MHZ, CALBC1_8MHZ_);
#define CALDCO_1MHZ_          0x10FE    /* DCOCTL  Calibration Data for 1MHz */
sfrb(CALDCO_1MHZ, CALDCO_1MHZ_);
#define CALBC1_1MHZ_          0x10FF    /* BCSCTL1 Calibration Data for 1MHz */
sfrb(CALBC1_1MHZ, CALBC1_1MHZ_);

#define CAL_DCO_16MHZ          0x0000    /* Index for DCOCTL  Calibration Data for 16MHz */
#define CAL_BC1_16MHZ          0x0001    /* Index for BCSCTL1 Calibration Data for 16MHz */
#define CAL_DCO_12MHZ          0x0002    /* Index for DCOCTL  Calibration Data for 12MHz */
#define CAL_BC1_12MHZ          0x0003    /* Index for BCSCTL1 Calibration Data for 12MHz */
#define CAL_DCO_8MHZ           0x0004    /* Index for DCOCTL  Calibration Data for 8MHz */
#define CAL_BC1_8MHZ           0x0005    /* Index for BCSCTL1 Calibration Data for 8MHz */
#define CAL_DCO_1MHZ           0x0006    /* Index for DCOCTL  Calibration Data for 1MHz */
#define CAL_BC1_1MHZ           0x0007    /* Index for BCSCTL1 Calibration Data for 1MHz */

#ifndef __ASSEMBLER__
/* Structured declaration */
typedef enum
{
    DCO_30_TAG      = 0x01,
    ADC12_1_TAG     = 0x10,
    EMPTY_TAG       = 0xFE,

} tlv_tags_t;

typedef struct
{
    unsigned char tag;
    unsigned char length;
    struct
    {
        unsigned char DCO_16MHZ;
        unsigned char BC1_16MHZ;
        unsigned char DCO_12MHZ;
        unsigned char BC1_12MHZ;
        unsigned char DCO_8MHZ;
        unsigned char BC1_8MHZ;
        unsigned char DCO_1MHZ;
        unsigned char BC1_1MHZ;
    } value;
} const dco_30_tag_t;

typedef struct
{
    unsigned char tag;
    unsigned char length;
    struct
    {
        unsigned int ADC_GAIN_FACTOR;
        unsigned int ADC_OFFSET;
        unsigned int ADC_15VREF_FACTOR;
        unsigned int ADC_15T30;
        unsigned int ADC_15T85;
        unsigned int ADC_25VREF_FACTOR;
        unsigned int ADC_25T30;
        unsigned int ADC_25T85;
    } value;
} const adc12_1_tag_t;

typedef struct
{
    unsigned char tag;
    unsigned char length;
} const empty_tag_t;

struct
{
    unsigned int checksum;
    empty_tag_t  empty;
    unsigned int dummy[11];
    adc12_1_tag_t adc12_1;
    dco_30_tag_t dco_30;
} const volatile TLV_bits asm("0x10c0");
#endif  // __ASSEMBLER__

#endif  /* __msp430_headers_tlv_h */
