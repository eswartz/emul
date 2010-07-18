#if !defined(__msp430_headers_usci_h__)
#define __msp430_headers_usci_h__

/* usi.h
 *
 * mspgcc project: MSP430 device headers
 * USCI module header
 *
 * (c) 2006 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: usci.h,v 1.7 2009/06/04 21:55:18 cliechti Exp $
 *
 * 2008-02-05 - modifications by G.Lemm <msp430@daqtix.com>
 * - added UC1IE and UC1IFG registers
 * - added UC*1*IE and UC*1*IFG bit definitions
 * 2009-05-19 - modifications by S. Balling <praktikum@innoventis.de>
 * - added switch for USCI0
 * - added USCI0_5
 * - added USCI1_5
 * - added USCI2_5
 * - added USCI3_5
 */

/* Switches:
__MSP430_HAS_USCI0__ - if device has USCI0
__MSP430_HAS_USCI1__ - if device has USCI1
__MSP430_HAS_USCI0_5__ - if device has USCI0 and is part of family 5
__MSP430_HAS_USCI1_5__ - if device has USCI1 and is part of family 5
__MSP430_HAS_USCI2_5__ - if device has USCI2 and is part of family 5
__MSP430_HAS_USCI3_5__ - if device has USCI3 and is part of family 5

*/

#define USIPE7              (0x80)      /* USI  Port Enable Px.7 */
#define USIPE6              (0x40)      /* USI  Port Enable Px.6 */
#define USIPE5              (0x20)      /* USI  Port Enable Px.5 */
#define USILSB              (0x10)      /* USI  LSB first  1:LSB / 0:MSB */
#define USIMST              (0x08)      /* USI  Master Select  0:Slave / 1:Master */
#define USIGE               (0x04)      /* USI  General Output Enable Latch */
#define USIOE               (0x02)      /* USI  Output Enable */
#define USISWRST            (0x01)      /* USI  Software Reset */

#define USICKPH             (0x80)      /* USI  Sync. Mode: Clock Phase */
#define USII2C              (0x40)      /* USI  I2C Mode */ 
#define USISTTIE            (0x20)      /* USI  START Condition interrupt enable */
#define USIIE               (0x10)      /* USI  Counter Interrupt enable */
#define USIAL               (0x08)      /* USI  Arbitration Lost */
#define USISTP              (0x04)      /* USI  STOP Condition received */
#define USISTTIFG           (0x02)      /* USI  START Condition interrupt Flag */
#define USIIFG              (0x01)      /* USI  Counter Interrupt Flag */

#define USIDIV2             (0x80)      /* USI  Clock Divider 2 */
#define USIDIV1             (0x40)      /* USI  Clock Divider 1 */ 
#define USIDIV0             (0x20)      /* USI  Clock Divider 0 */
#define USISSEL2            (0x10)      /* USI  Clock Source Select 2 */
#define USISSEL1            (0x08)      /* USI  Clock Source Select 1 */
#define USISSEL0            (0x04)      /* USI  Clock Source Select 0 */
#define USICKPL             (0x02)      /* USI  Clock Polarity 0:Inactive=Low / 1:Inactive=High */
#define USISWCLK            (0x01)      /* USI  Software Clock */

#define USIDIV_0            (0x00)      /* USI  Clock Divider: 0 */
#define USIDIV_1            (0x20)      /* USI  Clock Divider: 1 */
#define USIDIV_2            (0x40)      /* USI  Clock Divider: 2 */
#define USIDIV_3            (0x60)      /* USI  Clock Divider: 3 */
#define USIDIV_4            (0x80)      /* USI  Clock Divider: 4 */
#define USIDIV_5            (0xA0)      /* USI  Clock Divider: 5 */
#define USIDIV_6            (0xC0)      /* USI  Clock Divider: 6 */
#define USIDIV_7            (0xE0)      /* USI  Clock Divider: 7 */

#define USISSEL_0           (0x00)      /* USI  Clock Source: 0 */
#define USISSEL_1           (0x04)      /* USI  Clock Source: 1 */
#define USISSEL_2           (0x08)      /* USI  Clock Source: 2 */
#define USISSEL_3           (0x0C)      /* USI  Clock Source: 3 */
#define USISSEL_4           (0x10)      /* USI  Clock Source: 4 */
#define USISSEL_5           (0x14)      /* USI  Clock Source: 5 */
#define USISSEL_6           (0x18)      /* USI  Clock Source: 6 */
#define USISSEL_7           (0x1C)      /* USI  Clock Source: 7 */

#define USISCLREL           (0x80)      /* USI  SCL Released */
#define USI16B              (0x40)      /* USI  16 Bit Shift Register Enable */ 
#define USIFGDC             (0x20)      /* USI  Interrupt Flag don't clear */
#define USICNT4             (0x10)      /* USI  Bit Count 4 */
#define USICNT3             (0x08)      /* USI  Bit Count 3 */
#define USICNT2             (0x04)      /* USI  Bit Count 2 */
#define USICNT1             (0x02)      /* USI  Bit Count 1 */
#define USICNT0             (0x01)      /* USI  Bit Count 0 */

// UART-Mode Bits
#define UCPEN               (0x80)      /* Async. Mode: Parity enable */
#define UCPAR               (0x40)      /* Async. Mode: Parity     0:odd / 1:even */
#define UCMSB               (0x20)      /* Async. Mode: MSB first  0:LSB / 1:MSB */
#define UC7BIT              (0x10)      /* Async. Mode: Data Bits  0:8-bits / 1:7-bits */
#define UCSPB               (0x08)      /* Async. Mode: Stop Bits  0:one / 1: two */
#define UCMODE1             (0x04)      /* Async. Mode: USCI Mode 1 */
#define UCMODE0             (0x02)      /* Async. Mode: USCI Mode 0 */
#define UCSYNC              (0x01)      /* Sync-Mode  0:UART-Mode / 1:SPI-Mode */

// SPI-Mode Bits
#define UCCKPH              (0x80)      /* Sync. Mode: Clock Phase */
#define UCCKPL              (0x40)      /* Sync. Mode: Clock Polarity */
#define UCMST               (0x08)      /* Sync. Mode: Master Select */

// I2C-Mode Bits
#define UCA10               (0x80)      /* 10-bit Address Mode */
#define UCSLA10             (0x40)      /* 10-bit Slave Address Mode */
#define UCMM                (0x20)      /* Multi-Master Environment */
//#define res               (0x10)      /* reserved */
#define UCMODE_0            (0<<1)      /* Sync. Mode: USCI Mode: 0 */
#define UCMODE_1            (1<<1)      /* Sync. Mode: USCI Mode: 1 */
#define UCMODE_2            (2<<1)      /* Sync. Mode: USCI Mode: 2 */
#define UCMODE_3            (3<<1)      /* Sync. Mode: USCI Mode: 3 */

// UART-Mode Bits
#define UCSSEL1             (0x80)      /* USCI 0 Clock Source Select 1 */
#define UCSSEL0             (0x40)      /* USCI 0 Clock Source Select 0 */
#define UCRXEIE             (0x20)      /* RX Error interrupt enable */
#define UCBRKIE             (0x10)      /* Break interrupt enable */
#define UCDORM              (0x08)      /* Dormant (Sleep) Mode */
#define UCTXADDR            (0x04)      /* Send next Data as Address */
#define UCTXBRK             (0x02)      /* Send next Data as Break */
#define UCSWRST             (0x01)      /* USCI Software Reset */

// SPI-Mode Bits
//#define res               (0x20)      /* reserved */
//#define res               (0x10)      /* reserved */
//#define res               (0x08)      /* reserved */
//#define res               (0x04)      /* reserved */
//#define res               (0x02)      /* reserved */

// I2C-Mode Bits
//#define res               (0x20)      /* reserved */
#define UCTR                (0x10)      /* Transmit/Receive Select/Flag */
#define UCTXNACK            (0x08)      /* Transmit NACK */
#define UCTXSTP             (0x04)      /* Transmit STOP */
#define UCTXSTT             (0x02)      /* Transmit START */
#define UCSSEL_0            (0<<6)      /* USCI 0 Clock Source: 0 */
#define UCSSEL_1            (1<<6)      /* USCI 0 Clock Source: 1 */
#define UCSSEL_2            (2<<6)      /* USCI 0 Clock Source: 2 */
#define UCSSEL_3            (3<<6)      /* USCI 0 Clock Source: 3 */

#define UCBRF3              (0x80)      /* USCI First Stage Modulation Select 3 */
#define UCBRF2              (0x40)      /* USCI First Stage Modulation Select 2 */
#define UCBRF1              (0x20)      /* USCI First Stage Modulation Select 1 */
#define UCBRF0              (0x10)      /* USCI First Stage Modulation Select 0 */
#define UCBRS2              (0x08)      /* USCI Second Stage Modulation Select 2 */
#define UCBRS1              (0x04)      /* USCI Second Stage Modulation Select 1 */
#define UCBRS0              (0x02)      /* USCI Second Stage Modulation Select 0 */
#define UCOS16              (0x01)      /* USCI 16-times Oversampling enable */

#define UCBRF_0             (0x0<<4)    /* USCI First Stage Modulation: 0 */
#define UCBRF_1             (0x1<<4)    /* USCI First Stage Modulation: 1 */
#define UCBRF_2             (0x2<<4)    /* USCI First Stage Modulation: 2 */
#define UCBRF_3             (0x3<<4)    /* USCI First Stage Modulation: 3 */
#define UCBRF_4             (0x4<<4)    /* USCI First Stage Modulation: 4 */
#define UCBRF_5             (0x5<<4)    /* USCI First Stage Modulation: 5 */
#define UCBRF_6             (0x6<<4)    /* USCI First Stage Modulation: 6 */
#define UCBRF_7             (0x7<<4)    /* USCI First Stage Modulation: 7 */
#define UCBRF_8             (0x8<<4)    /* USCI First Stage Modulation: 8 */
#define UCBRF_9             (0x9<<4)    /* USCI First Stage Modulation: 9 */
#define UCBRF_10            (0xA<<4)    /* USCI First Stage Modulation: A */
#define UCBRF_11            (0xB<<4)    /* USCI First Stage Modulation: B */
#define UCBRF_12            (0xC<<4)    /* USCI First Stage Modulation: C */
#define UCBRF_13            (0xD<<4)    /* USCI First Stage Modulation: D */
#define UCBRF_14            (0xE<<4)    /* USCI First Stage Modulation: E */
#define UCBRF_15            (0xF<<4)    /* USCI First Stage Modulation: F */

#define UCBRS_0             (0<<1)      /* USCI Second Stage Modulation: 0 */
#define UCBRS_1             (1<<1)      /* USCI Second Stage Modulation: 1 */
#define UCBRS_2             (2<<1)      /* USCI Second Stage Modulation: 2 */
#define UCBRS_3             (3<<1)      /* USCI Second Stage Modulation: 3 */
#define UCBRS_4             (4<<1)      /* USCI Second Stage Modulation: 4 */
#define UCBRS_5             (5<<1)      /* USCI Second Stage Modulation: 5 */
#define UCBRS_6             (6<<1)      /* USCI Second Stage Modulation: 6 */
#define UCBRS_7             (7<<1)      /* USCI Second Stage Modulation: 7 */

#define UCLISTEN            (0x80)      /* USCI Listen mode */
#define UCFE                (0x40)      /* USCI Frame Error Flag */
#define UCOE                (0x20)      /* USCI Overrun Error Flag */
#define UCPE                (0x10)      /* USCI Parity Error Flag */
#define UCBRK               (0x08)      /* USCI Break received */
#define UCRXERR             (0x04)      /* USCI RX Error Flag */
#define UCADDR              (0x02)      /* USCI Address received Flag */
#define UCBUSY              (0x01)      /* USCI Busy Flag */
#define UCIDLE              (0x02)      /* USCI Idle line detected Flag */

//#define res               (0x80)      /* reserved */
//#define res               (0x40)      /* reserved */
//#define res               (0x20)      /* reserved */
//#define res               (0x10)      /* reserved */
#define UCNACKIE            (0x08)      /* NACK Condition interrupt enable */
#define UCSTPIE             (0x04)      /* STOP Condition interrupt enable */
#define UCSTTIE             (0x02)      /* START Condition interrupt enable */
#define UCALIE              (0x01)      /* Arbitration Lost interrupt enable */

#define UCSCLLOW            (0x40)      /* SCL low */
#define UCGC                (0x20)      /* General Call address received Flag */
#define UCBBUSY             (0x10)      /* Bus Busy Flag */
#define UCNACKIFG           (0x08)      /* NAK Condition interrupt Flag */
#define UCSTPIFG            (0x04)      /* STOP Condition interrupt Flag */
#define UCSTTIFG            (0x02)      /* START Condition interrupt Flag */
#define UCALIFG             (0x01)      /* Arbitration Lost interrupt Flag */

#define UCIRTXPL5           (0x80)      /* IRDA Transmit Pulse Length 5 */
#define UCIRTXPL4           (0x40)      /* IRDA Transmit Pulse Length 4 */
#define UCIRTXPL3           (0x20)      /* IRDA Transmit Pulse Length 3 */
#define UCIRTXPL2           (0x10)      /* IRDA Transmit Pulse Length 2 */
#define UCIRTXPL1           (0x08)      /* IRDA Transmit Pulse Length 1 */
#define UCIRTXPL0           (0x04)      /* IRDA Transmit Pulse Length 0 */
#define UCIRTXCLK           (0x02)      /* IRDA Transmit Pulse Clock Select */
#define UCIREN              (0x01)      /* IRDA Encoder/Decoder enable */

#define UCIRRXFL5           (0x80)      /* IRDA Receive Filter Length 5 */
#define UCIRRXFL4           (0x40)      /* IRDA Receive Filter Length 4 */
#define UCIRRXFL3           (0x20)      /* IRDA Receive Filter Length 3 */
#define UCIRRXFL2           (0x10)      /* IRDA Receive Filter Length 2 */
#define UCIRRXFL1           (0x08)      /* IRDA Receive Filter Length 1 */
#define UCIRRXFL0           (0x04)      /* IRDA Receive Filter Length 0 */
#define UCIRRXPL            (0x02)      /* IRDA Receive Input Polarity */
#define UCIRRXFE            (0x01)      /* IRDA Receive Filter enable */

//#define res                 (0x80)    /* reserved */
//#define res                 (0x40)    /* reserved */
#define UCDELIM1            (0x20)      /* Break Sync Delimiter 1 */
#define UCDELIM0            (0x10)      /* Break Sync Delimiter 0 */
#define UCSTOE              (0x08)      /* Sync-Field Timeout error */
#define UCBTOE              (0x04)      /* Break Timeout error */
//#define res                 (0x02)    /* reserved */
#define UCABDEN             (0x01)      /* Auto Baud Rate detect enable */

#define UCGCEN              (0x8000)    /* I2C General Call enable */
#define UCOA9               (0x0200)    /* I2C Own Address 9 */
#define UCOA8               (0x0100)    /* I2C Own Address 8 */
#define UCOA7               (0x0080)    /* I2C Own Address 7 */
#define UCOA6               (0x0040)    /* I2C Own Address 6 */
#define UCOA5               (0x0020)    /* I2C Own Address 5 */
#define UCOA4               (0x0010)    /* I2C Own Address 4 */
#define UCOA3               (0x0008)    /* I2C Own Address 3 */
#define UCOA2               (0x0004)    /* I2C Own Address 2 */
#define UCOA1               (0x0002)    /* I2C Own Address 1 */
#define UCOA0               (0x0001)    /* I2C Own Address 0 */

#define UCSA9               (0x0200)    /* I2C Slave Address 9 */
#define UCSA8               (0x0100)    /* I2C Slave Address 8 */
#define UCSA7               (0x0080)    /* I2C Slave Address 7 */
#define UCSA6               (0x0040)    /* I2C Slave Address 6 */
#define UCSA5               (0x0020)    /* I2C Slave Address 5 */
#define UCSA4               (0x0010)    /* I2C Slave Address 4 */
#define UCSA3               (0x0008)    /* I2C Slave Address 3 */
#define UCSA2               (0x0004)    /* I2C Slave Address 2 */
#define UCSA1               (0x0002)    /* I2C Slave Address 1 */
#define UCSA0               (0x0001)    /* I2C Slave Address 0 */

/* Aliases by mspgcc */
#define UCSSEL_UCLKI        UCSSEL_0
#define UCSSEL_ACLK         UCSSEL_1
#define UCSSEL_SMCLK        UCSSEL_2

#if defined(__MSP430_HAS_USCI0__)

/* -------- USCI0 */

#define UCA0CTL0_           0x0060      /* USCI A0 Control Register 0 */
sfrb(UCA0CTL0, UCA0CTL0_);
#define UCA0CTL1_           0x0061      /* USCI A0 Control Register 1 */
sfrb(UCA0CTL1, UCA0CTL1_);
#define UCA0BR0_            0x0062      /* USCI A0 Baud Rate 0 */
sfrb(UCA0BR0, UCA0BR0_);
#define UCA0BR1_            0x0063      /* USCI A0 Baud Rate 1 */
sfrb(UCA0BR1, UCA0BR1_);
#define UCA0MCTL_           0x0064      /* USCI A0 Modulation Control */
sfrb(UCA0MCTL, UCA0MCTL_);
#define UCA0STAT_           0x0065      /* USCI A0 Status Register */
sfrb(UCA0STAT, UCA0STAT_);
#define UCA0RXBUF_          0x0066      /* USCI A0 Receive Buffer */
/*READ_ONLY*/ sfrb(UCA0RXBUF, UCA0RXBUF_);
#define UCA0TXBUF_          0x0067      /* USCI A0 Transmit Buffer */
sfrb(UCA0TXBUF, UCA0TXBUF_);
#define UCA0ABCTL_          0x005D      /* USCI A0 Auto baud/LIN Control */
sfrb(UCA0ABCTL, UCA0ABCTL_);
#define UCA0IRTCTL_         0x005E      /* USCI A0 IrDA Transmit Control */
sfrb(UCA0IRTCTL, UCA0IRTCTL_);
#define UCA0IRRCTL_         0x005F      /* USCI A0 IrDA Receive Control */
sfrb(UCA0IRRCTL, UCA0IRRCTL_);

#define UCB0CTL0_           0x0068      /* USCI B0 Control Register 0 */
sfrb(UCB0CTL0, UCB0CTL0_);
#define UCB0CTL1_           0x0069      /* USCI B0 Control Register 1 */
sfrb(UCB0CTL1, UCB0CTL1_);
#define UCB0BR0_            0x006A      /* USCI B0 Baud Rate 0 */
sfrb(UCB0BR0, UCB0BR0_);
#define UCB0BR1_            0x006B      /* USCI B0 Baud Rate 1 */
sfrb(UCB0BR1, UCB0BR1_);
#define UCB0I2CIE_          0x006C      /* USCI B0 I2C Interrupt Enable Register */
sfrb(UCB0I2CIE, UCB0I2CIE_);
#define UCB0STAT_           0x006D      /* USCI B0 Status Register */
sfrb(UCB0STAT, UCB0STAT_);
#define UCB0RXBUF_          0x006E      /* USCI B0 Receive Buffer */
/*READ_ONLY*/ sfrb(UCB0RXBUF, UCB0RXBUF_);
#define UCB0TXBUF_          0x006F      /* USCI B0 Transmit Buffer */
sfrb(UCB0TXBUF, UCB0TXBUF_);
#define UCB0I2COA_          0x0118      /* USCI B0 I2C Own Address */
sfrw(UCB0I2COA, UCB0I2COA_);
#define UCB0I2CSA_          0x011A      /* USCI B0 I2C Slave Address */
sfrw(UCB0I2CSA, UCB0I2CSA_);
#endif /* __MSP430_HAS_USCI0__ */

#if defined(__MSP430_HAS_USCI1__)

/* -------- USCI1 */

#define UCA1CTL0_           0x00D0      /* USCI A1 Control Register 0 */
sfrb(UCA1CTL0, UCA1CTL0_);
#define UCA1CTL1_           0x00D1      /* USCI A1 Control Register 1 */
sfrb(UCA1CTL1, UCA1CTL1_);
#define UCA1BR0_            0x00D2      /* USCI A1 Baud Rate 0 */
sfrb(UCA1BR0, UCA1BR0_);
#define UCA1BR1_            0x00D3      /* USCI A1 Baud Rate 1 */
sfrb(UCA1BR1, UCA1BR1_);
#define UCA1MCTL_           0x00D4      /* USCI A1 Modulation Control */
sfrb(UCA1MCTL, UCA1MCTL_);
#define UCA1STAT_           0x00D5      /* USCI A1 Status Register */
sfrb(UCA1STAT, UCA1STAT_);
#define UCA1RXBUF_          0x00D6      /* USCI A1 Receive Buffer */
/*READ_ONLY*/ sfrb(UCA1RXBUF, UCA1RXBUF_);
#define UCA1TXBUF_          0x00D7      /* USCI A1 Transmit Buffer */
sfrb(UCA1TXBUF, UCA1TXBUF_);
#define UCA1ABCTL_          0x00CD      /* USCI A1 Auto baud/LIN Control */
sfrb(UCA1ABCTL, UCA1ABCTL_);
#define UCA1IRTCTL_         0x00CE      /* USCI A1 IrDA Transmit Control */
sfrb(UCA1IRTCTL, UCA1IRTCTL_);
#define UCA1IRRCTL_         0x00CF      /* USCI A1 IrDA Receive Control */
sfrb(UCA1IRRCTL, UCA1IRRCTL_);

#define UCB1CTL0_           0x00D8      /* USCI B1 Control Register 0 */
sfrb(UCB1CTL0, UCB1CTL0_);
#define UCB1CTL1_           0x00D9      /* USCI B1 Control Register 1 */
sfrb(UCB1CTL1, UCB1CTL1_);
#define UCB1BR0_            0x00DA      /* USCI B1 Baud Rate 0 */
sfrb(UCB1BR0, UCB1BR0_);
#define UCB1BR1_            0x00DB      /* USCI B1 Baud Rate 1 */
sfrb(UCB1BR1, UCB1BR1_);
#define UCB1I2CIE_          0x00DC      /* USCI B1 I2C Interrupt Enable Register */
sfrb(UCB1I2CIE, UCB1I2CIE_);
#define UCB1STAT_           0x00DD      /* USCI B1 Status Register */
sfrb(UCB1STAT, UCB1STAT_);
#define UCB1RXBUF_          0x00DE      /* USCI B1 Receive Buffer */
/*READ_ONLY*/ sfrb(UCB1RXBUF, UCB1RXBUF_);
#define UCB1TXBUF_          0x00DF      /* USCI B1 Transmit Buffer */
sfrb(UCB1TXBUF, UCB1TXBUF_);
#define UCB1I2COA_          0x017C      /* USCI B1 I2C Own Address */
sfrw(UCB1I2COA, UCB1I2COA_);
#define UCB1I2CSA_          0x017E      /* USCI B1 I2C Slave Address */
sfrw(UCB1I2CSA, UCB1I2CSA_);

#define UC1IE_              0x0006      /* USCI A1/B1 Interrupt enable register */
sfrb(UC1IE, UC1IE_);
#define UC1IFG_             0x0007      /* USCI A1/B1 Interrupt flag register */
sfrb(UC1IFG, UC1IFG_);

#define UCA1RXIE            (1<<0)
#define UCA1TXIE            (1<<1)
#define UCB1RXIE            (1<<2)
#define UCB1TXIE            (1<<3)

#define UCA1RXIFG           (1<<0)
#define UCA1TXIFG           (1<<1)
#define UCB1RXIFG           (1<<2)
#define UCB1TXIFG           (1<<3)
#endif /* __MSP430_HAS_USCI1__ */

#if defined(__MSP430_HAS_USCI0_5__)

/* -------- USCI0_5 */

#define UCA0CTL0_           0x05C1
sfrb(UCA0CTL0, UCA0CTL0_);
#define UCA0CTL1_           0x05C0
sfrb(UCA0CTL1, UCA0CTL1_);
#define UCA0BR0_            0x05C6
sfrb(UCA0BR0, UCA0BR0_);
#define UCA0BR1_            0x05C7
sfrb(UCA0BR1, UCA0BR1_);
#define UCA0MCTL_           0x05C8
sfrb(UCA0MCTL, UCA0MCTL_);
#define UCA0STAT_           0x05CA
sfrb(UCA0STAT, UCA0STAT_);
#define UCA0RXBUF_          0x05CC
sfrb(UCA0RXBUF, UCA0RXBUF_);
#define UCA0TXBUF_          0x05CE
sfrb(UCA0TXBUF, UCA0TXBUF_);
#define UCA0ABCTL_          0x05D0
sfrb(UCA0ABCTL, UCA0ABCTL_);
#define UCA0IRTCTL_         0x05D2
sfrb(UCA0IRTCTL, UCA0IRTCTL_);
#define UCA0IRRCTL_         0x05D3
sfrb(UCA0IRRCTL, UCA0IRRCTL_);
#define UCA0IE_             0x05DC
sfrb(UCA0IE, UCA0IE_);
#define UCA0IFG_            0x05DD
sfrb(UCA0IFG, UCA0IFG_);
#define UCA0IV_             0x05DE
sfrw(UCA0IV, UCA0IV_);
#define UCA0IV_L_           0x05DE
sfrb(UCA0IV_L, UCA0IV_L_);
#define UCA0IV_H_           0x05DF
sfrb(UCA0IV_H, UCA0IV_H_);

#define UCB0CTL0_           0x05E1
sfrb(UCB0CTL0, UCB0CTL0_);
#define UCB0CTL1_           0x05E0
sfrb(UCB0CTL1, UCB0CTL1_);
#define UCB0BR0_            0x05E6
sfrb(UCB0BR0, UCB0BR0_);
#define UCB0BR1_            0x05E7
sfrb(UCB0BR1, UCB0BR1_);
#define UCB0MCTL_           0x05E8
sfrb(UCB0MCTL, UCB0MCTL_);
#define UCB0STAT_           0x05EA
sfrb(UCB0STAT, UCB0STAT_);
#define UCB0RXBUF_          0x05EC
sfrb(UCB0RXBUF, UCB0RXBUF_);
#define UCB0TXBUF_          0x05EE
sfrb(UCB0TXBUF, UCB0TXBUF_);
#define UCB0I2COA_          0x05F0
sfrb(UCB0I2COA, UCB0I2COA_);
#define UCB0I2CSA_          0x05F2
sfrb(UCB0I2CSA, UCB0I2CSA_);
#define UCB0IE_             0x05FC
sfrb(UCB0IE, UCB0IE_);
#define UCB0IFG_            0x05FD
sfrb(UCB0IFG, UCB0IFG_);
#define UCB0IV_             0x05FE
sfrw(UCB0IV, UCB0IV_);
#define UCB0IV_L_           0x05FE
sfrb(UCB0IV_L, UCB0IV_L_);
#define UCB0IV_H_           0x05FF
sfrb(UCB0IV_H, UCB0IV_H_);

#endif /* __MSP430_HAS_USCI0_5__ */

#if defined(__MSP430_HAS_USCI1_5__)

/* -------- USCI1_5 */

// tbd
/*#define UCA1CTL0_           0x0601
#define UCA1CTL0_L         0x0601
#define UCA1CTL0_H         0x0602
#define UCA1CTL1_           0x0600
#define UCA1CTL1_L         0x0600
#define UCA1CTL1_H         0x0601
#define UCA1BR0_            0x0606
#define UCA1BR0_L          0x0606
#define UCA1BR0_H          0x0607
#define UCA1BR1_            0x0607
#define UCA1BR1_L          0x0607
#define UCA1BR1_H          0x0608
#define UCA1MCTL_           0x0608
#define UCA1MCTL_L         0x0608
#define UCA1MCTL_H         0x0609
#define UCA1STAT_           0x060A
#define UCA1STAT_L         0x060A
#define UCA1STAT_H         0x060B
#define UCA1RXBUF          0x060C
#define UCA1RXBUF_L        0x060C
#define UCA1RXBUF_H        0x060D
#define UCA1TXBUF          0x060E
#define UCA1TXBUF_L        0x060E
#define UCA1TXBUF_H        0x060F
#define UCA1ABCTL          0x0610
#define UCA1ABCTL_L        0x0610
#define UCA1ABCTL_H        0x0611
#define UCA1IRTCTL         0x0612
#define UCA1IRTCTL_L       0x0612
#define UCA1IRTCTL_H       0x0613
#define UCA1IRRCTL         0x0613
#define UCA1IRRCTL_L       0x0613
#define UCA1IRRCTL_H       0x0614
#define UCA1IE_             0x061C
#define UCA1IE_L_           0x061C
#define UCA1IE_H_           0x061D
#define UCA1IFG_            0x061D
#define UCA1IFG_L          0x061D
#define UCA1IFG_H          0x061E
#define UCA1IV_             0x061E
#define UCA1IV_L_           0x061E
#define UCA1IV_H_           0x061F
#define UCB1CTL0_           0x0621
#define UCB1CTL0_L         0x0621
#define UCB1CTL0_H         0x0622
#define UCB1CTL1_           0x0620
#define UCB1CTL1_L         0x0620
#define UCB1CTL1_H         0x0621
#define UCB1BR0_            0x0626
#define UCB1BR0_L          0x0626
#define UCB1BR0_H          0x0627
#define UCB1BR1_            0x0627
#define UCB1BR1_L          0x0627
#define UCB1BR1_H          0x0628
#define UCB1STAT_           0x062A
#define UCB1STAT_L         0x062A
#define UCB1STAT_H         0x062B
#define UCB1RXBUF          0x062C
#define UCB1RXBUF_L        0x062C
#define UCB1RXBUF_H        0x062D
#define UCB1TXBUF          0x062E
#define UCB1TXBUF_L        0x062E
#define UCB1TXBUF_H        0x062F
#define UCB1I2COA          0x0630
#define UCB1I2COA_L        0x0630
#define UCB1I2COA_H        0x0631
#define UCB1I2CSA          0x0632
#define UCB1I2CSA_L        0x0632
#define UCB1I2CSA_H        0x0633
#define UCB1IE_             0x063C
#define UCB1IE_L_           0x063C
#define UCB1IE_H_           0x063D
#define UCB1IFG_            0x063D
#define UCB1IFG_L          0x063D
#define UCB1IFG_H          0x063E
#define UCB1IV_             0x063E
#define UCB1IV_L_           0x063E
#define UCB1IV_H_           0x063F
*/
#endif /* __MSP430_HAS_USCI1_5__ */

#if defined(__MSP430_HAS_USCI2_5__)

/* -------- USCI2_5 */

// tbd
/*#define UCA2CTL0_           0x0641
#define UCA2CTL0_L         0x0641
#define UCA2CTL0_H         0x0642
#define UCA2CTL1_           0x0640
#define UCA2CTL1_L         0x0640
#define UCA2CTL1_H         0x0641
#define UCA2BR0_            0x0646
#define UCA2BR0_L          0x0646
#define UCA2BR0_H          0x0647
#define UCA2BR1_            0x0647
#define UCA2BR1_L          0x0647
#define UCA2BR1_H          0x0648
#define UCA2MCTL_           0x0648
#define UCA2MCTL_L         0x0648
#define UCA2MCTL_H         0x0649
#define UCA2STAT_           0x064A
#define UCA2STAT_L         0x064A
#define UCA2STAT_H         0x064B
#define UCA2RXBUF          0x064C
#define UCA2RXBUF_L        0x064C
#define UCA2RXBUF_H        0x064D
#define UCA2TXBUF          0x064E
#define UCA2TXBUF_L        0x064E
#define UCA2TXBUF_H        0x064F
#define UCA2ABCTL          0x0650
#define UCA2ABCTL_L        0x0650
#define UCA2ABCTL_H        0x0651
#define UCA2IRTCTL         0x0652
#define UCA2IRTCTL_L       0x0652
#define UCA2IRTCTL_H       0x0653
#define UCA2IRRCTL         0x0653
#define UCA2IRRCTL_L       0x0653
#define UCA2IRRCTL_H       0x0654
#define UCA2IE_             0x065C
#define UCA2IE_L_           0x065C
#define UCA2IE_H_           0x065D
#define UCA2IFG_            0x065D
#define UCA2IFG_L          0x065D
#define UCA2IFG_H          0x065E
#define UCA2IV_             0x065E
#define UCA2IV_L_           0x065E
#define UCA2IV_H_           0x065F
#define UCB2CTL0_           0x0661
#define UCB2CTL0_L         0x0661
#define UCB2CTL0_H         0x0662
#define UCB2CTL1_           0x0660
#define UCB2CTL1_L         0x0660
#define UCB2CTL1_H         0x0661
#define UCB2BR0_            0x0666
#define UCB2BR0_L          0x0666
#define UCB2BR0_H          0x0667
#define UCB2BR1_            0x0667
#define UCB2BR1_L          0x0667
#define UCB2BR1_H          0x0668
#define UCB2STAT_           0x066A
#define UCB2STAT_L         0x066A
#define UCB2STAT_H         0x066B
#define UCB2RXBUF          0x066C
#define UCB2RXBUF_L        0x066C
#define UCB2RXBUF_H        0x066D
#define UCB2TXBUF          0x066E
#define UCB2TXBUF_L        0x066E
#define UCB2TXBUF_H        0x066F
#define UCB2I2COA          0x0670
#define UCB2I2COA_L        0x0670
#define UCB2I2COA_H        0x0671
#define UCB2I2CSA          0x0672
#define UCB2I2CSA_L        0x0672
#define UCB2I2CSA_H        0x0673
#define UCB2IE_             0x067C
#define UCB2IE_L_           0x067C
#define UCB2IE_H_           0x067D
#define UCB2IFG_            0x067D
#define UCB2IFG_L          0x067D
#define UCB2IFG_H          0x067E
#define UCB2IV_             0x067E
#define UCB2IV_L_           0x067E
#define UCB2IV_H_           0x067F
*/
#endif /* __MSP430_HAS_USCI2_5__ */

#if defined(__MSP430_HAS_USCI3_5__)

/* -------- USCI3_5 */
// tbd
/*#define UCA3CTL0_           0x0681
#define UCA3CTL0_L         0x0681
#define UCA3CTL0_H         0x0682
#define UCA3CTL1_           0x0680
#define UCA3CTL1_L         0x0680
#define UCA3CTL1_H         0x0681
#define UCA3BR0_            0x0686
#define UCA3BR0_L          0x0686
#define UCA3BR0_H          0x0687
#define UCA3BR1_            0x0687
#define UCA3BR1_L          0x0687
#define UCA3BR1_H          0x0688
#define UCA3MCTL_           0x0688
#define UCA3MCTL_L         0x0688
#define UCA3MCTL_H         0x0689
#define UCA3STAT_           0x068A
#define UCA3STAT_L         0x068A
#define UCA3STAT_H         0x068B
#define UCA3RXBUF          0x068C
#define UCA3RXBUF_L        0x068C
#define UCA3RXBUF_H        0x068D
#define UCA3TXBUF          0x068E
#define UCA3TXBUF_L        0x068E
#define UCA3TXBUF_H        0x068F
#define UCA3ABCTL          0x0690
#define UCA3ABCTL_L        0x0690
#define UCA3ABCTL_H        0x0691
#define UCA3IRTCTL         0x0692
#define UCA3IRTCTL_L       0x0692
#define UCA3IRTCTL_H       0x0693
#define UCA3IRRCTL         0x0693
#define UCA3IRRCTL_L       0x0693
#define UCA3IRRCTL_H       0x0694
#define UCA3IE_             0x069C
#define UCA3IE_L_           0x069C
#define UCA3IE_H_           0x069D
#define UCA3IFG_            0x069D
#define UCA3IFG_L          0x069D
#define UCA3IFG_H          0x069E
#define UCA3IV_             0x069E
#define UCA3IV_L_           0x069E
#define UCA3IV_H_           0x069F
#define UCB3CTL0_           0x06A1
#define UCB3CTL0_L         0x06A1
#define UCB3CTL0_H         0x06A2
#define UCB3CTL1_           0x06A0
#define UCB3CTL1_L         0x06A0
#define UCB3CTL1_H         0x06A1
#define UCB3BR0_            0x06A6
#define UCB3BR0_L          0x06A6
#define UCB3BR0_H          0x06A7
#define UCB3BR1_            0x06A7
#define UCB3BR1_L          0x06A7
#define UCB3BR1_H          0x06A8
#define UCB3STAT_           0x06AA
#define UCB3STAT_L         0x06AA
#define UCB3STAT_H         0x06AB
#define UCB3RXBUF          0x06AC
#define UCB3RXBUF_L        0x06AC
#define UCB3RXBUF_H        0x06AD
#define UCB3TXBUF          0x06AE
#define UCB3TXBUF_L        0x06AE
#define UCB3TXBUF_H        0x06AF
#define UCB3I2COA          0x06B0
#define UCB3I2COA_L        0x06B0
#define UCB3I2COA_H        0x06B1
#define UCB3I2CSA          0x06B2
#define UCB3I2CSA_L        0x06B2
#define UCB3I2CSA_H        0x06B3
#define UCB3IE_             0x06BC
#define UCB3IE_L_           0x06BC
#define UCB3IE_H_           0x06BD
#define UCB3IFG_            0x06BD
#define UCB3IFG_L          0x06BD
#define UCB3IFG_H          0x06BE
#define UCB3IV_             0x06BE
#define UCB3IV_L_           0x06BE
#define UCB3IV_H_           0x06BF
*/
#endif /* __MSP430_HAS_USCI3_5__ */

#endif
