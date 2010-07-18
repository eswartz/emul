#ifndef __msp430_headers_usart_h
#define __msp430_headers_usart_h

/* usart.h
 *
 * mspgcc project: MSP430 device headers
 * USART module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: usart.h,v 1.12 2007/02/16 13:07:23 cliechti Exp $
 */

/* Switches:

__MSP430_HAS_I2C__ - if device has USART0 with I2C feature
__MSP430_HAS_UART1__ - if device has USART1

*/

/* -------- Common USART bit definitions */

#define PENA                0x80        /* UCTL */
#define PEV                 0x40
#define SPB                 0x20        /* to distinguish from stackpointer SP */
#define CHAR                0x10
#define LISTEN              0x08        /* Loopback */
#define SYNC                0x04        /* USART synchronous/asynchronous */
#define MM                  0x02
#define SWRST               0x01

#define CKPH                0x80        /* UTCTL */
#define CKPL                0x40
#define SSEL1               0x20
#define SSEL0               0x10
#define URXSE               0x08
#define TXWAKE              0x04
#define STC                 0x02
#define TXEPT               0x01

#define FE                  0x80        /* URCTL */
#define PE                  0x40
#define OE                  0x20
#define BRK                 0x10
#define URXEIE              0x08
#define URXWIE              0x04
#define RXWAKE              0x02
#define RXERR               0x01

/* Aliases by mspgcc */
#define SSEL_0              0x00        /* UCLKI */
#define SSEL_1              0x10        /* ACLK  */
#define SSEL_2              0x20        /* SMCLK */
#define SSEL_3              0x30        /* SMCLK */

#define SSEL_UCLKI          SSEL_0
#define SSEL_ACLK           SSEL_1
#define SSEL_SMCLK          SSEL_2
/*#define SSEL_SMCLK          SSEL_3*/

#if defined(__MSP430_HAS_UART0__)
/*UART0 ME/IE/IFG is different on F12x and F13x/F14x devices.
  With these defines, the right sfrs are choosen automaticaly.
  These defines should only be used with bit set and clear
  instructions as the real ME/IE/IFG sfrs might be modified
  somewhere else too!
  e.g.:
    ME1 = ME1_INIT;         //enable all other modules first
    ME2 = ME2_INIT;         //enable all other modules first
    U0ME |= UTXE0|URXE0;    //and then the USART0
*/
#if defined(__msp430x12x)  ||  defined(__msp430x12x2)
  #define U0ME              ME2         /* RX and TX module enable */
  #define U0IE              IE2         /* RX and TX interrupt of UART0 */
  #define U0IFG             IFG2        /* RX and TX interrupt flags of UART0 */
#else /* not a __msp430x12x or __msp430x12x2 */
  #define U0ME              ME1         /* RX and TX module enable */
  #define U0IE              IE1         /* RX and TX interrupt of UART0 */
  #define U0IFG             IFG1        /* RX and TX interrupt flags of UART0 */
#endif
#endif /*__MSP430_HAS_UART0__*/

/* UART1 aliases for consistency with UART0 */
#if defined(__MSP430_HAS_UART1__)
  #define U1ME              ME2         /* RX and TX module enable */
  #define U1IE              IE2         /* RX and TX interrupt of UART1 */
  #define U1IFG             IFG2        /* RX and TX interrupt flags of UART1 */
#endif


#if defined(__MSP430_HAS_UART0__)

/* -------- USART 0 */

#define U0CTL_              0x0070      /* UART 0 Control */
sfrb(U0CTL, U0CTL_);
#define U0TCTL_             0x0071      /* UART 0 Transmit Control */
sfrb(U0TCTL, U0TCTL_);
#define U0RCTL_             0x0072      /* UART 0 Receive Control */
sfrb(U0RCTL, U0RCTL_);
#define U0MCTL_             0x0073      /* UART 0 Modulation Control */
sfrb(U0MCTL, U0MCTL_);
#define U0BR0_              0x0074      /* UART 0 Baud Rate 0 */
sfrb(U0BR0, U0BR0_);
#define U0BR1_              0x0075      /* UART 0 Baud Rate 1 */
sfrb(U0BR1, U0BR1_);
#define U0RXBUF_            0x0076      /* UART 0 Receive Buffer */
sfrb(U0RXBUF, U0RXBUF_);
#define U0TXBUF_            0x0077      /* UART 0 Transmit Buffer */
sfrb(U0TXBUF, U0TXBUF_);

/* Alternate register names */

#define UCTL_               0x0070      /* UART Control */
sfrb(UCTL, UCTL_);
#define UTCTL_              0x0071      /* UART Transmit Control */
sfrb(UTCTL, UTCTL_);
#define URCTL_              0x0072      /* UART Receive Control */
sfrb(URCTL, URCTL_);
#define UMCTL_              0x0073      /* UART Modulation Control */
sfrb(UMCTL, UMCTL_);
#define UBR0_               0x0074      /* UART Baud Rate 0 */
sfrb(UBR0, UBR0_);
#define UBR1_               0x0075      /* UART Buad Rate 1 */
sfrb(UBR1, UBR1_);
#define RXBUF_              0x0076      /* UART Receive Buffer */
sfrb(RXBUF, RXBUF_);
#define TXBUF_              0x0077      /* UART Transmit Buffer */
sfrb(TXBUF, TXBUF_);

#define UCTL0_              0x0070      /* UART 0 Control */
sfrb(UCTL0, UCTL0_);
#define UTCTL0_             0x0071      /* UART 0 Transmit Control */
sfrb(UTCTL0, UTCTL0_);
#define URCTL0_             0x0072      /* UART 0 Receive Control */
sfrb(URCTL0, URCTL0_);
#define UMCTL0_             0x0073      /* UART 0 Modulation Control */
sfrb(UMCTL0, UMCTL0_);
#define UBR00_              0x0074      /* UART 0 Baud Rate 0 */
sfrb(UBR00, UBR00_);
#define UBR10_              0x0075      /* UART 0 Baud Rate 1 */
sfrb(UBR10, UBR10_);
#define RXBUF0_             0x0076      /* UART 0 Receive Buffer */
sfrb(RXBUF0, RXBUF0_);
#define TXBUF0_             0x0077      /* UART 0 Transmit Buffer */
sfrb(TXBUF0, TXBUF0_);

#define UCTL_0_             0x0070      /* UART 0 Control */
sfrb(UCTL_0, UCTL_0_);
#define UTCTL_0_            0x0071      /* UART 0 Transmit Control */
sfrb(UTCTL_0, UTCTL_0_);
#define URCTL_0_            0x0072      /* UART 0 Receive Control */
sfrb(URCTL_0, URCTL_0_);
#define UMCTL_0_            0x0073      /* UART 0 Modulation Control */
sfrb(UMCTL_0, UMCTL_0_);
#define UBR0_0_             0x0074      /* UART 0 Baud Rate 0 */
sfrb(UBR0_0, UBR0_0_);
#define UBR1_0_             0x0075      /* UART 0 Baud Rate 1 */
sfrb(UBR1_0, UBR1_0_);
#define RXBUF_0_            0x0076      /* UART 0 Receive Buffer */
sfrb(RXBUF_0, RXBUF_0_);
#define TXBUF_0_            0x0077      /* UART 0 Transmit Buffer */
sfrb(TXBUF_0, TXBUF_0_);

#if defined(__MSP430_HAS_I2C__)

/* USART 0 Control */
#define RXDMAEN             0x80        /* Receive DMA enable */
#define TXDMAEN             0x40        /* Transmit DMA enable */
#define I2C                 0x20        /* USART I2C */
#define XA                  0x10        /* I2C extended addressing */
/* LISTEN - as for non I2C version */
/* SYNC  - as for non I2C version */
#define MST                 0x02        /* I2C master */
#define I2CEN               0x01        /* I2C enable */

#define STTIE               0x80        /* Start condition */
#define GCIE                0x40        /* General call */
#define TXRDYIE             0x20        /* Transmit ready (transmit register empty) */
#define RXRDYIE             0x10        /* Receive ready (data received) */
#define ARDYIE              0x08        /* Access ready (operation complete) */
#define OAIE                0x04        /* Own address */
#define NACKIE              0x02        /* No acknowledge */
#define ALIE                0x01        /* Arbitration lost */

#define STTIFG              0x80        /* Start condition */
#define GCIFG               0x40        /* General call */
#define TXRDYIFG            0x20        /* Transmit ready (transmit register empty) */
#define RXRDYIFG            0x10        /* Receive ready (data received) */
#define ARDYIFG             0x08        /* Access ready (operation complete) */
#define OAIFG               0x04        /* Own address */
#define NACKIFG             0x02        /* No acknowledge */
#define ALIFG               0x01        /* Arbitration lost */

#define I2CWORD             0x80        /* Word data mode */
#define I2CRM               0x40        /* Repeat mode */
#define I2CSSEL1            0x20        /* Clock select bit 1 */
#define I2CSSEL0            0x10        /* Clock select bit 0 */
#define I2CTRX              0x08        /* Transmit */
#define I2CSTB              0x04        /* Start byte mode */
#define I2CSTP              0x02        /* Stop bit */
#define I2CSTT              0x01        /* Start bit */

#define I2CSSEL_0           (0<<4)      /* I2C clock select 0: UCLK */
#define I2CSSEL_1           (1<<4)      /* I2C clock select 1: ACLK */
#define I2CSSEL_2           (2<<4)      /* I2C clock select 2: SMCLK */
#define I2CSSEL_3           (3<<4)      /* I2C clock select 3: SMCLK */

#define I2CMM_0             0           /* Master mode 0 */
#define I2CMM_1             (SST)       /* Master mode 1 */
#define I2CMM_2             (STP|SST)   /* Master mode 2 */
#define I2CMM_3             (RM|STT)    /* Master mode 3 */
#define I2CMM_4             (STP)       /* Master mode 4 */

#define I2CBUSY             0x20        /* I2C module not idle */
#define I2CSCLLOW           0x10        /* SCL being held low */
#define I2CSBD              0x08        /* Received byte */
#define I2CTXUDF            0x04        /* Transmit underflow */
#define I2CRXOVR            0x02        /* Receiver overrun */
#define I2CBB               0x01        /* Bus busy */


#define I2CIV_NONE          0x00        /* I2C interrupt vector: No interrupt pending */
#define I2CIV_AL            0x02        /* I2C interrupt vector: Arbitration lost (ALIFG) */
#define I2CIV_NACK          0x04        /* I2C interrupt vector: No acknowledge (NACKIFG) */
#define I2CIV_OA            0x06        /* I2C interrupt vector: Own address (OAIFG) */
#define I2CIV_ARDY          0x08        /* I2C interrupt vector: Access ready (ARDYIFG) */
#define I2CIV_RXRDY         0x0A        /* I2C interrupt vector: Receive ready (RXRDYIFG) */
#define I2CIV_TXRDY         0x0C        /* I2C interrupt vector: Transmit ready (TXRDYIFG) */
#define I2CIV_GC            0x0E        /* I2C interrupt vector: General call (GCIFG) */
#define I2CIV_STT           0x10        /* I2C interrupt vector: Start condition (STTIFG) */

#define I2CIE_              0x0050      /* I2C interrupt enable */
sfrb(I2CIE, I2CIE_);
#define I2CIFG_             0x0051      /* I2C interrupt flag */
sfrb(I2CIFG, I2CIFG_);
#define I2CNDAT_            0x0052      /* I2C data count */
sfrb(I2CNDAT, I2CNDAT_);
#define I2CTCTL_            0x0071      /* I2C transfer control */
sfrb(I2CTCTL, I2CTCTL_);
#define I2CDCTL_            0x0072      /* I2C data control */
sfrb(I2CDCTL, I2CDCTL_);
#define I2CPSC_             0x0073      /* I2C PSC */
sfrb(I2CPSC, I2CPSC_);
#define I2CSCLH_            0x0074      /* I2C SCLH */
sfrb(I2CSCLH, I2CSCLH_);
#define I2CSCLL_            0x0075      /* I2C SCLL */
sfrb(I2CSCLL, I2CSCLL_);
#define I2CDRB_             0x0076      /* I2C data for byte access */
sfrb(I2CDRB, I2CDRB_);
#define I2CDRW_             0x0076      /* I2C data for word access */
sfrw(I2CDRW, I2CDRW_);
#define I2COA_              0x0118      /* I2C own address */
sfrw(I2COA, I2COA_);
#define I2CSA_              0x011A      /* I2C slave address */
sfrw(I2CSA, I2CSA_);
#define I2CIV_              0x011C      /* I2C interrupt vector */
sfrw(I2CIV, I2CIV_);

/* Backwards compatibility to older versions of the header file.
   Please consider using the new name I2CDRB.
 */
#define I2CDR_              0x0076      /* I2C data for byte access */
sfrb(I2CDR, I2CDR_);

/* Aliases by mspgcc */
#define I2CSSEL_UCLK        I2CSSEL_0      /* I2C clock select 0: UCLK */
#define I2CSSEL_ACLK        I2CSSEL_1      /* I2C clock select 1: ACLK */
#define I2CSSEL_SMCLK       I2CSSEL_2      /* I2C clock select 2: SMCLK */
//~ #define I2CSSEL_SMCLK       I2CSSEL_3      /* I2C clock select 3: SMCLK */

#endif /* __MSP430_HAS_I2C__ */
#endif /*__MSP430_HAS_UART0__*/

#if defined(__MSP430_HAS_UART1__)

/* -------- USART1 */

#define U1CTL_              0x0078      /* UART 1 Control */
sfrb(U1CTL, U1CTL_);
#define U1TCTL_             0x0079      /* UART 1 Transmit Control */
sfrb(U1TCTL, U1TCTL_);
#define U1RCTL_             0x007A      /* UART 1 Receive Control */
sfrb(U1RCTL, U1RCTL_);
#define U1MCTL_             0x007B      /* UART 1 Modulation Control */
sfrb(U1MCTL, U1MCTL_);
#define U1BR0_              0x007C      /* UART 1 Baud Rate 0 */
sfrb(U1BR0, U1BR0_);
#define U1BR1_              0x007D      /* UART 1 Baud Rate 1 */
sfrb(U1BR1, U1BR1_);
#define U1RXBUF_            0x007E      /* UART 1 Receive Buffer */
sfrb(U1RXBUF, U1RXBUF_);
#define U1TXBUF_            0x007F      /* UART 1 Transmit Buffer */
sfrb(U1TXBUF, U1TXBUF_);

#define UCTL1_              0x0078      /* UART 1 Control */
sfrb(UCTL1, UCTL1_);
#define UTCTL1_             0x0079      /* UART 1 Transmit Control */
sfrb(UTCTL1, UTCTL1_);
#define URCTL1_             0x007A      /* UART 1 Receive Control */
sfrb(URCTL1, URCTL1_);
#define UMCTL1_             0x007B      /* UART 1 Modulation Control */
sfrb(UMCTL1, UMCTL1_);
#define UBR01_              0x007C      /* UART 1 Baud Rate 0 */
sfrb(UBR01, UBR01_);
#define UBR11_              0x007D      /* UART 1 Baud Rate 1 */
sfrb(UBR11, UBR11_);
#define RXBUF1_             0x007E      /* UART 1 Receive Buffer */
sfrb(RXBUF1, RXBUF1_);
#define TXBUF1_             0x007F      /* UART 1 Transmit Buffer */
sfrb(TXBUF1, TXBUF1_);

#define UCTL_1_             0x0078      /* UART 1 Control */
sfrb(UCTL_1, UCTL_1_);
#define UTCTL_1_            0x0079      /* UART 1 Transmit Control */
sfrb(UTCTL_1, UTCTL_1_);
#define URCTL_1_            0x007A      /* UART 1 Receive Control */
sfrb(URCTL_1, URCTL_1_);
#define UMCTL_1_            0x007B      /* UART 1 Modulation Control */
sfrb(UMCTL_1, UMCTL_1_);
#define UBR0_1_             0x007C      /* UART 1 Baud Rate 0 */
sfrb(UBR0_1, UBR0_1_);
#define UBR1_1_             0x007D      /* UART 1 Baud Rate 1 */
sfrb(UBR1_1, UBR1_1_);
#define RXBUF_1_            0x007E      /* UART 1 Receive Buffer */
sfrb(RXBUF_1, RXBUF_1_);
#define TXBUF_1_            0x007F      /* UART 1 Transmit Buffer */
sfrb(TXBUF_1, TXBUF_1_);

#endif /* __MSP430_HAS_UART1__ */

#endif
