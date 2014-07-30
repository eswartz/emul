;   sound.i                     -- sound manager routines
;
;   (c) 2009 Edward Swartz
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
;   $Id: sound.i,v 1.11 2009-07-26 01:09:41 ejs Exp $

snd_voice_ports
    dw      SOUND+>2, SOUND+>8, SOUND+>E, SOUND+>14
    

sndinit
    PUSH    SP, 11
    li     0, >9FBF
    li     2, 10
    li     1, SOUND
    
    ; first iter: clear out console chip;
    ; next four iters: clear out extra chips
    ;
    ; on each iter, send the volume off and effect off commands for each voice 
$0:    
    movb   0, *1
    movb   0, @2(1)
    swpb   0
    movb   0, *1
    movb   0, @2(1)
    li     0, >DFFF
    movb   0, *1
    movb   0, @2(1)
    swpb   0
    movb   0, *1
    movb   0, @2(1)
    dect   2
    jnc	   $1+
	mov    @snd_voice_ports(2), 1
	jmp    $0    
$1:    
    POP     SP, 11
    
    rt

; ========================================================  classic sound list API


sndfetch1
    movb    *0+, 3
    rt
sndfetchv
    inc     0
    movb    *4, 3
    rt
    
 Vector soundlist, vidws
 
    ; check active duration
    movb    @snddur, 0
    jeq     $0+
   
    sb      #1, @snddur
    jne     $3+
     
$0:    
 
    mov     @sndlist, 0
    jeq     $3+
    
    li      2, sndfetch1
    
    movb    @sndflags, 1
    sla     1, 1
    jnc     $1+
    
    bl      @vraddr
    li      4, VDPRD
    li      2, sndfetchv
     
$1: 
    bl      *2
    jeq     $5+         ; end of song?
    
    ; duration first
    movb    3, @snddur

$4:
    ; then a set of volumes or tones (noise must have a dummy $ff byte)
    bl      *2
    jeq     $2+         ; end of group?
        
    movb    3, @SOUND
    coc     #>1000, 3    ; volume?
    jeq     $4-         ; yup 
    
    bl      *2          ; else it was the first byte of a two-byte tone; fetch next
    movb    3, @SOUND   
    jmp     $4-
    
$2:
    mov     0, @sndlist
    rtwp
    
$5:    
    clr     @sndlist
$3:
    rtwp


        