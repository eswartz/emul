/*
  StatusTest9900.java

  (c) 2005-2011 Edward Swartz

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
package v9t9.tests.inst9900;

import junit.framework.TestCase;
import v9t9.machine.ti99.cpu.Status9900;

/**
 * @author ejs
 */
public class StatusTest9900 extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(StatusTest9900.class);
    }
    public void testIt() {
        Status9900 status = new Status9900();

        status.expand(Status9900.ST_L);	/* L> */
        assertTrue(status.isH());
        assertTrue(status.isHE());
        assertFalse(status.isL());
        assertFalse(status.isEQ());
        
        status.expand(Status9900.ST_A); /* A> */
        assertTrue(status.isGT());
        assertFalse(status.isEQ());
        assertFalse(status.isLT());
        
        status.expand(Status9900.ST_E);
        assertTrue(status.isEQ());
        assertTrue(status.isHE());
        assertTrue(status.isLE());
        assertFalse(status.isNE());
        
        status.set_LAE((short)0);
        assertTrue(status.isHE());
        assertTrue(status.isEQ());
        assertTrue(status.isLE());
        assertFalse(status.isNE());
        assertFalse(status.isGT());
        assertFalse(status.isLT());

        status.set_LAE((short)1);
        assertTrue(status.isHE());
        assertTrue(status.isNE());
        assertFalse(status.isLE());
        assertFalse(status.isEQ());
        assertFalse(status.isL());
        assertFalse(status.isLT());

        status.set_LAE((short)-1);
        assertTrue(status.isHE());
        assertTrue(status.isNE());
        assertTrue(status.isLT());
        assertFalse(status.isLE());
        assertFalse(status.isEQ());
        assertFalse(status.isL());
        
        status.set_E(false);
        assertTrue(status.isNE());
        status.set_E(true);
        assertTrue(status.isEQ());

        /* overflow */
        status.set_LAEO((short)0x0);
        assertTrue(!status.isO());
        status.set_LAEO((short)-1);
        assertTrue(!status.isO());
        status.set_LAEO((short)0x8000);
        assertTrue(status.isO());

        /* parity */
        status.set_BYTE_LAEP((byte)0x0);
        assertTrue(!status.isP());
        status.set_BYTE_LAEP((byte)0x1);
        assertTrue(status.isP());
        status.set_BYTE_LAEP((byte)0xff);
        assertTrue(!status.isP());
        status.set_BYTE_LAEP((byte)0xab);
        assertTrue(status.isP());

        /* word op doesn't reset P */
        status.set_LAE((short)0x0);
        assertTrue(status.isP());

        /* carries */
        status.set_SHIFT_LEFT_CO((short)0x4000, (short)1);
        assertTrue(!status.isC());
        assertTrue(status.isO());
        status.set_SHIFT_LEFT_CO((short)0x8000, (short)1);
        assertTrue(status.isC());
        assertTrue(status.isO());

        status.set_SHIFT_LEFT_CO((short)0x4000, (short)2);
        assertTrue(status.isC());
        assertTrue(!status.isO());
        status.set_SHIFT_LEFT_CO((short)0x2000, (short)2);
        assertTrue(!status.isC());
        assertTrue(status.isO());

        status.set_SHIFT_RIGHT_C((short)0x1, (short)1);
        assertTrue(status.isC());
        status.set_SHIFT_RIGHT_C((short)0x2, (short)1);
        assertTrue(!status.isC());
        status.set_SHIFT_RIGHT_C((short)0x2, (short)2);
        assertTrue(status.isC());
        status.set_SHIFT_RIGHT_C((short)0x4, (short)2);
        assertTrue(!status.isC());
        
        /* adds */
        status.set_ADD_LAECO((short)0x1, (short)0x2);
        assertTrue(!status.isC());
        assertTrue(!status.isO());
        assertTrue(status.isNE());
        assertTrue(status.isGT());

        status.set_ADD_LAECO((short)0x7fff, (short)0x1);
        assertTrue(!status.isC());
        assertTrue(status.isO());

        status.set_ADD_LAECO((short)0x1, (short)0xffff);
        assertTrue(status.isC());
        assertTrue(!status.isO());

        status.set_ADD_LAECO((short)-0x1, (short)0xffff);
        assertTrue(status.isC());
        assertTrue(!status.isO());
        
        status.set_ADD_LAECO((short)0x8001, (short)0xffff);
        assertTrue(status.isC());
        assertTrue(!status.isO());

        status.set_ADD_LAECO((short)0x8000, (short)0x8000);
        assertTrue(status.isC());
        assertTrue(status.isO());
    }
}
