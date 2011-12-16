#!/bin/sh
[ -d gcc-debug ] || mkdir gcc-debug
cd gcc-debug
[ -f Makefile ] || cmake -DCMAKE_BUILD_TYPE=Debug -G "Unix Makefiles" ../..
make $*
