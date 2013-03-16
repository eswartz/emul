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

V9t9 includes a database of modules which you will see in the *Module Selector* dialog.
Any modules V9t9 detects will show up in this list.


Setup
-----------------

When you first start V9t9, it will show you a "ROM Setup" dialog where you can add paths pointing to your ROMs.
  The directories entered will be used to find both the system ROMs and module ROMs.

Unlike in earlier DOS version of V9t9 (and TI Emulator), V9t9 can detect ROMs by MD5 checksum, so you don't 
need to name any ROMs in any particular way.  The information about the system ROMs and a large database of 
TI-99/4A modules is built into V9t9 now.  So, all you need to do is point V9t9 to your collection and V9t9 
will discover them for you.


Loading Modules
------------------

If you have some module ROMs on disk somewhere and want to quickly try them out,
just drag one of them (e.g. "mymoduleg.bin") into V9t9.  It will detect the module and offer to launch it.

If V9t9 has problems with your module, <a href="contact.html">let me know</a>!

Making Module Lists
------------------

If you want to permanently add your own modules to V9t9, you can add a *module list* 
and access the entries in the *Module Selector* like this:

* Open the *Module Selector* dialog.

* Click the '*Add...*' button.

* Browse to the directory where your ROMs reside.

* V9t9 should detect the modules.  (If some aren't detected, <a href="contact.html">let me know</a>!)

* V9t9 will select (checkmark) by default the modules that it doesn't already know about.  You can add or remove entries as you desire.

* Enter a new *List File* -- usually called 'modules.xml' -- where you want to record your modules.

* All filenames in `modules.xml`  will be detected on the "Search Locations" path.  V9t9 records full paths by default when doing scanning, but feel free to edit the modules.xml file to make the filenames more portable.  (You can use relative paths from some "Search Location" path too.)

<hr/>

<div class="footer">
Last updated:  {{site.time}}
</div>
