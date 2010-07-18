#if !defined(__msp430_headers_usi_h__)
#define __msp430_headers_usi_h__

/* usi.h
 *
 * mspgcc project: MSP430 device headers
 * USI module header
 *
 * (c) 2005 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: usi.h,v 1.4 2007/08/20 13:02:33 coppice Exp $
 */

/* Switches:

*/

#define USICTL0_            0x0078      /* USI  Control Register 0 */
sfrb(USICTL0, USICTL0_);
#define USICTL1_            0x0079      /* USI  Control Register 1 */
sfrb(USICTL1, USICTL1_);
#define USICKCTL_           0x007A      /* USI  Clock Control Register */
sfrb(USICKCTL, USICKCTL_);
#define USICNT_             0x007B      /* USI  Bit Counter Register */
sfrb(USICNT, USICNT_);
#define USISRL_             0x007C      /* USI  Low Byte Shift Register */
sfrb(USISRL, USISRL_);
#define USISRH_             0x007D      /* USI  High Byte Shift Register */
sfrb(USISRH, USISRH_);
#define USICTL_             0x0078      /* USI  Control Register */
sfrw(USICTL, USICTL_);

#define USICCTL_            0x007A      /* USI  Clock and Counter Control Register */
sfrw(USICCTL, USICCTL_);

#define USISR_              0x007C      /* USI  Shift Register */
sfrw(USISR, USISR_);

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

#define USIDIV_0            (0x00)      /* USI  Clock Divider: 0 Divide by 1 */
#define USIDIV_1            (0x20)      /* USI  Clock Divider: 1 Divide by 2 */
#define USIDIV_2            (0x40)      /* USI  Clock Divider: 2 Divide by 4 */
#define USIDIV_3            (0x60)      /* USI  Clock Divider: 3 Divide by 8 */
#define USIDIV_4            (0x80)      /* USI  Clock Divider: 4 Divide by 16 */
#define USIDIV_5            (0xA0)      /* USI  Clock Divider: 5 Divide by 32 */
#define USIDIV_6            (0xC0)      /* USI  Clock Divider: 6 Divide by 64 */
#define USIDIV_7            (0xE0)      /* USI  Clock Divider: 7 Divide by 128 */
                                                          
#define USISSEL_0           (0x00)      /* USI  Clock Source: 0 SCLK */
#define USISSEL_1           (0x04)      /* USI  Clock Source: 1 ACLK */
#define USISSEL_2           (0x08)      /* USI  Clock Source: 2 SMCLK */
#define USISSEL_3           (0x0C)      /* USI  Clock Source: 3 SMCLK */
#define USISSEL_4           (0x10)      /* USI  Clock Source: 4 USISWCLK bit*/
#define USISSEL_5           (0x14)      /* USI  Clock Source: 5 TACCR0 */
#define USISSEL_6           (0x18)      /* USI  Clock Source: 6 TACCR1 */
#define USISSEL_7           (0x1C)      /* USI  Clock Source: 7 TACCR2 */
                                                         
#define USISCLREL           (0x80)      /* USI  SCL Released */
#define USI16B              (0x40)      /* USI  16 Bit Shift Register Enable */ 
#define USIIFGCC            (0x20)      /* USI  Interrupt Flag Clear Control */
#define USICNT4             (0x10)      /* USI  Bit Count 4 */
#define USICNT3             (0x08)      /* USI  Bit Count 3 */
#define USICNT2             (0x04)      /* USI  Bit Count 2 */
#define USICNT1             (0x02)      /* USI  Bit Count 1 */
#define USICNT0             (0x01)      /* USI  Bit Count 0 */

/* Aliases by mspgcc */
#define USIDIV_DIV1         USIDIV_0    /* USI  Clock Divider: 0 Divide by 1 */
#define USIDIV_DIV2         USIDIV_1    /* USI  Clock Divider: 1 Divide by 2 */
#define USIDIV_DIV4         USIDIV_2    /* USI  Clock Divider: 2 Divide by 4 */
#define USIDIV_DIV8         USIDIV_3    /* USI  Clock Divider: 3 Divide by 8 */
#define USIDIV_DIV16        USIDIV_4    /* USI  Clock Divider: 4 Divide by 16 */
#define USIDIV_DIV32        USIDIV_5    /* USI  Clock Divider: 5 Divide by 32 */
#define USIDIV_DIV64        USIDIV_6    /* USI  Clock Divider: 6 Divide by 64 */
#define USIDIV_DIV128       USIDIV_7    /* USI  Clock Divider: 7 Divide by 128 */

#define USISSEL_SCLK        USISSEL_0   /* USI  Clock Source: 0 SCLK */
#define USISSEL_ACLK        USISSEL_1   /* USI  Clock Source: 1 ACLK */
#define USISSEL_SMCLK       USISSEL_2   /* USI  Clock Source: 2 SMCLK */
//~ #define USISSEL_3           USISSEL_3   /* USI  Clock Source: 3 SMCLK */
#define USISSEL_USISWCLK    USISSEL_4   /* USI  Clock Source: 4 USISWCLK bit*/
#define USISSEL_TACCR0      USISSEL_5   /* USI  Clock Source: 5 TACCR0 */
#define USISSEL_TACCR1      USISSEL_6   /* USI  Clock Source: 6 TACCR1 */
#define USISSEL_TACCR2      USISSEL_7   /* USI  Clock Source: 7 TACCR2 */

#endif
