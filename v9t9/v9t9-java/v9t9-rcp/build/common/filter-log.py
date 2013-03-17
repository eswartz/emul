
#
#	Filter the logfile generated from ant to show only known errors
#	(to make for easier discovery of build problems, etc).
#

import re, sys

TARGET = re.compile(r'(.*):')
TASK_LINE = re.compile(r'\s+\[(.*)\]\s(.*)')
MKDIR_FOR_PLUGIN_LINE = re.compile(r'.*\[mkdir\].*(?:/|\\)([^/\\]+)(?:/|\\)@dot')
JAVAC_LINE = re.compile(r'.*\[javac\]\s*(.*)')

def process(inf, outf):
	lastPlugin = None
	
	for line in inf.xreadlines():
		sline = line.strip()
		match = MKDIR_FOR_PLUGIN_LINE.match(sline)
		if match:
			lastPlugin = match.group(1)
			print >>outf, "\n\n**** Building",lastPlugin,"\n"
			continue
		match = JAVAC_LINE.match(sline)
		if match:
			print >>outf, match.group(1)
			continue
		
		
if __name__ == "__main__":
	if len(sys.argv) < 3:
		print "Usage:",__file__,"<infile>","<outfile>"
		sys.exit(0)
		
	innie = open(sys.argv[1], 'rt')
	outie = sys.argv[2] == '-' and sys.stdout or open(sys.argv[2], 'wt')
	
	process(innie, outie)
	
	innie.close()
	outie.close()