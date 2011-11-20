#!/bin/bash

NORM=$(echo $0 | sed 's/\\/\//g')
BASEDIR=$(dirname "$NORM")

cd "$BASEDIR"
java -Djava.library.path=lwjgl/win32 -jar v9t9j.jar --swtgl
