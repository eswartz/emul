
program text2dv80;
uses dos;

type
  fdrtype=record
    name:packed array [1..10] of char;
    res10:word;
    flags:byte;
    recspersec:byte;
    secsused:word;
    eof:byte;
    reclen:byte;
    fixrecs:word;
    res20:array[0..107] of byte;
  end;
var
  fdr:fdrtype;

procedure gettiname(ibmname:string);
var
  x:byte;
  p:byte;

  bp:byte;
begin
  for p:=1 to 10 do
    fdr.name[p]:=' ';

  p:=1;
  x:=1;
  while x<=length(ibmname) do
  begin
    if (ord(ibmname[x])>=128) then
      fdr.name[p]:=chr(ord(ibmname[x])-128)
    else
      fdr.name[p]:=ibmname[x];
    inc(p);
    inc(x);
  end;
end;


procedure makedosname(var st:string);
const
  illegal:string='<>=,;:*?[]()/\!~';
var
  x:byte;
begin
  for x:=1 to 10 do
    if pos(st[x],illegal)<>0 then
      st[x]:=chr(ord(st[x])+128);
  st:=copy(st,1,8)+'.'+copy(st,9,2);
end;

procedure expand(var ins:string);
var
  st:string;
  i,o:byte;

begin
  i:=1;
  o:=0;
  st:='';
  while (i<=length(ins)) do
  begin
    if (ins[i]<>#9) then
      st:=st+ins[i]
    else
    begin
      repeat
        inc(o);
        st:=st+' ';
      until (o mod 8=0);
      dec(o);
    end;
    inc(o);
    inc(i);
  end;
  ins:=st;
end;


procedure createdv80(dname:string;ttiname:string);
var
  new:file;
  old:text;
  line:string;
  act:word;
  total:word;

  buffer:array[0..255] of byte;

  reclen:byte;

  linelen:byte;

  dir,name,ext:string;
  tiname:string;
begin
  assign(old,dname);
  {$i-}
  reset(old);
  if ioresult<>0 then
  begin
    writeln('Couldn''t open ',dname,'!');
    halt;
  end;

  fsplit(ttiname,dir,name,ext);
  gettiname(name);

  tiname:=name+ext;
  makedosname(tiname);
  tiname:=dir+tiname;

  assign(new,tiname);
  rewrite(new,1);
  if ioresult<>0 then
  begin
    writeln('Couldn''t create ',tiname,'!');
    halt;
  end;

  reclen:=80;
  seek(new,128);
  fdr.flags:=$82;

  fdr.recspersec:=256 div reclen;
  fdr.reclen:=reclen;

  total:=0;
  repeat
    readln(old,line);
    expand(line);

      linelen:=length(line)+1;

      if total+linelen>=256 then
      begin
        inc(fdr.secsused);
        inc(fdr.fixrecs);

        blockwrite(new,buffer,256);
        total:=0;
      end;

      move(line,buffer[total],linelen);
      inc(total,linelen);
      buffer[total]:=255;
      fdr.eof:=total;
  until eof(old);

  if total<>0 then
  begin
    blockwrite(new,buffer,256);
    inc(fdr.secsused);
    inc(fdr.fixrecs);
  end;


  fdr.secsused:=swap(fdr.secsused);

  seek(new,0);
  blockwrite(new,fdr,128);
  close(new);
  close(old);
end;



begin
  if paramcount<2 then
  begin
    writeln('TXT2DV80  by Edward Swartz');
    writeln;
    writeln('TXT2DV80  <destination TI name> <dos text file>');
    writeln;
    halt;
  end;

  createdv80(paramstr(2),paramstr(1));
end.
