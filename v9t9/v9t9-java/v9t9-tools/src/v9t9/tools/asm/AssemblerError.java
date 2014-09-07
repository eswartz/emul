/*
  AssemblerError.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm;

/**
 * @author Ed
 *
 */
public class AssemblerError {
	private final Exception exception;
	private SourceRef ref;

	public AssemblerError(Exception e, SourceRef ref) {
		this.exception = e;
		this.ref = ref;
	}
	
	public String getDescr() {
		return ref.filename + ":" + ref.lineno;
	}
	
	public SourceRef getRef() {
		return ref;
	}
	
	public Exception getException() {
		return exception;
	}
}
