package osrs.util;

import java.io.IOException;
import java.util.function.Predicate;

public class InstancedClassLoader extends ClassLoader {
    private final Predicate<String> filter;

    public InstancedClassLoader(Predicate<String> filter) {
        this.filter = filter;
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (filter.test(name)) {
            synchronized (getClassLoadingLock(name)) {
                var c = findLoadedClass(name);

                if (c == null) {
                    c = findClass(name);
                }

                if (resolve) {
                    resolveClass(c);
                }

                return c;
            }
        }

        return super.loadClass(name, resolve);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (filter.test(name)) {
            var resource = super.getResource(name.replace('.', '/') + ".class");

            if (resource == null) {
                throw new ClassNotFoundException(name);
            }

            try (var stream = resource.openStream()) {
                var bytes = stream.readAllBytes();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        throw new ClassNotFoundException(name);
    }
}
