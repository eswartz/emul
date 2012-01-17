---
title: Home
layout: wikistyle
---

Introduction
============

V9t9 emulates the TI-99/4A (by default) though it can support other
machines -- only made-up ones for now, using much of the same hardware.

History
-------- 

I've been working on this in various forms since 1992.  The Java port
was quite nastily ported directly from the C port starting 2005.  The C
port never really saw the light of day (it was a bit way too geeky for
the average user to understand, and only built against GNU C in Linux
and Metrowerks Codewarrior in Windows).  The C port was written from 
the assembly port which formed the original V9t9, back when it was 
still being sold.

Running
========

Setup
-----

Have Java 6 or newer installed on your system.  If you want to use the 
remote launch option, ensure you also have a Java plugin for your browser.

Local
-----

Download the archive below and unzip it somewhere on your system.

[V9t9.zip](http://eswartz.github.com/emul/data/v9t9.zip) 

    $ cd /path/to/v9t9
    $ javaws v9t9-local.jnlp

Remote
------

Click on the link below.  

[V9t9 Web Start](http://eswartz.github.com/emul/data/v9t9/v9t9.html)

License
=======

This software includes content licensed under [EPL v1.0](http://www.eclipse.org/legal/epl-v10.html) and code from [the Base64 library](http://iharder.net/base64) and [Vlad Roubtsov's HRTimer library](http://www.javaworld.com/javaworld/javaqa/2003-01/01-qa-0110-timing.html).


Last updated:  {{site.time}}
