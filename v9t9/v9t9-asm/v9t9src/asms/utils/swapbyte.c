#include <stdio.h>
#include <stdlib.h>

int	main(int argc, char *argv[])
{
	FILE	*romfile;
	char	rom[8192],romti[8192];

	if (argc<2)
	{
		printf("swapbyte [rom image]\n");
		exit(1);
	}

	if ((romfile=fopen(argv[1],"rb"))==NULL)
	{
		perror(argv[1]);
		exit(1);
	}

	if (!(fread(rom,8192,1,romfile)))
	{
		printf("Need 8192 bytes\n");
		fclose(romfile);
		exit(1);
	}
	fclose(romfile);

	swab(rom,romti,8192);

	if ((romfile=fopen(argv[1],"wb"))==NULL)
	{
		perror(argv[1]);
		exit(1);
	}

	if (!(fwrite(romti,8192,1,romfile)))
	{
		printf("Couldn't write 8192 bytes: ");
		perror(argv[1]);
		exit(1);
	}
	fclose(romfile);

	printf("Successful.\n");

	return 0;
}