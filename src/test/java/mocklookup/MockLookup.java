package mocklookup;

import java.lang.reflect.Field;
import java.util.Collection;
import static junit.framework.Assert.*;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Mock implementation of system default lookup suitable for use in unit tests.
 * The initial value just contains classpath services.
 */
public class MockLookup extends ProxyLookup {

    private static MockLookup DEFAULT;
    private static boolean making = false;
    private static volatile boolean ready;

    static {
        making = true;
        try {
            System.setProperty("org.openide.util.Lookup", MockLookup.class.getName());
            if (Lookup.getDefault().getClass() != MockLookup.class) {
                // Someone else initialized lookup first. Try to force our way.
                Field defaultLookup = Lookup.class.getDeclaredField("defaultLookup");
                defaultLookup.setAccessible(true);
                defaultLookup.set(null, null);
            }
            assertEquals(MockLookup.class, Lookup.getDefault().getClass());
        } catch (Exception x) {
            throw new ExceptionInInitializerError(x);
        } finally {
            making = false;
        }
    }

    /** Do not call this directly! */
    public MockLookup() {
        assertTrue(making);
        assertNull(DEFAULT);
        DEFAULT = this;
    }

    /**
     * Just ensures that this lookup is default lookup, but does not actually change its content.
     * Useful mainly if you have some test utility method which calls foreign code which might use default lookup,
     * and you want to ensure that any users of mock lookup will see the correct default lookup right away,
     * even if they have not yet called {@link #setLookup} or {@link #setInstances}.
     */
    public static void init() {
        if (!ready) {
            setInstances();
        }
    }

    /**
     * Sets the global default lookup with zero or more delegate lookups.
     * Caution: if you don't include Lookups.metaInfServices, you may have trouble,
     * e.g. {@link #makeScratchDir} will not work.
     * Most of the time you should use {@link #setInstances} instead.
     */
    public static void setLookup(Lookup... lookups) {
        ready = true;
        DEFAULT.setLookups(lookups);
    }

    /**
     * Sets the global default lookup with some fixed instances.
     * Will also include (at a lower priority) a {@link ClassLoader},
     * and services found from <code>META-INF/services/*</code> in the classpath.
     */
    public static void setInstances(Object... instances) {
        ClassLoader l = MockLookup.class.getClassLoader();
        setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l));
    }
    /**
     * Sets the global default lookup with some fixed instances and
     * content read from Services folder from system file system.
     * Will also include (at a lower priority) a {@link ClassLoader},
     * and services found from <code>META-INF/services/*</code> in the classpath.
     */
    public static void setLayersAndInstances(Object... instances) {
        ClassLoader l = MockLookup.class.getClassLoader();
        if (l != Lookup.getDefault().lookup(ClassLoader.class)) {
            setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l));
        }
        Lookup projects = Lookups.forPath("Services");
        Collection<?> initialize = projects.lookupAll(Object.class);
        //System.err.println("all: " + initialize);
        setLookup(Lookups.fixed(instances), Lookups.metaInfServices(l), Lookups.singleton(l), projects);
    }

}