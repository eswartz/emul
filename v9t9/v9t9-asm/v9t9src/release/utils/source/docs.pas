{
        Program to READ DOCUMENTATION.
        (Ustabe a setup program, so ignore implications about that)

}


program install6;
{$X+}
{$F+}

uses objects,drivers,app,menus,dialogs,views,msgbox,reader,dos,config,crt;


type
  PHeapView = ^THeapView;
  THeapView = object(TView)
    OldMem : LongInt;
    constructor Init(var Bounds: TRect);
    procedure Draw; virtual;
    procedure Update;
  end;



  tmyapp=object(tapplication)
    path:string;
    aborted:boolean;
    heapview:pheapview;

    sstr:string[46];            { last searched-for string }

    constructor init;

    procedure initstatusline; virtual;
    procedure initmenubar; virtual;
    procedure handleevent(var event:tevent);  virtual;
    procedure idle; virtual;
    destructor done; virtual;

    procedure search;
    procedure list;
    function readfile(filename,st:string):boolean;
  end;


var
  myapp:tmyapp;
  cmd:word;

function readafile(filename,st,find,other:string):integer;
  var
    f:file;
    document:tdocument;

    reader:preader;
    res:integer;
  begin
    assign(f,filename);
    {$i-}
    reset(f,1);
    if ioresult<>0 then
    begin
      messagebox('Could not open '+filename, nil, mfError+mfCancelButton);
      readafile:=0;
      exit;
    end;

    document.len:=filesize(f);
    if document.len>65521 then
    begin
      messagebox(filename+' should never be greater than 65521 bytes!', nil,
                 mfError+mfCancelbutton);
      close(f);
      readafile:=0;
      exit;
    end;
    if maxavail-128<document.len then
    begin
      messagebox('Not enough memory to read file!', nil, mfError+mfCancelButton);
      readafile:=0;
      close(f);
      exit;
    end;


    getmem(document.start,document.len);
    blockread(f,document.start^,document.len);
    close(f);


{    myapp.setscreenmode(smco80+smfont8x8);}
    reader:=new(preader, init(st, @document, find, 80, other));
    if reader=nil then
    begin
      freemem(document.start,document.len);
      readafile:=3;
    end
    else
    begin
    repeat
      cmd:=desktop^.execview(reader);
      if cmd=cmOk then
         res:=0
      else
      if cmd=cmYes then
         res:=2
      else
      if cmd=cmNo then
         res:=1
      else
         res:=0;

      if res=2 then
        if reader^.findmore then
          res:=2
        else
          res:=3;
    until (res<>2);
      dispose(reader,done);
      freemem(document.start,document.len);

    readafile:=res;
    end;
{    myapp.setscreenmode(smco80);}
  end;


(********************************************************)
(**                  THEAPVIEW                         **)
(********************************************************)


constructor THeapView.Init(var Bounds: TRect);
begin
  TView.Init(Bounds);
  OldMem := 0;
end;

procedure THeapView.Draw;
var
  S: String;
  B: TDrawBuffer;
  C: Byte;
begin
  OldMem := MemAvail;
  Str(OldMem:Size.X, S);
  C := GetColor(2);
  MoveChar(B, ' ', C, Size.X);
  MoveStr(B, S, C);
  WriteLine(0, 0, Size.X, 1, B);
end;


procedure THeapView.Update;
begin
  if (OldMem <> MemAvail) then DrawView;
end;



procedure upcasest(var str:string);  assembler;
{var
  x:integer;
begin
  for x:=1 to length(st) do
    st[x]:=upcase(st[x]);
end;}
asm
  push es
  push di
  les  di,str
  mov  cl,es:[di]
  xor  ch,ch
  inc  di
  jcxz @uout
@uloop:
  mov  al,es:[di]
  cmp  al,'a'
  jb   @unot
  cmp  al,'z'
  ja   @unot
  sub  al,32
  mov  es:[di],al
@unot:
  inc  di
  loop @uloop
@uout:
  pop  di
  pop  es
end;



procedure loseblanks(var str:string); assembler;
{var
  x:byte;
  nst:string;
begin
  x:=1;
  nst:='';
  while (x<=length(st)) do
  begin
    if not (st[x] in [' ',#9]) then
      nst:=nst+st[x];
    inc(x);
  end;
  st:=nst;
end;}

asm
  push es
  push di
  push si
  les  si,str
  mov  bx,si    { save length ptr }
  mov  cl,es:[si]
  xor  ch,ch
  mov  es:[si],ch       { overwrite string as we go along }
  inc  si
  mov  di,si
  jcxz @lbout
@lbloop:
  mov  al,es:[si]
  cmp  al,9
  je   @lbnot
  cmp  al,32
  je   @lbnot
  mov  es:[di],al
  inc  di
  inc  byte ptr es:[bx]
@lbnot:
  inc  si
  loop @lbloop
@lbout:
  pop  si
  pop  di
  pop  es
end;



(********************************************************)
(**                                                    **)
(********************************************************)


const

  cm2543      =200;

  cmsearch=201;
  cmlist=202;



(********************************************************)
(**                      TMYAPP                        **)
(********************************************************)

constructor tmyapp.init;
var
  r:trect;
  d:pdialog;
  v:pview;
begin
  tapplication.init;

  getdir(0,path);
  upcasest(path);
  if path[length(path)]<>'\' then
    path:=path+'\';
  if (copy(path,length(path)-6+1,6)='UTILS\') or
     (copy(path,length(path)-5+1,5)='DOCS\') then
    chdir('..');

  getdir(0,path);
  r.assign(74,size.y-1,80,size.y);
  heapview:=new(pheapview,init(r));
  insert(heapview);

  sstr:='';
end;


destructor tmyapp.done;
begin
  tapplication.done;
  clrscr;
end;


procedure tmyapp.initstatusline;
var
  r:trect;

begin
  getextent(r);
  r.a.y:=r.b.y-1;

  statusline:=new(pstatusline,init(r,newstatusdef(0, $ffff,
    newstatuskey('~F1~ Search', kbF1, cmsearch,
    newstatuskey('~F2~ Read', kbF2, cmlist,
    newstatuskey('~F10~ Toggle 25/50 lines',kbF10,cm2543,

    newstatuskey('~Alt-X~ Exit', kbAltX, cmquit,
    nil)))),
  nil)));

end;

procedure tmyapp.initmenubar;
var
  r:trect;

begin
  getextent(r);
  r.b.y:=r.a.y+1;

  menubar:=new(pmenubar, init(r, newmenu(
    newsubmenu('~R~ead', hcnocontext, newmenu(
      newitem('~L~ist...',   'F2', kbf2, cmlist,   hcnocontext,
      newline(
      newitem('~S~earch...', 'F1', kbf1, cmsearch, hcnocontext,
      nil)))),
    newsubmenu('S~c~reen', hcnocontext, newmenu(
      newitem('~T~oggle 25/50 lines', 'F10', kbf10, cm2543, hcnocontext,
      nil)),
    nil
    )))
  ));
end;


procedure tmyapp.handleevent(var event:tevent);
begin
  tapplication.handleevent(event);
  if event.what=evcommand then
    case event.command of
      cm2543:
        begin
          if (screenmode and smfont8x8)=smfont8x8 then
            setscreenmode(smco80)
          else
            setscreenmode(smco80+smfont8x8);
          heapview^.moveto(size.x-6,size.y-1);
        end;
      cmsearch:
          search;
      cmlist:
          list;
    end
  else
    exit;
  clearevent(event);
end;

procedure tmyapp.idle;
begin
  tapplication.idle;
  heapview^.update;
end;


function tmyapp.readfile(filename,st:string):boolean;
begin
  readfile:=readafile(filename,st,'','')=0;
end;

procedure tmyapp.search;
{  Purpose of this procedure is to look for a string in a help file.
   All the help files will be read with treader and if the phrase
   never appears, give error.
}
var
  d:pdialog;
  i:pinputline;
  t:pstatictext;
  b:pbutton;
  r:trect;

  s:searchrec;
  f:text;
  found:boolean;
  choice:integer;
begin
    r.assign(0,0,50,9);
    d:=new(pdialog,init(r,'Search for phrase'));
    d^.options:=d^.options or ofCentered;
    r.assign(2,2,48,4);
    t:=new(pstatictext,init(r,'Enter a phrase to search for in the help file '+
                              'and all the documentation.'));
    d^.insert(t);

    r.assign(2,4,48,5);
    i:=new(pinputline,init(r,46));
    d^.insert(i);

    r.assign(2,6,23,8);
    b:=new(pbutton,init(r,'~S~earch',cmOk,bfDefault));
    d^.insert(b);

    r.assign(25,6,48,8);
    b:=new(pbutton,init(r,'Cancel',cmCancel,bfnormal));
    d^.insert(b);
    d^.selectnext(false);

    d^.setdata(sstr);
    found:=desktop^.execview(d)=cmCancel;
    d^.getdata(sstr);
    dispose(d,done);

    if not found then
    begin
      found:=false;
      findfirst('DOCS\*.TXT',0,s);
      while (doserror=0) and not found and (choice>=2) do
      begin
        choice:=readafile('DOCS\'+s.name,'Search for phrase',sstr,'~M~ore');
        if (choice=3) then
          findnext(s)
        else
        if choice<>3 then
          found:=true;
      end;
    end
    else
      found:=true;

    if not found then
      messagebox('That phrase was not found in any more files.',
                  nil,mfError+mfOkButton);
end;


procedure tmyapp.list;
const
  filenames:array[0..23] of string[12]=
  ('V9t9.TXT',
   'DEMOS.TXT',
   'FORTH.TXT',
   'TRANSFER.TXT',
   'CONTACT.TXT',
   'ORDERING.TXT',
   'BINARIES.TXT',
   'DISTRIB.TXT',
   'UTILS.TXT',
   'CONFIG.TXT',
   'MODULES.TXT',
   '5CHANGES.TXT',
   'DEVLOG.TXT',
   'PROBLEMS.TXT',
   'BUGS.TXT',
   'FORMATS.TXT',
   'DISKS.TXT',
   'KEYBOARD.TXT',
   'SOUND.TXT',
   'SPEECH.TXT',
   'JOYSTICK.TXT',
   'RS232.TXT',
   'THANKS.TXT',
   'LEGAL.TXT'

   );

var
  d:pdialog;
  rb:pradiobuttons;
  b:pbutton;
  r:trect;
  cmd:word;
  ch:word;
begin
  ch:=0;
  repeat
    r.assign(0,0,70,17);
    d:=new(pdialog,init(r,'Select a Topic'));
    d^.options:=d^.options or ofCentered;

    r.assign(2,2,68,14);
    rb:=new(pradiobuttons,init(r,
      newsitem('~G~eneral',
      newsitem('~D~emonstrations',
      newsitem('Using ~F~ORTH',
      newsitem('~T~ransferring ROMs',
      newsitem('How to ~c~ontact me',
      newsitem('Fairware & ~O~rdering Info',
      newsitem('~B~inaries information',
      newsitem('Distributing ~V~9t9',
      newsitem('Ut~i~lities conventions',
      newsitem('Confi~g~uration',
      newsitem('~M~odules',
      newsitem('Changes from TIEMUL v~5~.01',
      newsitem('Development ~l~og',
      newsitem('Common ~p~roblems',
      newsitem('Known b~u~gs',
      newsitem('File form~a~ts',
      newsitem('DSKx ~e~mulation',
      newsitem('~K~eyboard',
      newsitem('Sou~n~d',
      newsitem('Speec~h~',
      newsitem('~J~oysticks',
      newsitem('RS~2~32 and PIO',
      newsitem('Thanks to u~s~ers',
      newsitem('Legal concerns',
      nil))))))))))))))))))))))))));
    d^.insert(rb);

    r.assign(2,17,13,19);
    b:=new(pbutton,init(r,'~R~ead',cmOk,bfDefault));
    d^.insert(b);
    r.assign(15,17,26,19);
    b:=new(pbutton,init(r,'Cancel',cmCancel,bfnormal));
    d^.insert(b);
    d^.selectnext(false);

    d^.setdata(ch);
    cmd:=desktop^.execview(d);
    d^.getdata(ch);
    dispose(d,done);
  until (cmd=cmcancel) or
        (readafile('DOCS\'+filenames[ch],'Documentation','','~M~ore topics...')=0);

end;

begin
  myapp.init;
  myapp.run;
  myapp.done;
end.