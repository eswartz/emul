#ifndef __msp430_headers_dma_h
#define __msp430_headers_dma_h

/* dma12.h
 *
 * mspgcc project: MSP430 device headers
 * DMA module header
 *
 * (c) 2002 by Steve Udnerwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: dma.h,v 1.8 2008/11/07 08:28:40 sb-sf Exp $
 */

/* Switches:

__MSP430_HAS_DMAIV__  - if the device has a DMAIV register
__MSP430_HAS_DMA_3__  - if module has 16-bit address registers
__MSP430_HAS_DMAX_3__ - if module has 20-bit address registers (430X core)

 */

#define DMACTL0_             0x0122  /* DMA module control 0 */
sfrw(DMACTL0,DMACTL0_);
#define DMACTL1_             0x0124  /* DMA module control 1 */
sfrw(DMACTL1, DMACTL1_);
#if defined(__MSP430_HAS_DMAIV__)
#define DMAIV_               0x0126  /* DMA module interrupt vector word */
sfrw(DMAIV, DMAIV_);
#endif

#if defined(__MSP430_HAS_DMA_3__)
#define DMA0CTL_             0x01E0  /* DMA channel 0 control */
sfrw(DMA0CTL, DMA0CTL_);
#define DMA0SA_              0x01E2  /* DMA channel 0 source address */
sfrw(DMA0SA, DMA0SA_);
#define DMA0DA_              0x01E4  /* DMA channel 0 destination address */
sfrw(DMA0DA, DMA0DA_);
#define DMA0SZ_              0x01E6  /* DMA channel 0 transfer size */
sfrw(DMA0SZ, DMA0SZ_);

#define DMA1CTL_             0x01E8  /* DMA channel 1 control */
sfrw(DMA1CTL, DMA1CTL_);
#define DMA1SA_              0x01EA  /* DMA channel 1 source address */
sfrw(DMA1SA, DMA1SA_);
#define DMA1DA_              0x01EC  /* DMA channel 1 destination address */
sfrw(DMA1DA, DMA1DA_);
#define DMA1SZ_              0x01EE  /* DMA channel 1 transfer size */
sfrw(DMA1SZ, DMA1SZ_);

#define DMA2CTL_             0x01F0  /* DMA channel 2 control */
sfrw(DMA2CTL, DMA2CTL_);
#define DMA2SA_              0x01F2  /* DMA channel 2 source address */
sfrw(DMA2SA, DMA2SA_);
#define DMA2DA_              0x01F4  /* DMA channel 2 destination address */
sfrw(DMA2DA, DMA2DA_);
#define DMA2SZ_              0x01F6  /* DMA channel 2 transfer size */
sfrw(DMA2SZ, DMA2SZ_);
#endif

#if defined(__MSP430_HAS_DMAX_3__)
#define DMA0CTL_             0x01D0  /* DMA channel 0 control */
sfrw(DMA0CTL, DMA0CTL_);
#define DMA0SA_              0x01D2  /* DMA channel 0 source address */
sfra(DMA0SA, DMA0SA_);
#define DMA0SAL_             0x01D2  /* DMA channel 0 source address */
sfrw(DMA0SAL, DMA0SAL_);
#define DMA0DA_              0x01D6  /* DMA channel 0 destination address */
sfra(DMA0DA, DMA0DA_);
#define DMA0DAL_             0x01D6  /* DMA channel 0 destination address */
sfrw(DMA0DAL, DMA0DAL_);
#define DMA0SZ_              0x01DA  /* DMA channel 0 transfer size */
sfrw(DMA0SZ, DMA0SZ_);

#define DMA1CTL_             0x01DC  /* DMA channel 1 control */
sfrw(DMA1CTL, DMA1CTL_);
#define DMA1SA_              0x01DE  /* DMA channel 1 source address */
sfra(DMA1SA, DMA1SA_);
#define DMA1SAL_             0x01DE  /* DMA channel 1 source address */
sfrw(DMA1SAL, DMA1SAL_);
#define DMA1DA_              0x01E2  /* DMA channel 1 destination address */
sfrw(DMA1DA, DMA1DA_);
#define DMA1DAL_             0x01E2  /* DMA channel 1 destination address */
sfrw(DMA1DAL, DMA1DAL_);
#define DMA1SZ_              0x01E6  /* DMA channel 1 transfer size */
sfrw(DMA1SZ, DMA1SZ_);

#define DMA2CTL_             0x01E8  /* DMA channel 2 control */
sfrw(DMA2CTL, DMA2CTL_);
#define DMA2SA_              0x01EA  /* DMA channel 2 source address */
sfra(DMA2SA, DMA2SA_);
#define DMA2SAL_             0x01EA  /* DMA channel 2 source address */
sfrw(DMA2SAL, DMA2SAL_);
#define DMA2DA_              0x01EE  /* DMA channel 2 destination address */
sfra(DMA2DA, DMA2DA_);
#define DMA2DAL_             0x01EE  /* DMA channel 2 destination address */
sfrw(DMA2DAL, DMA2DAL_);
#define DMA2SZ_              0x01F2  /* DMA channel 2 transfer size */
sfrw(DMA2SZ, DMA2SZ_);
#endif

#define DMA0TSEL0           0x0001  /* DMA channel 0 transfer select bit 0 */
#define DMA0TSEL1           0x0002  /* DMA channel 0 transfer select bit 1 */
#define DMA0TSEL2           0x0004  /* DMA channel 0 transfer select bit 2 */
#define DMA0TSEL3           0x0008  /* DMA channel 0 transfer select bit 3 */
#define DMA1TSEL0           0x0010  /* DMA channel 1 transfer select bit 0 */
#define DMA1TSEL1           0x0020  /* DMA channel 1 transfer select bit 1 */
#define DMA1TSEL2           0x0040  /* DMA channel 1 transfer select bit 2 */
#define DMA1TSEL3           0x0080  /* DMA channel 1 transfer select bit 3 */
#define DMA2TSEL0           0x0100  /* DMA channel 2 transfer select bit 0 */
#define DMA2TSEL1           0x0200  /* DMA channel 2 transfer select bit 1 */
#define DMA2TSEL2           0x0400  /* DMA channel 2 transfer select bit 2 */
#define DMA2TSEL3           0x0800  /* DMA channel 2 transfer select bit 3 */

#define DMA0TSEL_0          (0<<0)  /* DMA channel 0 transfer select 0:  DMA_REQ */
#define DMA0TSEL_1          (1<<0)  /* DMA channel 0 transfer select 1:  Timer_A CCRIFG.2 */
#define DMA0TSEL_2          (2<<0)  /* DMA channel 0 transfer select 2:  Timer_B CCRIFG.2 */
#define DMA0TSEL_3          (3<<0)  /* DMA channel 0 transfer select 3:  I2C receive */
#define DMA0TSEL_4          (4<<0)  /* DMA channel 0 transfer select 4:  I2C transmit */
#define DMA0TSEL_5          (5<<0)  /* DMA channel 0 transfer select 5:  DAC12.0IFG */
#define DMA0TSEL_6          (6<<0)  /* DMA channel 0 transfer select 6:  ADC12 (ADC12IFG) */
#define DMA0TSEL_7          (7<<0)  /* DMA channel 0 transfer select 7:  Timer_A (TACCR0.IFG) */
#define DMA0TSEL_8          (8<<0)  /* DMA channel 0 transfer select 8:  Timer_B (TBCCR0.IFG) */
#define DMA0TSEL_9          (9<<0)  /* DMA channel 0 transfer select 9:  UART1 receive */
#define DMA0TSEL_10         (10<<0) /* DMA channel 0 transfer select 10: UART1 transmit */
#define DMA0TSEL_11         (11<<0) /* DMA channel 0 transfer select 11: Multiplier ready */
#define DMA0TSEL_14         (14<<0) /* DMA channel 0 transfer select 14: previous DMA channel DMA2IFG */
#define DMA0TSEL_15         (15<<0) /* DMA channel 0 transfer select 15: DMAE0 */

#define DMA1TSEL_0          (0<<4)  /* DMA channel 1 transfer select 0:  DMA_REQ */
#define DMA1TSEL_1          (1<<4)  /* DMA channel 1 transfer select 1:  Timer_A CCRIFG.2 */
#define DMA1TSEL_2          (2<<4)  /* DMA channel 1 transfer select 2:  Timer_B CCRIFG.2 */
#define DMA1TSEL_3          (3<<4)  /* DMA channel 1 transfer select 3:  I2C receive */
#define DMA1TSEL_4          (4<<4)  /* DMA channel 1 transfer select 4:  I2C transmit */
#define DMA1TSEL_5          (5<<4)  /* DMA channel 1 transfer select 5:  DAC12.0IFG */
#define DMA1TSEL_6          (6<<4)  /* DMA channel 1 transfer select 6:  ADC12 (ADC12IFG) */
#define DMA1TSEL_7          (7<<4)  /* DMA channel 1 transfer select 7:  Timer_A (TACCR0.IFG) */
#define DMA1TSEL_8          (8<<4)  /* DMA channel 1 transfer select 8:  Timer_B (TBCCR0.IFG) */
#define DMA1TSEL_9          (9<<4)  /* DMA channel 1 transfer select 9:  UART1 receive */
#define DMA1TSEL_10         (10<<4) /* DMA channel 1 transfer select 10: UART1 transmit */
#define DMA1TSEL_11         (11<<4) /* DMA channel 1 transfer select 11: Multiplier ready */
#define DMA1TSEL_14         (14<<4) /* DMA channel 1 transfer select 14: previous DMA channel DMA0IFG */
#define DMA1TSEL_15         (15<<4) /* DMA channel 1 transfer select 15: DMAE0 */

#define DMA2TSEL_0          (0<<8)  /* DMA channel 2 transfer select 0:  DMA_REQ */
#define DMA2TSEL_1          (1<<8)  /* DMA channel 2 transfer select 1:  Timer_A CCRIFG.2 */
#define DMA2TSEL_2          (2<<8)  /* DMA channel 2 transfer select 2:  Timer_B CCRIFG.2 */
#define DMA2TSEL_3          (3<<8)  /* DMA channel 2 transfer select 3:  I2C receive */
#define DMA2TSEL_4          (4<<8)  /* DMA channel 2 transfer select 4:  I2C transmit */
#define DMA2TSEL_5          (5<<8)  /* DMA channel 2 transfer select 5:  DAC12.0IFG */
#define DMA2TSEL_6          (6<<8)  /* DMA channel 2 transfer select 6:  ADC12 (ADC12IFG) */
#define DMA2TSEL_7          (7<<8)  /* DMA channel 2 transfer select 7:  Timer_A (TACCR0.IFG) */
#define DMA2TSEL_8          (8<<8)  /* DMA channel 2 transfer select 8:  Timer_B (TBCCR0.IFG) */
#define DMA2TSEL_9          (9<<8)  /* DMA channel 2 transfer select 9:  UART1 receive */
#define DMA2TSEL_10         (10<<8) /* DMA channel 2 transfer select 10: UART1 transmit */
#define DMA2TSEL_11         (11<<8) /* DMA channel 2 transfer select 11: Multiplier ready */
#define DMA2TSEL_14         (14<<8) /* DMA channel 2 transfer select 14: previous DMA channel DMA1IFG */
#define DMA2TSEL_15         (15<<8) /* DMA channel 2 transfer select 15: DMAE0 */

#define ENNMI               0x0001  /* Enable NMI interruption of DMA */
#define ROUNDROBIN          0x0002  /* Round-Robin DMA channel priorities */
#define DMAONFETCH          0x0004  /* DMA transfer on instruction fetch */

#define DMAREQ              0x0001  /* Initiate DMA transfer with DMATSEL */
#define DMAABORT            0x0002  /* DMA transfer aborted by NMI */
#define DMAIE               0x0004  /* DMA interrupt enable */
#define DMAIFG              0x0008  /* DMA interrupt flag */
#define DMAEN               0x0010  /* DMA enable */
#define DMALEVEL            0x0020  /* DMA level sensitive trigger select */
#define DMASRCBYTE          0x0040  /* DMA source byte */
#define DMADSTBYTE          0x0080  /* DMA destination byte */
#define DMASRCINCR0         0x0100  /* DMA source increment bit 0 */
#define DMASRCINCR1         0x0200  /* DMA source increment bit 1 */
#define DMADSTINCR0         0x0400  /* DMA destination increment bit 0 */
#define DMADSTINCR1         0x0800  /* DMA destination increment bit 1 */
#define DMADT0              0x1000  /* DMA transfer mode bit 0 */
#define DMADT1              0x2000  /* DMA transfer mode bit 1 */
#define DMADT2              0x4000  /* DMA transfer mode bit 2 */

#define DMASWDW             (0<<6)  /* DMA transfer: source word to destination word */
#define DMASBDW             (1<<6)  /* DMA transfer: source byte to destination word */
#define DMASWDB             (2<<6)  /* DMA transfer: source word to destination byte */
#define DMASBDB             (3<<6)  /* DMA transfer: source byte to destination byte */

#define DMASRCINCR_0        (0<<8)  /* DMA source increment 0: source address unchanged */
#define DMASRCINCR_1        (1<<8)  /* DMA source increment 1: source address unchanged */
#define DMASRCINCR_2        (2<<8)  /* DMA source increment 2: source address decremented */
#define DMASRCINCR_3        (3<<8)  /* DMA source increment 3: source address incremented */

#define DMADSTINCR_0        (0<<10) /* DMA destination increment 0: destination address unchanged */
#define DMADSTINCR_1        (1<<10) /* DMA destination increment 1: destination address unchanged */
#define DMADSTINCR_2        (2<<10) /* DMA destination increment 2: destination address decremented */
#define DMADSTINCR_3        (3<<10) /* DMA destination increment 3: destination address incremented */

#define DMADT_0             (0<<12) /* DMA transfer mode 0: single */
#define DMADT_1             (1<<12) /* DMA transfer mode 1: block */
#define DMADT_2             (2<<12) /* DMA transfer mode 2: interleaved */
#define DMADT_3             (3<<12) /* DMA transfer mode 3: interleaved */
#define DMADT_4             (4<<12) /* DMA transfer mode 4: single, repeat */
#define DMADT_5             (5<<12) /* DMA transfer mode 5: block, repeat */
#define DMADT_6             (6<<12) /* DMA transfer mode 6: interleaved, repeat */
#define DMADT_7             (7<<12) /* DMA transfer mode 7: interleaved, repeat */


/* Aliases by mspgcc */
#define DMADT_SINGLE        (0<<12) /* DMA transfer mode 0: single */
#define DMADT_BLOCK         (1<<12) /* DMA transfer mode 1: block */
#define DMADT_INTERLEAVED   (2<<12) /* DMA transfer mode 2: interleaved */
//~ #define DMADT_3             (3<<12) /* DMA transfer mode 3: interleaved */
#define DMADT_SINGLEREPEAT  (4<<12) /* DMA transfer mode 4: single, repeat */
#define DMADT_BLOCKREPEAT   (5<<12) /* DMA transfer mode 5: block, repeat */
#define DMADT_INTERLEAVEDREPEAT (6<<12) /* DMA transfer mode 6: interleaved, repeat */
//~ #define DMADT_7             (7<<12) /* DMA transfer mode 7: interleaved, repeat */

#define DMA0TSEL_DMA_REQ    (0<<0)  /* DMA channel 0 transfer select 0:  DMA_REQ */
#define DMA0TSEL_TACCR2     (1<<0)  /* DMA channel 0 transfer select 1:  Timer_A CCRIFG.2 */
#define DMA0TSEL_TBCCR2     (2<<0)  /* DMA channel 0 transfer select 2:  Timer_B CCRIFG.2 */
#define DMA0TSEL_I2CRX      (3<<0)  /* DMA channel 0 transfer select 3:  I2C receive */
#define DMA0TSEL_I2CTX      (4<<0)  /* DMA channel 0 transfer select 4:  I2C transmit */
#define DMA0TSEL_DAC12      (5<<0)  /* DMA channel 0 transfer select 5:  DAC12.0IFG */
#define DMA0TSEL_ADC12      (6<<0)  /* DMA channel 0 transfer select 6:  ADC12 (ADC12IFG) */
#define DMA0TSEL_TACCR0     (7<<0)  /* DMA channel 0 transfer select 7:  Timer_A (TACCR0.IFG) */
#define DMA0TSEL_TBCCR0     (8<<0)  /* DMA channel 0 transfer select 8:  Timer_B (TBCCR0.IFG) */
#define DMA0TSEL_USART1RX   (9<<0)  /* DMA channel 0 transfer select 9:  UART1 receive */
#define DMA0TSEL_USART1TX   (10<<0) /* DMA channel 0 transfer select 10: UART1 transmit */
#define DMA0TSEL_MUL        (11<<0) /* DMA channel 0 transfer select 11: Multiplier ready */
//~ #define DMA0TSEL_14         (14<<0) /* DMA channel 0 transfer select 14: previous DMA channel DMA2IFG */
//~ #define DMA0TSEL_15         (15<<0) /* DMA channel 0 transfer select 15: DMAE0 */

#define DMA1TSEL_DMA_REQ    (0<<4)  /* DMA channel 1 transfer select 0:  DMA_REQ */
#define DMA1TSEL_TACCR2     (1<<4)  /* DMA channel 1 transfer select 1:  Timer_A CCRIFG.2 */
#define DMA1TSEL_TBCCR2     (2<<4)  /* DMA channel 1 transfer select 2:  Timer_B CCRIFG.2 */
#define DMA1TSEL_I2CRX      (3<<4)  /* DMA channel 1 transfer select 3:  I2C receive */
#define DMA1TSEL_I2CTX      (4<<4)  /* DMA channel 1 transfer select 4:  I2C transmit */
#define DMA1TSEL_DAC12      (5<<4)  /* DMA channel 1 transfer select 5:  DAC12.0IFG */
#define DMA1TSEL_ADC12      (6<<4)  /* DMA channel 1 transfer select 6:  ADC12 (ADC12IFG) */
#define DMA1TSEL_TACCR0     (7<<4)  /* DMA channel 1 transfer select 7:  Timer_A (TACCR0.IFG) */
#define DMA1TSEL_TBCCR0     (8<<4)  /* DMA channel 1 transfer select 8:  Timer_B (TBCCR0.IFG) */
#define DMA1TSEL_USART1RX   (9<<4)  /* DMA channel 1 transfer select 9:  UART1 receive */
#define DMA1TSEL_USART1TX   (10<<4) /* DMA channel 1 transfer select 10: UART1 transmit */
#define DMA1TSEL_MUL        (11<<4) /* DMA channel 1 transfer select 11: Multiplier ready */
//~ #define DMA1TSEL_14         (14<<4) /* DMA channel 1 transfer select 14: previous DMA channel DMA0IFG */
//~ #define DMA1TSEL_15         (15<<4) /* DMA channel 1 transfer select 15: DMAE0 */

#define DMA2TSEL_DMA_REQ    (0<<8)  /* DMA channel 2 transfer select 0:  DMA_REQ */
#define DMA2TSEL_TACCR2     (1<<8)  /* DMA channel 2 transfer select 1:  Timer_A CCRIFG.2 */
#define DMA2TSEL_TBCCR2     (2<<8)  /* DMA channel 2 transfer select 2:  Timer_B CCRIFG.2 */
#define DMA2TSEL_I2CRX      (3<<8)  /* DMA channel 2 transfer select 3:  I2C receive */
#define DMA2TSEL_I2CTX      (4<<8)  /* DMA channel 2 transfer select 4:  I2C transmit */
#define DMA2TSEL_DAC12      (5<<8)  /* DMA channel 2 transfer select 5:  DAC12.0IFG */
#define DMA2TSEL_ADC12      (6<<8)  /* DMA channel 2 transfer select 6:  ADC12 (ADC12IFG) */
#define DMA2TSEL_TACCR0     (7<<8)  /* DMA channel 2 transfer select 7:  Timer_A (TACCR0.IFG) */
#define DMA2TSEL_TBCCR0     (8<<8)  /* DMA channel 2 transfer select 8:  Timer_B (TBCCR0.IFG) */
#define DMA2TSEL_USART1RX   (9<<8)  /* DMA channel 2 transfer select 9:  UART1 receive */
#define DMA2TSEL_USART1TX   (10<<8) /* DMA channel 2 transfer select 10: UART1 transmit */
#define DMA2TSEL_MUL        (11<<8) /* DMA channel 2 transfer select 11: Multiplier ready */
//~ #define DMA2TSEL_14         (14<<8) /* DMA channel 2 transfer select 14: previous DMA channel DMA1IFG */
//~ #define DMA2TSEL_15         (15<<8) /* DMA channel 2 transfer select 15: DMAE0 */

#endif
