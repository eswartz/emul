
{
        Unit for Turbo Vision and INSTALL.

        Throw up a dialog box which lets a person read a document.

}

unit    reader;
{$X+}

interface

uses app,drivers,objects,views,dialogs,msgbox;

const
  cdocwindow=#57;

  cmnewscrollpos=2001;

type
  ptext=^ttext;
  ttext=array[0..65521] of char;

  pdocument=^tdocument;
  tdocument=record
    len:longint;
    start:ptext;
  end;


  pedscroll=^tedscroll;
  tedscroll=object(tview)
    tsize:word;
    pos:word;

    constructor init(bounds:trect; asize:word);
    procedure draw; virtual;
    procedure handleevent(var event:tevent); virtual;
  end;

  pdocwindow=^tdocwindow;
  tdocwindow=object(tview)
    viewpoint:word;
    stop,sbot:word;
    ttext:ptext;
    lines:word;
    len:word;
    scroll:pedscroll;
    constructor init(var bounds:trect; adocument:pdocument; ascroll:pedscroll);
    procedure handleevent(var event:tevent); virtual;
    procedure draw; virtual;
    function getpalette:ppalette; virtual;

    procedure print;
    function findtext(st:string):boolean;
    function findline(l:word):word;
    procedure getline(a:word; var str:string);
    function countlines:word;
    function nextlf(a:word):word;
  end;

  preader=^treader;
  treader=object(tdialog)
    lastfind:string;
    mydocwindow:pdocwindow;
    constructor init(atitle:string; document:pdocument; find:string;
                     wid:byte; other:string);
    function findmore:boolean;
  end;



implementation

constructor treader.init(atitle:string; document:pdocument; find:string;
                         wid:byte; other:string);
const
  message:string=#24#25' Scrolls  Shift+'#24#25' Selects  Alt+P Prints';
var
  dr,r:trect;
  d:pdialog;
  v:pview;
  doc:pdocwindow;
  b:pbutton;
  s:pedscroll;
begin
  { Dialog }

  desktop^.getextent(dr);

  dr.assign(0,0,wid,dr.b.y-3);

  tdialog.init(dr,atitle);
  options:=options or ofCentered or ofBuffered or ofSelectable;
  lastfind:=find;

  { Document window }

  r.assign(0,dr.b.y-5,length(message),dr.b.y-4);
  v:=new(pstatictext,init(r,message));
  v^.options:=v^.options or ofCenterX;
  insert(v);

  r.assign(wid-2,2,wid-1,dr.b.y-6);
  s:=new(pedscroll,init(r,1));
  insert(s);

  r.assign(1,2,wid-2,dr.b.y-6);
  doc:=new(pdocwindow,init(r, document, s));
  mydocwindow:=doc;
  if not doc^.findtext(find) then
  begin
    dispose(doc);
    fail;
    exit;
  end;

  insert(doc);
  s^.tsize:=doc^.lines;

  { Buttons }

  if other='' then
  begin
    r.assign(2,dr.b.y-3,dr.b.x-2,dr.b.y-1);
    b:=new(pbutton, init(r, '~F~inished reading', cmOk, bfDefault));
    insert(b);
  end
  else
  begin
    r.assign(2,dr.b.y-3,dr.b.x div 2-2,dr.b.y-1);
    b:=new(pbutton, init(r, '~F~inished reading', cmOk, bfDefault));
    insert(b);
    r.assign(dr.b.x div 2+2,dr.b.y-3,dr.b.x-2,dr.b.y-1);
    if pos('topics',other)<>0 then
    b:=new(pbutton, init(r, other, cmNo, bfnormal))
    else
    b:=new(pbutton, init(r, other, cmYes, bfnormal));
    insert(b);
  end;


  selectnext(false);
end;

function treader.findmore:boolean;
begin
  inc(mydocwindow^.viewpoint);
  findmore:=mydocwindow^.findtext(lastfind);
end;



constructor tdocwindow.init(var bounds:trect; adocument:pdocument; ascroll:pedscroll);
var
  r:trect;
begin
  tview.init(bounds);
  ttext:=adocument^.start;
  len:=adocument^.len;
  viewpoint:=0;
  lines:=countlines;
  options:=options or ofPreProcess or ofPostProcess or ofSelectable;
  showcursor;
  blockcursor;
  scroll:=ascroll;
end;


procedure tdocwindow.handleevent(var event:tevent);
var
  shift:byte absolute $40:$17;
  down:boolean;
  oldv:word;
begin
  if event.what=evkeydown then
  begin
    down:=true;
    oldv:=viewpoint;
    case event.keycode of
      kbup     :
        begin
          if (viewpoint>=1) then dec(viewpoint);
          down:=false;
        end;
      kbdown   : if (viewpoint+1<lines-1) then inc(viewpoint);
      kbpgup   :
        begin
          if (viewpoint>=size.y)
          then dec(viewpoint,size.y)
          else viewpoint:=0;
          down:=false;
        end;
      kbpgdn   : if (viewpoint+size.y+1<lines-1)
                 then inc(viewpoint,size.y)
                 else viewpoint:=lines-2;
      kbaltp   :
          print;
    else
      exit;
    end;
    if shift and 3<>0 then
    begin
      if stop=sbot then
      begin
        stop:=oldv;
        sbot:=viewpoint;
      end;

      if sbot<stop then
      begin
        oldv:=sbot;
        sbot:=stop;
        stop:=oldv;
      end;
      if down then
        sbot:=viewpoint;
      if not down then
        stop:=viewpoint;

    end
    else
    begin
      stop:=0;
      sbot:=0;
    end;
    message(scroll,evcommand,cmnewscrollpos,@viewpoint);
  end
  else
    exit;
  drawview;
  clearevent(event);
end;


procedure tdocwindow.draw;
var
  l:integer;
  t:tdrawbuffer;
  c:word;
  st:string;
  a:word;

  fir:integer;
begin
  c:=$30;

  fir:=viewpoint-size.y div 2;
  if fir+size.y>=lines then fir:=lines-size.y-1;
  if fir<0 then fir:=0;

  cursor.x:=0;
  cursor.y:=viewpoint-fir;

  showcursor;
  blockcursor;

  a:=findline(fir);
  for l:=0 to size.y-1 do
  begin
    if (stop=sbot) or (l+fir<stop) or (l+fir>=sbot) then
      c:=$30
    else
      c:=$1f;
    movechar(t,' ',c,size.x);
    if a<len then
    begin
      getline(a,st);
      movestr(t[1],st,c);
      a:=nextlf(a);
    end;
    writeline(0,l,size.x,1,t);
  end;
end;


function tdocwindow.getpalette:ppalette;
const
  p:string[length(cdocwindow)]=cdocwindow;
begin
  getpalette:=@p;
end;


function tdocwindow.findline(l:word):word;  assembler;
{var
  a:word;
begin
  a:=0;
  while l>0 do
  begin
    a:=nextlf(a);
    dec(l);
  end;
  findline:=a;
end;}
asm
  push es
  push di
  les  di,self
  mov  cx,es:[di].len
  les  di,es:[di].ttext
  mov  bx,di
  mov  dx,l
  or   dx,dx
  jz   @fl2
@fl1:
  or   dx,dx
  jz   @flout
  mov  al,0ah
  repne scasb
  inc  di
  dec  dx
  jmp  @fl1
@flout:
  dec  di
@fl2:
  mov  ax,di
  sub  ax,bx
  pop  di
  pop  es
end;


{  Assumes file is formatted }
procedure tdocwindow.getline(a:word; var str:string);  assembler;
{var
  ch:char;
begin
  st:='';
  repeat
    ch:=ttext^[a];
    if (ch<>#10) and (ch<>#13) then
      st:=st+ch;
    inc(a);
  until ch=#10;
end;}
asm
  push es
  push di
  push ds
  push si

  les  di,str

  lds  si,self
  lds  si,[si].ttext
  add  si,a

  mov  bx,di
  mov  byte ptr es:[bx],0
  inc  di
  mov  cx,80
@gl1:
  lodsb
  cmp  al,0ah
  je   @glo
  cmp  al,0dh
  je   @gl1
  stosb
  inc  byte ptr es:[bx]
  loop  @gl1
@glo:
  pop  si
  pop  ds
  pop  di
  pop  es
end;

function tdocwindow.countlines:word;
var
  t:word;
  a:word;

begin
  t:=1;
  a:=0;
  while a<len do
  begin
    a:=nextlf(a);
    inc(t);
  end;

  countlines:=t;
end;


function tdocwindow.nextlf(a:word):word;  assembler;
{
var
  stop:boolean;
begin
  stop:=false;
  while (a<len) and not stop do
  begin
    if ttext^[a]=#10 then
      stop:=true;
    inc(a);
  end;
  nextlf:=a;
end;}

asm
  push es
  push di
  les  di,self
  mov  cx,es:[di].len
  sub  cx,a
  les  di,es:[di].ttext
  mov  dx,di
  add  di,a
  xor  bx,bx            { stop }
  mov  al,0ah
{  jmp  @nll}
@nl1:
  repne scasb
{  mov  al,es:[di]
  cmp  al,0ah
  jne  @nl2
  mov  bx,1
@nl2:
  inc  di
@nll:
  or   cx,cx
  jz   @nlo
  or   bx,bx
  jz   @nl1
@nlo:}
  mov  ax,di
  sub  ax,dx
  pop  di
  pop  es
end;


function tdocwindow.findtext(st:string):boolean;
procedure upcasest(var st:string);
var
  x:integer;
begin
  for x:=1 to length(st) do
    st[x]:=upcase(st[x]);
end;
var
  a:word;
  l:word;
  tst:string;
  found:boolean;
begin
  if st='' then
  begin
    viewpoint:=0;
    findtext:=true;
    exit;
  end;

  upcasest(st);
  a:=findline(viewpoint);
  l:=viewpoint;
  found:=false;
  while (a<len) and not found do
  begin
    getline(a,tst);
    upcasest(tst);
    if pos(st,tst)<>0 then
      found:=true
    else
    begin
      a:=nextlf(a);
      inc(l);
    end;
  end;
  if not found then
  begin
    viewpoint:=0;
    findtext:=false;
  end
  else
  begin
    viewpoint:=l;
    findtext:=true;
  end;
end;


procedure tdocwindow.print;

procedure border(x:byte); assembler;
asm
  mov ah,$10
  mov al,1
  mov bh,x
  int $10
end;

procedure noansi(var st:string);
const
  trans:string[128]=
   'CueaaaaceeeiiiAAeeEooouuyOUc Y faiounNao?++  !<>'+
   '.:#|++++++|+++++++++-++++++++=+++++++++++++#o||^'+
   'abgpogmsttOdffEn=+><||/=o../n2.ÿ';

var
  i:integer;
begin
  for i:=1 to length(st) do
    if ord(st[i])>=128 then
      st[i]:=trans[ord(st[i])-127];
end;

const
   cmokascii=4000;


var
  d:pdialog;
  i:pinputline;
  b:pbutton;
  t:pstatictext;
  r:trect;

  w:pdialog;

  fst:string;
  f:text;
  finished:boolean;
  l:word;
  st:string;

  aa,bb:word;
  com:word;

begin
  repeat
    r.assign(0,0,50,11);
    if stop=sbot then
      d:=new(pdialog,init(r,'Print File'))
    else
      d:=new(pdialog,init(r,'Print Selection'));
    d^.options:=d^.options or ofCentered;

    r.assign(2,2,48,3);
    t:=new(pstatictext,init(r,'Enter device or filename to print to:'));
    d^.insert(t);

    r.assign(2,4,48,5);
    i:=new(pinputline,init(r,64));
    d^.insert(i);



    r.assign(2,6,24,8);
    b:=new(pbutton,init(r,'~P~rint',cmOk,bfDefault));
    d^.insert(b);

    r.assign(26,6,48,8);
    b:=new(pbutton,init(r,'Print ~A~SCII only',cmYes,bfnormal));
    d^.insert(b);

    r.assign(13,8,36,10);
    b:=new(pbutton,init(r,'~C~ancel',cmCancel,bfnormal));
    d^.insert(b);

{    d^.enablecommands(cmokascii);}

    st:='PRN';
    d^.setdata(st);
    d^.selectnext(false);


    com:=desktop^.execview(d);
    if com<>cmCancel then
    begin
      d^.getdata(st);
      assign(f,st);
      {$i-}
      rewrite(f);
      if ioresult<>0 then
        finished:=messagebox('Could not open '+st,nil,mfError+mfOkCancel)=cmcancel
      else
      begin
        if stop=sbot then
        begin
          aa:=0;
          bb:=lines;
        end
        else
        begin
          aa:=stop;
          bb:=sbot;
        end;

        border(15);

        for l:=aa to bb-1 do
        begin
          aa:=findline(l);
          getline(aa,st);
          if com=cmyes then noansi(st);
          writeln(f,st);
          if ioresult<>0 then
          begin
            desktop^.delete(w);
            messagebox('Error writing to '+st,nil,mfError+mfCancelButton);
            exit;
          end;
        end;

        border(0);
        close(f);
        finished:=true;
        end;
    end
    else
      finished:=true;
  until finished;
end;


constructor tedscroll.init(bounds:trect; asize:word);
begin
  tview.init(bounds);
  pos:=0;
  tsize:=asize;
  options:=ofpreprocess+ofpostprocess;
end;

procedure tedscroll.handleevent(var event:tevent);
begin
  tview.handleevent(Event);

  if event.what=evcommand then
    if event.command=cmnewscrollpos then
    begin
      pos:=word(event.infoptr^);
      drawview;
      clearevent(event);
    end;
end;

procedure tedscroll.draw;
var
  w:word;

  y:integer;
  t:tdrawbuffer;

  s,b:integer;

  rp:integer;
begin

  for y:=0 to size.y-1 do
    t[y]:=$100+ord('±');

  if tsize<size.y then
  begin
    s:=0;
    b:=size.y-1;
  end
  else
  begin
    s:=(pos)*size.y div (tsize+size.y);
    b:=(pos+size.y)*size.y div (tsize+size.y);
  end;

  for y:=s to b do
    t[y]:=$900+ord('Û');

  writebuf(0,0,1,size.y,t);
end;


end.