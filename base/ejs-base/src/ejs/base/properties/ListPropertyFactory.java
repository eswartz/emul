/**
 * 
 */
package ejs.base.properties;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * @author ejs
 *
 */
public class ListPropertyFactory implements IClassPropertyFactory {

	private static ListPropertyFactory instance = new ListPropertyFactory();
	
	private static final String ARRAY= "arrayList";
	private static final String LINKED  = "linkedList";

	/* (non-Javadoc)
	 * 
	 */
	public Object create(String id) {
		if (LINKED.equals(id) || id.length() == 0)
			return new LinkedList<Object>();
		if (ARRAY.equals(id))
			return new ArrayList<Object>();
		return null;
	}

	/* (non-Javadoc)
	 * 
	 */
	public String getId(Object value) {
		if (value instanceof ArrayList)
			return ARRAY;
		if (value instanceof LinkedList)
			return LINKED;
		return null;
	}

	/* (non-Javadoc)
	 * 
	 */
	public String[] getIds() {
		return new String[] { ARRAY, LINKED };
	}

	/**
	 * @return
	 */
	public static IClassPropertyFactory getInstance() {
		return instance;
	}

}
