$big = 0;
if ($ARGV[0] =~ /-big/) {
	$big = 1;
}

$i = 0;
while ($i < 256) {
	$st = "0x";

	$j = 0;
	while ($j < 8) {
		if (!$big) {
			if ($i & (1 << $j))  {
				$st .= "01" ;
			} else {
				$st .= "00" ;
			}
		} else {
			if ($i & (0x80 >> $j)) {
				$st .= "01" ;
			} else {
				$st .= "00" ;
			}
		}
		$j++;
		if ($j ==  4) {
			$st .= "L, 0x" ;
		}
	}

	$st .= "L,";

	print "\t/* $i */\t$st\n";
	$i++;
}
