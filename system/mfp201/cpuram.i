;
;	cpuram.i						-- ROM workspace RAM usage
;
;	(c) 2010 Edward Swartz
;
;   This program is free software; you can redistribute it and/or modify
;   it under the terms of the GNU General Public License as published by
;   the Free Software Foundation; either version 2 of the License, or
;   (at your option) any later version.
; 
;   This program is distributed in the hope that it will be useful, but
;   WITHOUT ANY WARRANTY; without even the implied warranty of
;   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
;   General Public License for more details.
; 
;   You should have received a copy of the GNU General Public License
;   along with this program; if not, write to the Free Software
;   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
;   02111-1307, USA.  
;
;	$Id: cpuram.i,v 1.10 2009-03-01 17:06:54 ejs Exp $

;==========================================================================

;   ================================================
;   Special "system variables" that should be visible regardless of bank switch can modify

sharedvarbase equ >EF60 ; where vars shared with FORTH are seen
privvarbase equ >E700   ; where ROM private vars are stored

    aorg    sharedvarbase
 
; this block of variables is visible via equates to FORTH -- 99memory.fs should reflect

vintflags   bss 1       ; VDP interrupt flags
nvblnk      equ >80     ;       1: suppress blink cursor and blank screen
nvkeyscan   equ >40     ;       1: suppress scan keyboard
nvsprmot    equ >20     ;       1: suppress sprite motion
nsoundlist  equ >10     ;       1: suppress sound list

vstatus     bss 1       ; VDP status last read during interrupt

userint     bss 2       ; user VDP interrupt handler (called when VDP interrupts)
timerisr    bss 2       ; user timer handler (called when clock ticks)

nsprmot     bss 1       ; number of sprites in motion (00 = none)

sndflags    bss 1       ; sound list flags
                        ;       $00: CPU RAM, $80: VDP 
                        ;       $0f: tempo adjust (signed: -4 to 3) 
sndlist     bss 2       ; address of classic sound list (0 = none), incremented on tick
snddur      bss 1       ; duration of current group

        even
        
; this block of variables is included in a video mode save/restore 
_CPURAMSTART equ $

vpob        bss 1       ; VDP "page offset" bank (added to V9938 bank to select the page outside 64k)
vblinkflag  bss 1       ; flag to set blink bit in text2 
vpgrow      bss 2       ; VDP "page row offset" (added to V9938 commands to select the page)
vtextpage   bss 2       ; VDP text-ish page offset (screen, patterns, colors) (in addition to vpob)

vidvarstart bss 2       ; start addr of important video variables
vidvarsize  bss 2       ; size of important video variables

_CPURAMSIZE  equ $ - _CPURAMSTART


