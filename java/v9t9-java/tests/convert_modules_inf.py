
import re, sys

import xml.etree.ElementTree as ET


def convert(inffile, xmlfile):
    # build a tree structure
    root = ET.Element("modules")
    
    inf = open(inffile, "r")
    for line in inf.readlines():
        module = ET.SubElement(root, "module")
    
        line = line.strip()
        if len(line) == 0:
            continue
        
        parts = line.split(",")
        name = parts[0][1:-1]
        module.set("name", name)
        
        entries = ET.SubElement(module, "moduleEntries")

        blank = ET.Comment(line)
        entries.append(blank)
        
        base = parts[1].lower()
        
        for attr in parts[2:]:
            if attr=="ROM":
                entry = ET.SubElement(entries, "romModuleEntry")
                entry.set("fileName", base + "c.bin")
            elif attr=="GROM":
                entry = ET.SubElement(entries, "gromModuleEntry")
                entry.set("fileName", base + "g.bin")
            elif attr=="ROM1" or attr=="BANKED":
                entry = ET.SubElement(entries, "bankedModuleEntry")
                entry.set("fileName", base + "c.bin")
                entry.set("fileName2", base + "d.bin")
            elif attr=="ROM2":
                # ignore
                pass
            elif attr=="MMRAM":
                entry = ET.SubElement(entries, "moduleEntry")
                entry.set("stored", "true")
                entry.set("fileName", base + "r.bin")
                entry.set("domain", "CPU")
                entry.set("address", "0x7000")
                entry.set("size", "0x1000")
                
    tree = ET.ElementTree(root)
    tree.write(xmlfile)
    
if __name__ == "__main__":
    convert(sys.argv[1], sys.argv[2])


