---
title: V9t9 (TI-99/4A Emulator)
layout: wikistyle
---
 
Introduction
============


<div class='lookyhere' style='width:33%'>
<p>
	<a href='images/v9t9-window.png'>
	<img alt="V9t9 image" src="images/v9t9-window_th.png" width="232" height="157"/>
	</a>

</p>


<a href="http://s3.amazonaws.com/V9t9/data/v9t9.zip">
<img src="images/v9t9-webstart-button.png" />
<p>
<i>Download v9t9.zip</i>
</p>
</a>
<span class="timestamp"> </span>

<!--  not rebuilt yet

<p/>

<a href="http://s3.amazonaws.com/V9t9/data/mac_v9t9.zip">
<img src="images/v9t9-webstart-button.png" />
<p>
<i>Download mac_v9t9.zip</i>
</p>
</a>
<span class="timestamp"> </span>
-->

</div>


<div style='width:80%;'>
<p>
V9t9 emulates the TI-99/4A on your computer.
</p>

<p>
It supports:
</p>
<ul>
    <li>Configurable PC controller to joystick mapping</li>
    <li>RS232/PIO output and TI Impact Printer emulation</li>
    <li>Reading/writing cassette recordings to files</li>
	<li>Drag and drop / auto-detection of modules, disks, demos, 99/4A files</li>
	<li>Ability to save/restore sessions</li>
	<li>Module formats:  V9t9, MESS (.rpk/.zip), PBX banked modules</li>
	<li>Disk support (files in native filesystem, sector images, track images)</li>
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



Examples
=======

<p>
Sound samples:
<ul>
	<li><a href='audio/jawbreaker.mp3' type='audio/mpeg'>Jawbreaker theme (45s)</a></li>
	<li><a href='audio/cassette-recording.mp3' type='audio/mpeg'>Cassette recording (11s)</a></li>
	<li><a href='audio/parsec-speech.wav' type='audio/x-wav'>Parsec speech</a></li>
	<li><a href='audio/teii-speech.wav' type='audio/x-wav'>Terminal Emulator II speech</a></li>
</ul>
</p>


Recent Changes
===========

<span class="timestamp"> </span>


<h2>2017/03/05 <a href="http://s3.amazonaws.com/V9t9/data/v9t9-170305.zip">(download)</a></h2>
<ul><b>New/published features:</b>
    <li>Allow configuring PC controller to joystick mappings</li>
    <li>Extract image importer into its own tool (v9t9.sh -tool ConvertImages), with enhanced support for various 4/8/15/16 bit depth modes and palettes</li>
</ul>
<ul><b>Bug fixes:</b>
    <li>Allow editing the way controllers map to joysticks <a href="https://github.com/eswartz/emul/issues/13">(bug #13)</a></li>
    <li>Fix detection of keyboard in certain modules <a href="https://github.com/eswartz/emul/issues/9">(bug #9)</a></li>
    <li>Find P-Code binaries more reliably <a href="https://github.com/eswartz/emul/issues/8">(bug #8)</a></li>
    <li>Improve keyboard mapping for joysticks <a href="https://github.com/eswartz/emul/issues/6">(bug #6)</a></li>
	<li>Add help to v9t9.sh</li>
    <li>If v9t9 crashes on Linux, due to PulseAudio issues, you can pass <tt>-Dv9t9.sound.java=true</tt> to use Java implementation</li>
    <li>Avoid cases where configuration settings are lost on macOS</li>
    <li>Fix bug in X instruction</li>
    <li>Various disassembler/assembler fixes</li>
</ul>


<h2>2015/07/21 <a href="http://s3.amazonaws.com/V9t9/data/v9t9-150721.zip">(download)</a></h2>
<ul><b>New/published features:</b>
    <li>Add RS232/PIO configuration -- for now, just determining whether the printer dialog opens or not</li>
    <li>Improve printer dialog UI, allowing saving images to disk, and using less memory when multiple pages are printed</li>
    <li>Add cassette reading/writing configuration button</li>
    <li>Support PgUp/PgDn and Ctrl Home/End in debugger CPU instruction view</li>
</ul>
<ul><b>Bug fixes:</b>
    <li>Improve emulation speed significantly (for lower-end machines) <a href="https://github.com/eswartz/emul/issues/2">(related to bug #2)</a></li>
    <li>Improve video updating synchronization for less tearing <a href="https://github.com/eswartz/emul/issues/2">(bug #3)</a></li>
    <li>Fix keyboard buffering that interfered with gameplay <a href="https://github.com/eswartz/emul/issues/2">(bug #4)</a></li>
    <li>Fix cataloging and distinguishing of modules which are named the same <a href="https://github.com/eswartz/emul/issues/2">(bug #5)</a>.<br/>
    V9t9 knows about a large number of unnamed (auto-start) and ambiguously named modules ("Milliken", "For English") and names them for you.  V9t9 now also identifies and filters out duplicate modules. 
    <br/><b>NOTE:</b> the format of the <tt>modules.xml</tt> file has changed, so saving any changes with this version may not work with older releases.</li>
    <li>Improve setup wizard load speed and make modules page simpler.</li>
    <li>Fix disk/device selector history (can easily swap directories/images between DSKx entries without opening/closing dialog)</li>
    <li>Ship <tt>v9t9render</tt> libraries.  <b>If V9t9 crashes on startup</b> for you, especially when running under VirtualBox, try passing <tt>--client SWTAWT</tt> to <tt>v9t9.sh</tt> or <tt>v9t9.bat</tt> to work around it.</li>
</ul>
<ul><b>Site features:</b>
    <li>Added archives of older V9t9 builds.</li>
</ul>

<h2>2015/07/02 <a href="http://s3.amazonaws.com/V9t9/data/v9t9-150702.zip">(download)</a></h2>
<ul>
<li>add -Djna.nosys=true to v9t9.sh by default.</li>
<li>allow VMARGS to be set outside v9t9.sh.</li>
<li>allow -Dv9t9.sound.rate=... to change ALSA rate and -Dv9t9.sound.java=true to bypass custom sound usage for Linux and Windows (possible fixes for <a href="https://github.com/eswartz/emul/issues/2">bug #2</a>)
</li> 
<li>fix problem with USCD P-System option no longer being available (thanks to RvK for noticing)
</li> 
<li>try again to fix OS X class loading issues (thanks Scott S. and Michael R. for reminding me)
</li> 
<li>revamped device configuration -- one button for selecting devices, then each device icon allows
individual configuration
</li>
<li>other GUI tweaks
</li>
</ul>

<h2>2014/05/19 <a href="http://s3.amazonaws.com/V9t9/data/v9t9-140519.zip">(download)</a></h2>

<ul>
<li>fix OS X support (for real?)
</li>
<li>added initial RS232 &quot; PIO support with TI Impact Printer emulation (try the "Printer Example" demo)
</li>
<li>fix Windows XP (pre-OpenGL 1.5) support
</li>
</ul>

<h2>2013/10/20 <a href="http://s3.amazonaws.com/V9t9/data/v9t9-131020.zip">(download)</a></h2>
<ul>
<li>fix bug with joystick 'fire' detection in hand-coded assembly  
</li>
<li>add breakpoint support to debugger (right-click on an instruction to set/reset)  
</li>
<li>fix bug using numeric keypad as joysticks (use Num Lock + Scroll Lock) 
</li>
<li>fix launching for Win 7 
</li>
</ul>

<h2>2013/06/17 <a href="http://s3.amazonaws.com/V9t9/data/v9t9-130617.zip">(download)</a></h2>
<ul>
<li>added support for Corcomp double-density disk controller
</li>
<li>make audio gate more consistent
</li>
<li>fix double-sided disk detection with *.dsk files
</li>
<li>add EA/8K Super Cart support
</li>
<li>fixed bug losing history from disk selector dialog
</li>
<li>fixed bug leading to appearance that module list setup could not find 
all the required files
</li>
<li>changed module list setup.  Instead of showing only modules
registered in a central database, V9t9 will prompt you to establish a
modules.xml file containing a custom list of modules detected on your
ROM paths.
</li>
<li>more accurate DIV cycle counting
</li>
</ul>

<h2>Older releases</h2>

Well, I don't have any and can't rebuild them, since they use Java Web Start, which is essentially obsolete now
and apparently impossible to build or use in new Java releases.


Running
========


Setup
-----

Have Java 6 or newer installed on your system.  

You will need ROMs to actually use the emulator.  I don't provide these but 
**[this page](v9t9-roms.html)** will give you some instructions.


Installation
----------

Download the <code>v9t9.zip</code> archive and unzip it somewhere on your system.


Running from Windows
----------

Launch V9t9 by double-clicking the `v9t9.bat` file.  

If this exits immediately, be sure `java` is on your `PATH`.  If it crashes
under a VM, try passing <tt>--client SWTAWT</tt>.

Alternately, run a `Command Prompt` and type:

    cd \path\to\extracted\v9t9
    v9t9.bat

Running from OS X or Linux
------------

Launch V9t9 by double-clicking the `v9t9.sh` file.
  
If this exits immediately, be sure `java` is on your `PATH`.  If it crashes
under a VM, try passing <tt>--client SWTAWT</tt>.

Alternately, run a `Terminal` and type:

    cd /path/to/extracted/v9t9
    ./v9t9.sh


Keyboard Mappings
========

The 99/4A keyboard has 40 keys and your keyboard has more.  The 99/4A formed the rest of the ASCII character set using the "Fctn" key with other alphanumeric keys.  Also, 99/4A programs often refer to symbolic key names like "REDO" and "PROC'D", which map to "Fctn" plus number keys.

In V9t9, use "`Alt`" for "`Fctn`".

These are the `Fctn-`+_number_ mappings:

* `Fctn-1` = `DELETE`
* `Fctn-2` = `INSERT`
* `Fctn-3` = `ERASE`
* `Fctn-4` = `CLEAR`  (this also stops BASIC programs and some RS232/PIO device operations; hold it down)
* `Fctn-5` = `BEGIN`
* `Fctn-6` = `PROC'D`
* `Fctn-7` = `AID` (often used for help)
* `Fctn-8` = `REDO`
* `Fctn-9` = `BACK` (like Esc)

These keys that don't exist on the 99/4A are automatically mapped as follows by V9t9.  

These choices try to align with the patterns of typical 99/4A software.  If you object, please file a feature request!

* `Arrow Down` = `Fctn-X`
* `Arrow Up` = `Fctn-E`
* `Arrow Left` = `Fctn-S`
* `Arrow Right` = `Fctn-D`
* `Home` = `Fctn-5` (BEGIN)
* `End` = `Fctn-0`
* `Insert` = `Fctn-2` (INSERT)
* `Delete` = `Fctn-1` (DELETE)

(The keys above are the same for the numeric keypad when Num Lock is enabled and joystick mode is not enabled.)

* `Page Up` = `Fctn-6` (PROC'D)
* `Page Down` = `Fctn-4` (CLEAR)
* `F`*number* = `Fctn-`*number*

Joystick Mappings
====

The standard 99/4A system came with two joysticks -- *aka* "handheld controllers".

V9t9 provides several ways to emulate the 99/4A joysticks.

PC Controllers
----

V9t9 can use connected PC controllers or joysticks and map them to 99/4A joysticks.  

* V9t9 only selects controllers with X and Y controls which don't act like mice.
* You must restart V9t9 for it to detect and use changes in connected joysticks.
* Select the "Joystick" icon in the left-hand toolbar to configure how the connected 
controllers map to the TI Joystick.
* This dialog (in `Interactive` mode) shows live updates of whichever controllers were detected at startup
and allows you to assign their components to Joystick 1 or 2 (or neither) and then
choose how each component contributes to the joystick.

   * `IGNORE`: the component is not used (handy for buggy USB controllers with stuck values)
   * `X_AXIS`, `Y_AXIS`: map an analog control to the X or Y axes
   * `DIRECTIONAL`: map a single analog control to X and Y axes
   * `UP`, `DOWN`, `LEFT`, `RIGHT`: map buttons to these directions (useful for Playstation 3 directional-pad buttons)
   * `BUTTON`: map the button to Fire

* You may also edit the configuration (`Edit` mode).  The syntax should be somewhat obvious.

V9t9 will keep track of various combinations of detected controllers, but may not migrate
settings from e.g. controller A if you run with controllers A and B the next time.  

Whatever edits you make in the `Interactive` or `Edit` modes applies only for the specific
controller setup active when you run V9t9.

See `~/.v9t9j/config` 
and `ControllerConfig` and `Joystick1Config` and `Joystick2Config` for details
if you want to copy settings between controller configurations without doing this by hand.

Emulating with host keyboard
----

V9t9 can use the keyboard's numeric keypad as "joystick #1" (and also "joystick #2" if you enjoy a difficult challenge).  

Press *Scroll Lock* to toggle between modes.  

By default the numeric keypad is used for ASCII numbers or 99/4A keyboard arrows (whatever `Num Lock` indicates).  You'll see a notification to the lower-right telling you the current mode.

When you see "Using numpad for joystick #1 (shift for #2)", you can use the numeric keypad.

<pre>
    8

4   5   6      fire = 'Enter', '+', or '-'   reset = 5 (in case something's stuck)

    2
</pre>

If you need to emulate "joystick #2", hold down `Shift`.  (I told you it'd be a challenge.)

Standard keyboard mappings
---

99/4A programs usually support keyboard-only setups:

Player 1:

<pre>
    E      

S       D      fire = Q

    X
</pre>

Player 2:

<pre>
    I

J       K      fire = Y

    M
</pre>






<hr/>

Contact
=======

Please see <a href="contact.html">this page</a> for details.

History
-------- 

I've been working on this in various forms since 1992, when I started working
on the idea in Turbo Pascal as "tiemul.pas" in the computer lab, and once crashed the
computer's server when I ran it there without permission.

That next summer, I started rewriting the emulator in assembly for DOS and soon made the
emulator into a product: "TI Emulator!" (yes, with the exclamation point).  It was
my first venture into the world of business -- and copyright infringement.  (I had
been shipping ROMs around, and TI reminded me that I should have a license agreement and
be paying royalties.)

This was an exciting time, interacting with people all over the
world through letters and emails.  A loyal user suggested the name "V9t9" and the product
was renamed and a few more versions were released.

I got several requests to support then-current 
custom hardware for the aging TI-99/4A, but unfortunately was not experienced enough to
infer the operation of hardware without having it in person, so those efforts never got
off the ground.

A few years later, I moved from DOS to Linux and started porting V9t9 to C.
This project was exciting, since it involved making the thing portable and capable
of supporting multiple kinds of graphics, sound, and input APIs.  This 
port never really saw the light of day (it was a bit way too geeky for
the average user to understand, and only built against GNU C in Linux
and Metrowerks Codewarrior in Windows).

Many years later, I started porting V9t9 to Java in 2005. The Java port
was originally quite nastily ported directly from the C port, and I've
been gradually rewriting chunks in a proper object oriented format ever since.

I would be remiss not to mention the driving motivation for continuing to work
on the emulator at all in the 2000's, after my original TI-99/4A became mostly unusable (no
access to a TV or monitor with RCA connectors, for example) -- 
Thierry Nouspikel's
excellent compendium of technical data, <a href="http://www.nouspikel.com/ti99/titechpages.htm">The TI-99/4A Tech Pages</a>.  
I have gone back to this site again and
again over the years to refine my understanding of hardware I've not used in years.


Advanced
=======

Please see <a href="advanced.html">this page</a> for advanced usage and 
configuration.


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

