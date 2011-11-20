#!/bin/bash

NORMSCRIPT=$(echo $0 | sed 's/\\/\//g')
BASEDIR=$(dirname "$NORMSCRIPT")

PLAT=win32
WS=win32
ARCH=$(uname -m)

OS=$(uname)
if [ "$OS" = "Linux" ]; then
	OS=linux
	WS=gtk
fi

SWT=org.eclipse.swt.${WS}.${OS}.${ARCH}_3.6.2.v3659c.jar
echo $SWT

cd "$BASEDIR"
java -Djava.library.path=lwjgl/$OS:$SWT -Xmx256M -jar v9t9j.jar --swtgl
