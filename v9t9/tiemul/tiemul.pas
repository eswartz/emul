{$s-}
{$f-}
{$m 16384,0,655360}
program ti_emulator;
uses crt,dos,objects,drivers;
type
  bytes=array[0..65521] of byte;
  vbytes=array[0..16383] of byte;
  words=array[0..32765] of word;
const
  pal:array[0..15,0..2] of byte=
    ((0,0,0),(0,0,0),(0,48,0),(0,63,0),(0,0,48),(0,0,63),(32,0,0),(0,48,48),
     (48,0,0),(63,0,0),(48,48,0),(63,63,0),(0,32,0),(48,0,48),(48,48,48),
     (63,63,63));
type
  breakptr=^breakrec;
  breakrec=
    record
      where:word;
      next:breakptr;
    end;

  filerec=
    record
      attribs:byte;
      pabstart:word;
      opened:boolean;
      mode:byte;
      reclen:byte;
      seq:boolean;
      fixed:boolean;
      catalog:boolean;
      eeohef:boolean;
      openmode:byte;
      thefile:file;
      display:boolean;
      isprogram:boolean;
    end;

var

  cpu:^bytes;
  cpuw:^words;
  vdp:vbytes;
  gpl:^bytes;
  gpllim:word;
  gplw:^words;

  { cpu regs }
  ip:word;
  st:word;
  wp:word;

  { vdp regs }
  vaddr,gaddr:word;
  vr:array[0..7] of byte;
  screen,colors,patterns:word;
  blank:boolean;
  cols:word;
  startchanged,endchanged:word;       { VDP screen changes }
  anychanged:boolean;
  f:file;
  hours,minutes,seconds,seconds100:word;
  sixties,cur60:byte;

  d,s,t1,t2:word;
  tb1,tb2:byte;
  sip,int:word;
  in1,in2,in3:word;
  vwrt,gwrt,grd:boolean;     { is the program writing an address? }
  xop:boolean;    { true if in an XOP }
  inting:boolean;
  attribute:byte;
  soundread:boolean;
  soundvol:boolean;
  soundvalue:word;

  debugging,fast:boolean;
  break:breakptr;
  curint,intcount:integer;
  firstchar,lastchar:word;


  fcbs:array[1..9] of filerec;
procedure dodsr;
  var
    op:byte;
    filename:string;
    device:string;
    pab:word;
    fn:byte;   { file number }
    res:byte;
    temp:string;
    addr:word;
    actred,actwrt:word;
    toread,towrt:word;
    error:boolean;
  procedure findpab;
  var
    f:boolean;
  begin
    fn:=0;
    f:=false;
    while (fn<9) and not f do
    begin
      inc(fn);
      f:=fcbs[fn].pabstart=pab;
    end;
    if (not f) then
    if (op=0) OR (OP=5) OR (OP=6) OR (OP=7) then
    begin
      fn:=1;
      while (fn<9) and (fcbs[fn].opened) do
        inc(fn);
      if fn>=9 then
        fn:=$FF;
    end
    else
      fn:=0;
  end;
  procedure seterror(er:byte);
  begin
    vdp[fcbs[fn].pabstart+1]:=vdp[fcbs[fn].pabstart+1] and 31 or (er*32);
    if er=5 then
    with fcbs[fn] do
    begin
      close(thefile);
      opened:=false;
      pabstart:=$ffff;
    end;
    error:=true;
  end;
  procedure setattribs;
  var
    tm:byte;
  begin
    with fcbs[fn] do
    begin
      SETERROR(0);
      ERROR:=FALSE;
      tm:=(vdp[pabstart+1] and $6) shr 1;
      if (OPENMODE<>0) AND (tm<>openmode) then
        seterror(3);
      fixed:=vdp[pabstart+1] and $10=0;
      seq:=vdp[pabstart+1] and $1=0;
      display:=vdp[pabstart+1] and $8=0;
      RECLEN:=VDP[PABSTART+4];
    end;
  end;
  procedure setfilename;
  begin
    if (filename<>'.') then
    with fcbs[fn] do
    begin
      FILENAME:=COPY(FILENAME,2,255);
      if filename[0]>#8 then
        filename:=copy(filename,1,8)+'.'+copy(filename,9,2);
      assign(thefile,'c:\tp\ti994a\'+filename);
    end;
  end;
  begin
    { get the filename }
    t1:=cpuw^[$8356 shr 1]-cpuw^[$8354 shr 1]-1;
    pab:=t1-9;
    { VDP[t1]=ptr to length, name... }
    device[0]:=chr(vdp[t1]);
    for t2:=t1+1 to t1+ord(device[0]) do
      device[t2-t1]:=chr(vdp[t2]);
    filename:=device;
    device:=copy(device,1,pos('.',device)-1);
    filename:=copy(FILENAME,pos('.',filename),255);
    error:=false;
    if device='' then
    begin
{      cpu^[$837d]:=cpu^[$837d] or $20;
      seterror(0);
      inc(cpuw^[wp+11],0);
      error:=true;}
    end
    else
    begin
      op:=vdp[pab];
      vdp[pab+1]:=op and 31;
      findpab;
      if (op<>0) AND (OP<>5) AND (OP<>6) AND (OP<>7) and (fn=0) then   { bad file }
      begin
        seterror(2);   { 2=bad open attr }
      end
      else
      if (fn=$FF) then   { open here }
      begin
        seterror(4);   { 4=out of space }
      end
      else
      case op of
        0 : { open }
          begin
            with fcbs[fn] do
            begin
              if opened then
                seterror(7)
              else
                begin
                  pabstart:=pab;
                  if (device='DSK1') or (device='DSK2') or (device='DSK3') then
                  begin
                    catalog:=false;
                    setfilename;
                    openmode:=(vdp[pabstart+1] and $6 shr 1);
                    if filename='.' then
                    begin
                      catalog:=true;
                      vdp[pab+4]:=163;
                    end
                    else
                      if vdp[pab+4]=0 then
                        vdp[pab+4]:=80;
                    OPENED:=FALSE;
                    case openmode of
                      0 :
                        begin
                          {$i-}
                          reset(thefile,1);
                          if (ioresult=2) OR (FILESIZE(THEFILE)=0) then
                          begin
                            IF FILESIZE(THEFILE)=0 THEN
                              CLOSE(THEFILE);
                            rewrite(thefile,1);
                            res:=ioresult;
                            if (res=4) or (res=101) then
                              seterror(4)
                            else
                            IF RES=5 THEN
                              seterror(1)
                            ELSE
                            BEGIN
                              OPENED:=TRUE;
                              ATTRIBS:=$0;
                              RECLEN:=80;
                              BLOCKWRITE(THEFILE,ATTRIBS,1);
                              BLOCKWRITE(THEFILE,RECLEN,1);
                            END;
                          end
                          else
                          begin
                            blockread(thefile,attribs,1);
                            blockread(thefile,reclen,1);
                            if (attribs and $80<>0) or
                               (reclen<>vdp[pab+4]) then
                              seterror(2);
                            vdp[pab+1]:=attribs;
                            vdp[pab+4]:=reclen;
                            OPENED:=TRUE;
                          end;

                          {$I+}
                        end;
                      1 :
                        begin
                          {$i-}
                          rewrite(thefile,1);
                          res:=ioresult;
                          if (res=4) or (res=101) then
                            seterror(4)
                          else
                          if res=5 then
                            seterror(1);
                          OPENED:=FALSE;

                          {$I+}
                        end;
                      2 :
                        begin
                          {$i-}
                          reset(thefile,1);
                          res:=ioresult;
                          if res=2 then
                            seterror(2)
                          else
                          begin
                            blockread(thefile,attribs,1);
                            blockread(thefile,reclen,1);
                            if (attribs and $98<>vdp[pab+1] and $18) or
                               (reclen<>vdp[pab+4]) then
                               seterror(2);
                            vdp[pab+1]:=attribs;
                            vdp[pab+4]:=reclen;
                          end;
                          {$i-}
                        end;
                      3 :
                        begin
                          {$i-}
                          reset(thefile,1);
                          res:=ioresult;
                          if res=2 then
                          begin
                            rewrite(thefile,1);
                            if ioresult<>0 then
                              seterror(4);
                          end
                          else
                          begin
                            blockread(thefile,attribs,1);
                            blockread(thefile,reclen,1);
                            if (attribs and $98<>vdp[pab+1] and $18) or
                               (reclen<>vdp[pab+4]) then
                               seterror(2);
                            vdp[pab+1]:=attribs;
                            vdp[pab+4]:=reclen;
                          end;
                        end;
                    end;
                  end
                  else
                    if (copy(device,1,5)='RS232') then
                    begin
                      if device[0]=#5 then
                        device:='COM1'
                      else
                        if (device[6]='/') then
                          device:='COM'+device[7]
                        else
                          seterror(2);
                      {$i-}
                      assign(thefile,device);
                      { set baud rates and stuff later }
                      if mode<>1 then
                        reset(thefile,1)
                      else
                        rewrite(thefile,1);
                      {$i+}
                      if ioresult<>0 then
                        seterror(2);
                    end;
                end;
            end;
          end;
        1 :
          with fcbs[fn] do
            if not opened then
              seterror(7)
            else
            begin
              {$i-}
              setattribs;
              seek(thefile,0);
              if mode<>2 then
              begin
                blockwrite(thefile,attribs,1);
                blockwrite(thefile,reclen,1);
              end;
              close(thefile);
              res:=ioresult;
              if res=101 then
                seterror(4);
              opened:=false;
              pabstart:=$ffff;
            end;
        2 :
        begin
          SETATTRIBS;
          with fcbs[fn] do
            if not opened then
              seterror(7)
            else
            if mode and 1=1 then
              seterror(3)
            else
            if not catalog then
            begin
              toread:=vdp[pabstart+4];
              addr:=vdp[pabstart+2]*256+vdp[pabstart+3];
              if fixed then
              begin
                toread:=reclen;
                {$i-}
                blockread(thefile,vdp[addr],toread,actred);
                {$i+}
                if (toread<>actred) or (ioresult<>0) then
                  seterror(5);
                vdp[pabstart+5]:=actred;
              end
              else
              begin
                actred:=0;
                {$i-}
                blockread(f,actred,1);
                {$i+}
                if actred=255 then
                  eeohef:=true
                else
                begin
                  {$i-}
                  blockread(thefile,vdp[addr],actred,toread);
                  {$i+}
                  if (toread<>actred) or (ioresult<>0) then
                    seterror(5);
                  vdp[pabstart+5]:=toread;
                end;
              end;
            end
            else
            begin   { read catalog entry }
            end;
         end;
        3 :
        BEGIN
          SETATTRIBS;
          with fcbs[fn] do
            if not opened then
              seterror(7)
            else
            if mode=2 then
              seterror(3)
            else
            begin
              addr:=vdp[pabstart+2]*256+vdp[pabstart+3];
              towrt:=vdp[pabstart+5];
              if fixed then
                towrt:=reclen;
              {$i-}
              blockwrite(thefile,vdp[addr],towrt,actwrt);
              {$i+}
              if (actwrt<>towrt) or (ioresult<>0) then
                seterror(4);
            end;
          END;
        4 :
        BEGIN
          SETATTRIBS;
          with fcbs[fn] do
            if not opened then
              seterror(7)
            else
            if (seq and (mode and 1<>0)) or not fixed then
              seterror(3)
            else
            begin
              addr:=vdp[pabstart+6]*256+vdp[pabstart+7];
              {$i-}
              if seq then
                seek(thefile,2)
              else
                seek(thefile,addr*word(reclen)+2);
              {$i+}
              if ioresult=104 then
                seterror(5);
            end;
            END;
        5 :
          with fcbs[fn] do
            begin
              OPENED:=FALSE;
              if op=5 then
              begin
                setfilename;
                {$i-}
                reset(thefile,1);
                {$i+}
                if (filename='.') then
                  seterror(2)
                else
                if ioresult<>0 then
                  seterror(7)
                else
                begin
                  addr:=vdp[pab+2]*256+vdp[pab+3];
                  toread:=vdp[pab+6]*256+vdp[pab+7];
                  vdp[pab+6]:=0;
                  vdp[pab+7]:=0;
                  {$i-}
                  blockread(thefile,attribs,1);
                  blockread(thefile,reclen,1);
                  if attribs and $80=0 then
                    seterror(7)
                  else
                  begin
                    blockread(thefile,vdp[addr],toread,actred);
                    res:=ioresult;
                    vdp[pab+6]:=actred shr 8;
                    vdp[pab+7]:=actred and 255;
                    pabstart:=$ffff;
                  end;
                  CLOSE(THEFILE);
                  {$i+}
                end;
              end
          end;
        6 :
        begin
          with fcbs[fn] do
          begin
            OPENED:=FALSE;
            setfilename;
            if filename='.' then
              seterror(7)
            else
            begin
              rewrite(THEfILE,1);
              res:=ioresult;
              if (res=4) or (res=101) then
                seterror(4)
              else
                IF RES=5 THEN
                seterror(1);

              addr:=vdp[pab+2]*256+vdp[pab+3];
              towrt:=vdp[pab+6]*256+vdp[pab+7];
              {$i-}
              attribs:=$80;
              blockwrite(thefile,attribs,1);
              blockwrite(thefile,reclen,1);
              blockwrite(thefile,vdp[addr],towrt,actwrt);
              if (ioresult<>0) or (actwrt<>towrt) then
                seterror(4);
              pabstart:=$ffff;
              CLOSE(THEFILE);
              {$I+}
            end;
          end;
        end;
        7 :
          with fcbs[fn] do
          begin
            OPENED:=FALSE;
            setfilename;
            if filename='.' then
              seterror(3)
            else
            begin
              erase(thefile);
              if ioresult=1 then
                seterror(1);
              pabSTART:=$ffff;
            end;
          end;

        8 : ;
        9 :
          with fcbs[fn] do
          begin
            SETATTRIBS;
            vdp[pabstart+8]:=0;
            if (vdp[pabstart+1] and $e0=4) then
              vdp[pabstart+8]:=vdp[pabstart+8] or $80;
            if (vdp[pabstart+1] and $e0=2) then
              vdp[pabstart+8]:=vdp[pabstart+8] or $40;
            if vdp[pabstart+1] and $80=1 then
              vdp[pabstart+8]:=vdp[pabstart+8] or $10;
            if isprogram then
              vdp[pabstart+8]:=vdp[pabstart+8] or $8;
            if not fixed then
              vdp[pabstart+8]:=vdp[pabstart+8] or $4;
            if (vdp[pabstart+1] and $e0=8) then
              vdp[pabstart+8]:=vdp[pabstart+8] or $2;
            if filepos(thefile)>=filesize(thefile) then
              vdp[pabstart+8]:=vdp[pabstart+8] or $1;
          end;
      end;
    end;
    if not error then
    BEGIN
      inc(cpuw^[wp+11],2);
      CPU^[$837D]:=CPU^[$837D] AND NOT $20;
    END;
    ip:=cpuw^[wp+11]shr 1;
  end;


procedure handledevice(on:byte);
{
$4000 for disk:

4000 AA02 0000 4044
4006 0000 404A 4010
400C 0000 0000 4016
4012 5B38 0110 401C
4018 5B48 0111 4022
401E 5B52 0112 4028
4024 5BAC 0113 402E
402A 5C78 0114 4034
4030 5CE8 0115 403A
4036 5DAE 0116 0000
403C 5D5A 0546 494C ...FIL
4042 4553 0000 4070 ES....
4048 0000 4052 504E
404E 0344 534B 405C .DSK..
4054 505C 0444 534B ...DSK
405A 3100 4066 5062 1.....
4060 0444 534B 3200 .DSK2.
4066 0000 5068 0444 .....D
406C 534B 3300 C1CB SK3...

}
CONST
  dskdsr:array[0..55] of word=
  ($AA02,$0000,$4044,
$0000,$404A,$4010,
$0000,$0000,$4016,
$5B38,$0110,$401C,
$5B48,$0111,$4022,
$5B52,$0112,$4028,
$5BAC,$0113,$402E,
$5C78,$0114,$4034,
$5CE8,$0115,$403A,
$5DAE,$0116,$0000,
$5D5A,$0546,$494C,
$4553,$0000,$4070,
$0000,$4052,$504E,
$0344,$534B,$405C,
$505C,$0444,$534B,
$3100,$4066,$5062,
$0444,$534B,$3200,
$0000,$5068,$0444,
$534B,$3300);



  begin
    if on=1 then
      case cpuw^[wp+12] of
        $1100..$11fe :   { disk! }
        begin
          move(dskdsr,cpu^[$4000],56*2);
{          for t1:=$4000 shr 1 to ($4000+59*2) shr 1 do
            cpuw^[t1]:=swap(cpuw^[t1]);}
        end;
      end
    else
      for t1:=$4000 shr 1 to $5fff shr 1 do
        cpuw^[t1]:=0;
  end;




procedure dointerrupt;
begin  { executes interrupt CURINT }
  if curint>0 then
  begin
    inting:=true;
    t1:=cpuw^[curint*2] shr 1;
    t2:=cpuw^[curint*2+1] shr 1;
    cpuw^[t1+13]:=wp shl 1;
    cpuw^[t1+14]:=ip shl 1;
    cpuw^[t1+15]:=st;
    wp:=t1;
    ip:=t2;
    intcount:=1;
  end
  else
    inting:=false;
end;




function decimal(st:string):word;
  var
    t:word;
    h:string[16];
    p:integer;
  begin
    h:='0123456789ABCDEF';
    t:=0;
    for p:=1 to length(st) do
      t:=t*16+pos(upcase(st[p]),h)-1;
    decimal:=t;
  end;

procedure addbreak(op1:string);
var
  cur,old:breakptr;
begin
          cur:=break;
          if cur=nil then
	  begin
            new(cur);
            break:=cur;
            cur^.next:=nil;
          end;
          while cur^.next<>nil do
            cur:=cur^.next;
	  old:=cur;
          if cur<>break then
            new(cur);
          old^.next:=cur;
          cur^.where:=decimal(op1)shr 1;
	  cur^.next:=nil;
end;



function search(a:word):breakptr;
  var
    o,cur:breakptr;
    f:boolean;
  begin
    cur:=break;
    f:=false;
    while (cur<>nil) and not f do
    begin
      f:=cur^.where=ip;
      o:=cur;
      cur:=cur^.next;
    end;
    if f then
      search:=o
    else
      search:=nil;
  end;



procedure drawscreen;
  function swap(a:byte):byte;
  begin
    swap:=(a and 15)*16+a div 16;
  end;
  var
    a,col:word;
    va:integer;
    c:byte;
    t:array[0..31] of byte;
    sa:byte;
  begin
    if true then
    begin
    if cols<>40 then
      a:=8
    else
      a:=0;
    col:=0;
    for c:=0 to 31 do
    begin
      t[c]:=swap(vdp[colors+c]);
      if t[c] and 15=0 then
        t[c]:=t[c] or (attribute and 15);
      if t[c] and 240=0 then
        t[c]:=t[c] or (attribute shl 4);
    end;
    sa:=swap(attribute);
    for va:=0 to cols*24-1 do
    begin
      c:=vdp[screen+va];
      mem[$b800:a]:=c;
      if cols<>40 then
      begin
	mem[$b800:a+1]:=t[c shr 3]
      end
      else
	mem[$b800:a+1]:=sa;
      inc(a,2);
      inc(col);
      if col>=cols then
      begin
	a:=a+(80-cols*2);
	col:=0;
      end;
    end;
    end;
    startchanged:=16383;
    endchanged:=0;
    anychanged:=false;
  end;
procedure setfont(b,e:word);
procedure stfont(b,e:word);  assembler;
asm
  push ds
  push bp

    mov ax,$80
    int 10h


  mov bh,8
  mov bl,0
  mov cx,e
  sub cx,b
  mov dx,b
  push ds
  pop es
  mov ax,b
  shl ax,1
  shl ax,1
  shl ax,1
  add ax,offset vdp
  add ax,patterns

  mov bp,ax

  mov ax,$1100
  int 10h

    mov ah,$1
    mov cx,$2000
    int 10h

    mov ax,$1003
    mov bl,0
    int 10h

    mov ax,$1013
    mov bx,$100
    int 10h

    mov ax,$1012
    mov bx,0
    mov cx,16
    push ds
    pop es
    mov dx,offset pal
    int 10h

    mov dx,$3d4
    mov ax,$a709
    out dx,ax

  pop bp
  pop ds
end;
begin
  stfont(b,e);
end;
function getword:word;
  var
    t:word;
  begin
    t:=(cpuw^[ip]);
    inc(ip);
    if ip>=32768 then ip:=0;
    getword:=t;
  end;

{ interpret instructions }

procedure doit;
  var
    sw,dw,c:word;  { dest, source, wr, shiftcount }
    b:boolean;   { byte operation? }
    ts,td:byte;
    o:word;      { opcode }
    res,too:word;
    muldiv:longint;
  procedure makets;
  begin
    if not b then      { WORD }
    case ts of
      0 : s:=(wp+s);
      1 : s:=(cpuw^[wp+s]) shr 1;    { *Rx = ADDRESS in bytes. }
      2 :
	begin
	  t1:=getword;
	  if s and 15<>0 then
	    s:=(cpuw^[wp+s]+t1) shr 1
	  else
	    s:=t1 shr 1;
	end;
      3 :
	begin
	  t1:=s;                      { t1= register }
	  s:=(cpuw^[wp+s]) shr 1;
	  inc(cpuw^[wp+t1],2);
	end;
    end
    else
    case ts of
      0 : s:=(wp+s) shl 1;
      1 : s:=(cpuw^[(wp+s)]);
      2 :
	begin
	  if s and 15<>0 then
	    s:=(cpuw^[wp+s]+getword)
	  else
	    s:=getword;
	end;
      3 :
	begin
	  t1:=s;
	  s:=(cpuw^[(wp+s)]);
	  inc(cpuw^[(wp+t1)]);
	end;
    end;
  end;

  procedure maketd;
  begin
    if not b then      { WORD }
    case td of
      0 : d:=(wp+d);
      1 : d:=(cpuw^[wp+d]) shr 1;    { *Rx = ADDRESS in bytes. }
      2 :
	begin
	  t1:=getword;
          if d and 15<>0 then
            d:=(cpuw^[wp+d]+t1) shr 1
          else
            d:=t1 shr 1;
        end;
      3 :
        begin
          t1:=d;                      { t1= register }
          d:=(cpuw^[wp+d]) shr 1;
          inc(cpuw^[wp+t1],2);
        end;
    end
    else
    case td of
      0 : d:=(wp+d) shl 1;
      1 : d:=(cpuw^[(wp+d)]);
      2 :
        begin
          if d and 15<>0 then
            d:=(cpuw^[wp+d]+getword)
          else
	    d:=getword;
        end;
      3 :
        begin
          t1:=d;
          d:=(cpuw^[(wp+d)]);
	  inc(cpuw^[(wp+t1)]);
        end;
    end;
{    if B then
      if ((d>=0) and (d<$2000)) or ((d>=$4000) and (d<$8000)) then
        d:=d
      else
    else
      if ((d>=0) and (d<$1000)) or ((d>=$2000) and (d<$4000)) then
        d:=d;}
  end;

  procedure compare(mask,res,too:word);  assembler;
  asm
    mov cx,&st     { to be the result }
    mov dx,mask
    not dx
    and cx,dx
    not dx
    mov ax,too
    mov bx,res
    test dx,$2000
    je  @t1
    cmp bx,ax
    jnz @t1
    or cx,$2000
  @t1:
    test dx,$8000
    je @t2
    cmp bx,ax
    jbe @t2
    or cx,$8000
  @t2:
    test dx,$4000
    je @t3
    cmp bx,ax
    jle @t3
    or cx,$4000
  @t3:
    test dx,$1000
    je @t4
    cmp bx,ax
    jnc @t4
    or cx,$1000
  @t4:
    test dx,$800
    je @t5
    cmp bx,ax
    jno @t5
    or cx,$800
  @t5:
    test dx,$200
    je @t7
    cmp xop,false
    je  @t7
    or cx,$200
  @t7:
    mov &st,cx

  end;

  procedure comparebytes(mask:word;res,too:byte);  assembler;
  asm
    mov cx,&st     { to be the result }
    mov dx,mask
    not dx
    and cx,dx
    not dx
    mov ah,too
    mov bh,res
    test dx,$2000
    je  @t1
    cmp bh,ah
    jnz @t1
    or cx,$2000
  @t1:
    test dx,$8000
    je @t2
    cmp bh,ah
    jbe @t2
    or cx,$8000
  @t2:
    test dx,$4000
    je @t3
    cmp bh,ah
    jle @t3
    or cx,$4000
  @t3:
    test dx,$1000
    je @t4
    cmp bh,ah
    jnc @t4
    or cx,$1000
  @t4:
    test dx,$800
    je @t5
    cmp bh,ah
    jno @t5
    or cx,$800
  @t5:
    test dx,$400
    je @t6
    cmp bh,ah
    jpe @t6
    or cx,$400
  @t6:
    test dx,$200
    je @t7
    cmp xop,false
    je  @t7
    or cx,$200
  @t7:
    mov &st,cx

  end;


  procedure setbyte(addr:word; x:byte);
  var
    vreg:byte;
  begin
    if ((addr>=$2000) and (addr<$4000)) or ((addr>=$a000) and (addr<=$ffff))
    then
      cpu^[addr and $fffe+1-addr and 1]:=x
    else
    case addr of
      $8000..$83ff :
	 cpu^[addr and $fe+1-addr and 1+$8300]:=x;
      $8c00..$8fff :   { set VDP byte }
        if addr shr 1 and 1=0 then
	begin
	  vaddr:=vaddr and $3fff;
	  vdp[vaddr]:=x;
	  anychanged:=true;
	  if (vaddr>=patterns) and (vaddr<patterns+2048) then
            firstchar:=$3fff
	  else
	    if (vaddr>=screen) and (vaddr<screen+cols*24) then
(*	      if vaddr>startchanged then
		if vaddr>endchanged then
		  endchanged:=vaddr
		else
	      else
		startchanged:=vaddr*)
              anychanged:=true
	    else
	      if (vaddr>=colors) and (vaddr<colors+32) then
	      begin
		drawscreen;
	      end;

	  inc(vaddr);
	end
        else
(*      $8c02..$8fff :*)
	begin
	  if NOT vwrt then
	    vaddr:=x
	  else
	  begin
	    vaddr:=vaddr or (x shl 8);
	    if vaddr and $f000=$8000 then   { set VDP reg }
	    begin
	      vreg:=(vaddr and $700)shr 8;
	      x:=vaddr and $ff;
	      vr[vreg]:=vaddr and $ff;
	      case vreg of
		$0 : ;
		$1 :
		  begin
		    if x>=$80 then;
		    x:=x shl 1;
		    if x>=$80 then
		      blank:=false
		    else
		      blank:=true;
		    x:=x shl 1;
		    if x>=$80 then;   { interrupt disable/enable }
		    x:=x shl 1;
		    if x>=$80 then
		      cols:=40
		    else
		      cols:=32;
		    startchanged:=0;
		    endchanged:=767;
		    anychanged:=true;
		    drawscreen;
		    x:=x shl 1;
		    if x>=$80 then;   { multicolor mode }
		  end;
		$2 :
		  begin
		    screen:=(word(x)*$400) and $3fff;
		    startchanged:=0;
		    endchanged:=767;
		    anychanged:=true;
		    drawscreen;
		  end;
		$3 :
		  begin
		    colors:=(word(x)*$40) and $3fff;
		    startchanged:=0;
		    endchanged:=767;
		    anychanged:=true;
		    drawscreen;
		  end;
		$4 :
		  begin
		    patterns:=(word(x)*$800) and $3fff;
		    startchanged:=0;
		    endchanged:=767;
		    anychanged:=true;
		    setfont(0,256);
		  end;
		$5 : ;
		$6 : ;
		$7 :
		  begin
		    attribute:=x;
		    startchanged:=0;
		    endchanged:=767;
		    anychanged:=true;
		    drawscreen;
		  end;
	      end;
	    end
	    else
	      if vaddr>=$4000 then   { if so, enable writing }
	      begin
	      end;
	    vaddr:=vaddr and $3fff;  { don't write past end of array }
	  end;

	  vwrt:=not vwrt;
	end;
      $9c00..$9fff :
        if addr shr 1 and 1=0 then
	begin
	  if gaddr<=gpllim then
	    gpl^[gaddr]:=x;
	  inc(gaddr);
	  grd:=false;
	  gwrt:=false;
	end
        else
(*
      $9c02..$9fff :*)
	begin
	  if gwrt then
	  begin
	    gaddr:=gaddr or x
	  end
	  else
	    gaddr:=x shl 8;
	  gwrt:=not gwrt;
	  grd:=false;
	end;
      $8400..$87ff :
	begin
	  if x=$9f then
	  begin
	    nosound;
	    soundvol:=false;
	    soundread:=false;
	  end
	  else
	    if x and $f0=$90 then
	    begin
	    {  sound(soundvalue);}
	      soundread:=false;
	    end
	    else
	      if (x and $f0=$80) or soundread then
	      begin
		if not soundread then
		  soundvalue:=x and $f
		else
		begin
		  soundvalue:=longint(111860 div (x shl 4+soundvalue));
		  if soundvol then
		    {sound(soundvalue);}
		end;
		soundread:=not soundread;
	      end
	      else
		soundread:=false;

	end;

      $8800..$8bff :   { what happens if WRITING to read ports? }
	begin
	end;
      $9000..$97ff : ;   { speech }
      $9800..$9bff : runerror;   { reading }
    end;

  end;

  procedure setword(addr:word; x:word);
  var
    vreg:byte;
  begin
    if ((addr shl 1>=$2000) and (addr shl 1<$4000)) or
       ((addr shl 1>=$a000) and (addr shl 1<=$ffff))
    then
      cpuw^[addr]:=x
    else
    case addr shl 1 of
      $8000..$83ff :
	cpuw^[addr AND $7F+$4180]:=x;
    else
      setbyte(addr,x shr 8);
    end;

  end;

  function readbyte(addr:word):byte;
  var
    t:byte;
  begin
    if (addr<$8000) or (addr>=$a000) then
      readbyte:=cpu^[addr and $fffe+(1-addr and 1)]
    else
      case addr of
	$9800..$9bff :
	  begin
	    IF ADDR SHR 1 AND 1=1 THEN
	    BEGIN
	    if grd then
	    begin
	      readbyte:=(gaddr+1) and $ff;
	      inc(gaddr);
	    end
	    else
	      readbyte:=(gaddr+1) shr 8;
	    grd:=not grd
	    END
	    ELSE
	    BEGIN
	    t:=gpl^[gaddr];
	    inc(gaddr);
	    readbyte:=t;
	    grd:=false;
	    gwrt:=false;
	    END;
	  end;
	$8000..$83ff :
	  readbyte:=cpu^[addr and $fe+1-addr and 1+$8300];

	$8800..$8bff :
	  begin
	    IF ADDR SHR 1 AND 1=1 THEN
	    if (seconds100>=cur60) then
	      readbyte:=$80
	    else
	      readbyte:=0
	    ELSE
	    BEGIN
	    t:=vdp[vaddr and $3fff];
	    inc(vaddr);
	    vaddr:=vaddr and $3fff;
	    readbyte:=t;
	    vwrt:=false;
	    END;
	  end;
{        $8802 :
	  begin
	  end;}
{        $9802 :
	  begin
	  end;}
	end;

  end;

  function readword(addr:word):word;
  var
    t:word;
  begin
    if (addr shl 1<$8000) or (addr shl 1>=$a000) then
      readword:=cpuw^[addr]
    else
      case addr shl 1 of
	$8000..$83ff :
	  readword:=cpuw^[addr and $7F+$4180];
      else
	readword:=readbyte(addr);
      end;
  end;

  begin
    { first, figure big area of instruction }
    { WAIT:

      Steps in doing this here thang: (no order)

      (a) get the operands, mark them with variables
      (b) figure the instruction
      (c) execute instruction, making sure VDP,GPL are obeyed

    }
    { THIS WILL GET THE OPERAND LIMITS }
    ts:=0; td:=0;
    b:=false;
    case in1 of
      $4000..$ffff:  { 1 = general, 2 args }
      begin
	o:=in1 and $f000;
	b:=(in1 and $1000)<>0;
	td:=(in1 and $0c00)shr 10;
	d:=(in1 and $3c0)shr 6;
	ts:=(in1 and $30) shr 4;
	s:=in1 and 15;
	makets;
	maketd;
	case o of
	  $4000 :
	    begin
	      cpuw^[d]:=not readword(s) and cpuw^[d];
              compare($e000,cpuw^[d],0);
            end;
          $5000 :
            begin
              d:=d and $fffe+1-d and 1;
              cpu^[d]:=not readbyte(s) and cpu^[d];
              comparebytes($e400,cpu^[d],0);
            end;
          $6000 :
            begin
              st:=st and not $f800;
              dec(cpuw^[d],readword(s));
              asm
                pushf
                jnc @ohwell
		or &st,$1000
              @ohwell:
                popf
                jno @owell
                or &st,$800
              @owell:
              end;
              compare($e000,cpuw^[d],0);
            end;
          $7000 :
            begin
              st:=st and not $fc00;
              d:=d and $fffe+1-d and 1;
              dec(cpu^[d],readbyte(s));
              asm
                pushf
		jnc @ohwell1
                or &st,$1000
              @ohwell1:
                popf
                jno @owell1
                or &st,$800
              @owell1:
              end;
              comparebytes($e400,cpu^[d],0);
            end;
          $8000 :
            begin
              compare($e000,readword(s),readword(d));
            end;
          $9000 :
            begin
	      comparebytes($e400,readbyte(s),readbyte(d));
            end;
          $a000 :
            begin
              st:=st and not $f800;
              inc(cpuw^[d],readword(s));
              asm
                pushf
                jnc @ohwell2
                or &st,$1000
              @ohwell2:
                popf
                jno @owell2
                or &st,$800
              @owell2:
              end;
	      compare($e000,cpuw^[d],0);
            end;
          $b000 :
            begin
              st:=st and not $fc00;
              d:=d and $fffe +1-d and 1;
              inc(cpu^[d],readbyte(s));
              asm
                pushf
                jnc @ohwell3
                or &st,$1000
              @ohwell3:
                popf
                jno @owell3
                or &st,$800
              @owell3:
	      end;
              comparebytes($e400,cpu^[d],0);
            end;
          $c000 :
            begin
              setword(d,readword(s));
              compare($e000,cpuw^[d],0);
            end;
          $d000 :
            begin
              setbyte(d,readbyte(s));
              comparebytes($f000,cpu^[d and $fffe+1-d and 1],0);
            end;
          $e000 :
            begin
              cpuw^[d]:=cpuw^[d] or readword(S);
	      compare($e000,cpuw^[d],0);
            end;
          $f000 :
            begin
              d:=d and $fffe+1-d and 1;
              cpu^[d]:=cpu^[d] or readbyte(s);
              comparebytes($e400,cpu^[d],0);
            end;
        end;


      end;
      $400..$7ff :   { 6 = general, one arg }
      begin
	o:=in1 and $ffc0;
	ts:=(in1 and $30) shr 4;
	s:=in1 and 15;
	makets;

	case o of
	  $400 :
	    begin   { blwp }
	      t1:=ip shl 1;
	      t2:=wp shl 1;
	      wp:=cpuw^[s] shr 1;
	      ip:=cpuw^[s+1] shr 1;
	      cpuw^[wp+13]:=t2;
	      cpuw^[wp+14]:=t1;
	      cpuw^[wp+15]:=st;
              inc(intcount);
	    end;
	  $440 :
	    ip:=s;
	  $480 :  { execute }
	    begin
	      sip:=ip;
	      ip:=s;
	      in1:=cpuw^[s];
	      doit;
	      ip:=sip;
	    end;
	  $4c0 :
	    cpuw^[s]:=0;
	  $500 :
	    begin
	      st:=st and not $e800;
	      t1:=cpuw^[s];
	      cpuw^[s]:=-t1;
	      if t1=0 then
		st:=st or $800;
	      compare($e000,cpuw^[s],0);
	    end;
	  $540 :
	    begin
	      cpuw^[s]:=not cpuw^[s];
	      compare($e000,cpuw^[s],0);
	    end;
	  $580 :
	    begin
	      st:=st and not $f800;
	      inc(cpuw^[s]);
	      asm
		pushf
		jno @noov
		or &st,$800
	      @noov:
		popf
		jnc @noovv
		or &st,$1000
	      @noovv:
	      end;
	      compare($e000,cpuw^[s],0);
	    end;
	  $5c0 :
	    begin
	      st:=st and not $f800;
	      inc(cpuw^[s],2);
	      asm
		pushf
		jno @noov2
		or &st,$800
	      @noov2:
		popf
		jnc @noovv2
		or &st,$1000
	      @noovv2:
	      end;
	      compare($e000,cpuw^[s],0);
	    end;
	  $600 :
	    begin
	      st:=st and not $f800;
	      t1:=cpuw^[s];
	      if t1=0 then
		st:=st and not $1000 or $800
	      else
		st:=st or $1000;
	      dec(cpuw^[s]);
	      compare($e000,cpuw^[s],0);
	    end;
	  $640 :
	    begin
	      st:=st and not $f800;
	      dec(cpuw^[s],2);
	      asm
		pushf
		jno @noov3
		or &st,$800
	      @noov3:
		popf
		jnc @noovv3
		or &st,$1000
	      @noovv3:
	      end;
	      compare($e000,cpuw^[s],0);
	    end;
	  $680 :
	    begin
	      cpuw^[wp+11]:=ip shl 1;
	      ip:=s;
	    end;
	  $6c0 :
	    begin
	      cpuw^[s]:=swap(cpuw^[s]);
	    end;
	  $700 :
	    begin
	      cpuw^[s]:=$ffff;
	    end;
	  $740 :
	    begin
	      st:=st and not $e800;
	      t1:=cpuw^[s];
	      cpuw^[s]:=abs(INTEGER(t1));
	      if t1=$8000 then
		st:=st or $800;
	      compare($e000,cpuw^[s],0);
	    end;
	end;

      end;
      $800..$bff :   { 5 = shifts }
      begin
	o:=in1 and $ff00;
	c:=(in1 and $f0) shr 4;
	if c=0 then
	  c:=cpuw^[wp];
	if c=0 then
	  c:=16;
	s:=in1 and 15;
	makets;
	case o of
	  $800 :
	    begin
	      st:=st and not $f000;
	      t1:=cpuw^[s];
	      asm
		mov ax,t1
		mov cx,c
		sar ax,cl
		jnc @out0
		or &st,$1000
	      @out0:
		mov t1,ax
	      end;
	      cpuw^[s]:=t1;
	      compare($e000,t1,0);
	    end;
	  $900 :
	    begin
	      st:=st and not $f000;
	      t1:=cpuw^[s];
	      asm
		mov ax,t1
		mov cx,c
		shr ax,cl
		jnc @out1
		or &st,$1000
	      @out1:
		mov t1,ax
	      end;
	      cpuw^[s]:=t1;
	      compare($e000,t1,0);
	    end;
	  $a00 :
	    begin
	      st:=st and not $f800;
	      t1:=cpuw^[s];
	      asm
		mov ax,t1
		mov cx,c
		sal ax,cl
		pushf
		jnc @out2
		or &st,$1000
	      @out2:
		popf
		jno @out23
		or &st,$800
	      @out23:
		mov bx,ax
		xor bx,t1
		test bx,$8000
		je @out22
		or &st,$800
	      @out22:
		mov t1,ax
	      end;
	      cpuw^[s]:=t1;
	      compare($e000,t1,0);
	    end;
	  $b00 :
	    begin
	      st:=st and not $f000;
	      t1:=cpuw^[s];
	      asm
		mov ax,t1
		mov cx,c
		ror ax,cl
		jnc @out3
		or &st,$1000
	      @out3:
		mov t1,ax
	      end;
	      cpuw^[s]:=t1;
	      compare($e000,t1,0);
	    end;
	end;
      end;
      $200..$33f :   { 8 = immediates }
      begin
	o:=in1 and $ffe0;
	d:=in1 and 15;
{        s:=getword;}
	maketd;
	case o of
	  $200 : cpuw^[d]:=GETWORD;
	  $220 : begin
		   st:=st and not $f800;
		   inc(cpuw^[d],getword);
		   asm
		     pushf
		     jnc @noc
		     or &st,$1000
		   @noc:
		     popf
		     jno @noo
		     or &st,$800
		   @noo:
		   end;
		 end;
	  $240 : cpuw^[d]:=cpuw^[d] and getword;
	  $260 : cpuw^[d]:=cpuw^[d] or getword;
	  $280 :
	    too:=getword;
	  $2a0 : cpuw^[d]:=wp*2;
	  $2c0 : cpuw^[d]:=st;
	  $2e0 : wp:=getword shr 1;
	  $300 : begin  st:=(st and $fff0) or (getword and 15);  inting:=false;  end;
	end;
	if o<=$280 then
	begin
	  if o<>$280 then
	    too:=0;
	  res:=cpuw^[d];
	  compare($e000,res,too);
	end;
      end;
      $340..$3ff :   { 7 = control }
      begin
	o:=in1 and $ffe0;
	case o of
	  $380 :   { rtwp }
	    begin
	      ip:=cpuw^[wp+14] shr 1;
	      st:=cpuw^[wp+15];
	      wp:=cpuw^[wp+13] shr 1;
              dec(intcount);
              if intcount=0 then
              begin
                inting:=false;
                dec(curint);
                dointerrupt;
              end;
	    end;
	end;
	{ no operands }
      end;
      $1000..$1fff:  { 2 = jumps }
      begin
	o:=in1 and $ff00;
	d:=in1 and 255;
	if d>=128 then
	  d:=d-256;
	case o of
	  $1000 : inc(ip,d);
	  $1100 : if (st and $6000=0) then inc(ip,d);
	  $1200 : if ((st and $8000)=0) or (st and $2000<>0) then inc(ip,d);
	  $1300 : if (st and $2000<>0) then inc(ip,d);
	  $1400 : if (st and $8000<>0) or (st and $2000<>0) then inc(ip,d);
	  $1500 : if (st and $4000<>0) then inc(ip,d);
	  $1600 : if (st and $2000=0) then inc(ip,d);
	  $1700 : if (st and $1000=0) then inc(ip,d);
	  $1800 : if (st and $1000<>0) then inc(ip,d);
	  $1900 : if (st and $800=0) then inc(ip,d);
	  $1a00 : if (st and $8000=0) and (st and $2000=0) then inc(ip,d);
	  $1b00 : if (st and $8000<>0) and (st and $2000=0) then inc(ip,d);
	  $1c00 : if (st and $400<>0) then inc(ip,d);

	  $1d00 : handledevice(1);
	  $1e00 : handledevice(0);
	  $1f00 : st:=st and $dfff;
	end;
      end;
      $2000..$2bff:  { 3 = logical compares }
      begin
	o:=in1 and $fc00;
	d:=(in1 and $03c0)shr 6;
	ts:=(in1 and $30)shr 4;
	s:=(in1 and 15);
	makets;
	maketd;
	t1:=readword(s);
	case o of
	  $2000 : if (t1 and cpuw^[d])=t1 then
		    st:=st or $2000
		  else
		    st:=st and not $2000;
	  $2400 : if (t1 and not cpuw^[d])=t1 then
		    st:=st or $2000
		  else
		    st:=st and not $2000;
	  $2800 :
	    begin
	      cpuw^[d]:=cpuw^[d] xor t1;
	      compare($e000,cpuw^[d],0);
	    end;
	end;
      end;
      $2c00..$2fff:  { 9 = XOP }
      begin
	o:=in1 and $f800;
	d:=(in1 and $3c0) shr 6;
	ts:=(in1 and $30) shr 4;
	s:=in1 and 15;
	makets;
	maketd;
	case o of
	  $2c00 :
	    begin
	      t1:=ip;
	      t2:=wp;
	      wp:=cpuw^[$22+d*2];
	      ip:=cpuw^[$22+d*2+1];
	      cpuw^[wp+11]:=s;
	      cpuw^[wp+13]:=t2 shl 1;
	      cpuw^[wp+14]:=t1 shl 1;
	      st:=st or $200;
	      cpuw^[wp+15]:=st;
	    end;
	end;
      end;
      $3000..$37ff:  { 4 = CRU }
      begin
	o:=in1 and $fc00;
	c:=(in1 and $3c0) shr 6;
	if c=0 then c:=16;
	ts:=(in1 and $30) shr 4;
	s:=in1 and 15;
	makets;
	{ do nothing with this ... }
	case o of
	  $3000 :          { load CRU }
	    begin
	      if not b then
		compare($e400,0,0)
	      else
		comparebytes($e400,0,0);
	    end;
	  $3400 :
	    begin
	      cpuw^[s]:=cpuw^[s] or not ($ffff shr c);
	      if not b then
		compare($e400,0,0)
	      else
		compare($e400,0,0);
	    end;
	end;
      end;
      $3800..$3fff:  { 9 = mul/div }
      begin
	o:=in1 and $fc00;
	d:=(in1 and $3c0) shr 6;
	ts:=(in1 and $30) shr 4;
	s:=in1 and 15;
	makets;
	maketd;
	case o of
	  $3800 :
	    begin
	      muldiv:=longint(readword(S)*cpuw^[d]);
	      cpuw^[d]:=muldiv div 65536;
	      cpuw^[d+1]:=muldiv;
	    end;
	  $3c00 :
	    begin
	      st:=st and not $800;
	      muldiv:=longint(cpuw^[d])*65536+cpuw^[d+1];
	      t1:=readword(S);
	      if (t1<=cpuw^[d]) then
		st:=st or $800
	      else
	      begin
		cpuw^[d]:=muldiv div t1;
		cpuw^[d+1]:=muldiv mod t1;
	      end;
	    end;
	end;
      end;
    end;
  end;

{ disassemble instructions }
{ *********************************************************************** }
{ *********************************************************************** }
{ *********************************************************************** }
{ *********************************************************************** }
{ *********************************************************************** }
  procedure dstart(d:integer);  assembler;
  asm
    push si
    cli
    mov ax,0
    mov bx,d
    mov cx,8
    div cl
    mov cl,ah
    xor ah,ah
    xchg bx,ax
    mov si,80
    mul si
    add bx,ax

    mov dx,$3d4
    mov ah,bh
    mov al,$c
    cli
    out dx,ax
    mov ah,bl
    inc al
    out dx,ax
    sti
    sti
    pop si
  end;


procedure disassemble;
  var
    sw,dw,c:word;  { dest, source, wr, shiftcount }
    b:boolean;   { byte operation? }
    ts,td:byte;
    o:word;      { opcode }
    res,too:word;
    muldiv:longint;

    instruction:string;
    op1,op2,op:string[32];

    orgip:word;
    ch:char;
  function strg(a:word):string;
  var
    tmp:string[4];
  begin
    str(a,tmp);
    strg:=tmp;
  end;
  function hex(A:word):string;
  const
    nums:string[16]='0123456789ABCDEF';
  var
    dig:byte;
    tmp:string[5];
    x:byte;
  begin
    tmp:='';
    for x:=1 to 4 do
    begin
      tmp:=nums[a mod 16+1]+tmp;
      a:=a shr 4;
    end;
    hex:=tmp;
  end;

  procedure makets;
  begin
    if not b then      { WORD }
    case ts of
      0 : s:=(wp+s);
      1 : s:=(cpuw^[wp+s]) shr 1;    { *Rx = ADDRESS in bytes. }
      2 :
	begin
	  if s and 15<>0 then
	    s:=(cpuw^[wp+s]+getword) shr 1
	  else
	    s:=getword shr 1;
	end;
      3 :
	begin
	  t1:=s;                      { t1= register }
	  s:=(cpuw^[wp+s]) shr 1;
{          inc(cpuw^[wp+t1],2);}
	end;
    end
    else
    case ts of
      0 : s:=(wp+s) shl 1;
      1 : s:=(cpuw^[(wp+s)]);
      2 :
        begin
          if s and 15<>0 then
	    s:=(cpuw^[wp+s]+getword)
	  else
            s:=getword;
        end;
      3 :
        begin
          t1:=s;
          s:=(cpuw^[(wp+s)]);
{          inc(cpuw^[(wp+t1)]);}
        end;
    end;
  end;

  procedure maketd;
  begin
    if not b then      { WORD }
    case td of
      0 : d:=(wp+d);
      1 : d:=(cpuw^[wp+d]) shr 1;    { *Rx = ADDRESS in bytes. }
      2 :
        begin
          if d and 15<>0 then
            d:=(cpuw^[wp+d]+getword) shr 1
	  else
            d:=getword shr 1;
        end;
      3 :
        begin
          t1:=d;                      { t1= register }
          d:=(cpuw^[wp+d]) shr 1;
{          inc(cpuw^[wp+t1],2);}
        end;
    end
    else
    case td of
      0 : d:=(wp+d) shl 1;
      1 : d:=(cpuw^[(wp+d)]);
      2 :
        begin
          if d and 15<>0 then
            d:=(cpuw^[wp+d]+getword)
	  else
            d:=getword;
        end;
      3 :
        begin
	  t1:=d;
          d:=(cpuw^[(wp+d)]);
{          inc(cpuw^[(wp+t1)]);}
	end;
    end;
  end;

{  procedure makets;
  begin
    if not b then
    case ts of
      0 : s:=(wp+s);
      1 : s:=(cpuw^[wp+s]);
      2 :
        begin
          if s and 15<>0 then
            s:=cpuw^[wp+s]+getword
          else
	    s:=getword;
	end;
      3 :
        begin
          t1:=s;
	  s:=(cpuw^[wp+s]);
          inc(cpuw^[wp+t1],2);
        end;
    end
    else
    case ts of
      0 : s:=(wp+s) shl 1;
      1 : s:=(cpuw^[(wp+s)]);
      2 :
        begin
          if s and 15<>0 then
            s:=cpuw^[wp+s]+getword
	  else
            s:=getword;
        end;
      3 :
        begin
          t1:=s;
	  s:=(cpuw^[(wp+s)])shl 1;
          inc(cpuw^[(wp+t1)]);
        end;
    end;
  end;
  procedure maketd;
  begin
    if not b then
    case td of
      0 : d:=(wp+d);
      1 : d:=(cpuw^[wp+d]);
      2 :
        begin
          if d and 15<>0 then
            d:=cpuw^[wp+d]+getword
          else
            d:=getword;
        end;
      3 :
        begin
          t1:=d;
          d:=(cpuw^[wp+d]) shl 1;
          inc(cpuw^[wp+t1],2);
	end;
    end
    else
    case td of
      0 : d:=(wp+d)shl 1;
      1 : d:=(cpuw^[(wp+d)]);
      2 :
        begin
          if d and 15<>0 then
            d:=cpuw^[wp+d]+getword
          else
            d:=getword;
        end;
      3 :
        begin
          t1:=d;
          d:=(cpuw^[(wp+d)])shl 1;
          inc(cpuw^[(wp+t1)]);
        end;
    end;
  end;
 }


  function readbyte(addr:word):byte;
  var
    t:byte;
  begin
    if (addr<$8000) or (addr>=$a000) then
      readbyte:=cpu^[addr and $fffe+(1-addr and 1)]
    else
      case addr of
        $8300..$83ff :
          readbyte:=cpu^[addr and $fffe+1-addr and 1];
        $8000..$80ff :
	  readbyte:=cpu^[addr and $fffe+1-addr and 1+$300];
        $8100..$81ff :
          readbyte:=cpu^[addr and $fffe+1-addr and 1+$200];
        $8200..$82ff :
          readbyte:=cpu^[addr and $fffe+1-addr and 1+$100];

        $8800..$8801 :
          begin
{            vaddr:=vaddr and $3fff;}
            t:=vdp[vaddr];
{            inc(vaddr);}
	    vaddr:=vaddr and $3fff;
            readbyte:=t;
{            vwrt:=false;}
          end;
        $8802 :
	  begin
            if (seconds100>=cur60) then
              readbyte:=$80
            else
              readbyte:=0;
          end;
        $9800..$9801 :
          begin
            t:=gpl^[gaddr];
{            inc(gaddr);}
            readbyte:=t;
          end;
        $9802 :
          begin
            if grd then
            begin
	      readbyte:=(gaddr+1) and $ff;
{              inc(gaddr);}
	    end
            else
              readbyte:=(gaddr+1) shr 8;
{            grd:=not grd;}
          end;
        end;

  end;

  function readword(addr:word):word;
  var
    t:word;
  begin
    if (addr shl 1<$8000) or (addr shl 1>=$a000) then
      readword:=cpuw^[addr]
    else
      case addr shl 1 of
        $8300..$83ff :
          readword:=cpuw^[addr];
        $8000..$80ff :
          readword:=cpuw^[addr+$300];
        $8100..$81ff :
          readword:=cpuw^[addr+$200];
	$8200..$82ff :
          readword:=cpuw^[addr+$100];

        $8800..$8801 :
          begin
            t:=vdp[vaddr];
{            inc(vaddr,2);}
	    vaddr:=vaddr and $3fff;
            readword:=t;
          end;
        $8802 :
          begin
            if (seconds100>=cur60) then
              readword:=$80
            else
              readword:=0;
          end;
        $9800..$9801 :
          begin
            t:=gpl^[gaddr];
{            inc(gaddr,2);}
            readword:=t;
          end;
	$9802 :
          begin
            if grd then
              readword:=gaddr and $ff
            else
              readword:=gaddr shr 8;
            grd:=not grd;
          end;
        end;


  end;

  var
    old,cur:breakptr;


  begin
    asm
    mov ax,$1013
    mov bx,$100
    int 10h
    end;
    if (d>=$8c00) and (d<=$8c02) then
    begin
      dstart(0);
      if not fast then
        repeat until readkey=#13;
      dstart(12);
    end;
    ts:=0; td:=0;
    orgip:=ip;
    b:=false;
    instruction:='DATA';
    case in1 of
      $200..$33f :   { 8 = immediates }
      begin
	o:=in1 and $ffe0;
        s:=in1 and 15;
{        d:=getword;}
        td:=128;
        case o of
          $200 : begin
            instruction:='LI  ';
          end;
          $220 : begin
            instruction:='AI  ';
          end;
          $240 : begin
            instruction:='ANDI';
          end;
	  $260 : begin
            instruction:='ORI ';
	  end;
          $280 : begin
            instruction:='CI  ';
          end;
          $2a0 : begin
            instruction:='STWP';
            td:=255;
          end;
          $2c0 : begin
            instruction:='STST';
            td:=255;
          end;
          $2e0 : begin
            instruction:='LWPI';
            ts:=128;
            td:=255;
	    s:=d;
          end;
          $300 : begin
            instruction:='LIMI';
            ts:=128;
	    td:=255;
            s:=d;
          end;
        end;
      end;
      $340..$3ff :   { 7 = control }
      begin
        o:=in1 and $ffe0;
        td:=255;
        ts:=255;
        case o of
	  $380 :   { rtwp }
            begin
              instruction:='RTWP';
            end;
        end;
        { no operands }
      end;
      $400..$7ff :   { 6 = general, one arg }
      begin
        o:=in1 and $ffc0;
        ts:=(in1 and $30) shr 4;
        s:=in1 and 15;
	td:=255;
        case o of
          $400 :
            begin   { blwp }
	      instruction:='BLWP';
            end;
          $440 :
            instruction:='B   ';
          $480 :  { execute }
            begin
              instruction:='X   ';
            end;
          $4c0 :
            instruction:='CLR ';
          $500 :
            begin
              instruction:='NEG ';
            end;
          $540 :
            begin
	      instruction:='INV ';
            end;
          $580 :
            begin
              instruction:='INC ';
            end;
          $5c0 :
            begin
              instruction:='INCT';
            end;
          $600 :
            begin
              instruction:='DEC ';
            end;
          $640 :
            begin
	      instruction:='DECT';
            end;
          $680 :
            begin
              instruction:='BL  ';
            end;
          $6c0 :
            begin
              instruction:='SWPB';
            end;
          $700 :
            begin
              instruction:='SETO';
            end;
          $740 :
            begin
	      instruction:='ABS ';
            end;
        end;

      end;
      $800..$bff :   { 5 = shifts }
      begin
        o:=in1 and $ff00;
        c:=(in1 and $f0) shr 4;
        if c=0 then
          c:=cpuw^[wp];
        s:=in1 and 15;
        d:=c;
        td:=129;
        case o of
          $800 :
	    begin
              instruction:='SRA ';
            end;
          $900 :
            begin
              instruction:='SRL ';
            end;
          $a00 :
            begin
              instruction:='SLA ';
            end;
          $b00 :
            begin
              instruction:='SRC ';
            end;
        end;
      end;
      $1000..$1fff:  { 2 = jumps }
      begin
        o:=in1 and $ff00;
        d:=(in1 and 255);
        if d>=128 then d:=d-256;
        d:=d*2+2;
        ts:=160;
        td:=160;
        case o of
          $1000 : instruction:='JMP ';
          $1100 : instruction:='JLT ';
          $1200 : instruction:='JLE ';
          $1300 : instruction:='JEQ ';
          $1400 : instruction:='JHE ';
          $1500 : instruction:='JGT ';
	  $1600 : instruction:='JNE ';
          $1700 : instruction:='JNC ';
          $1800 : instruction:='JOC ';
          $1900 : instruction:='JNO ';
          $1a00 : instruction:='JL  ';
          $1b00 : instruction:='JH  ';
          $1c00 : instruction:='JOP ';

          $1d00 : instruction:='SBO ';
          $1e00 : instruction:='SBZ ';
          $1f00 : instruction:='TB  ';
        end;
      end;
      $2000..$2bff:  { 3 = logical compares }
      begin
        o:=in1 and $fc00;
	d:=(in1 and $03c0)shr 6;
        ts:=(in1 and $30)shr 4;
        s:=(in1 and 15);
        case o of
          $2000 : instruction:='COC ';
          $2400 : instruction:='CZC ';
          $2800 : instruction:='XOR ';
        end;
      end;
      $2c00..$2fff:  { 9 = XOP }
      begin
        o:=in1 and $f800;
        d:=(in1 and $3c0) shr 6;
        ts:=(in1 and $30) shr 4;
        s:=in1 and 15;
        case o of
	  $2c00 :
            begin
              instruction:='XOP ';
            end;
        end;
      end;
      $3000..$37ff:  { 4 = CRU }
      begin
        o:=in1 and $fc00;
        c:=(in1 and $3c0) shr 6;
        if c=0 then c:=16;
        ts:=(in1 and $30) shr 4;
        s:=in1 and 15;
        td:=129;
        { do nothing with this ... }
        case o of
	  $3000 :          { load CRU }
            begin
              instruction:='LDCR';
              d:=c;
              td:=128;
            end;
          $3400 :
            begin
              instruction:='STCR';
              d:=c;
              td:=128;
            end;
        end;
      end;
      $3800..$3fff:  { 9 = mul/div }
      begin
	o:=in1 and $f800;
        d:=(in1 and $3c0) shr 6;
        ts:=(in1 and $30) shr 4;
        s:=in1 and 15;
        case o of
          $3800 :
            begin
              instruction:='MUL ';
            end;
          $3c00 :
            begin
              instruction:='DIV ';
            end;
        end;
      end;
      $4000..$ffff:  { 1 = general, 2 args }
      begin
        o:=in1 and $f000;
        b:=(in1 and $1000)<>0;
        td:=(in1 and $0c00)shr 10;
        d:=(in1 and $3c0)shr 6;
        ts:=(in1 and $30) shr 4;
        s:=in1 and 15;
        case o of
          $4000 :
            begin
              instruction:='SZC ';

            end;
          $5000 :
            begin
              instruction:='SZCB';
	    end;
          $6000 :
            begin
              instruction:='S   ';
            end;
          $7000 :
            begin
              instruction:='SB  ';
            end;
          $8000 :
            begin
              instruction:='C   ';
            end;
          $9000 :
            begin
              instruction:='CB  ';
	    end;
          $a000 :
            begin
              instruction:='A   ';
            end;
          $b000 :
            begin
              instruction:='AB  ';
            end;
          $c000 :
            begin
              instruction:='MOV ';
            end;
          $d000 :
            begin
              instruction:='MOVB';
	    end;
          $e000 :
            begin
              instruction:='SOC ';
            end;
          $f000 :
            begin
              instruction:='SOCB';
            end;
        end;


      end;
    end;
    if instruction='DATA' then
    begin
      ts:=128;
      s:=in1;
      td:=255;
    end;
    textcolor(15);
    textbackground(8);
    op1:='';
    { instruction has been made. }
    case ts of
      0 : op1:='R'+strg(s);
      1 : op1:='*R'+strg(s);
      2 : if s<>0 then op1:='@>'+hex(getword)+'(R'+strg(s)+')' else op1:='@>'+hex(getword);
      3 : op1:='*R'+strg(s)+'+';
      128: op1:='>'+hex(getword);
      129: op1:='>'+hex(d);
      160: op1:='$+';
    end;
    instruction:=instruction+' '+op1+',';
    if (o>=$1000) and (o<$2000) then
      dec(instruction[0]);
    op2:='';
    case td of
      0 : op2:='R'+strg(d);
      1 : op2:='*R'+strg(d);
      2 : if d<>0 then op2:='@>'+hex(getword)+'(R'+strg(d)+')' else op2:='@>'+hex(getword);
      3 : op2:='*R'+strg(d)+'+';
      128: op2:='>'+hex(getword);
      129: op2:='>'+hex(d);
      160: op2:='>'+hex(d);
    else
      dec(instruction[0]);
    end;
    instruction:=instruction+op2;
    window(1,25,40,29);
    gotoxy(1,5);
    write(hex((orgip-1)*2),'=');
    for t1:=orgip-1 to ip-1 do
      write(hex(cpuw^[t1]),' ');
    gotoxy(21,5);
    writeln(copy(instruction,1,19));
    { now, update all ze information }
    window(1,30,40,49);

    { show addresses, all that stuff }

    gotoxy(1,8);
    write(instruction);  clreol; writeln;
    ip:=orgip;
    if (op1<>'$+') and (ts<128) then
    begin
      write(op1,'':12-length(op1),' =');
      gotoxy(14,wherey);
      clreol;
      case ts of
        0 : ;
        1,3 :
          begin
            write('@>',hex(cpuw^[wp+s]));
          end;
        2 :
          begin
            if s=0 then
            BEGIN
              write('=>',copy(op1,3,4));
            END
	    else
            begin
              write('=>',hex(cpuw^[ip]+cpuw^[wp+s]));
            end;
          end;

      end;
      makets;
      gotoxy(24,wherey);
      if ((b) and (odd(s)) and (s>=wp*2) and (s<=wp*2+31)) then
      BEGIN
        write('=LO (R',strg((s-wp*2)div 2),')');
      END;

      gotoxy(34,wherey);
      if b then
      BEGIN
        write('=',copy(hex(readbyte(s)),3,2));
      END
      else
      BEGIN
	write('=',hex(readword(s)));
      END;
    end
    else
      clreol;
    writeln;
    if (op2<>'') or (op1='$+') and (ts<255) then
    begin
      write(op2,'':12-length(op2),' =');
      gotoxy(14,wherey);
      clreol;
      case td of
        0 : ;

        1,3 :
        BEGIN
            write('@>',hex(cpuw^[(wp+d)]));
        END;
        2 :
	  begin
            If d=0 then
            BEGIN
            write('=>',copy(op2,3,4));
            END
            else
            BEGIN
              write('=>',hex(cpuw^[ip]+cpuw^[wp+d]));
            END;
          end;
        129 : ;
        160 :
          d:=(orgip-1)*2+d;
      else
        td:=160;
      end;
      gotoxy(34,wherey);
      if (td<>160) and (td<>129) then
      begin
        maketd;
	gotoxy(24,wherey);
        if ((b) and (odd(d)) and (d>=wp*2) and (d<=wp*2+31)) then
        BEGIN
          write('=LO (R',strg((d-wp*2)div 2),')');
        END;
        gotoxy(34,wherey);
        if b then
        BEGIN
          write('=',copy(hex(readbyte(d)),3,2));
        END
        else
        BEGIN
          write('=',hex(readword(d)));
        END;
      end
      else
        if op1='$+' then
        BEGIN
          write('=',hex(d));
        END;
    end
    else
      clreol;


    gotoxy(15,2);
    write(hex(wp*2));
    for t1:=0 to 15 do
    begin
      gotoxy(5+(t1 mod 4)*10,3+t1 div 4);
      write(hex(cpuw^[wp+t1]));
    end;
    gotoxy(5,7);  write(hex((orgip-1)*2));
    gotoxy(15,7);
    for t1:=0 to 6 do
      if st and ($8000 shr t1)<>0 then
      case t1 of
        0 : BEGIN write('L'); END;
	1 : BEGIN write('A');  END;
        2 : BEGIN write('E');  END;
        3 : BEGIN write('C');  END;
        4 : BEGIN write('O'); END;
        5 : BEGIN write('P');  END;
        6 : BEGIN write('X');  END;
      end
      else
      BEGIN
        write(' ');
      END;
    gotoxy(35,7);
    if inting then
    begin
      textcolor(8);
      textbackground(15);
    end;
    write(hex(st and 15));
    textcolor(15);
    textbackground(8);

    { show VDP data }
    gotoxy(17,11);
    write(hex(vaddr));

    t2:=vaddr;
    if t2>=8 then
      t2:=t2-8
    else
      t2:=0;

    for t1:=t2 to t2+15 do
    begin
      if (t1-t2)mod 8=0 then
      begin
        gotoxy(1,12+(t1-t2)div 8);
        write(hex(t1));
      end;
      if t1=vaddr then
        textcolor(8)
      else
        textcolor(15);
      gotoxy(6+((t1-t2) mod 8)*3,12+(t1-t2)div 8);
      write(copy(hex(vdp[t1]),3,4));
      gotoxy(30+(t1-t2) mod 8,12+(t1-t2)div 8);
      if vdp[t1]>32 then
        write(chr(vdp[t1]))
      else
        write('.');
    end;

    { show GPL data }
    gotoxy(17,14);
    write(hex(gaddr));

    t2:=gaddr;
    if t2>=8 then
      t2:=t2-8
    else
      t2:=0;

    for t1:=t2 to t2+15 do
    begin
      if (t1-t2)mod 8=0 then
      begin
        gotoxy(1,15+(t1-t2)div 8);
	write(hex(t1));
      end;
      if t1=gaddr then
        textcolor(8)
      else
        textcolor(15);
      gotoxy(6+((t1-t2) mod 8)*3,15+(t1-t2)div 8);
      write(copy(hex(gpl^[t1]),3,4));
      gotoxy(30+(t1-t2) mod 8,15+(t1-t2)div 8);
      if gpl^[t1]>32 then
        write(chr(gpl^[t1]))
      else
        write('.');
    end;


    if not fast then
      repeat until keypressed;

    if keypressed then
    begin
      ch:=readkey;
      if ch=#32 then
        fast:=not fast;
      if ch=#13 then
        fast:=false;
      if ch=#27 then
        asm
          int 3
        end;
      if ch=#8 then
        begin
	  dstart(0);
          drawscreen;
          repeat until keypressed;
          repeat ch:=readkey; until not keypressed;
          dstart(12);
        end;
      if (ch=#0) and (readkey=#2) then
        debugging:=false;
      if ch=#10 then   { skip next 4 bytes }
      begin
        op1:=hex(ip*2);
        addbreak(op1);
        debugging:=false;
        fast:=true;
      end;

      if upcase(ch)='B' then
      begin
        sound(1402);
        delay(100);
        nosound;
        gotoxy(18,19);
        readln(op1);
        if op1[1]='*' then
        begin
          t1:=decimal(copy(op1,2,4)) shr 1;
          if search(t1)<>nil then
            search(t1)^.where:=0;
        end
        else
        begin
          addbreak(op1);
	end;
      end;
    end;


    ip:=orgip;

  end;


procedure setupdisassembly;
  begin
    window(1,30,40,49);
    textcolor(7);
    textbackground(8);
    writeln('---------------------------------------');
    writeln('Workspace  WP=xxxx');
    writeln(' R0=xxxx   R1=xxxx   R2=xxxx   R3=xxxx');
    writeln(' R4=xxxx   R5=xxxx   R6=xxxx   R7=xxxx');
    writeln(' R8=xxxx   R9=xxxx  R10=xxxx  R11=xxxx');
    writeln('R12=xxxx  R13=xxxx  R14=xxxx  R15=xxxx');
    writeln(' IP=xxxx   ST=LAECOPX         INT=xxxx');
    writeln;
    writeln; { source }
    writeln; { dest   }
    writeln('VDP        VP = xxxx');
    writeln('xxxx=xx xx xx xx xx xx xx xx xxxxxxxx');
    writeln('xxxx=xx xx xx xx xx xx xx xx xxxxxxxx');
    writeln('GROM       GP = xxxx');
    writeln('xxxx=xx xx xx xx xx xx xx xx xxxxxxxx');
    writeln('xxxx=xx xx xx xx xx xx xx xx xxxxxxxx');
    writeln;
    write('Space = Continue/Pause,  Enter = Step');
  end;


const
  fctn:array[1..10] of byte=(3,4,7,2,14,12,1,6,15,255);

var
  ch:char;

  oldchar,newchar:char;


  e:tevent;
begin
  getmem(cpu,65535);
  cpuw:=pointer(cpu);
  getmem(gpl,65535);
  gplw:=pointer(gpl);

  assign(f,'ticpu.hex');
  reset(f,1);
  blockread(f,cpu^,32768);
  blockread(f,cpu^[32768],32768);
  close(f);
  assign(f,'tIEXTc0.hex');
  reset(f,1);
  blockread(f,cpu^[$6000],8192);
  close(f);
  for t1:=0 to 32767 do
    cpuw^[t1]:=swap(cpuw^[t1]);
  for t1:=$8300 shr 1 to $83ff shr 1 do
    cpuw^[t1]:=0;
  cpuw^[$83e0 shr 1+13]:=$9800;
  cpuw^[$83e0 shr 1+14]:=$0100;
  cpuw^[$83e0 shr 1+15]:=$8c02;

  assign(f,'tigpl.hex');
  reset(f,1);
  blockread(f,gpl^,32768);
  close(f);

  FOR T2:=$6000 TO 65535 DO
    GPL^[T2]:=0;

  assign(f,'tiextg.hex');
  reset(f,1);
  blockread(f,gpl^[$6000],32768);
  close(f);

 {
  ASSIGN(F,'tombg.HEX');
  RESET(F,1);
  BLOCKREAD(F,GPL^[$6000],3150);
  CLOSE(F);
  }
  gpllim:=$dfff;

  textmode(co40+font8x8);
  asm
    mov ah,$1
    mov cx,$2000
    int 10h
  end;

  asm
    mov ax,$1003
    mov bl,0
    int 10h

    mov ax,$1013
    mov bx,$100
    int 10h

    mov ax,$1012
    mov bx,0
    mov cx,16
    push ds
    pop es
    mov dx,offset pal
    int 10h

    mov dx,$3d4
    mov ax,$a709
    out dx,ax
  end;

  dstart(12);

  vr[0]:=0;    screen:=0; colors:=$300; patterns:=$800;
  cols:=32;
  vr[1]:=$e0;
  vr[2]:=0;
  vr[3]:=$e;
  vr[4]:=1;
  vr[5]:=$6;
  vr[6]:=0;
  vr[7]:=$00;
  attribute:=0;
  st:=0;
  wp:=0;
  ip:=0;
  vaddr:=0;
  gaddr:=0;
  vwrt:=false;
  gwrt:=false;
  grd:=false;
  cur60:=0;
  xop:=false;

  ip:=(cpuw^[1] shr 1);
  wp:=(cpuw^[0] shr 1);

{  ip:=$a000 shr 1;}

  inting:=false;
  s:=0;
  soundread:=false;
  soundvol:=false;
  soundvalue:=1;
  setupdisassembly;
  fast:=false;
  cpuw^[$83c4 div 2]:=0;
  debugging:=true;
  break:=nil;
  oldchar:=#$ff;
  initevents;
  blank:=false;
  startchanged:=$3fff;
  endchanged:=0;
  anychanged:=true;
  firstchar:=patterns;
  lastchar:=patterns+2048;
  sixties:=0;
  while 0=0 do
  begin
    inc(seconds);
    if (seconds>2550) then
    begin
      seconds:=0;
      if firstchar<>0 then
      begin
        setfont(0,256);
        firstchar:=0;
      end;
      if anychanged then drawscreen;
      anychanged:=false;
    end;
    if ((CPU^[$83CF]>0) and (seconds mod 100=0)) OR (SECONDS MOD 1000=0) then
    begin
      int:=st and 15;
      if (st and 15>0) and not inting then    { execute interrupt }
      begin
        curint:=int;
        dointerrupt;
{	inting:=true;
        t1:=cpuw^[int] shr 1;
	t2:=cpuw^[int+1] shr 1;
	cpuw^[t1+13]:=wp shl 1;
	cpuw^[t1+14]:=ip shl 1;
	cpuw^[t1+15]:=st;
	wp:=t1;
	ip:=t2;
        intcount:=1;
        curint:=int;}
      end;
    end;
    if search(ip)<>nil then
    begin
      debugging:=true;
      fast:=false;
      SEARCH(IP)^.WHERE:=0;
      d:=0;
      s:=0;
    end;
    if ip shl 1=$2b2 then  { keyboard interrupt -- I shall interrupt! }
    begin
      if not keypressed then
      begin
	newchar:=#$ff;
	oldchar:=#$ff;
      end
      else
      begin
	newchar:=readkey;
	if newchar=#0 then
	begin
	  newchar:=readkey;
	  if (newchar>=#59) and (newchar<=#68) then
	    newchar:=chr(fctn[ord(newchar)-58])
	  else
	    case newchar of
	      #75 : BEGIN newchar:=#8;  CPU^[$8376]:=0; CPU^[$8377]:=$FC; END;
	      #77 : BEGIN newchar:=#9; CPU^[$8376]:=0; CPU^[$8377]:=4; END;
	      #72 : BEGIN newchar:=#11; CPU^[$8376]:=$FC; CPU^[$8377]:=0; END;
	      #80 : BEGIN newchar:=#10; CPU^[$8376]:=$4; CPU^[$8377]:=0; END;
              #2 : nosound;
	    else
	      newchar:=#$ff;
	    end;
	end;
      end;
      if newchar=#$ff then
	cpu^[$837d]:=0
      else
	cpu^[$837d]:=$20;
      cpu^[$8374]:=ord(newchar);
      oldchar:=newchar;
      CPU^[$8375]:=0;
      ip:=cpuw^[wp+11] shr 1;   { return }
    end
    else
    if (ip>=$2000) and (ip<$3000) then
    begin
      nosound;
      if ip=cpuw^[cpuw^[$2002]shr 1+1]shr 1 then
      begin
        sound(700);
        delay(10);
        nosound;
        { initialize }
        for t1:=1 to 9 do
          fcbs[t1].opened:=false;
        ip:=cpuw^[wp+11] shr 1;
      end
      else
        begin
          dodsr;
        end;

    end;


(*    if (ip>=$2000) and (ip<$3000) then
    begin
      nosound;
      if ip=cpuw^[cpuw^[$2002]shr 1+1]shr 1 then
      begin
        sound(700);
        delay(10);
        nosound;
        { initialize }
        ip:=cpuw^[wp+11] shr 1;
      end
      else
        begin
          ip:=cpuw^[wp+11] shr 1;
        end;

    end;
  *)

    in1:=getword;
    getmouseevent(e);
    if e.what<>evnothing then
    begin
      debugging:=true;
      s:=0;
      d:=0;
    end;
    if debugging then
      dstart(12)
    else
      dstart(0);
    asm
      mov dx,$3d4
      mov ax,$a709
      out dx,ax
    end;
    if debugging then
      disassemble;
    doit;
  end;

end.
