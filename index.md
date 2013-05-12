---
title: V9t9 (TI-99/4A Emulator)
layout: wikistyle
---
 
Introduction
============


<div class='lookyhere' style='width:inherit'>
<p>
	<a href='images/v9t9-window.png'>
	<img alt="V9t9 image" src="images/v9t9-window_th.png" width="232" height="157"></img>
	</a>
</p>
<div class='lookyhere' style='text-align:left'>
Sound samples:
<ul>
	<li><a href='audio/jawbreaker.mp3' type='audio/mpeg'>Jawbreaker theme (45s)</a></li>
	<li><a href='audio/cassette-recording.mp3' type='audio/mpeg'>Cassette recording (11s)</a></li>
	<li><a href='audio/parsec-speech.wav' type='audio/x-wav'>Parsec speech</a></li>
	<li><a href='audio/teii-speech.wav' type='audio/x-wav'>Terminal Emulator II speech</a></li>
</ul>
</div>
</div>


<div style='width:80%;'>
<p>
V9t9 emulates the TI-99/4A (by default) though it can support other
machines -- only made-up ones for now, using much of the same hardware.
</p>

<p>
It supports:
</p>
<ul>
	<li>Drag and drop / auto-detection of modules, disks, demos, 99/4A files -- try it!</li>
	<li>Ability to save/restore sessions</li>
	<li>Module formats:  V9t9, MESS (.rpk/.zip) (new!)</li>
	<li>Disk support (files in native filesystem, sector images, track images (new!))</li>
	<li>TMS 9918A video</li>
	<li>TMS 9919 sound</li>
	<li>TMS 5220 speech</li>
	<li>Demo playback and recording support</li>
	<li>UCSD P-Code System</li>
	<li>Rudimentary debugger</li>
	<li>Image import</li>
	<li>"Realistic" rendering of the monitor</li>
</ul>

<p>
(Some features here are <a href="advanced.html">advanced</a>.)
</p>
	
</div>

<hr/>

History
-------- 

I've been working on this in various forms since 1992.  The Java port
was quite nastily ported directly from the C port starting 2005.  The C
port never really saw the light of day (it was a bit way too geeky for
the average user to understand, and only built against GNU C in Linux
and Metrowerks Codewarrior in Windows).  The C port was written from 
the assembly port which formed the original V9t9, back when it was 
still being sold.


Recent Changes
===========

<span class="timestamp"> </span>

<ul>
<li>2013/05/12: revamped ROM Setup dialog so you can see, also, what modules will be detected 
</li>
<li>2013/05/12: auto-detection of modules (including Mini Memory) and P-Code DSRs should be more reliable 
</li>
<li>2013/04/28: added Event Log button to review notifications that you may have missed  
</li>
<li>2013/04/27: make buttons more uniform when they have menus; make Load/Save State button a bit more intuitive to use without a menu   
</li>
<li>2013/04/18: ship V9t9 as a zip file only
</li>
<li>2013/03/30: fix issue closing V9t9 in OS X 
</li>
<li>2013/03/13: support handling files encoded with Archiver 3 (e.g. from http://tigameshelf.net)
</li>
<li>2013/03/11: support drag'n'drop of module images into the emulator!
</li>
<li>2013/03/10: support drag'n'drop of disk images into the emulator to load
the disk into the drive, and also to run programs on the disk -- try it out!
(Also see the 'Run...' button in the Disk Selector.)
</li>
<li>2013/03/09: improve UI for Module Selector and allow adding user modules --
RPK format supported as well!
</li>
<li>2013/03/03: split data out from V9t9 JAR to reduce download size
</li>
<li>2013/03/02: "file in a directory" disk access should work now, because the
proper emulated ROMs are actually included (!).
</li><li>2013/03/02: Module Selector should work now -- was incorrectly checking
filenames instead of content, as promised (!).
</li><li>2013/02/27: Make OS X build available -- unfortunately, only through a shell script.
</li>

</ul>
Running
========

Setup
-----

Have Java 6 or newer installed on your system.  

You will need ROMs to actually use the emulator.  I don't provide these but 
**[this page](v9t9-roms.html)** will give you some instructions.


Installation
----------

<div class='lookyhere'>
<a href="http://s3.amazonaws.com/V9t9/data/v9t9.zip">
<img src="images/v9t9-webstart-button.png" />
<p>
v9t9.zip
</p><p>
<i>Download</i>
</p>
</a>
<span class="timestamp"> </span>
</div>

Download the archive to the right and unzip it somewhere on your system.


Running from Windows
----------

Launch V9t9 by double-clicking the `v9t9.bat` file.  

If this exits immediately, be sure `java` is on your `PATH`.

Alternately, run a `Command Prompt` and type:

    cd \path\to\extracted\v9t9
    v9t9.bat

Running from OS X or Linux
------------

Launch V9t9 by double-clicking the `v9t9.sh` file.
  
If this exits immediately, be sure `java` is on your `PATH`.

Alternately, run a `Terminal` and type:

    cd /path/to/extracted/v9t9
    ./v9t9.sh


Advanced
=======

Please see <a href="advanced.html">this page</a> for advanced usage and 
configuration.

Contact
=======

Please see <a href="contact.html">this page</a> for details.


License
=======

The V9t9 Java codebase is licensed under EPL v1.0, with the exception that *no commercial
redistribution is allowed*.

This software includes content licensed under [EPL v1.0](http://www.eclipse.org/legal/epl-v10.html); 
code from [the Base64 library](http://iharder.net/base64);  
[Vlad Roubtsov's HRTimer library](http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html);
LWJGL utilities from Kevin Glass and Brian Matzon; and the various packages from Apache Commons. 

<hr/>
<div class="footer">
Last updated:  {{site.time}}
</div>


<div style='float:right;text-align:center;width:inherit'>
<p>
        <img alt="Friend of Eclipse" src="http://www.eclipse.org/donate/images/friendslogo200.jpg"></img>
</p>
</div>

