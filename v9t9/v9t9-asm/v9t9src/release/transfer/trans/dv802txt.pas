program out;

var
  f:file;
  st:string;
  buf:array[0..255] of char;
  x:byte;
  off:word;
  eoff,eofil:boolean;
  red:word;
  ch:char;
  lenn:byte;

function readchar:char;
begin
  if (off>=256) or (buf[off]=#$ff) then
  begin
    off:=0;
    blockread(f,buf,256,red);
    eofil:=red=0;
  end;
  inc(off);
  eoff:=eofil and (off+1>=red);
  readchar:=buf[off-1];
end;

begin
  if paramcount<1 then halt;

  assign(f,paramstr(1));
  reset(f,1);

  seek(f,128);
  off:=256;
  eoff:=false;
  eofil:=false;

  while not eoff do
  begin
    lenn:=ord(readchar);
    if not eoff then
    begin
      x:=1;
      st:='';
      while x<=lenn do
      begin
        ch:=readchar;
        if (ch<>#12) then
          st:=st+ch;
        inc(x);
      end;
      writeln(st);
    end;
  end;

  close(f);
end.
