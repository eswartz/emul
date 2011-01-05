#!/bin/sh
[ -d gcc-release ] || mkdir gcc-release
cd gcc-release
[ -f Makefile ] || cmake -DCMAKE_BUILD_TYPE=Release -G "Unix Makefiles" ../..
make $*
