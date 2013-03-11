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
	<li>Ability to save/restore sessions</li>
	<li>Built-in module database</li>
	<li>TMS 9918A video</li>
	<li>TMS 9919 sound</li>
	<li>TMS 5220 speech</li>
	<li>Disk support (files in native filesystem, sector images, track images)</li>
	<li>Demo playback and recording support</li>
	<li>UCSD P-Code System</li>
	<li>Rudimentary debugger</li>
	<li>Image import (new!)</li>
	<li>"Realistic" rendering of the monitor (new!)</li>
</ul>

<p>
(The debugger and other "RnD" features are enabled by right-clicking the screen and
invoking "Enable Advanced Controls".  Then, a new toolbar appears at the bottom of
the screen with a "Toggle Debugger" button.)
</p>
	
<p>
V9t9 also contains support for:
</p>
	<ul>
		<li>V9938 (MSX2) (new!)</li>
		<li>Multiple TMS9919 ("ForTI") support (new!)</li>
		<li>Experimental FORTH processor ("F99B") (new!)</li>
	</ul>
	
<p>These can be enabled by modifying the *.jnlp file and passing "Forth99B"
in the &lt;argument&gt; element of the &lt;application-desc&gt; element.
</p>	
</div>


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

<ul>
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

Have Java 6 or newer installed on your system.  (The "Using Web Start" section may 
assist with this.)

You will need ROMs to actually use the emulator.  I don't provide these but 
**[this page](v9t9-roms.html)** will give you some instructions.


Installing Locally
--------------------

<div class='lookyhere'>
<a href="http://s3.amazonaws.com/V9t9/data/v9t9.zip">V9t9.zip</a>
<p>
<i>Download</i>
</p>
</div>

Download the archive to the right and unzip it somewhere on your system.

<b>On Windows or Linux:</b>

Launch it by double-clicking the `v9t9-local.jnlp` file in your favorite operating system 
(though only Linux and Windows have been tested so far).

Or, if your Java isn't installed properly, try:

    $ cd /path/to/v9t9
    $ javaws v9t9-local.jnlp

(or the equivalent in Windows)

<b>On OS X:</b>

Java Web Start does not play nicely with SWT on OS X, so you need to use a script instead:

    $ cd /path/to/v9t9
    $ sh v9t9.sh


Using Web Start (Windows or Linux only)
--------------------

<div  class='lookyhere' style=' padding: 0em 1em;'>
<script type="text/javascript">
    //var dir = location.href.substring(0, location.href.lastIndexOf('/')+1);
    var url = "http://s3.amazonaws.com/V9t9/data/v9t9-remote.jnlp";
    deployJava.launchButtonPNG = 'images/v9t9-webstart-button.png';
    deployJava.createWebStartLaunchButton(url, '1.6.0');
</script>
<noscript>This page requires JavaScript.</noscript>
<p>
<i>Launch</i>
</p>
</div>

Click on the button to the right.  This will fetch the most recent V9t9 build each time you use it.

(Java Web Start does not play nicely with SWT on OS X, sorry.)

Contact
=======

Sources are hosted in github at:  <a href="https://github.com/eswartz/emul">
https://github.com/eswartz/emul</a>.

If you have questions or issues, please contact me at ed.swartz.twofiftyeight@gmail.com, but
use a number instead of spelling it out like that.


License
=======

The V9t9 Java codebase is licensed under EPL v1.0, with the exception that *no commercial
redistribution is allowed*.

This software includes content licensed under [EPL v1.0](http://www.eclipse.org/legal/epl-v10.html); 
code from [the Base64 library](http://iharder.net/base64);  
[Vlad Roubtsov's HRTimer library](http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html);
LWJGL utilities from Kevin Glass and Brian Matzon; and the various packages from Apache Commons. 



<div class="footer">
Last updated:  {{site.time}}
</div>
