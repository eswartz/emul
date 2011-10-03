"""
Import a directory of module ROMs into an XML file. 
"""
import re, sys, os, os.path

import xml.etree.ElementTree as ET


def convert(root, db, dir):
    
    for proot, dirs, files in os.walk(dir):
        files.sort()
        for file in files:
            addentry(root, db, dir, file)
        for dir in dirs:
            convert(root, db, dir)

CPUENT = re.compile(r'.*c\.bin')
CPU2ENT = re.compile(r'.*d\.bin')
GROMENT = re.compile(r'.*g\.bin')

PART = re.compile(r'(.*)\(File .*of.*\)\((.*)\)')
GROMONLY = re.compile(r'(.*)\((.*)\)')

def addentry(root, db, dir, file):    
    if not file.endswith(".bin"):
        return
    
    base = file[:-4]
    m = PART.match(base)
    if m:
        modname = m.group(1)
        ext = m.group(2)[-1:].lower()
    else:
        m = GROMONLY.match(base)
        if m:
            modname = base
            ext = "g"
        else:
            modname = base[:-1]
            ext = base[-1:].lower()
        
    #print modname,ext
    
    try:
        module = db[modname]
        entries = module.find("moduleEntries")
    except KeyError, e:
        module = ET.SubElement(root, "module")
        db[modname] = module
        module.set("name", modname)
        entries = ET.SubElement(module, "moduleEntries")
        entries.text = "\n"
        entries.tail = "\n"

    #print root,dir,file
    intname = discover_name(os.path.join(dir, file))
    if intname:
        module.set("name", intname)
    
    attr = "fileName"
    if ext=="c":
        entry = entries.findtext("bankedModuleEntry")
        if entry is not None: 
            entry = entry[0]
        else:
            entry = ET.SubElement(entries, "romModuleEntry")
    elif ext=="g":
        entry = ET.SubElement(entries, "gromModuleEntry")
    elif ext=="d":
        entry = entries.findtext("bankedModuleEntry")
        if entry is not None: 
            entry = entry[0]
        else:
            entry = entries.find("romModuleEntry")
            if entry is not None: 
                oentry = entry
                entries.remove(oentry)
                entry = ET.SubElement(entries, "bankedModuleEntry")
                entry.set("fileName", oentry.get("fileName"))
            else:
                entry = ET.SubElement(entries, "bankedModuleEntry")
                
        attr = "fileName2"
    else:
        print "Unknown:",file
        return    
    entry.set(attr, file)
    entry.text = "\n"
    entry.tail = "\n"

def getname(f):
    namelen = ord(f.read(1))
    if namelen > 0:
        name = f.read(namelen)
        print name
        name = name.replace('\0', ' ')
        for c in name:
            if ord(c) < 32 or ord(c) > 127:
                print "oops, bad char: ",c," (",ord(c),")"
                name = None
                break
        return name
    return None
    
def discover_name(path):
    def be16(st):
        return (ord(st[0]) << 8) | (ord(st[1]))
    
    f = open(path, "rb")
    try:
        base = 0
        size = os.path.getsize(path)
        lastname = None
        while base < size:
            f.seek(base)
            data = f.read(2)
            if ord(data[0]) == 0xaa: # and ord(data[1]) != 0xff:
                f.seek(base+6)
                header = be16(f.read(2))
                while header != 0 and header >= 0x6000:
                    f.seek(header - 0x6000)
                    next = be16(f.read(2))
                    code = be16(f.read(2))
                    name = getname(f)
                    if name:
                        lastname = name
                    header = next
            elif ord(data[0]) == 0xee:
                f.seek(base + 4)
                name = getname(f)
                if name:
                    lastname = name
                
            base += 0x2000
        return lastname
    finally:
        f.close()
        
if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "Run as: import_module_dir.py <dir> <modules.inf>"
        sys.exit(1)
		
    # build a tree structure
    root = ET.Element("modules")
    root.text = "\n"
    root.tail = "\n"

    db = {}

    convert(root, db, os.path.abspath(sys.argv[1]))
    
    tree = ET.ElementTree(root)
    tree.write(sys.argv[2], "UTF-8")



