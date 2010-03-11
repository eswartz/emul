#!/bin/bash
for i in 22.50 45 90 180 ; do
    SIZE=`echo $i | perl -nle 'print $_*64/90.'`
    FILE=icons_${SIZE}.png
    inkscape icons.svg --export-png=$FILE --export-dpi=$i
done
