---
title: Advanced Stuff
layout: wikistyle
---

Track Disk Images
==================

V9t9 now supports disk track images (\*.trk).  Yes, I have been able to transfer
the Advanced Diagnostics, DisKassembler, and Draw 'n' Plot disks and run them
in V9t9.

I don't currently have access to
the software to fetch these from your own 99/4A, but I'm sure someone with a 
working system can do it.  See <a href="http://nouspikel.group.shef.ac.uk//ti99/disks.htm#Tracks%20&amp;%20sectors"> Thierry's page</a> for details.

The format is:

<pre>
'trak'       (magic, 4 bytes)
0x1          (version, 1 byte)
&num;tracks      (1 byte)
&num;sides       (1 byte) 
0            (unused, 1 byte)
track size   (maximum size in bytes, big-endian, 2 bytes)
data offset  (from start of file: usually 12; big-endian, 2 bytes)
</pre>

Then, each track's raw data follows, as you might fetch with the algorithm at
<a href="http://nouspikel.group.shef.ac.uk//ti99/disks2.htm#Track%20access">Thierry's page</a>.
Each track must use the same number of bytes.  V9t9 is very relaxed about the actual track data, since 
the track data can have a variable number of lead bytes between sectors.

V9t9 has only been tested with FM (single-density) disks so far.  If you would 
like to donate a dumped MFM disk image for me to test, I'd <a href="contact.html">appreciate it</a>.

V9938 / MSX2 video support
===============

The front page mentions V9938 / MSX2 support.  I emulated this strictly based
on data sheets and some random pieces of documentation some people sent me in the
early days of V9t9.  It may, thus, be completely wrong in some ways.

For example, docs mention that the 99/4A ROM's purportedly incorrect
VDP register mappings don't work with the V9938.  V9t9 does the "right thing" and
masks the address ranges to 16k for legacy graphics modes -- I didn't want to
make a fake DSR ROM to patch the startup code.  If it's important to emulate all
the buggy behavior, I'd appreciate references to programs that actually do V9938
rendering, so I can test.

Anyway, I was able to run ZORK in 80-column mode in V9t9.  So I guess it works
well enough ;)

Enabling
----------

To enable this, you'll need the v9t9.zip file (not the web launcher).  Edit
the `v9t9-local.jnlp` file and remove the comments (&lt;!-- and --&gt;) lines at the end
around this &lt;argument&gt; node:

<pre>
	&lt;application-desc 
		main-class="v9t9.gui.Emulator"&gt;
		...
<b>             <argument>EnhancedTI994A</argument> </b>
		...
   	 &lt;/application-desc>
    
</pre>

Or in the `v9t9.sh` file, add `EnhancedTI994A` to the command line:

<pre>
	"$JAVA" -cp "v9t9j.jar:$SWT:libs/*" -Djava.library.path=tmpdir $VMARGS  v9t9.gui.Emulator <b>EnhancedTI994A</b>
</pre>

You'll know this worked if you start up and notice the palette in the startup screen 
is a bit off -- the V9938 palette is slightly different from the TMS9918A's.

Forth99B Machine
==================

I'll need to rename this, since it is not a 9900-based machine.  But it does
use the 99/4A VDP, GROM/GRAM, and disk image formats.  And it uses the V9938 video chip
and a crazy custom sound chip with 12 voices and sound effects.  

All this stuff is only documented in the sources.  It's 100% a hobby project,
so use at your own risk.  It can and will change unpredictably!

Enabling
----------

To enable this, you'll need the v9t9.zip file (not the web launcher).  Edit
the `v9t9-local.jnlp` file and remove the comments (&lt;!-- and --&gt;) lines at the end
around this &lt;argument&gt; node:

<pre>
	&lt;application-desc 
		main-class="v9t9.gui.Emulator"&gt;
		...
<b>            <argument>Forth99B</argument> </b>
		...
   	 &lt;/application-desc>
    
</pre>

Or in the `v9t9.sh` file, add `Forth99B` to the command line:

<pre>
	"$JAVA" -cp "v9t9j.jar:$SWT:libs/*" -Djava.library.path=tmpdir $VMARGS  v9t9.gui.Emulator <b>Forth99B</b>
</pre>

Quick setup
----------

* Forth99 uses a GRAM disk image called `f99bgram.bin`.  This will be copied
into your `${config}/.v9t9j/module_ram` directory on first launch.
* If you want to use disk images instead, call the word `DSK1`.  Call `GRAM` to get
back to the GRAM image.
* Use `edit` to edit the current block.  Use <i>number</i> `>edit` to edit
a specific block.
* Unfortunately the built-in dictionary is stored mostly in GROM to save space,
so `words` will only show your custom words.  
Check <a href="https://github.com/eswartz/emul/tree/master/v9t9/v9t9-java/tools/Forth99">the sources</a>
to see what's available.

Image Import
===============

You can load graphics files into V9t9 and it will render them in the current graphics mode. 
This is totally a hobby feature and is not intended to be useful to anyone, for any reason.

This feature may be a bit of a mystery -- unfortunately it only works nicely 
with a real video chip (e.g. V9938 / MSX2) in a high-res mode.  When you try this with
the TMS9918A graphics 0 mode, the results are poor.  

Try it in a bitmapped mode (like for Parsec), or use the EnhancedTI994A machine (above) and the Debugger (below) to force the system into a better mode.  Or, use the Forth99B machine
and use one of "0 mode" through "10 mode" to activate different graphics modes.

And pause the emulator so the running program doesn't overwrite
the results ;)


RnD Settings
==================

Right-click (or Ctrl-Click on a single-button Mac mouse) on the emulator screen and select `Advanced Controls` to enable a toolbar at the bottom of the screen.  This allows you to:

* view emulated CPU performance & interrupt rate
* send a NMI (not really useful on 99/4A since it crashes the system)
* turn on a machine listing (e.g. `instrs_full.txt` in your temporary folder)
* access the built-in debugger
* modify speech synthesis settings
* edit configuration variables


Configuration Variables
--------------------

V9t9 is a hobby project of mine, thus there are lots of ways to configure it to
act slightly or completely different.

As mentioned above, the `Advanced Controls` main screen context menu option will enable the advanced toolbar, which has a button for editing configuration variables on the far right.

Change the options to cause a change in behavior.

*FAIR WARNING:*  several of these options refer to unimplemented or half-implemented
features, and some variables are used internally and shouldn't really be editable
in this dialog.  Don't be surprised if V9t9 crashes!

Configuration Files
--------------------

V9t9 stores two configuration files for your settings.  Both are located under
your user directory (e.g. `%USERPROFILE%` in Windows or `$HOME` on Linux and Mac OS X)
in a directory called `.v9t9j`.

The file `config` stores global emulator settings, like the window location and size,
monitor effects, sound volume, etc.

The file `workspace.StandardTI994A` file stores settings specific to V9t9 when
emulating the TI-99/4A.

These files are normal XML.  The conventions should be obvious from the contents:
the &lt;str&gt; element names a single-valued configuration variable, and &lt;strs&gt; element
names a variable representing a list of &lt;str&gt; elements.

Using Custom ROMs
---------------------

The *Setup ROMs* dialog expects standard TI-99/4A ROMs but feel free to substitute
your own.  Since V9t9 won't detect these by content, you'll need to either:

* rename your console ROM as `994arom.bin`
* rename your console GROM as `994agrom.bin`

OR, these configuration variables in `workspace.StandardTI994A`:

* `RomFileName`
* `GromFileName`

For more advanced memory configuration, make a custom `modules.xml` and 
pretend your ROMs are a module.  See below.


Module ROM formats
---------------------

If you're curious about the details, V9t9 supports two primary formats for module ROMs.  

First, in the V9t9 family of emulators, ROMs are stored as raw binary files in named groups like:

* `<module>G.bin` (GROM image at >6000)
* `<module>C.bin` (ROM image at >6000)
* `<module>D.bin` (ROM second bank at >6000)

The "C" and/or "D" variants may not be present, and sometimes not even the "G" variant.

V9t9 also handles naming used in the "tosec" naming convention.

Finally, V9t9 supports the MESS RPK format.  This is a ZIP-format archive with raw binary images and
several XML files.  V9t9 uses the `softlist.xml` file to organize the contents.  

Module list format
-------------------

The `modules.xml` file format saved by the *Module Selector* dialog's "*Add*" option
is a simple XML file with a top-level &lt;modules&gt; element containing zero
or more &lt;module&gt; elements.

Each &lt;module&gt; element contains &lt;memoryEntries&gt; elements describing
the files that contribute to various memory regions.  As an example:

<pre>
&lt;modules&gt;
    &lt;module name="My Module Name"&gt;
        &lt;memoryEntries&gt;
            &lt;gromModuleEntry fileName="moduleg.bin" /&gt;
               &lt;!-- either this or the following, or neither --&gt;
            &lt;romModuleEntry fileName="modulec.bin" /&gt;  
            &lt;!-- for standard 'write to 0x6000' type bank switching --&gt;
            &lt;bankedModuleEntry fileName="modulec.bin" fileName2="moduled.bin" /&gt;  
        &lt;/memoryEntries&gt;
    &lt;/module&gt;
&lt;/modules&gt;
</pre>

Note that `bankedModuleEntry` supports the "standard" 2-bank-in-2-files model as well as files
where N banks are stored in one file (e.g. as used in retroclouds' Pitfall).  You may need the `reversed` attribute as well.

The `entry` elements above may also include a plain &lt;memoryEntry&gt; element.  All of them support these attributes:

* `fileName`:  filename, relative path, or absolute path to the file contents
* `fileName2`:  second filename for two-banked modules where each ROM is in a separate file.  Relative path, or absolute path to the file contents for banked modules
* `fileMD5`:  MD5 sum for fileName 
* `file2MD5`:  MD5 sum for fileName2
* `domain`:  memory domain:  one of `'CPU'`, `'GRAPHICS'`, `'SPEECH'` or `'VIDEO'`
* `address`:  load address of file
* `size`:  size of memory region in bytes (can be negative to denote the maximum memory area,
if the file's size is variable)
* `offset`:  offset into fileName in bytes
* `offset2`: offset into fileName2 in bytes
* `stored`: if `true`, the file is non-volatile RAM (e.g. for Mini Memory) and is saved in the 
path denoted by the `StoredRamPath`  variable
* `reversed`: for banked module entries: if `true`, the banks in the file are stored in reversed order
from the `write address`.  For example, writes to >6000,
>6002, >6004, and >6006 normally select banks 0, 1, 2, 3; with `true`, they select banks 3, 2, 1, 0.



<hr/>
<div class="footer">
Last updated:  {{site.time}}
</div>

