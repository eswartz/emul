\	99memory.fs					-- FORTH memory layout
\
\	(c) 1996-2009 Edward Swartz
\
\   This program is free software; you can redistribute it and/or modify
\   it under the terms of the GNU General Public License as published by
\   the Free Software Foundation; either version 2 of the License, or
\   (at your option) any later version.
\ 
\   This program is distributed in the hope that it will be useful, but
\   WITHOUT ANY WARRANTY; without even the implied warranty of
\   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
\   General Public License for more details.
\ 
\   You should have received a copy of the GNU General Public License
\   along with this program; if not, write to the Free Software
\   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
\   02111-1307, USA.  
\
\	$Id: 99memory.fs,v 1.11 2009-01-17 23:20:51 ejs Exp $

\ only forth also cross
\ >target


\ asm
>minimal

T has? enhanced-mode H [if]
$ff80       constant    VDPRD
$ff82       constant    VDPST
$ff88       constant    VDPWD
$ff8a       constant    VDPWA
$ff8c       constant    VDPCL
$ff8e       constant    VDPWI
$ff90       constant    GPLRD
$ff92       constant    GPLRA
$ff94       constant    GPLWD
$ff96       constant    GPLWA
$ff98       constant    SPCHRD
$ff9a       constant    SPCHWT
$ffa0       constant    SOUND  \ ... 0x20!
[else]
$8c02       constant    VDPWA
$8c00       constant    VDPWD
$8800       constant    VDPRD
$8802       constant    VDPST
$8c04       constant    VDPCL
$8c06       constant    VDPWI
$9800       constant    GPLRD
$9802       constant    GPLRA
$9c00       constant    GPLWD
$9c02       constant    GPLWA
$9000       constant    SPCHRD
$9400       constant    SPCHWT
$8400       constant    SOUND
[endif]

\   Memory layout
\
\   This $2400 comes from nforth.tsm -- the console ROM 
\   reserves $2000...$2400 
\
\   These constants are not available to the end-user.
\
>cross

T has? enhanced-mode H [if]

$D800 constant low-ram-start

$4000 constant high-ram-start

[else]

$2400 constant low-ram-start

$a000 constant high-ram-start

[endif]

>minimal

low-ram-start
            constant  StartRAM
$400        constant   (#rp)
$400        constant   (#sp)
$100        constant   (#pad)
$100        constant   (##-pad)
$100        constant   (#slit-pad)
$100        constant   (#tib)
\ #>C00  (3800 or E400)
$C0C        constant  (#blk)      \ block space
\ 3C0C  or F00C;    CPU RAM -> F700 in enhanced mode
\ (user space)

