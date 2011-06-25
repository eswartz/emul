\	init.fs						-- FORTH initialization/bootstrap
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
\	$Id: init.fs,v 1.29 2009-02-25 02:18:24 ejs Exp $

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

\ Colon def for booting
| : (boot)
    (cold)
;

| Code ((cold))
1 $:
\   dbg

    li RP , (rp0) #
    li SP , (sp0) #

    li rNEXT R , ' @Next #

    li  R11 , ' (boot) 2 +  #   \ !!
    li DODOES , ' :dodoes #
    li DOCON , ' :docon #
    li DOCOL , ' :docol #
    \ infinite loop
    limi 1 #

    b   rDOCOL *R

end-code

code BYE
    blwp 0 @>
end-code

: ABORT
    ." aborted" cr
\   forth definitions           \ !!!
    quit
;

| Code ((abort))
    limi 0 #
    lwpi fws #
    clr r12
    sbo 1 #
    sbo 2 #
    sbz 3 #
    clr vintflags @>
    li  R11 , ' ABORT 2 +  #    \ !!
    b   rDOCOL *R

end-code

| : (splash)
	." V9t9 FORTH" cr
	." press shift+ctrl+fctn+space to abort" cr
	cr

	\ debug notes
	\ ." file words are really inefficient" cr
;

| : (cold) 
	\ This code is very important!
	\ We didn't initialize these DEFERed words earlier
	\ since their deferral address must reside in RAM
	\ in order to be changed.

	\ Also, the use of (IS) is specific to
	\ the cross compiler, which assumes we have more
	\ stuff between the NFA and the PFA than we do.
	\ (i.e., not >body)

	['] (EMIT) ['] EMIT  (IS)
	['] (EMIT) ['] EMIT8 (IS)
	['] (KEY?) ['] KEY?  (IS)
	['] (KEY) ['] KEY 	 (IS)
	['] (TYPE) ['] TYPE  (IS)
    ['] (lfind) ['] (find) (IS) 
    ['] NOOP ['] (creat) (IS)
    
\ 	flush user dictionary and user variables
	dp0 dp !		\ bye bye!
	
[  has? user-vars [if]  ]	
	udp0 udp !
[  [endif]  ]
	
\	set up wordlist 	!!!
	fence >latest !

	decimal

\	Set up initial variable vals

	(rp0) rp0 ! 	
	(sp0) sp0 !
	(pad0) (pad) ! 	
	(#-pad0) (#pad) !
	(slit-pad0) (spad) !

    sp0 @ sp!
    0 blk !
    
    empty-buffers
    init-editor

    (splash)
   
[ [[[ test-level @ [IF] ]]] ]
	." Running tests..." cr
	runtests
[ [[[ [THEN] ]]] ]

	quit
;

