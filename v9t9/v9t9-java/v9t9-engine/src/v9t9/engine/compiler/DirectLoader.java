package v9t9.engine.compiler;

import java.security.SecureClassLoader;

/** Simple-minded loader for constructed classes. */
class DirectLoader extends SecureClassLoader {
    protected DirectLoader() {
        super();
    }

    protected Class<?> load(String name, byte[] data) {
        return super.defineClass(name, data, 0, data.length);
    }
}