---
title: V9t9 and ROMs
layout: wikistyle
---

V9t9 and ROMs
================

V9t9 needs ROMs to emulate the TI-99/4A system.  These ROMs are copyrighted and not distributed with 
V9t9 (or by me).  

The legal way to get such ROMs is to start up your own TI-99/4A system and transfer the ROMs over, or 
purchase a license for them from Texas Instruments.

Base System ROMs
-----------------

The minimal ROMs needed are:

* TI-99/4A console ROM:  8k, MD5 sum `6CC4BC2B6B3B0C33698E6A03759A4CAB`
* TI-99/4A console GROM:  24k, MD5 sum `ED8FF714542BA850BDEC686840A79217`

Optional ROMs are:

* Speech Synthesizer ROM:  40k, MD5 sum `7ADCAF64272248F7A7161CFC02FD5B3F`
* TI Disk Controller DSR ROM:  8k, MD5 sum `C9A737D6930F5FD1D96829FD89359CF1`
  
  (note: a ROM fetched from the machine may have unknown content in the last 16 bytes, since these are 
  memory-mapped registers, so the checksum only considers the first 0x1FF0 bytes -- 
  "head --bytes=-16 tidiskdsr.bin | md5sum")

Module ROMs
-----------------

In addition, you will need module (cartridge) ROMs to do anything besides TI BASIC programming.  

At least in the V9t9 family of emulators, these are named in groups like:

* `<module>G.bin` (GROM image at >6000)
* `<module>C.bin` (ROM image at >6000)
* `<module>D.bin` (ROM second bank at >6000)

The "C" and/or "D" variants may not be present, and sometimes not even the "G" variant.

Setup
-----------------

When you first start V9t9, it will show you a "ROM Setup" dialog where you can add paths pointing to your ROMs.
  The directories entered will be used to find both the system ROMs and module ROMs.

Unlike in earlier DOS version of V9t9 (and TI Emulator), V9t9 can detect ROMs by MD5 checksum, so you don't 
need to name any ROMs in any particular way.  The information about the system ROMs and a large database of 
TI-99/4A modules is built into V9t9 now.  So, all you need to do is point V9t9 to your collection and V9t9 
will discover them for you.
