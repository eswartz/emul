/**
 * 
 */
package v9t9.tools.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ejs
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Category {
	String value();
	String DEVELOPMENT = "Development";
	String DISKUTILS = "Disk Utilities";
	String OTHER = "Other";
}
