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
	 * @see org.ejs.chiprocksynth.editor.model.IClassPropertyFactory#create(java.lang.String)
	 */
	public Object create(String id) {
		if (LINKED.equals(id) || id.length() == 0)
			return new LinkedList<Object>();
		if (ARRAY.equals(id))
			return new ArrayList<Object>();
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IClassPropertyFactory#getId(java.lang.Object)
	 */
	public String getId(Object value) {
		if (value instanceof ArrayList)
			return ARRAY;
		if (value instanceof LinkedList)
			return LINKED;
		return null;
	}

	/* (non-Javadoc)
	 * @see org.ejs.chiprocksynth.editor.model.IClassPropertyFactory#getIds()
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
