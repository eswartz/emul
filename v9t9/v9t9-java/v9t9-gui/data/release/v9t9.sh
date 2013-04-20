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

NORMSCRIPT=$(echo $0 | sed 's/\\/\//g')
BASEDIR=$(dirname "$NORMSCRIPT")

JAVA=java
VMARGS=-Xmx256M

OS=$(uname)

if [ "$OS" = "Darwin" ]; then
    VMARGS="$VMARGS -XstartOnFirstThread"
fi

VMARGS="$VMARGS -Dlog4j.configuration=jar:file:./v9t9j.jar!/log4j.properties"
#VMARGS="$VMARGS -Dlog4j.configuration=jar:file:./v9t9j.jar!/debug.properties"

cd "$BASEDIR"
"$JAVA" $VMARGS  -jar v9t9j.jar "$@"


