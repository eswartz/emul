/*
  TestMemoryRanges.java

  (c) 2008-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.common.tests;

import junit.framework.TestCase;
import v9t9.common.asm.MemoryRange;
import v9t9.common.asm.MemoryRanges;

public class TestMemoryRanges extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    public void testRanges() {
    	MemoryRanges ranges = new MemoryRanges();
        MemoryRange range;
        ranges.addRange(0, 0x2000, true);
        range = ranges.getRangeContaining(0);
        assertNotNull(range);
        range = ranges.getRangeContaining(0x40);
        assertNotNull(range);
        MemoryRange range2 = ranges.getRangeContaining(0x1ffE);
        assertEquals(range, range2);
        range = ranges.getRangeContaining(0x2000);
        assertFalse(range.isCode());
        assertEquals(MemoryRange.EMPTY, range.getType());
    }

}
