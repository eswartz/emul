#   make_icons.sh
# 
#   (c) 2010-2012 Edward Swartz
# 
#   This program is free software; you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation; either version 2 of the License, or
#   (at your option) any later version.
#  
#   This program is distributed in the hope that it will be useful, but
#   WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#   General Public License for more details.
#  
#   You should have received a copy of the GNU General Public License
#   along with this program; if not, write to the Free Software
#   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
#   02111-1307, USA.
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



