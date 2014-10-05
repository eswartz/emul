;
;	ram.inc						-- FORTH ROM workspace RAM usage
;
;	(c) 1996-2001 Edward Swartz
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
;	$Id: ram.i,v 1.29 2009-03-01 17:06:55 ejs Exp $

;==========================================================================

;   ================================================
;   General variables for use by ROM

    aorg    _RAM      
     
vstacksize equ  >40
vstack  bss vstacksize      ; video stack is stored here for speed in slow-mode

sysstack bss    >40         ; system stack
sysstacksize equ $ - sysstack

; _RAMSTART equ $ 

vbitbuf bss 256         ; buffer for bitmap font manip

;-----------------------------------

kbdlast bss 1           ; last char pressed (or 0)
kbdtimer bss    1           ; timer (1/60 s) since last repeat

kbdscan bss 1           ; most recent scancode     (0-47)
kbdshft bss 1           ; most recent shift status (>70)

kbdhead bss 1           ; head of kbd buffer
kbdtail bss 1           ; tail of kbd buffer  
                    ; head==tail => empty

kbdbufsize equ  32
kbdbuf  bss kbdbufsize          

kbdlimit bss    1           ; 1/60s before repeating
kbddelay bss    1           ; delay between keyscans

kbdflag bss 1           ; keyboard state
                        ; | >80 = currently repeating bit
                        
    even
;--------------------------------------
randnoise bss 2			; random noise
randseed1 bss 2			; random seed (lfsr)
randseed2 bss 2			; random seed

;--------------------------------------

dskstacksize 	equ	>20
dskstack bss	dskstacksize

forthdsk bss	10			; filename for FORTH disk

;--------------------------------------

; struct PlayingVoice

pv_clock    equ 0           ; the clock for the voice (done when > 64k)
pv_incr     equ 2           ; the increment per clock (0 = owned by other voice)
pv_hertz    equ 4           ; the frequency of the voice
pv_track    equ 6           ; the global track owning this voice (addr)
pv_port     equ 8           ; the sound port
pv_freqmask equ 10          ; sound command frequency mask for the voice
pv_volmask  equ 11          ; sound command volume mask for the voice
pv_size     equ 12

; endstruct

NUMVOICES   equ 16
voices      bss pv_size * NUMVOICES   ; 4 sets of 3 tones + 1 noise
VOICES_END equ $

; struct LiveTrack

lt_cmdptr   equ 0           ; pointer to next command (start of lump)
lt_clock    equ 2           ; accumulator timing til next lump (when > 64k)
lt_incr     equ 4           ; clock increment for lump length
lt_tempoincr equ 6          ; increment for current tempo
lt_a_d      equ 8           ; attack, decay (byte)
lt_h_r      equ 9           ; hold, release (byte)
lt_volume   equ 10          ; current volume (byte)
lt_sustain  equ 11          ; sustain ratio (when adhr != 0)
lt_vibrato  equ 12          ; vibrato 
lt_tremolo  equ 13          ; tremolo 
lt_waveform equ 14          ; waveform 
lt_balance  equ 15          ; balance
lt_size     equ 16

; endstruct

NUMTRACKS   equ 8
tracks      bss lt_size * NUMTRACKS
TRACKS_END  equ $

; struct LiveSong

MAXTRACKSPERSONG equ 4 

ls_phrase   equ 0          ; pointer to current phrase
ls_phrases  equ 2           ; ptr in zero-terminated array of phrases 
ls_tracks   equ 4           ; map of logical tracks to global tracks
ls_size     equ 4 + MAXTRACKSPERSONG

; endstruct

NUMSONGS    equ 4
songs       bss ls_size * NUMSONGS
SONGS_END   equ $

_RAMEND 	equ  $

