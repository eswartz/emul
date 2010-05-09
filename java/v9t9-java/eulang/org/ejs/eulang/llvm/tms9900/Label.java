package org.ejs.eulang.llvm.tms9900;


public class Label {
    int id;     // unique label
    private String name; // real or unique name
    
    public Label(String name) {
    	org.ejs.coffee.core.utils.Check.checkArg(name);
        setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
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
		final Label other = (Label) obj;
		if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}
}
