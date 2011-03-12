#!/bin/bash
resize_icons() {
        BASE=$1
        for i in 22.50 45 90 180 ; do
            SIZE=`echo $i | perl -nle 'print $_*64/90.'`
            FILE=${BASE}_${SIZE}.png
            if [ "${BASE}.svg" -nt "$FILE" ] ; then
                    inkscape ${BASE}.svg --export-png=$FILE --export-dpi=$i
            fi
        done
}

resize_icons "icons"
resize_icons "dev_icons"

