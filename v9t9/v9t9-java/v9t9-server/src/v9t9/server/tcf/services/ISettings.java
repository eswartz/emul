/*
  ISettings.java

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
package v9t9.server.tcf.services;

import java.util.Map;

import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;

/**
 * @author ejs
 *
 */
public interface ISettings extends IService {

	String NAME = "Settings";
	
	String COMMAND_QUERY_ALL = "queryAll";
	String COMMAND_GET = "get";
	String COMMAND_SET = "set";

	/*
	 * Keys for #queryAllSettings  
	 */
	String PROP_NAME = "name";
	String PROP_LABEL = "label";
	String PROP_DESCRIPTION = "description";
	String PROP_TYPE = "type";
	String PROP_CONTEXT = "context";
	String PROP_DEFAULT = "default";

	/*
	 * Types for #queryAllSettings 'type' 
	 */
	String TYPE_STRING = "string";
	String TYPE_INT = "int";
	String TYPE_BOOL = "bool";
	String TYPE_LIST = "list";
		
    interface DoneQueryAllCommand {
        /**
         * Called when command is done.
         * @param token command handle.
         * @param error error object or null.
         * @param settingsMap map of name to type (TYPE_xxx)
         */
        void doneQueryAllCommand(IToken token, Exception error,
        		Map<String, String> settingsMap);
    }

    IToken queryAll(DoneQueryAllCommand done);
    
    interface DoneReadSettingCommand {
        /**
         * Called when command is done.
         * @param token command handle.
         * @param error error object or null.
         * @param value value of setting
         */
        void doneGetCommand(IToken token, Exception error,
        		Object value);
    }

    IToken get(String name, DoneReadSettingCommand done);
    

    interface DoneSetCommand {
        /**
         * Called when command is done.
         * @param token command handle.
         * @param error error object or null.
         */
        void doneSetCommand(IToken token, Exception error,
        		Object oldValue);
    }

    IToken set(String name, Object value, DoneSetCommand done);
}
