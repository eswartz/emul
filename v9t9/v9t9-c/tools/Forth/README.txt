
This is an implementation of Forth for V9t9 -- but as a ROM kernel!

nforth.rom:  CPU ROM (nforth.tsm), initial FORTH dictionary
	 -- this contains a set of XOPs to access the OS services
	 -- this uses scratch pad RAM to hold a set of workspaces for
	 FORTH, interrupts, video handling, traditional DSRs
	 -- this assumes some additional memory in expansion RAM at >2000

nforthc.bin: FORTH dictionary (overflow #1)

nforthg.bin: FORTH dictionary (overflow #2, loaded into RAM)
	 -- organized as a series of blocks which are copied into RAM at startup
	 -- in GROM, the memory is stored as repeating <$aa55> <start> <stop> <data...>
	 where <start>/<stop> are the ranges of RAM to copy the following data to.

nforth.grm:	 kernel data (nforth.gsm)
	 -- keyboard mapping
	 -- startup font

========

Cross-compiling

99build.fs is the driver.  This uses gforth's cross.fs to aid in generating a FORTH
kernel for another system as well as 99asm.fs to compile assembler words.

After a lot of setup, 99prims.fs is interpreted, which defines
primitives in 9900 assembly.  Then, constants.fs, kernel.fs,
interp.fs, dict.fs, compile.fs, files.fs, and init.fs are read.
kernel.fs allows all of its words to be overridden by primitives --
thus, it may (theoretically) define the lion's share of words if
performance allows.

The kernel is built into (host) memory into the addresses the
dictionary will take at runtime.  Calls to save-region at the end of
99build.fs will determine whether any parts need to be emitted to GROM
(instead of RAM, which can't be easily saved and loaded by V9t9 except
with session files) and will emit the binary code for regions into the
appropriate files using a special block notation (interpreted by the
v9t9 rom).

FORTH defines variables, but the dictionary is compiled into ROM.
Such words are initialized in two steps.  Parenthesized 0-words --
like (DP0), (RP0), etc. -- are constants or calculated values encoded
into ROM.  At startup (COLD), the values are written into User
variables which reside in RAM. A similar situation holds for deferred
words.  See init.fs.

Conventions:

-- the words T and H will switch the vocabulary back and forth from the TARGET to the
HOST.  This allows us to use the right value for CELLS, ",", etc.  These values are
established in 99config.fs (which is invoked before 99build.fs on the command line,
since we can't properly locate the input file).

-- the G word will establish a "ghost" vocabulary, which contains
definitions of words that can execute in the host Forth!

Features:

-- the test" word can take a string whose contents will be added to a
test method, executed at ROM startup.  This words by generating a
99tests.fth file which is populated with these tests and later parsed
into the ROM.  The variable "test-level" can be modified to control
which level of tests are compiled in -- anything below that level is
included.

Level 0 brings no tests; level 3 brings all tests.  The words 1test",
2test", 3test" can be used to define words at higher levels of
logging.

At startup, a dot is printed for each successful test.  If a test fails,
its name is printed instead.  Of course, none of this prevents a test
from printing its own stuff and confusing the issue.


==================

The current optimal setup uses direct threading and non-inlined next.  See
99config.fs for the flags.

Indirect threading uses more memory and is slower.  It works as well as direct
threading though.

Inlined next is a bit faster but uses 0x100 more bytes with 454 words defined,
so it has some slight disadvantages.

There is a flag for enabling profiling, which adds after the NFA a pointer to
an address which can hold a counter for how many times the word is invoked.
This is buggy, currently -- it causes some crashes and test failures.  Likely
some words are doing direct NFA>XT conversions or similar... not sure.


