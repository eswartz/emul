#ifndef __msp430_headers_compa_h
#define __msp430_headers_compa_h

/* compa.h
 *
 * mspgcc project: MSP430 device headers
 * COMPA module header
 *
 * (c) 2002 by M. P. Ashton <data@ieee.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: compa.h,v 1.6 2005/08/17 14:28:46 coppice Exp $
 */

/* Switches:
__MSP430_HAS_CAPLUS__
*/

#define CACTL1_             0x0059  /* Comparator A Control 1 */
sfrb(CACTL1,CACTL1_);
#define CACTL2_             0x005A  /* Comparator A Control 2 */
sfrb(CACTL2,CACTL2_);
#define CAPD_               0x005B  /* Comparator A Port Disable */
sfrb(CAPD,CAPD_);

#define CAIFG               0x01    /* Comp. A Interrupt Flag */
#define CAIE                0x02    /* Comp. A Interrupt Enable */
#define CAIES               0x04    /* Comp. A Int. Edge Select: 0:rising / 1:falling */
#define CAON                0x08    /* Comp. A enable */
#define CAREF0              0x10    /* Comp. A Internal Reference Select 0 */
#define CAREF1              0x20    /* Comp. A Internal Reference Select 1 */
#define CARSEL              0x40    /* Comp. A Internal Reference Enable */
#define CAEX                0x80    /* Comp. A Exchange Inputs */

#define CAREF_0             0x00    /* Comp. A Int. Ref. Select 0 : Off */
#define CAREF_1             0x10    /* Comp. A Int. Ref. Select 1 : 0.25*Vcc */
#define CAREF_2             0x20    /* Comp. A Int. Ref. Select 2 : 0.5*Vcc */
#define CAREF_3             0x30    /* Comp. A Int. Ref. Select 3 : Vt*/

#define CAOUT               0x01    /* Comp. A Output */
#define CAF                 0x02    /* Comp. A Enable Output Filter */
#define P2CA0               0x04    /* Comp. A Connect External Signal to CA0 : 1 */
#define P2CA1               0x08    /* Comp. A Connect External Signal to CA1 : 1 */
#if defined(__MSP430_HAS_CAPLUS__)
#define P2CA2               0x10    /* Comp. A -Terminal Multiplexer */
#define P2CA3               0x20    /* Comp. A -Terminal Multiplexer */
#define P2CA4               0x40    /* Comp. A +Terminal Multiplexer */
#define CASHORT             0x80    /* Comp. A Short + and - Terminals */
#else
#define CACTL24             0x10
#define CACTL25             0x20
#define CACTL26             0x40
#define CACTL27             0x80
#endif

#define CAPD0               0x01    /* Comp. A Disable Input Buffer of Port Register .0 */
#define CAPD1               0x02    /* Comp. A Disable Input Buffer of Port Register .1 */
#define CAPD2               0x04    /* Comp. A Disable Input Buffer of Port Register .2 */
#define CAPD3               0x08    /* Comp. A Disable Input Buffer of Port Register .3 */
#define CAPD4               0x10    /* Comp. A Disable Input Buffer of Port Register .4 */
#define CAPD5               0x20    /* Comp. A Disable Input Buffer of Port Register .5 */
#define CAPD6               0x40    /* Comp. A Disable Input Buffer of Port Register .6 */
#define CAPD7               0x80    /* Comp. A Disable Input Buffer of Port Register .7 */

/* Aliases by mspgcc */
#define CAREF_OFF           CAREF_0
#define CAREF_025           CAREF_1
#define CAREF_050           CAREF_2
#define CAREF_VT            CAREF_3
        
#endif
