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
    clr     @sndlist
    bl      @snd_seq_init
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


; ======================================================== structured music API

;   Initialize (live) tracks
;
snd_tracks_init
    PUSH    SP, 11

    li      r0, 30 * 256
    bl      @snd_track_tempo_to_incr
    
    li      r4, tracks
    clr     r2
$0:
    bl      @snd_track_init
    ci      r4, TRACKS_END
    jl      $0
    
    POP     SP, 11
    rt

;   Initialize a track
;
;   In:
;           R4 = track
;           R2 = track ptr
;           R0 = tempo
;   Out:
;           R4 = next track
snd_track_init
    mov     r2, *r4+    ; lt_cmdptr
    clr     *r4+        ; lt_clock
    seto    *r4+        ; lt_incr
    mov     r0,*r4+     ; lt_tempoincr
    clr     *r4+        ; lt_a_d, lt_s_r
    mov     #>0f00,*r4+     ; lt_volume, lt_sustain
    clr     *r4+        ; lt_vibrato, lt_tremolo
    clr     *r4+        ; lt_waveform, lt_balance
    rt
        
;   Reset a track's effects
;
;   In:
;       R4 = LiveTrack
snd_track_reset
    clr     @lt_a_d(r4)  ; + s_r
    clr     r0
    movb    r0, @lt_sustain(r4)
    clr     @lt_vibrato(r4)  ; + lt_tremolo
    mov     r0, @lt_waveform(r4) ; + lt_balance
    rt

;   Is a track done?
;
;   In:
;       R4 = LiveTrack ptr
;   Out:
;       EQ = track is done
;       R0 = xxx
snd_track_isDone
    mov     @lt_cmdptr(r4), r0
    rt

;   For a given whole notes per minute, figure the clock amount to add
;   per quarter note (16 units) per 1/60 tick.
;
;   64K * WPM * 4 / 3600
;
;   In:
;       hi(R0) = tempo in WPM, assuming quarter note is a beat (e.g. 30 = 120 BPM)
;   Out:
;       R0 = clock increment for quarter note
;       R1 = xxx
snd_track_tempo_to_incr
    srl     r0, 8
    sla     r0, 2
    clr     r1
    div     #3600, r0
    jno     $0+
    seto    r0 
$0:    
    rt    
    
;   Given a number of 1/64 beats for a track lump, figure the increment
;   per tick, which ends once it overflows >= 65536.
;
;   In:
;       R4 = track ptr
;       hi(R0) = length in 1/64 beats
;   Out:
;       R0 = xxx
;       R1 = increment
;       R2 = xxx
snd_track_length_to_incr
    mov     @lt_tempoincr(r4), r2
    mov     r2, r1
    srl     r1, 12
    sla     r2, 4    
    srl     r0, 8
    div     r0, r1
    rt

;   Get the hertz for a given note value in the form [octave | note],
;   scaled by 16
;
;   In:
;       R4 = track ptr
;       hi(R0) = note
;   Out:
;       R0 = xxx
;       R1 = hertz * 16
snd_track_note_to_hertz_16
    movb    r0, r1
    andi    r1, >0f00
    srl     r1, 7           ; note offset
    mov     @snd_scale_12_tone_octave_11(r1), r1
    
    srl     r0, 12          ; octave
    neg     r0
    ai      r0, 11 - 4      ; shift
    jeq     $0+
    srl     r1, r0    
$0:    
    rt
    
;   The hertz values for C through B on a 12-tone scale in octave 11.
;   We shift these down by 2^N to obtain values for octaves 1 through 10.
; 
snd_scale_12_tone_octave_11:
    dw  33488, 35479, 37589, 39824
    dw  42192, 44701, 47359, 50175 
    dw  53159, 56320, 59669, 63217 
    dw  0, 0, 0, 0

;   Execute a single tick for playing tracks.
;
;   Out:
;       R4 = xxx
snd_tracks_tick
       PUSH SP, 11
       
       li   r4, tracks
$0:
       mov  @lt_cmdptr(r4), r0
       jeq  $1+
       
       bl   @snd_track_tick
$1:
       ai   r4, lt_size       
       ci   r4, TRACKS_END
       jl   $0-
       
       POP  SP, 11
       rt
    
;   Execute a single tick of a track.
;
;   In:
;       R4 = track ptr
;   Out:
;       R0-R3 = xxx
;
snd_track_tick
    a       @lt_incr(r4), @lt_clock(r4)
    joc     $3+
    ;   not end of lump, continue
    rt
        
$3:
    PUSH    SP, 5, 11
    jmp     $1+
    
$2:
    ; end of track
    clr     @lt_cmdptr(r4)
    
    ; one last tick
    bl      @snd_voices_tick
    jmp     $0+
        
$1:
    mov     @lt_cmdptr(r4), r5
    jeq     $0+
    
    movb    *r5+, r0
    jne     $9+            ; assertion error, should be at lump
    
    movb    *r5+, r0        ; get lump length
    jeq     $2-             ; end of track?
    
    bl      @snd_track_length_to_incr
    
    ; length includes this tick
    mov     r1, @lt_incr(r4)
    mov     r1, @lt_clock(r4)
    
$2:
    movb    *r5+, r0        ; next command
    movb    r0, r1          ; keep R0 in case low nybble is used
    srl     r1, 11          ; hi nybble -> word offset
    mov     @snd_track_commands(r1), r1
    jeq     $2-
    bl      *r1
    jmp     $2-

$0: 
    POP     SP, 5, 11   
    rt
    
snd_track_commands
    dw      snd_track_cmd_lump     ; 0
    dw      snd_track_cmd_note     ; 1
    dw      snd_track_cmd_volume   ; 2
    dw      snd_track_cmd_adhsr    ; 3
    dw      snd_track_cmd_tempo    ; 4
    dw      snd_track_cmd_vibrato  ; 5
    dw      snd_track_cmd_tremolo  ; 6
    dw      snd_track_cmd_waveform  ; 7
    ;dw      snd_track_cmd_sweep     ; 8
    dw      0
    dw      snd_track_cmd_balance   ; 9 
    dw      0, 0, 0, 0, 0        ; A,B,C, D
    dw      snd_track_cmd_jump      ; E
    dw      snd_track_cmd_stop      ; F

snd_track_cmd_lump:
    ; done (reached the next lump)
    dec     r5
$9:    
    mov     r5, @lt_cmdptr(r4)
    
    ;   update the voices for this tick
   ; bl      @snd_voices_tick
    jmp     $0-
    
snd_track_cmd_note:
    PUSH    SP, 11
    
    andi    r0, >0F00
    movb    r0, r7              ; save flags
    
    movb    *r5+, r0
    
    bl      @snd_track_note_to_hertz_16
    mov     r1, r6              ; save hertz
    
    movb    *r5+, r0
    bl      @snd_track_length_to_incr
    mov     r6, r0
    
    PUSH    SP, 5
        
    movb    r7, r7
    jne     $3+                ; force noise? 
    
    ci      r0, 54 * 16        ; too low?  (Note: enhanced chip!)
    jhe     $0+

    li      r7, >0400           ; variable periodic noise (+1)
$3:
snd_track_note_noise:    
    ai      r7, ->0100
    
    coc     #>0300, r7
    jne     $1+
    
    ; variable pitch: need two voices
    bl      @snd_seq_alloc_noise_voices
    jmp     $2+
    
$1:
    ; simple single noise channel    
    movb    r7, r0
    li      r2, >8888          ; only a noise
    bl      @snd_seq_alloc_note  

    jmp     $2+

$0:    
    li      r2, >7777          ; any melodic voice  
    srl     r0, 4               ; scale to normal hertz
    bl      @snd_seq_alloc_note

$2:    
    POP     SP, 5
    
    POP     SP, 11
    rt

snd_track_cmd_volume:
    andi    r0, >f00
    movb    r0, @lt_volume(r4)
    rt
    
snd_track_cmd_jump:
    movb    *r5+, r0
    sra     r0, 8
    a       r0, r5
    rt
    
snd_track_cmd_tempo:
    PUSH    SP, 11
    movb    *r5+, r0
    bl      @snd_track_tempo_to_incr
    mov     r0, @lt_tempoincr(r4) 
    POP     SP, 11
    rt

snd_track_cmd_adhsr:
    PUSH    SP, 11
    
    andi    r0, >F00
    movb    r0, @lt_sustain(r4)
    
    movb    *r5+, @lt_a_d(r4)
    movb    *r5+, @lt_h_r(r4)
    
    POP     SP,11
    rt
    
snd_track_cmd_vibrato:
    movb    *r5+, @lt_vibrato(r4)
    rt
        
snd_track_cmd_tremolo:
    movb    *r5+, @lt_tremolo(r4)
    rt
        
snd_track_cmd_waveform:
    andi    r0, >0f00
    movb    r0, @lt_waveform(r4)
    rt
    
snd_track_cmd_balance:
    movb    *r5+,@lt_balance(r4)
    rt    
    
snd_track_cmd_stop:
    b       @snd_track_reset

snd_voice_ports
    dw      SOUND+>2, SOUND+>8, SOUND+>E, SOUND+>14
    
;   Initialize the voices
;
;
snd_voices_init
    li      r1, voices
    li      r2, snd_voice_ports
$0:    
    li      r3, >8090
$1:
    clr     *r1+        ; pv_clock
    clr     *r1+        ; pv_incr
    clr     *r1+        ; pv_hertz
    clr     *r1+        ; pv_track
    mov     *r2, *r1+   ; pv_port
    mov     r3, *r1+    ; pv_freqmask, pv_volmask
    ai      r3, >2020   
    jnc     $1          ; >E0F0 -> 0110
    
    inct    r2          ; next port
    ci      r1, VOICES_END
    jl      $0
    
    rt
    
;   Allocate a voice for a track,
;   set the hertz and length of the note,
;   and play the voice.
;
;   In:
;       R5 = voice
;       R4 = track
;       R0 = hertz/noise
;       R1 = ticks increment, or 0 if claiming a voice for noise
;   Out:
;       R0-R3 = xxx
snd_voice_alloc
    PUSH    SP, 11
    
    mov     r4, @pv_track(r5)
    mov     r0, @pv_hertz(r5)
    clr     @pv_clock(r5)
    mov     r1, @pv_incr(r5)

    ; set the effects
    mov     @pv_port(r5), r3
    movb    @pv_volmask(r5), r2
    
    inct    r3                  ; point to command port
    
    movb    r2, *r3    ; reset
    
    movb    @lt_sustain(r4), r1     ; envelope on?
    jeq     $0+
    
    movb    r2, r0
    ori     r0, >0100           ; envelope/sustain command
    movb    r0, *r3

    movb    r1, @2(r3)          ; sustain amount
        
    movb    r2, r0
    ori     r0, >0200           ; envelope attack/decay command
    movb    r0, *r3

    movb    @lt_a_d(r4), @2(r3) ; values

    movb    r2, r0
    ori     r0, >0300           ; envelope hold/release command
    movb    r0, *r3

    movb    @lt_h_r(r4), @2(r3) ; values

$0:    
    movb    @lt_vibrato(r4), r0     ; vibrato on?
    jeq     $0+
   
    movb    r2, r1
    ori     r1, >0400           ; vibrato command
    movb    r1, *r3
    
    movb    r0, @2(r3) ; values
     
$0:    
    movb    @lt_tremolo(r4), r0     ; tremolo on?
    jeq     $0+
   
    movb    r2, r1
    ori     r1, >0500           ; tremolo command
    movb    r1, *r3
    
    movb    r0, @2(r3) ; values
     
$0:    
    movb    @lt_waveform(r4), r0     ; custom waveform
    jeq     $0+
   
    movb    r2, r1
    ori     r1, >0600           ; waveform command
    movb    r1, *r3
    
    movb    r0, @2(r3) ; values

$0:    
    movb    @lt_balance(r4), r0     ; balance
    
    movb    r2, r1
    ori     r1, >0900
    movb    r1, *r3
    
    movb    r0, @2(r3)  ; value
    
    bl      @snd_voice_apply2
    
    POP     SP,11
    rt
     
;   Apply the hertz and volume for a voice. 
;
;   In:
;       R5 = voice
;   Out:
;       R0-R3 = xxx
;       R4 = track
snd_voice_apply
    mov     @pv_track(r5), r4
snd_voice_apply2
    movb    @pv_freqmask(r5), r2
    cb      #>E0, r2
    jne     $0+
   
    ; noise
    socb    @pv_hertz(r5), r2
    mov     @pv_port(r5), r1
    movb    r2, *r1
    jmp     $1+
      
$0:    
    li      r0, >1
    li      r1, >B4F4
    div     @pv_hertz(r5), r0
    mov     @pv_port(r5), r1
    
    ; R0 is, say >3F9.  We write >89 >3F
    mov     r0, r3
    sla     r3, 4       ; get lo byte
    swpb    r0
    andi    r0, >0f00
    socb    r2, r0
    movb    r0, *r1
    movb    r3, *r1
$1:    
    movb    @lt_volume(r4), r0
    
    mov     @pv_incr(r5), r2      ; tone voices for noise are silent
    jne     $1+
    
    ; write pan for noise too
    movb    @lt_balance(r4), r0
    movb    #>f9, @2(r1)
    movb    r0, @4(r1)
    
    li      r0, >F00
    
$1:  
    socb    @pv_volmask(r5), r0
    movb    r0, *r1
    rt

;   Tick one step of a voice.
;   In:
;       R4 = track ptr
;       R5 = voice ptr
;   Out:
;       R2 = xxx
snd_voice_tick:
    mov     @pv_incr(r5), r2      ; do nothing if inactive (or claimed by noise)
    jeq     $0+
    
    a       r2, @pv_clock(r5)
    jnc     $1+
    
    ; end of note
    ; write "volume off" or key release to command port
    li      r0, >0F00
    socb    @pv_volmask(r5), r0
    mov     @pv_port(r5), r1
    movb    @lt_sustain(r4), r2
    jeq     $2+
    
    inct    r1          ; send key off command rather than volume off
    
$2: 
    movb    r0, *r1

    clr     @pv_track(r5)
    
    ; was it a noise?
    cb      #>E0, @pv_freqmask(r5)
    jne     $0+
    
    ; if so, the previous was ours too
    clr     @pv_track - pv_size(r5)

$0:
$1:
    rt

;   Tick one step of the active voices.
;
;   Out:
;       R0-R3 = xxx
snd_voices_tick:
    PUSH    SP, 4, 5, 11
    li      r5, voices
$0:
    mov     @pv_track(r5), r4
    jeq     $1+
    bl      @snd_voice_tick
$1:    
    ai      r5, pv_size
    ci      r5, VOICES_END
    jl      $0
    POP     SP, 4, 5, 11
    rt

;   Allocate a note by claiming a voice.
;
;   In:
;       R4 = track ptr
;       R0 = hertz or noise code
;       R1 = time increment  
;       R2 = mask of allowed voices
;       R3 = # ticks in sustain
;   Out:
;       R2 = xxx
;       R3 = xxx
;       R5 = allocated voice
snd_seq_alloc_note
    PUSH    SP, 11
    li      r5, voices
$0:
    srl     r2, 1           ; allowed voice?
    jnc     $2+
    
    mov     @pv_track(r5), r11   ; already in use?
    jne     $2+

    bl      @snd_voice_alloc
    jmp     $1+
    
$2:
    ai      r5, pv_size
    ci      r5, VOICES_END
    jl      $0-
    
    ; no voice allocated
    
$1:
    POP     SP, 11
    rt

;   Allocate voice 2 and voice 3 from a generator to
;   support a noise with variable frequency. 
;
;   In:
;       R0 = hertz * 16
;       R1 = length of note
;       R4 = track pointer
;       hi(R7) = noise (03 or 07)
;   Out:
;       R5 = noise voice allocated
;
snd_seq_alloc_noise_voices
    PUSH    SP, 11
    
    mov     r0, r2
    srl     r2, 4              ; get pitch*15/16
    s       r2, r0
    li      r2, >4444          ; only a voice 2
    mov     r1, r6             ; save length
    clr     r1                 ; not a real note
    bl      @snd_seq_alloc_note  

    ; take the next voice for noise
    ai      r5, pv_size
    
    movb    r7, r0
    mov     r6, r1             ; restore length
    li      r2, >8888          ; only a noise 
    
    bl      @snd_voice_alloc
    POP     SP, 11
    rt
    
;   Tick a song.  
;
;   In:
;       R6 = song ptr
;   Out:
;       R6 = song ptr
snd_song_tick:
    mov     @ls_phrase(6), 5
    jne     $0+
    
    ; moving to next phrase
    mov     @ls_phrases(6), 7
    mov     *7+, 5
    mov     5, @ls_phrase(6)
    mov     7, @ls_phrases(6)

    ;...

$0:
     rt   
    
;   Tick the sequencer.  Advances playing notes and goes to new lump,
;   phrase, etc. automatically.
;    
snd_seq_tick    
    ;dbg
    PUSH    SP, 11
    
    bl      @snd_voices_tick
    
    ; for now
    bl      @snd_tracks_tick

    POP     SP, 11
    ;dbgf     
    
    rt

;;;;    
    ; step through songs and tick them
    li      6, songs
$0:
    mov     *6, 1
    jeq     $1+
    
    bl      @snd_song_tick
$1:  
    inct    6
    ci      6, SONGS_END
    jne     $0-
    
    POP     SP, 11
    ;dbgf     
    
    rt

snd_seq_init
    ;dbg
    PUSH    SP, 11
    bl      @snd_tracks_init
    bl      @snd_voices_init
        
    li      r0, test_track
    ;mov     r0, @tracks + lt_cmdptr         ;;; auto start
    
    POP     SP, 11
    ;dbgf
    rt

 Vector sound_sequencer, vidws
    li      SP, vstack + vstacksize
    bl      @snd_seq_tick
    rtwp    

;   External entry
;
;   In:
;       R2 = track ptr
xsnd_queue_track:
    PUSH    SP, 4, 11
    li      4, tracks
$0:
    mov     @lt_cmdptr(4), @lt_cmdptr(4)
    jeq     $1+

    ai      4, lt_size
    ci      4, TRACKS_END
    jl      $0
$2:
    POP     SP, 4, 11
    rt
$1:
    ; assign
    li      r0, 30 * 256
    bl      @snd_track_tempo_to_incr
    
    bl      @snd_track_init    
    jmp     $2-
   
    
test_track0:
    db      >00, 16,  >28
    db      >00, 16,  >10, >40, 10
    db      >00, 16,  >10, >44, 10
    db      >00, 16,  >10, >47, 10
    db      >00, 64,  >10, >40, 60,  >10, >44, 60,  >10, >47, 60,  >10, >50, 60
    db      >00, 16
    db      >00, 1,   >40, 40
    db      >00, 15,  >10, >00, 15,     >10, >10, 15
    db      >00, 15,  >10, >03, 15,     >10, >13, 15
    db      >00, 15,  >10, >05, 15,     >10, >15, 15
    db      >00, 15,  >10, >07, 15,     >10, >17, 15
    db      >00, 15,  >10, >09, 15,     >10, >19, 15
    db      >00, 15,  >10, >0B, 15,     >10, >1B, 15
    db      >00, 15,  >10, >11, 15,     >10, >21, 15
    db      >00, 15,  >10, >14, 15,     >10, >24, 15
    db      >00, 15,  >10, >17, 15,     >10, >27, 15
    db      >00, 15,  >10, >1A, 15,     >10, >2A, 15
    db      >00, 15,  >10, >22, 15,     >10, >32, 15
    db      >00, 15,  >10, >25, 15,     >10, >35, 15
    db      >00, 15,  >10, >28, 15,     >10, >38, 15
    db      >00, 15,  >10, >30, 15,     >10, >40, 15
    db      >00, 15,  >10, >33, 15,     >10, >43, 15
    db      >00, 0

test_track1:
    db      >00, 16, >2F, >40, 255
tt0:    
    db      >00, 1, >10, >40, 32,  >10, >50, 32,  >10, >60, 32 
    db      >00, 1, >2E
    db      >00, 1, >2D
    db      >00, 1, >2C
    db      >00, 1, >2B
    db      >00, 1, >2A
    db      >00, 1, >29
    db      >00, 1, >28
    db      >00, 1, >27
    db      >00, 1, >26
    db      >00, 1, >25
    db      >00, 1, >24
    db      >00, 1, >23
    db      >00, 1, >22
    db      >00, 1, >21
    db      >00, 1, >20
    db      >00, 1, >21
    db      >00, 1, >22
    db      >00, 1, >23
    db      >00, 1, >24
    db      >00, 1, >25
    db      >00, 1, >26
    db      >00, 1, >27
    db      >00, 1, >28
    db      >00, 1, >29
    db      >00, 1, >2A
    db      >00, 1, >2B
    db      >00, 1, >2C
    db      >00, 1, >2D
    db      >00, 1, >2E
    db      >00, 1, >2F
    db      >00, 1, >E0
    db      tt0 - $ + 1
    
test_track:
    ; track volume = 8, ADSR = ...
    db      >00, 16 ,  >28 ,  >40, 32
    db		  	>38,  >33, >2D ,  >50, >44 , >60, >44 ;
    db			>71, >10, >20, 4
    db      >00, 16 
    db      >00, 16, >10, >30, 8
    db      >00, 16 
    db      >00, 16, >10, >40, 16
    db      >00, 16 
    db      >00, 32, >10, >51, 32
    db      >00, 16
    db      >00, 64, >10, >62, 64
    db      >00, 16,   >90, >80
    db      >00, 25,  >10, >00, 15,     >10, >10, 15,   >90, >98
    db      >00, 25,  >10, >03, 15,     >10, >13, 15
    db      >00, 25,  >10, >05, 15,     >10, >15, 15,   >90, >b0
    db      >00, 25,  >10, >07, 15,     >10, >17, 15,    >40, 40
    db      >00, 25,  >10, >09, 15,     >10, >19, 15,   >90, >c0
    db      >00, 25,  >10, >0B, 15,     >10, >1B, 15
    db      >00, 25,  >10, >11, 15,     >10, >21, 15,     >40, 48,   >90, >f0
    db      >00, 25,  >10, >14, 15,     >10, >24, 15
    db      >00, 25,  >10, >17, 15,     >10, >27, 15,   >90, >00
    db      >00, 25,  >10, >1A, 15,     >10, >2A, 15,    >40, 52    
    db      >00, 25,  >10, >22, 15,     >10, >32, 15,   >90, >20
    db      >00, 25,  >10, >25, 15,     >10, >35, 15
    db      >00, 25,  >10, >28, 15,     >10, >38, 15,    >40, 60,   >90, >30
    db      >00, 25,  >10, >30, 15,     >10, >40, 15,   >90, >40
    db      >00, 25,  >10, >33, 15,     >10, >43, 15
    db      >00, 25,  >10, >36, 15,     >10, >46, 15,    >40, 64,   >90, >50
    db      >00, 25,  >10, >39, 15,     >10, >49, 15
    db      >00, 25,  >10, >3B, 15,     >10, >4B, 15,   >90, >60
    db      >00, 25,  >10, >42, 15,     >10, >52, 15,    >40, 68,   >90, >70
    db      >00, 25,  >10, >45, 15,     >10, >55, 15,   >90, >7f
    db      >00, 25,  >10, >48, 15,     >10, >58, 15
    db      >00, >00
    
test_track4:
    ; track volume = 8, ADSR = ...
    db      >00, 16,  >20,  >40, 32  
    db       >38, >D4, >2C  
    db       >50, >44 , >60, >44 ;
tt1:    
   
    db      >00, 10,  >70,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >33, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20

     db      >00, 10,  >74,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >53, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20
    
    
 
      db      >00, 10,  >71,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >53, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20
    
    
 
   db      >00, 10,  >76,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >33, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20
    
  db      >00, 10,  >72,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >33, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20    

 
    
    db      >00, 10,  >73,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >53, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20    
  
    db      >00, 10,  >75,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >53, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20
    
    db      >00, 10,  >77,  >10, >35, 15  ;,     >10, >45, 15
    db      >00, 10,  >10, >43, 15,     >10, >33, 15
      db      >00, 15,  >10, >48, 15
    db      >00, 15,  >10, >53, 15
    db      >00, 15,  >10, >58, 15
    db      >00, 15,  >10, >63, 15
    db      >00, 15,  >10, >68, 15
    db      >00, 15,  >10, >73, 15
    db      >00, 20
    
    db      >00, 01, >E0
    db      tt1 - $ - 1
    db      >00, >00
    even
        
test_track5:
    db      >00, 16,  >28,  >40, 32
    ;db      >00, 25,  >10, >00, 15
    ;db      >00, 25,  >10, >04, 15
    ;db      >00, 25,  >10, >08, 15
    
    ;db       >60, >44
    db       >38, >23, >3A
    
    db      >00, 8,  >14, >13, 1
    ;db      >00, 8,  >14, >13, 1
    ;db      >00, 8,  >14, >13, 1
    ;db      >00, 8,  >14, >13, 1
    db      >00, 8,  >14, >25, 1
    ;db      >00, 8,  >14, >25, 1
    ;db      >00, 8,  >14, >25, 1
    ;db      >00, 8,  >14, >25, 1
    db      >00, 8,  >14, >35, 1
    ;db      >00, 8,  >14, >35, 1
    ;db      >00, 8,  >14, >35, 1
    ;db      >00, 8,  >14, >35, 1
    db      >00, 25
    db      >00, 8,  >15, >53, 5
    db      >00, 8,  >16, >43, 5
    db      >00, 8,  >17, >33, 5
    
    db      >00, 8,  >18, >53, 1
    db      >00, 8,  >18, >53, 2
    db      >00, 8,  >18, >53, 3
    db      >00, 8,  >18, >53, 4
    db      >00, 8,  >18, >63, 1
    db      >00, 8,  >18, >63, 2
    db      >00, 8,  >18, >63, 3
    db      >00, 8,  >18, >63, 4
    db      >00, 8,  >18, >73, 1
    db      >00, 8,  >18, >73, 2
    db      >00, 8,  >18, >73, 3
    db      >00, 8,  >18, >73, 4
    db      >00, 25
    db      >00, >00
           even
     
     ; good snareish      
    ;db       >34, >14, >88
    ; db      >00, 8,  >18, >83, 2
;  db       >38, >23, >3A  
;db      >00, 8,  >18, >73, 2
        