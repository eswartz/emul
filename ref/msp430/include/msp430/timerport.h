#ifndef __msp430_headers_timerport_h
#define __msp430_headers_timerport_h

/* timerport.h
 *
 * mspgcc project: MSP430 device headers
 * Timer / IO port module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: timerport.h,v 1.2 2002/12/28 06:50:18 coppice Exp $
 */

/* Switches: none */

#define TPCTL_              0x004B  /* Timer/Port Control */
sfrb(TPCTL,TPCTL_);
#define EN1FG               0x01
#define RC1FG               0x02
#define RC2FG               0x04
#define EN1                 0x08
#define ENA                 0x10
#define ENB                 0x20
#define TPSSEL0             0x40
#define TPSSEL1             0x80
/* The EN1 signal of TPCNT1 is coded with with Bits 3-5 in TPCTL */
#define TPCNT1_EN_OFF       0                       /* TPCNT1 is disabled */
#define TPCNT1_EN_ON        ENA                     /*   "    is enabled */
#define TPCNT1_EN_nTPIN5    ENB                     /*   "    is enabled with ~TPIN.5 */
#define TPCNT1_EN_TPIN5     (TPSSEL0|ENB)           /*   "    is enabled with TPIN.5 */
#define TPCNT1_EN_nCIN      (ENB|ENA)               /*   "    is enabled with ~CIN */
#define TPCNT1_EN_CIN       (TPSSEL0|ENB|ENA)       /*   "    is enabled with CIN */

/* Source select of clock input coded with Bits 6-7 in TPCTL */
#define TPSSEL_CLK1_CIN     0                       /* CLK1 source is signal at CIN (default) */
#define TPSSEL_CLK1_ACLK    TPSSEL0                 /* CLK1 source is ACLK */
#define TPSSEL_CLK1_MCLK    TPSSEL1                 /* CLK1 source is MCLK */

/* DATA REGISTER ADDRESSES */
#define TPCNT1_             0x004C  /* Timer/Port Counter 1 */
sfrb(TPCNT1,TPCNT1_);
#define TPCNT2_             0x004D  /* Timer/Port Counter 2 */
sfrb(TPCNT2,TPCNT2_);

#define TPD_                0x004E  /* Timer/Port Data */
sfrb(TPD,TPD_);
#define TPD_0               0x01
#define TPD_1               0x02
#define TPD_2               0x04
#define TPD_3               0x08
#define TPD_4               0x10
#define TPD_5               0x20
#define CPON                0x40
#define B16                 0x80

#define TPE_                0x004F  /* Timer/Port Enable */
sfrb(TPE,TPE_);
#define TPE_0               0x01
#define TPE_1               0x02
#define TPE_2               0x04
#define TPE_3               0x08
#define TPE_4               0x10
#define TPE_5               0x20
#define TPSSEL2             0x40
#define TPSSEL3             0x80
/* Source select of clock input coded with Bits 6-7 in TPE
   NOTE: If the control bit B16 in TPD is set, TPSSEL2/3
         are 'don't care' and the clock source of counter
         TPCNT2 is the same as of the counter TPCNT1. */
#define TPSSEL_CLK2_TPIN5   0                   /* CLK2 source is signal TPIN.5 (default) */
#define TPSSEL_CLK2_ACLK    TPSSEL2             /* CLK2 source is ACLK */
#define TPSSEL_CLK2_MCLK    TPSSEL3             /* CLK2 source is MCLK */
#define TPSSEL_CLK2_OFF     (TPSSEL3|TPSSEL2)   /* CLK2 source is disabled  */

#endif
