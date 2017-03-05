;   video_modes.inc
; 
;   (c) 1996-2016 Edward Swartz
; 
;   All rights reserved. This program and the accompanying materials
;   are made available under the terms of the Eclipse Public License v1.0
;   which accompanies this distribution, and is available at
;   http://www.eclipse.org/legal/epl-v10.html
; 

;       0=text mode
;           >0000 = screen
;           >0800 = patts
;           >1000+= free
;
vtxt db 0,>0,1,>B0,2,>0,4,>1,-1
    dw  >0,960,>0,>0,>800,>800,0,0,0,0,>1000
    dw 256,192
    dw vtextterms, >2818, hunder

;       1=graphics mode
;           >0000 = screen
;           >0300 = sprites
;           >0380 = colors
;           >03A0 = sprite motion
;           >0420 = sprite patterns (really 0->800; only 132-255 usable)
;           >0800 = char patts
;           >1000+= free
vgfx 	db	0,>0,1,>A0,2,>0,3,>E,4,>1,5,>6,6,>0,-1
	dw	>0,768,>380,>20,>800,>800,>000,>300,>3A0,0,>1000
	dw 256,192
	dw vtextterms, >2018, hunder

;       2=bitmap mode (graphics 2 mode)
;           >0000 = patts
;           >1800 = screen
;           >1B00 = sprites
;           >1B80 = sprite motion
;           >1C00 = sprite patts    (really >1800)
;           >2000 = colors
;           >3800+= free
;
vbit db	0,>2,1,>A0,2,>6,3,>ff,4,>03,5,>36,6,>3,-1
	dw	>1800,>300,>2000,>1800,>0000,>1800,>1800,>1b00,>1B80,0,>3800
    dw 256,192
    dw vbitterms, >2018,hff

;       3=graphics 3 mode (256x192x8+8), sprites 2
;           >0000 = patts
;           >1800 = sprite patts
;           >1900 = sprite colors
;           >1B00 = sprites
;           >1B80 = sprite motion
;           >1C00 = sprite patts ( really >1800)
;           >2000 = colors
;           >3800 = screen
;           >3B00+= free
;
vbit3  db  0,>4,1,>A0,2,>E,3,>ff,4,>03,5,>36,>B,0,6,>3,-1
    dw  >3800,768,>2000,>1800,>0,>1800,>1800,>1b00,>1b80,>1900,>3B00
    dw 256,192
    dw vbitterms, >2018, hff
    
    
;       4=graphics 4 mode (256x212x16)
;           >0000 = patts
;           >7000 = sprite patts
;           >7800 = sprite colors
;           >7A00 = sprites
;           >7A80 = sprite motion
;           >8000+= free
;
vbit4  db  0,>6,1,>A0,2,>0,5,>F4,>B,0,6,>E,-1
    dw >0000,0,>0000,>0000,>0,>6400,>7000,>7A00,>7A80,>7800,>8000
    dw 256,212
    dw >80, 1, >FFFE
    dw vbit4terms, >201B, hff

;       5=graphics 5 mode (512x212x4)
;           >0000 = patts
;           >7000 = sprite patts
;           >7800 = sprite colors
;           >7A00 = sprites
;           >7A80 = sprite motion
;           >8000+= free
;
vbit5  db  0,>8,1,>A0,2,>0,5,>F4,>B,0,6,>E,7,>FF,-1
    dw  >0000,0,>0000,>0000,>0,>6A00,>7000,>7A00,>7A80,>7800,>8000
    dw 512,212
    dw >80, 2, >FFFC
    dw vbit5terms, >401B, hff

;       6=graphics 6 mode (512x212x16)
;           >0000 = patts
;           >D400 = sprite colors
;           >D600 = sprites (1B0)
;           >D680 = sprite motion
;           >D800 = sprite patts (1B)
;           >E000+= free
;
vbit6  db  0,>A,1,>A0,2,>0,5,>AC,>B,>1,6,>1B,-1
    dw  >0000,0,>0000,>0000,>0,>D400,>D800,>D600,>D680,>D400,>E000
    dw 512,212
    dw >100, 1, >FFFE
    dw vbit4terms, >401B, hff

;       7=graphics 7 mode (256x212x256)
;           >0000 = patts
;           >D400 = sprite colors
;           >D600 = sprites (1AC)
;           >D680 = sprite motion
;           >D800 = sprite patts (1B)
;           >E000+= free
;
vbit7  db  0,>E,1,>A0,2,>0,5,>AC,>B,>1,6,>1B,-1
    dw  >0000,0,>0000,>0000,>0,>D400,>D800,>D600,>D680,>D400,>E000
    dw 256,212
    dw >100, 0, >FFFF
    dw vbit7terms, >201B, hff

;       8=text 2 mode
;           >0000 = screen (to >870 for 212-line mode)
;           >0A00 = colors (blinks)
;           >1000 = patts
;           >1800+= free
;
vtxt2  db  0,>4,1,>B0,2,>0,3,>2f,>A,0,4,>2,>D,>22,-1
    dw  >0,2160,>A00,2160/8,>1000,>800,0,0,0,0,>1800
    dw  512, 212
    dw vtextterms, >501B, hunder
    
;      9=monochrome bitmap mode (graphics 2 mode)
;           >0000 = patts
;           >1800 = screen
;           >1B00 = sprites
;           >1B80 = sprite motion
;           >1C00 = sprite patts    (really >1800)
;           >2000 = colors
;           >2040+= free (seems like 2800)
;
vmonobit db 0,>2,1,>B0,2,>6,3,>80,4,>03,5,>36,6,>3,-1
    dw  >1800,>300,>2000,>800,>0000,>1800,>1800,>1B00,>1B80,0,>2800
    dw 256,192
    dw vbitterms, >2018, hff

;       10=multicolor mode
;           >0000 = screen
;           >0300 = sprites
;           >0380 = sprite motion
;           >0800 = char patts (colors)
;           >1000 = sprites patts
;           >1800+= free
vmulti    db  0,>0,1,>a8,2,>0,3,>0,4,>1,5,>6,6,>0,-1
    dw  >0,768,0,0,>800,>800,>1000,>300,>380,0,>1800
    dw 256,192
    dw vmultiterms, >2018, hunder
    

hff db >ff
    even
vmodesetups	
    dw vtextsetup       ; 0
    dw vgraphsetup      ; 1
    dw vbitmapsetup     ; 2
    dw vbitmap3setup    ; 3
    dw vbitmap4setup    ; 4
    dw vbitmap5setup    ; 5
    dw vbitmap6setup    ; 6
    dw vbitmap7setup    ; 7
    dw vtext2setup      ; 8
    dw vmonosetup       ; 9
    dw vmultisetup      ; 10
