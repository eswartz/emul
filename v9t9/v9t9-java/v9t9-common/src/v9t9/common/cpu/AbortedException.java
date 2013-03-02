/*
  AbortedException.java

  (c) 2011 Edward Swartz

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
package v9t9.common.cpu;

/**
 * This exception is thrown from within an interpreter
 * or compiled code to indicate the current stream of
 * execution must be restarted somehow -- either due to
 * interrupts changing, due to changes in the memory map,
 * etc. 
 * @author ejs
 *
 */
public class AbortedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AbortedException() {}
}