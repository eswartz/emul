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

ARCH=$(uname -m)
if [ "$ARCH" = "i686" ]; then
	ARCH=x86
fi

JAVA=java
OS=$(uname)
VMARGS=-Xmx256M

if [ "$OS" = "Linux" ]; then
	OS=linux
	WS=gtk
elif [ "$OS" = "Darwin" ]; then
    OS=macosx
    WS=cocoa
    VMARGS="$VMARGS -XstartOnFirstThread"
else
	OS=win32
	WS=win32
	
	if [ "$PROCESSOR_ARCHITEW6432" = "AMD64" ] ; then
		ARCH=x86_64
		JAVA="C:/program files/java/jre6/bin/java"
	fi
fi

SWT=libs/org.eclipse.swt.${WS}.${OS}.${ARCH}.jar
NATIVES=v9t9j-natives-${OS}-intel.jar

JARS="libs/bcel-5.2.jar \
	libs/gnu-getopt-1.0.13.jar \
	libs/jinput.jar \
	libs/jna.jar \
	libs/lwjgl.jar \
	libs/lwjgl_util.jar \
	libs/org.apache.* \
	libs/org.eclipse.core.* \
	libs/org.eclipse.equinox.* \
	libs/org.eclipse.jface* \
	libs/org.eclipse.swt.jar \
	libs/org.eclipse.tm.tcf.jar \
	libs/svgSalamander.jar"

JARPATH=""
for jar in $JARS; do
	JARPATH=$JARPATH:$jar
done
	
#echo $SWT $NATIVES
#echo $JAVA
#echo $JARPATH

mkdir -p tmpdir
unzip -o -d tmpdir $NATIVES 

cd "$BASEDIR"
"$JAVA" -cp "v9t9j.jar:$SWT:libs/*" -Djava.library.path=tmpdir $VMARGS  v9t9.gui.Emulator 


