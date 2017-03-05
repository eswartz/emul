;   cpuram.i
; 
;   (c) 2010-2013 Edward Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

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


