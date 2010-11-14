
<host

$400    constant start-dp

host>

start-dp    DP !

\   boot addr

0 ,

Create varBlock     10 allot

varBlock        Constant var-1
varBlock 2 +    Constant cvar-2
varBlock 3 +    Constant dvar-3

1 var-1 !
2 cvar-2 c!
$ffff.aaaa dvar-3 d!

: bootit
    10 varBlock var-1 !
    20 varBlock cvar-2 c!
    $1234.5678 varBlock dvar-3 d!
;

' bootit  start-dp !
