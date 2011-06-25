#!/usr/bin/perl -w

print "#include \"16bit.h\"\n";
print "#if __i386__ || __INTEL__\n";

$big = 0;
while ($big < 2) {
  $i = 0;
  while ($i < 256) {
	  # for each byte, try to write two longs
	  # in the form:
	  #  *(u32 *)o = 0x01010001*f | 0x00000100*b;

    print "static void drawrow$i(u8 *o,u8 f,u8 b)\n{\n";

	$j = 0;
	while ($j < 8) {
		if ($j == 0 || $j == 4) {
			$f = "0x";
			$b = "0x";
		}
		if (!$big) {
			if ($i & (1 << $j))  {
				$f .= "01" ; $b .= "00";
			} else {
				$b .= "01" ; $f .= "00";
			}
		} else {
			if ($i & (0x80 >> $j)) {
				$f .= "01" ; $b .= "00";
			} else {
				$b .= "01" ; $f .= "00";
			}
		}
		$j++;

		# half done, write a long
		if ($j == 4) {
			print "\t*(u32 *)o = ($f * f) | ($b * b);\n";
			
		} elsif ($j == 8) {
			print "\t*(u32 *)(o+4) = ($f * f) | ($b * b);\n";
		}
	}

	print "}\n";
	$i++;
  }
  $big++;
  if ($big == 1) {
	  print "\n#else /* big-endian */\n";
  }
}
print "\n#endif /* endianness */\n";

print "\nvoid (*vdpdrawrow[])(u8 *,u8,u8)=\n{\n";
$i = 0;
while ($i < 256) {
	print "drawrow$i,";
	if ($i % 6 == 5) {
		print "\n";
	}
	$i++;
}
print "};\n";
