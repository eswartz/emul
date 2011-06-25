
\
\
\   The actual target contents of the kernel.
\
\

lock

>rom

\ make space for the instructions going to >boot and >cold 

0 , 0 , 0 , 0 ,

include user.fs

order

(up0) tup .s !   \ even if we don't have USER variables, this will be used for other stuff we want in RAM

order

include 99prims.fs

unlock

\ \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

lock

>rom

order

\   Define user vars now that we've defined the target routines
\   backing its implementation
User rp0
User sp0
User (pad)
User (#pad)
User (spad)

include kernel.fs
include dict.fs
include interp.fs
include compile.fs
include include.fs
\ include files.fs
include blocks.fs
include video.fs 
include lib/99ram
include editor.fs
include init.fs
\ include benchmark.fs
\ include testing.fs

VDPWA       constant    VDPWA
VDPWD       constant    VDPWD
VDPRD       constant    VDPRD
VDPST       constant    VDPST
GPLRD       constant    GPLRD
SOUND       constant    SOUND

\ copy the cold and abort addresses into the start of the memory block,
\ for the V9t9 ROM to consume
\
' ((cold))
' ((abort))

$460 [[[ ROM-start-addr ]]] 4 + !       \ break
    [[[ ROM-start-addr ]]] 6 + !        \ break
$460 [[[ ROM-start-addr ]]] 0 + !       \ boot
    [[[ ROM-start-addr ]]] 2 + !        \ boot


\ \\\\\\\\\\\\\\\\\

User >latest        \ latest definition

\   Return latest definition's nfa
: latest
    >latest @
;

\   The initial fence


\   Add extra words which will be loaded from GROM

has? enhanced-mode [if]

[[[ $4006 tdp ! ]]]

include locals.fs
include benchmark.fs

[then]


[[[ init-test-level [if]  ]]]  

append-test-file

[[[ [endif] ]]]

\   The RAM dictionary is copied from GROM to high-ram-start.
\   We use 'tram' to keep track of whatever stuff
\   is chosen to reside in RAM.

[[[ high-ram-start there $20 + max ]]] constant dp0


has? user-vars [if]

[[[ tudp @ ]]] constant udp0

[endif]

here constant fence

unlock
