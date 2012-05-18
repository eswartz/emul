/**
 * 
 */
package v9t9.engine.demos.format;

import v9t9.common.demos.IDemoEventFormatter;

/**
 * @author ejs
 *
 */
public interface IDemoFormatterRegistry {

	void registerDemoEventFormatter(IDemoEventFormatter formatter);

	IDemoEventFormatter findFormatterByBuffer(String bufferId);
	IDemoEventFormatter findFormatterByEvent(String eventIdentifier);

}