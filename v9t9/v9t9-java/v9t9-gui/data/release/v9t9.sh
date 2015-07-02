#!/bin/sh
#   v9t9.sh
# 
#   (c) 2011-2013 Edward Swartz
# 
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Eclipse Public License v1.0
#   which accompanies this distribution, and is available at
#   http://www.eclipse.org/legal/epl-v10.html
# 

if [ -z "$BASEDIR" ] ; then
	NORMSCRIPT=$(echo $0 | sed 's/\\/\//g')
	BASEDIR=$(dirname "$NORMSCRIPT")
fi

if [ -z "$JAVA" ]; then
	JAVA=java
fi	
VMARGS="$VMARGS -Xmx256M"

if [ "$1" = "-debug" ]; then
	VMARGS="$VMARGS -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:8000"
	shift
fi	

OS=$(uname)

if [ "$OS" = "Darwin" ]; then
    VMARGS="$VMARGS -XstartOnFirstThread"
fi
VMARGS="$VMARGS -Djna.nosys=true"

# VMARGS="$VMARGS -Dv9t9.sound.rate=44100"

VMARGS="$VMARGS -Dlog4j.configuration=jar:file:$BASEDIR/v9t9j.jar!/log4j.properties"
#VMARGS="$VMARGS -Dlog4j.configuration=jar:file:$BASEDIR/v9t9j.jar!/debug.properties"

VMARGS="$VMARGS $V9T9_VMARGS"
  
"$JAVA" $VMARGS -cp "$BASEDIR"  -jar $BASEDIR/v9t9j.jar "$@"


