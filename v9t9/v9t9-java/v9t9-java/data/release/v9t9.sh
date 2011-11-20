#!/bin/bash

NORMSCRIPT=$(echo $0 | sed 's/\\/\//g')
BASEDIR=$(dirname "$NORMSCRIPT")

ARCH=$(uname -m)
if [ "$ARCH" = "i686" ]; then
	ARCH=x86
fi

JAVA=java
OS=$(uname)
if [ "$OS" = "Linux" ]; then
	OS=linux
	WS=gtk
else
	# TODO: distinguish MacOS X...
	OS=win32
	WS=win32
	
	if [ "$PROCESSOR_ARCHITEW6432" = "AMD64" ] ; then
		ARCH=x86_64
		JAVA="C:/program files/java/jre6/bin/java"
	fi
fi

SWT=org.eclipse.swt.${WS}.${OS}.${ARCH}_3.6.2.v3659c.jar
echo $SWT
echo $JAVA

cd "$BASEDIR"
"$JAVA" -Djava.library.path=lwjgl/$OS -cp $SWT -Xmx256M -jar v9t9j.jar --swtgl
