#
#  GLADE can occasionally regenerate gtkcallbacks.c with 
#  repeats of existing functions.
#
#  This script identifies the existing functions and deletes the
#  empty new ones.
#

#  map of function names to 1
%function_map = ();

sub scanfile($$);

# call as scanfile(filename, rewrite?)

sub scanfile($$) {
  my $filename = shift;
  my $rewrite = shift;
  my $changed = 0;

  my $ifcount = 0;
  my $incdepth = 0;
  
  local *FILE;
  local *NEW;

  print "Scanning $filename...\n";

  open (FILE, "<$filename") || return;
  if ($rewrite) {
	open(NEW, ">$filename.tmp") || die;
  }

  while (<FILE>) {
	my $line = $_;

	if ($line =~ /\#include \"(gtkcallbacks-.*)\"/) {
	  # include subfile

	  if ($rewrite) { print NEW $line; }

	  my $subfile = $1;

	  # don't rewrite subfiles but gather function defs
	  scanfile($subfile, 0);
	}

	elsif ($line =~ /\#if/) {
	  # ignore stuff in conditionals just to be safe

	  if ($rewrite) { print NEW $line; }
	  $ifcount++;
	  while ($ifcount) {
		$line = <FILE>;
		if ($rewrite) { print NEW $line; }
		if ($line =~ /\#endif/) {
		  $ifcount--;
		} elsif ($line =~ /\#if/) {
		  $ifcount++;
		}
	  }
	}
	
	# look for return type on one line, and decl on next
	elsif ($line =~ /^void|^gboolean/) {
	  my $decl = <FILE>;
	  if ($decl =~ /^(on_[^( \n]*)/) {
		
#		print "function '$1'\n";
		
		if (defined($function_map{$1})) {
		  print "duplicate '$1'!\n";
		  # skip
		  while (<FILE> !~ /^}/) { };
	    $changed = 1;
	  } else {
		$function_map{$1} = "1";
		if ($rewrite) {
		  print NEW $line;
		  print NEW $decl;
		}
	  }
		  
		} else {
		  if ($rewrite) {
			print NEW $line;
			print NEW $decl;
		  }
		}
	} else {
	  if ($rewrite) { print NEW $line; }
	}
  }
  close(FILE);
  if ($rewrite) { close(NEW); }

  if ($rewrite) {
	if ($changed) {
	  rename("$filename.old2", "$filename.old3");
	  rename("$filename.old", "$filename.old2");
	  rename("$filename", "$filename.old") || die;
	  rename("$filename.tmp", "$filename") || die;
	} else {
	  print "No changes to $filename\n";
	}
  }
}


$filename = $ARGV[0];
scanfile($filename, 1);	# rewrite main file

