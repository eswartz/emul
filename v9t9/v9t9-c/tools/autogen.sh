#!/bin/sh
aclocal
autoconf
automake -a
./configure "$@"

