/*
  Demo.java

  (c) 2012 Edward Swartz

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
package v9t9.engine.demos;

import java.net.URI;

import v9t9.common.demos.IDemo;

/**
 * @author ejs
 *
 */
public class Demo implements IDemo {

	private final URI parentURI;
	private final URI uri;
	private final String name;
	private String description;

	public Demo(URI dirURI, URI demoURI, String name, String description) {
		this.parentURI = dirURI;
		this.uri = demoURI;
		this.name = name;
		this.description = description;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemo#getURI()
	 */
	@Override
	public URI getURI() {
		return uri;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemo#getParentURI()
	 */
	@Override
	public URI getParentURI() {
		return parentURI;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemo#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.demo.IDemo#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Demo other = (Demo) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	
}
