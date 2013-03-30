#   make_icons.sh
# 
#   (c) 2010-2012 Edward Swartz
# 
#   All rights reserved. This program and the accompanying materials
#   are made available under the terms of the Eclipse Public License v1.0
#   which accompanies this distribution, and is available at
#   http://www.eclipse.org/legal/epl-v10.html
# 
export_png() {
	BASE=$1
	FILE=$2
	DPI=$3
        if [ ! -f "$FILE" ] || [ "${BASE}.svg" -nt "$FILE" ] ; then
             inkscape ${BASE}.svg --export-png=$FILE --export-dpi=$DPI
        fi
}

resize_icons() {
        BASE=$1
	shift
	SIZES=$@
        for i in $SIZES ; do
        	echo $BASE $i
            SIZE=`echo $i | perl -nle 'print $_*64/90.'`
            FILE=${BASE}_${SIZE}.png
	    export_png "$BASE" "$FILE" $i
        done
}

ICON_SIZES="22.50 45 90 180"
resize_icons "v9t9" $ICON_SIZES 135 270 360 720
resize_icons "icons" $ICON_SIZES
resize_icons "dev_icons" $ICON_SIZES

export_png "cpu" "cpu.png" 90
export_png "key" "key_32.png" 90
export_png "key" "key_64.png" 180
export_png "key" "key_128.png" 360
export_png "key" "key_256.png" 720
export_png "key" "key_512.png" 1440



