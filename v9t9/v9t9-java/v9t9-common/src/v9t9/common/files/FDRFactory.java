/*
  FDRFactory.java

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
package v9t9.common.files;

import java.io.File;
import java.io.IOException;

public class FDRFactory {
	/**
	 * Get the FDR accessor for a file on disk.
	 * <p>
	 * This FDR is <b>not validated</b> against the file attributes.
	 * @param file
	 * @return FDR or null 
	 * @throws IOException if cannot read file
	 */
    public static FDR createFDR(File file) throws IOException {
        // try TIFILES first, since it has a signature
        try {
        	return TIFILESFDR.readFDR(file);
        } catch (InvalidFDRException e) {
        }
        
        // try V9t9 FDR
        try {
        	return V9t9FDR.readFDR(file);
        } catch (InvalidFDRException e) {
        }
        
        return null;
    }
}
