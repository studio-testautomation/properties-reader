package in.testautomationstudio.commons.reader;

/**
 * Simple contract for a configuration binder that can populate a target bean
 * from an external configuration source (for example, a properties file).
 *
 * <p>Implementations of this interface are responsible for reading a
 * configuration source and assigning values to fields on the provided bean.
 * The expectation is that implementing classes will honor annotations such as
 * {@code in.testautomationstudio.commons.annotation.Configuration} (class-level)
 * and {@code in.testautomationstudio.commons.annotation.PropertyKey}
 * (field-level) to discover which resource to read and which keys map to
 * which fields.</p>
 *
 * <h2>Contract and responsibilities</h2>
 * <ul>
 *   <li>Implementations must populate the mutable state of the supplied
 *       bean instance; they must not create new bean instances.</li>
 *   <li>Implementations should be defensive about missing or malformed input
 *       and either set sensible defaults or raise runtime exceptions with
 *       meaningful messages.</li>
 *   <li>Thread-safety: callers should assume implementations may not be
 *       thread-safe unless explicitly documented otherwise. If concurrent
 *       binding is required, the implementation should document its
 *       concurrency guarantees.</li>
 * </ul>
 *
 * <h2>Example use</h2>
 * <pre>{@code
 * // 1) Create a configuration holder class annotated with @Configuration
 * // and @PropertyKey on fields. Then bind it:
 * MyConfig cfg = new MyConfig();
 * ConfigurationReader<MyConfig> reader = new PropertiesReader<>();
 * reader.loadBean(cfg);
 * // After invocation, cfg's annotated fields are populated from the
 * // configured properties file or from a file path supplied to the reader.
 * }</pre>
 *
 * @param <T> the type of the configuration bean handled by this reader
 */
public interface ConfigurationReader<T> {
    /**
     * Load configuration into the provided bean instance.
     *
     * @param bean non-null bean whose annotated fields will be populated
     */
    void loadBean(T bean);
}
