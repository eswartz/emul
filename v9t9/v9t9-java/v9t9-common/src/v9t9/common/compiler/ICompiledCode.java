/*
  ICompiledCode.java

  (c) 2008-2011 Edward Swartz

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
package v9t9.common.compiler;

/**
 * @author ejs
 *
 */
public interface ICompiledCode {
	 /**
     * Using the current CPU state, run any number of instructions, 
     * and save away changed CPU state before return.
     * 
     * @return true if code exited normally (i.e. max # instructions invoked, 
     * or jumped outside its own block), false if exec.cpu.PC refers to
     * instruction that must be emulated. 
     */
	boolean run();
}
