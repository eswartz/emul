/**
 * 
 */
package org.ejs.eulang;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.types.LLType;

/**
 * The calling convention is a per-function convention that tells how the 
 * logical argument list and return type are prepared for passing and returning
 * from a function.  The passing and returning mechanisms are also used by the 
 * function when it is compiled, to ensure there are no mismatches.
 * <p>
 * For example, a simple case may be a register-only convention with "(Int => Int)".
 * Here, we could say that the argument locations are { Place=REGISTER, number=0 } (R0)
 * @author ejs
 *
 */
public interface ICallingConvention {

	enum Operation {
		COPY,
		SPLIT
	};
	class Action {
		/** for arguments, 0 ... to original arg count */
		public int inpos;
		public Operation operation;
		public int l; 
	};

	class Location  {
		/** symbol */
		public String name;
		/** the type of the location */
		public LLType type;
		public Location(String name, LLType type) {
			super();
			this.name = name;
			this.type = type;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			Location other = (Location) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
		
		
	}
	class RegisterLocation extends Location {
		public IRegClass regClass;	
		public int number;
		/** offset into actual data, 0=low */
		public int bitOffset;
		public RegisterLocation(String name, LLType type, int bitOffset, IRegClass regClass,
				int number) {
			super(name, type);
			this.bitOffset = bitOffset;
			this.regClass = regClass;
			this.number = number;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + bitOffset;
			result = prime * result + number;
			result = prime * result
					+ ((regClass == null) ? 0 : regClass.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			RegisterLocation other = (RegisterLocation) obj;
			if (bitOffset != other.bitOffset)
				return false;
			if (number != other.number)
				return false;
			if (regClass == null) {
				if (other.regClass != null)
					return false;
			} else if (!regClass.equals(other.regClass))
				return false;
			return true;
		}

		
		
	};
	class StackLocation extends Location {
		/** offset in bytes into a stack frame; 0 = start, +N = next entry, etc. */
		public int offset;

		public StackLocation(String name, LLType type, int offset) {
			super(name, type);
			this.offset = offset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + offset;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			StackLocation other = (StackLocation) obj;
			if (offset != other.offset)
				return false;
			return true;
		}
		
		
		
	};
	
	public class StackBarrierLocation extends Location {

		private final int pushedArgsSize;

		/**
		 * @param name
		 * @param type
		 */
		public StackBarrierLocation(String name, LLType type, int pushedArgsSize) {
			super(name, type);
			this.pushedArgsSize = pushedArgsSize;
		}
		
		public int getPushedArgumentsSize() {
			return pushedArgsSize;
		}
	}
	
	public class TempLocation extends Location {
		public IRegClass regClass;
		/** offset into actual data, 0=low */
		public int bitOffset;

		public TempLocation(String name, LLType type, int bitOffset, IRegClass regClass) {
			super(name, type);
			this.bitOffset = bitOffset;
			this.regClass = regClass;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + bitOffset;
			result = prime * result
					+ ((regClass == null) ? 0 : regClass.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			TempLocation other = (TempLocation) obj;
			if (bitOffset != other.bitOffset)
				return false;
			if (regClass == null) {
				if (other.regClass != null)
					return false;
			} else if (!regClass.equals(other.regClass))
				return false;
			return true;
		}
		
	};

	/** 
	 * This is a caller-allocated memory location used to store a large memory return 
	 * value from a function. 
	 * This appears both in the argument locations and in the return location.
	 */
	public class CallerStackLocation extends RegisterLocation {

		public CallerStackLocation(String name, LLType type, IRegClass regClass, int reg) {
			super(name, type, 0, regClass, reg);
		}
	}
	
	/** Get locations for canonical arguments */
	Location[] getArgumentLocations();
	/** Get locations for canonical return value */
	Location[] getReturnLocations();
	
	/** Get the reserved registers for the class */
	int[] getFixedRegisters(IRegClass regClass);
}
