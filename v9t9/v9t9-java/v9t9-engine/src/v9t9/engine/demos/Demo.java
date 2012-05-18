/**
 * 
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
