#ifndef __msp430_headers_esp430e_h
#define __msp430_headers_esp430e_h

/* esp430e.h
 *
 * mspgcc project: MSP430 device headers
 * ESP module header
 *
 * (c) 2003 by Steve Underwood <steveu@coppice.org>
 * Originally based in part on work by Texas Instruments Inc.
 *
 * $Id: esp430e.h,v 1.6 2008/10/09 15:00:14 sb-sf Exp $
 */

/* Switches: none */

#define ESPCTL_             0x0150  /* ESP430 Control Register */
sfrw(ESPCTL, ESPCTL_);
#define MBCTL_              0x0152  /* Mailbox Control Register */
sfrw(MBCTL, MBCTL_);
#define MBIN0_              0x0154  /* Incoming Mailbox 0 Register */
sfrw(MBIN0, MBIN0_);
#define MBIN1_              0x0156  /* Incoming Mailbox 1 Register */
sfrw(MBIN1, MBIN1_);
#define MBOUT0_             0x0158  /* Outgoing Mailbox 0 Register */
sfrw(MBOUT0, MBOUT0_);
#define MBOUT1_             0x015A  /* Outgoing Mailbox 1 Register */
sfrw(MBOUT1, MBOUT1_);

#define    RET0_            0x01C0  /* ESP430 Return Value 0 */
sfrw(RET0, RET0_);
#define    RET1_            0x01C2  /* ESP430 Return Value 1 */
sfrw(RET1, RET1_);
#define    RET2_            0x01C4  /* ESP430 Return Value 2 */
sfrw(RET2, RET2_);
#define    RET3_            0x01C6  /* ESP430 Return Value 3 */
sfrw(RET3, RET3_);
#define    RET4_            0x01C8  /* ESP430 Return Value 4 */
sfrw(RET4, RET4_);
#define    RET5_            0x01CA  /* ESP430 Return Value 5 */
sfrw(RET5, RET5_);
#define    RET6_            0x01CC  /* ESP430 Return Value 6 */
sfrw(RET6, RET6_);
#define    RET7_            0x01CE  /* ESP430 Return Value 7 */
sfrw(RET7, RET7_);
#define    RET8_            0x01D0  /* ESP430 Return Value 8 */
sfrw(RET8, RET8_);
#define    RET9_            0x01D2  /* ESP430 Return Value 9 */
sfrw(RET9, RET9_);
#define    RET10_           0x01D4  /* ESP430 Return Value 10 */
sfrw(RET10, RET10_);
#define    RET11_           0x01D6  /* ESP430 Return Value 11 */
sfrw(RET11, RET11_);
#define    RET12_           0x01D8  /* ESP430 Return Value 12 */
sfrw(RET12, RET12_);
#define    RET13_           0x01DA  /* ESP430 Return Value 13 */
sfrw(RET13, RET13_);
#define    RET14_           0x01DC  /* ESP430 Return Value 14 */
sfrw(RET14, RET14_);
#define    RET15_           0x01DE  /* ESP430 Return Value 15 */
sfrw(RET15, RET15_);
#define    RET16_           0x01E0  /* ESP430 Return Value 16 */
sfrw(RET16, RET16_);
#define    RET17_           0x01E2  /* ESP430 Return Value 17 */
sfrw(RET17, RET17_);
#define    RET18_           0x01E4  /* ESP430 Return Value 18 */
sfrw(RET18, RET18_);
#define    RET19_           0x01E6  /* ESP430 Return Value 19 */
sfrw(RET19, RET19_);
#define    RET20_           0x01E8  /* ESP430 Return Value 20 */
sfrw(RET20, RET20_);
#define    RET21_           0x01EA  /* ESP430 Return Value 21 */
sfrw(RET21, RET21_);
#define    RET22_           0x01EC  /* ESP430 Return Value 22 */
sfrw(RET22, RET22_);
#define    RET23_           0x01EE  /* ESP430 Return Value 23 */
sfrw(RET23, RET23_);
#define    RET24_           0x01F0  /* ESP430 Return Value 24 */
sfrw(RET24, RET24_);
#define    RET25_           0x01F2  /* ESP430 Return Value 25 */
sfrw(RET25, RET25_);
#define    RET26_           0x01F4  /* ESP430 Return Value 26 */
sfrw(RET26, RET26_);
#define    RET27_           0x01F6  /* ESP430 Return Value 27 */
sfrw(RET27, RET27_);
#define    RET28_           0x01F8  /* ESP430 Return Value 28 */
sfrw(RET28, RET28_);
#define    RET29_           0x01FA  /* ESP430 Return Value 29 */
sfrw(RET29, RET29_);
#define    RET30_           0x01FC  /* ESP430 Return Value 30 */
sfrw(RET30, RET30_);
#define    RET31_           0x01FE  /* ESP430 Return Value 31 */
sfrw(RET31, RET31_);

#define ESP430_STAT0        RET0      /* STATUS0 of ESP430 */
#define ESP430_STAT1        RET1      /* STATUS1 of ESP430 */
#define WAVEFSV1            RET2      /* Waveform Sample V1 offset corrected*/
#define WAVEFSI1            RET5      /* Waveform Sample I1 offset corrected*/
#if defined __MSP430_HAS_ESP430E1A__
#define WAVEFSI2            RET6      /* Waveform Sample I2 offset corrected*/
#endif
#define ACTENERGY1_LO       RET8      /* Active energy I1 Low Word */
#define ACTENERGY1_HI       RET9      /* Active energy I1 High Word */
#if defined __MSP430_HAS_ESP430E1A__
#define ACTENERGY2_LO       RET10     /* Active energy I2 Low Word */
#define ACTENERGY2_HI       RET11     /* Active energy I2 High Word*/
#endif
#define REACTENERGY_LO      RET12     /* Reactive energy Low Word */
#define REACTENERGY_HI      RET13     /* Reactive energy High Word */
#define APPENERGY_LO        RET14     /* Apparent energy Low Word */
#define APPENERGY_HI        RET15     /* Apparent energy High Word */
#define ACTENSPER1_LO       RET16     /* Active energy I1 for last mains period Low Word */
#define ACTENSPER1_HI       RET17     /* Active energy I1 for last mains period High Word */
#if defined __MSP430_HAS_ESP430E1A__
#define ACTENSPER2_LO       RET18     /* Active energy I2 for last mains period Low Word */
#define ACTENSPER2_HI       RET19     /* Active energy I2 for last mains period High Word */
#elif defined __MSP430_HAS_ESP430E1B__
#define IRMS_2_LO           RET18     /* RMS_2 Low Word */
#define IRMS_2_HI           RET19     /* RMS_2 High Word */
#endif
#define POWERFCT            RET20     /* Power factor */
#define CAPIND              RET21     /* Power factor: neg: inductive pos: cap. (LowByte)*/
#define MAINSPERIOD         RET22     /* Mains period */
#define V1RMS               RET23     /* Voltage RMS V1 value last second */
#define IRMS_LO             RET24     /* Current RMS value last second I1 I2 Low Word */
#define IRMS_HI             RET25     /* Current RMS value last second I1 I2 High Word */
#define VPEAK               RET26     /* Voltage V1 absolute peak value */
#define IPEAK               RET27     /* Current absolute peak value I1 I2 */
#define LINECYCLCNT_LO      RET28     /* Line cycle counter Low Word */
#define LINECYCLCNT_HI      RET29     /* Line cycle counter High Word */
#define NMBMEAS_LO          RET30     /* Number of Measurements for CPU signal Low Word */
#define NMBMEAS_HI          RET31     /* Number of Measurements for CPU signal High Word */

/* ESPCTL */
#define ESPEN               (0x0001)  /* ESP430 Module enable */
#define ESPSUSP             (0x0002)  /* ESP430 Module suspend */
#define IREQ                (0x0004)  /* NOT supported by current ESP430 Software */

/* RET0 - Status0 Flags */
#define WFSRDYFG            (0x0001)  /* New waveform Samples ready Flag */
#define I2GTI1FG            (0x0002)  /* Current I2 greater then I1 Flag */
#define ILREACHEDFG         (0x0004)  /* Interrupt level reached Flag */
#define ENRDYFG             (0x0008)  /* New Energy values ready Flag */
#define ZXLDFG              (0x0010)  /* Zero Crossing of V1 Flag (leading edge) */
#define ZXTRFG              (0x0020)  /* Zero Crossing of V1 Flag (trailing edge) */
#define CALRDYFG            (0x0040)  /* Calibration values ready Flag */
#define TAMPFG              (0x0080)  /* Tampering Occured Flag */
#define NEGENFG             (0x0100)  /* Negativ Energy Flag */
#define VDROPFG             (0x0200)  /* Voltage drop occured Flag */
#define VPEAKFG             (0x0400)  /* Voltage exceed VPeak level Flag */
#define I1PEAKFG            (0x0800)  /* Current exceed I1Peak level Flag */
#define I2PEAKFG            (0x1000)  /* Current exceed I2Peak level Flag */
//#define RESERVED          (0x8000)  /* Reserved */
//#define RESERVED          (0x8000)  /* Reserved */
#define ACTIVEFG            (0x8000)  /* Measurement or Calibration running Flag */

/* MBCTL */
#define IN0IFG              (0x0001)  /* Incoming Mail 0 Interrupt Flag */
#define IN1IFG              (0x0002)  /* Incoming Mail 1 Interrupt Flag */
#define OUT0FG              (0x0004)  /* Outgoing Mail 0 Flag */
#define OUT1FG              (0x0008)  /* Outgoing Mail 1 Flag */
#define IN0IE               (0x0010)  /* Incoming Mail 0 Interrupt Enable */
#define IN1IE               (0x0020)  /* Incoming Mail 1 Interrupt Enable */
#define CLR0OFF             (0x0040)  /* Switch off automatic clear of IN0IFG */
#define CLR1OFF             (0x0080)  /* Switch off automatic clear of IN1IFG */
#define OUT0IFG             (0x0100)  /* Outgoing Mail 0 Interrupt Flag */
#define OUT1IFG             (0x0200)  /* Outgoing Mail 1 Interrupt Flag */
#define OUT0IE              (0x0400)  /* Outgoing Mail 0 Interrupt Enable */
#define OUT1IE              (0x0800)  /* Outgoing Mail 1 Interrupt Enable */

/* Messages to ESP */
#define mRESET              (0x0001)  /* Restart ESP430 Software */
#define mSET_MODE           (0x0003)  /* Set Operation Mode for ESP430 Software */
#define mCLR_EVENT          (0x0005)  /* Clear Flags for ESP430 Software */
#define mINIT               (0x0007)  /* Initialize ESP430 Software */
#define mTEMP               (0x0009)  /* Request Temp. Measurement from ESP430 Software */
#define mSWVERSION          (0x000B)  /* Request software version of ESP430 */
#define mREAD_PARAM         (0x000D)  /* Request to read the parameter with no. "Parameter No." */
#define mREAD_I2            (0x000F)  /* Request to read the I2 channel (only if not used) */

#define mSET_CTRL0          (0x0200)  /* Set Control Register 0 */
#define mSET_CTRL1          (0x0202)  /* Set Control Register 1 */
#define mSET_EVENT          (0x0204)  /* Set which Evenets should cause an message */
#define mSET_PHASECORR1     (0x0206)  /* Set Phase Correction fo I1 */
#define mSET_PHASECORR2     (0x0208)  /* Set Phase Correction fo I2 */
#define mSET_V1OFFSET       (0x020A)  /* Set Offset for V1 */
#define mSET_I1OFFSET       (0x020C)  /* Set Offset for I1 */
#define mSET_I2OFFSET       (0x020E)  /* Set Offset for I2 */
#define mSET_ADAPTI1        (0x0210)  /* Set Adaption factor for I1 */
#define mSET_ADAPTI2        (0x0212)  /* Set Adaption factor for I2 */
#define mSET_GAINCORR1      (0x0214)  /* Set Gain Correction for Power P1 */
#define mSET_POFFSET1_LO    (0x0216)  /* Set Power Offset for Power P1 */
#define mSET_POFFSET1_HI    (0x0218)  /* Set Power Offset for Power P1 */
#define mSET_GAINCORR2      (0x021A)  /* Set Gain Correction for Power P2 */
#define mSET_POFFSET2_LO    (0x021C)  /* Set Power Offset for Power P2 */
#define mSET_POFFSET2_HI    (0x021E)  /* Set Power Offset for Power P2 */
#define mSET_INTRPTLEVL_LO  (0x0220)  /* Set Interrupt Level */
#define mSET_INTRPTLEVL_HI  (0x0222)  /* Set Interrupt Level */
#define mSET_CALCYCLCNT     (0x0224)  /* Set number of main cycles for calibration */
#define mSET_STARTCURR_FRAC (0x0226)  /* Set start current */
#define mSET_STARTCURR_INT  (0x0228)  /* Set start current */
#define mSET_NOMFREQ        (0x022A)  /* Set nominal main frequency */
#define mSET_VDROPCYCLS     (0x022C)  /* Set cylces for VDrop detection */
#define mSET_RATIOTAMP      (0x022E)  /* Set ratio for tamper detection */
#define mSET_ITAMP          (0x0230)  /* Set minimum current for tamper detection */
#define mSET_VDROPLEVEL     (0x0232)  /* Set level for VDrop detection */
#define mSET_VPEAKLEVEL     (0x0234)  /* Set level for VPeak detection */
#define mSET_IPEAKLEVEL     (0x0236)  /* Set level for IPeak detection */
#define mSET_DCREMPER       (0x0238)  /* Set number of periods for DC-removal */

/* Flags for mSET_CTRL0 */
#define CURR_I2             (0x0001)  /* 0: No I2 path, only I1 path is used */
                                      /* 1: I2 path implemented (CT, dc-tol CT or shunt) */
#define CURR_I1             (0x0002)  /* 0: Current transformer, dc-tol CT or shunt */
                                      /* 1: Rogowski coil (not yet implemented) */
#define MB                  (0x0004)  /* Intrpt to CPU: 0: energy level  1: #measurements */
#define NE0                 (0x0008)  /* Neg. energy treatment:      00: clear neg. energy */
#define NE1                 (0x0010)  /* 01: use absolute energy   10: use energy as it is */
#define DCREM_V1            (0x0020)  /* DC removal for V1:  0: off  1: on */
#define DCREM_I1            (0x0040)  /* DC removal for I1:  0: off  1: on */
#define DCREM_I2            (0x0080)  /* DC removal for I2:  0: off  1: on */

/* Messages from ESP */
#define mEVENT              (0x0001)  /* Event Status Flag for ESP430 Software */
#define mTEMPRDY            (0x0003)  /* Temperature measurement completed and in MBIN1 */
#define mSWRDY              (0x0005)  /* Software version in MBIN1 */
#define mPARAMRDY           (0x0007)  /* Parameter requested by mREAD_PARAM returned in MBIN1  */
#define mPARAMSET           (0x0009)  /* Parameter has been set */
#define mI2RDY              (0x000B)  /* I2 value ready */

/* EVENT: Event Message Enable Bits */
#define WFSRDYME            (0x0001)  /* New waveform Samples ready */
#define I2GTI1ME            (0x0002)  /* Current I2 greater then I1 */
#define ILREACHEDME         (0x0004)  /* Interrupt level reached */
#define ENRDYME             (0x0008)  /* New Energy values ready */
#define ZXLDME              (0x0010)  /* Zero Crossing of V1 (leading edge) */
#define ZXTRME              (0x0020)  /* Zero Crossing of V1 (trailing edge) */
#define CALRDYME            (0x0040)  /* Calibration values ready */
#define TAMPME              (0x0080)  /* Tampering Occured */
#define NEGENME             (0x0100)  /* Negativ Energy */
#define VDROPME             (0x0200)  /* Voltage drop occured */
#define VPEAKME             (0x0400)  /* Voltage exceed VPeak level */
#define I1PEAKME            (0x0800)  /* Current exceed I1Peak level */
#define I2PEAKME            (0x1000)  /* Current exceed I2Peak level */
//#define RESERVED            (0x8000)  /* Reserved */
//#define RESERVED            (0x8000)  /* Reserved */
#define ACTIVEME            (0x8000)  /* Measurement of Calibration running */


/* ESP Modes */
#define modeIDLE            (0x0000)  /* Set Mode: Idle Mode */
#define modeCALIBRATION     (0x0002)  /* Set Mode: Calibration Mode */
#define modeMEASURE         (0x0004)  /* Set Mode: Measure Mode */
#define modeRESET           (0x0006)  /* Set Mode: Reset and Restart the ESP430 module */
#define modeINIT            (0x0008)  /* Set Mode: Initialize ESP430 module */

#endif
