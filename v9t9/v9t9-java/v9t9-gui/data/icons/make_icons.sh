#!/bin/bash
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
        for i in 22.50 45 90 180 ; do
        	echo $BASE $i
            SIZE=`echo $i | perl -nle 'print $_*64/90.'`
            FILE=${BASE}_${SIZE}.png
	    export_png "$BASE" "$FILE" $i
        done
}

resize_icons "v9t9"
resize_icons "icons"
resize_icons "dev_icons"

export_png "cpu" "cpu.png" 90


