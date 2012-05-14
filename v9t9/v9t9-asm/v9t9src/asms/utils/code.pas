program use_the_encoding_scheme_for_ti_emulator_config_file_options;

{
        To use this program, give the basename for a list of
        options:

        CODE cnf

        And it will read "cnf.lst" and make "cnf.out".

}

var
  i,o:text;

  st:string;

  x:byte;

  c:word;

function ror(val:byte; pos:byte):word;  assembler;
asm
  mov al,val
  xor ah,ah
  mov cl,pos
  ror ax,cl
end;


begin
     {$i-}
  if paramcount<1 then
  begin
    writeln('CODE <basename>');
    writeln;
    writeln('This will take a list of variable names (for configuration files)');
    writeln('to be encoded for a hash table.');
    writeln;
    writeln('<basename>.lst contains the list.');
    writeln('<basename>.out is created with the variable names and codes.');
    halt;
  end;

  assign(i,paramstr(1)+'.lst');
  reset(i);
  if ioresult<>0 then
  begin
    writeln('Couldn''t open');
    halt;
  end;

  assign(o,paramstr(1)+'.out');
  rewrite(o);
  if ioresult<>0 then
  begin
    writeln('Couldn''t create');
    halt;
  end;

  while not eof(i) do
  begin
    readln(i,st);
    if st='' then
      writeln(o,st)
    else
    begin
        for x:=1 to length(st) do
              st[x]:=upcase(st[x]);

        c:=0;

        for x:=1 to length(st) do
           c:=c xor ror(ord(st[x]),x-1);

        writeln(o,st,' = ',c);
    end;
  end;

  close(o);
  close(i);
end.
