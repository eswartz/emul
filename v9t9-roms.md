---
title: V9t9 and ROMs
layout: wikistyle
---

V9t9 and ROMs
================

V9t9 needs ROMs to emulate the TI-99/4A system.  These ROMs are copyrighted and not distributed with 
V9t9 (or by me).  

The legal way to get such ROMs is to start up your own TI-99/4A system and transfer the ROMs over, or 
purchase a license for them from Texas Instruments.  Or, there may be ways to find these ROMs 
somewhere else.

V9t9 can recognize ROMs by content.  They do not need special names (as in previous versions),
only a path to the ROMs on your system.

If you have custom modules or ROMS, see "Adding Modules" below.

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

Note: you can supply your own console ROM/GROM images if you like.   Check out the
<a href="advanced.html">"Advanced Setup" page</a> for more information.

Module ROMs
-----------------

In addition, you will need module (cartridge) ROMs to do anything besides TI BASIC programming.  

V9t9 supports the classic TI Emulator / V9t9 <code>\*c.bin/\*g.bin/etc</code> format, and the 
newer RPK format and MESS formats.  V9t9 also auto-detects Mini Memory and saves the non-volatile RAM for you.  

V9t9 allows you to organize your favorite modules in *module lists* so you can quickly switch between
the ones you use most often.


V9t9 Setup Wizard
-----------------

When you first start V9t9, it will show you a *V9t9 Setup* wizard where you can add paths pointing to your ROMs
and modules.
  The directories entered will be used to find both the system ROMs and module ROMs.

V9t9 will show you whether it found required ROMs and optional ROMs, as well as detected modules 
available in the ROM paths.

Unlike in earlier DOS version of V9t9 (and TI Emulator), V9t9 can detect ROMs by MD5 checksum, so you don't 
need to name any ROMs in any particular way. 


Module Setup
------------------

The next page of the V9t9 Setup wizard allows you to configure a *module list*.  The default location
for the file is called *modules.xml* and lives in a *.v9t9j* directory in your home directory.

You need at least one module list to use the *Module Selector* dialog, and you may have as many as you need.

As mentioned above, the path configuration page of the *V9t9 Setup* wizard gives you a preview of
the modules detected on the ROM paths.  

This module list page lets you choose which modules appear in the
list.  Check an entry for it to appear in the Module Selector dialog.  You may click a module name to rename it, if needed.

When closing the dialog, V9t9 will save any updates to the module.xml files.  If it is prompting you to
make a change you don't expect, this may be because the ROM paths changed and different files were detected.
V9t9 will back up the module.xml file (with a "~" extension) if you want to compare the changes and restore
the original file.


Module Selector
------------------

The *Module Selector* dialog mentioned above will show the modules in your lists.  You can use the
filter or type in the tree to quickly find a module by name.  Press Enter on an entry, double click it,
or press *Switch module...* to load the module and reset the emulated computer.

If you want to add a custom module list to the dialog, use the *Setup...* button and point to a new *module.xml* file.  If you want to remove a module list from the dialog, right-click an entry and select *Remove list*.  This does
not affect the database on disk.



Quickly Loading Modules
------------------

If you have some module ROMs on disk somewhere and want to quickly try them out,
just drag one of them (e.g. "mymoduleg.bin") into V9t9.  It will detect the module and offer to launch it.

If V9t9 has problems with your module, <a href="contact.html">let me know</a>!


<hr/>

<div class="footer">
Last updated:  {{site.time}}
</div>
