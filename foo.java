class foo {
		long a;
		Object o;
		int x,y;
	static public void main(String[] args) {
		foo f = new foo();
		f.run();
	}
	void run() {
		a++;
		if (o != null) {
			o.toString();
		}
		for (x=0; x<10; x++) {
			y+=x+3;
			short z = (short)0xffff;
			z >>>= 3;
			System.out.println("z="+z);
			y-=z;
		}

	}
void fooh() {
	long myA = a;
	Object myO = o;
	try {
		int cnt=10;
		while (cnt-->0) {
		myA++;
		myO.hashCode();
		}
	} finally {
		a = myA;
		o = myO;
	}
}

}

