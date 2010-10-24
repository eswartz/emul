
\   ---------   memory map

$0000       constant    I/O

$0000       constant    VDPRD
$0002       constant    VDPST
$0008       constant    VDPWD
$000a       constant    VDPWA
$000c       constant    VDPCL
$000e       constant    VDPWI

$0010       constant    GPLRD
$0012       constant    GPLRA
$0014       constant    GPLWD
$0016       constant    GPLWA
$0018       constant    SPCHRD
$001a       constant    SPCHWT

$0020       constant    KEYRD
$0022       constant    KEYWR

$0040       constant    SOUND  \ ... 0x20!

$0080       constant    ROM



$ffc0       constant    SysCalls    \ ... 0x20

$ffe0       constant    IntVecs     \ ... 0x20


\ -----------   GROM addresses

$0          constant    grom_kbdlist
$130        constant    grom_font8x8
$930        constant    grom_font5x6

\ -----------   constants

7           constant    CTX_INT        

