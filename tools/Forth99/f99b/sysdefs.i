
\   ---------   memory map

$0000       constant    I/O

$0000       constant    VDPRD
$0002       constant    VDPST
$0008       constant    VDPWD
$000a       constant    VDPWA       \ high then low
$000c       constant    VDPCL
$000e       constant    VDPWI

$0010       constant    GPLRD
$0012       constant    GPLRA       \ high then low
$0014       constant    GPLWD
$0016       constant    GPLWA
$0018       constant    SPCHRD
$001a       constant    SPCHWT

$0020       constant    KBD

$0040       constant    SOUND  \ ... 0x20!

\   ---------  peripherals

$0080       constant    'INTS
$0081       constant    'INTSP
    
    $1      constant    M_INT_BKPT
    $2      constant    M_INT_EXT
    $4      constant    M_INT_VDP
    $8      constant    M_INT_KBD

$0082       constant    'KBD
$0083       constant    'KBDA

$00ff       constant    'DBG

\   -----------------------    

$0400       constant    ROM

ROM         constant    (BOOT)
ROM 2 +     constant    (COLD)
ROM 4 +     constant    DP0
ROM 6 +     constant    (LATEST)

$80         constant    (#RP)
$80         constant    (#SP)

$100        constant   (#pad)
$100        constant   (##-pad)
$100        constant   (#slit-pad)
$100        constant   (#tib)
$C0C        constant   (#blk)      \ block space
$80         constant   (#up)      \ user space

\ $fec0       constant    UP0         \ grows up
\ $ff40       constant    SP0         \ grows down
\ $ffc0       constant    RP0
\ UP0         constant    RamTop      \ grows down

$ffc0  (#RP) - (#SP) - (#pad) - (##-pad) - (#slit-pad) - (#tib) - (#blk) - (#up) -
             constant RamTop    \ grows down

| RamTop (#rp) +                constant (rp0)      \ grows down
| (rp0) (#sp) +                 constant (sp0)      \ grows down
| (sp0)                         constant (pad0)     \ grows up
| (pad0) (#pad) (##-pad) + +    constant (#-pad0)       \ grows down
| (#-pad0)                      constant (slit-pad0)    \ grows up
| (slit-pad0) (#slit-pad) +     constant (tib0)         \ grows up
| (tib0) (#tib) +               constant (first)    \ grows up
| (first) (#blk) +              constant (limit)
| (limit)                       constant (up0)






$ffc0       constant    SysCalls    \ ... 0x20

$ffe0       constant    IntVecs     \ ... 0x20

    15      constant    INT_RESET
    14      constant    INT_NMI
    3       constant    INT_KBD
    2       constant    INT_VDP
    0       constant    INT_BKPT

\ -----------   GROM addresses

$0          constant    grom_kbdlist
$130        constant    grom_font8x8
$930        constant    grom_font5x6

\ -----------   constants

0           constant    CTX_SP       
2           constant    CTX_RP       
4           constant    CTX_UP       
7           constant    CTX_INT        

