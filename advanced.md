---
title: Advanced Usage
layout: wikistyle
---

Advanced Settings
==================

Right-click on the emulator screen and select `Advanced Controls` to enable
a toolbar at the bottom of the screen.  This allows you to:

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




