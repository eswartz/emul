;   ram.inc
; 
;   (c) 1996-2014 Ed Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

;==========================================================================

;   ================================================
;   General variables for use by ROM

    aorg    _RAM      
     
vstacksize equ  >40
vstack  bss vstacksize      ; video stack is stored here for speed in slow-mode

sysstack bss    >40         ; system stack
sysstacksize equ $ - sysstack

; _RAMSTART equ $ 

_VIDVARSTART equ $

vscreen	bss	2			; VDP addr of screen
vscreensz	bss	2			; VDP size of screen table
vpatts	bss	2			; VDP addr of patterns
vpattsz	bss	2			; VDP size of pattern table
vcolors bss	2			; VDP addr of colors
vcolorsz bss	2			; VDP size of color table
vsprites bss	2			; VDP addr of sprites
vsprcol bss 2           ; VDP addr of sprite color table (0 if not sprite 2 mode)
vsprpat bss	2			; VDP addr of sprite patterns
vsprmot	bss	2			; VDP addr of sprite motion
vfree	bss	2			; usable space

vdrawchar bss	2			; draw char in window (BLWP @)
vscroll	bss	2			; scroll window up a line (BLWP @)
vclearline	bss	2			; clear line (BL @)  [R0=window coord, R2=length; preserve 0, 3]

vbsize	bss	2			; bitmapped font size (x/y)

vcoordaddr bss	2			; get SIT addr of R0 coord   [R0=window coord => R0=addr, R1=shift; 
                            ;                               preserve R2]

vcrstimer bss	1			; timer for blink
vcrsblink bss	1			; limit in 1/60 s
vcursor	bss	2			; cursor blinker (BLWP @)
vcurs	bss	1			; cursor blink state (0 or >80)

vcursunder bss	8	   	; char or bits under cursor

vheight bss 1           ; phys screen height in chars
vwidth  bss 2           ; phys screen width in chars
    even
    
vbit4stride bss 2        ; row stride  
vbit4shift bss 2        ; shift for column # to byte
vbit4mask bss 2        ;  mask for column # to byte portion

vfont	bss	2			; GROM font addr

;---------------------------------------

vfgbg	bss	2			; foreground|background color
vch	bss	1			; current char

vcurschar bss	1			; char of cursor

vwx		bss	1			; window left
vwy		bss	1			; window right
vwxs	bss	1			; width of window
vwys	bss	1			; height of window
vwcy	bss	1			; last cleared row

vmode	bss	1         ; last set video mode (not M_xxx)

    even
    
vtermptr bss 2          ; pointer to standard term stuff for mode
    
vx		bss	1			; x-coord of cursor in window
vy		bss	1			; y-coord of cursor in window

vmono	bss	1
vidmode	bss	1			; what mode are we in?  (M_xxxx)

M_text	equ	0
M_graph	equ	1
M_multi equ 2
M_bit	equ	3			; both mono and color
M_bit4	equ	4			; new bitmap modes
M_text2 equ 5           ; 80-column

vscrnx  bss 2           ; res x
vscrny  bss 2           ; res y

vlinex	bss	2
vliney	bss	2

savedvregs bss 16       ; first 16 VRs set via vwreg
vregr1  equ savedvregs + 1           ; VDP register 1
vregr7  equ savedvregs + 7           ; VDP register 7

_VIDVARSIZE  equ $ - _VIDVARSTART

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

