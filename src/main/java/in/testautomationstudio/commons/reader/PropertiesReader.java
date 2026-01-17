package in.testautomationstudio.commons.reader;

import in.testautomationstudio.commons.annotation.Configuration;
import in.testautomationstudio.commons.annotation.PropertyKey;
import in.testautomationstudio.commons.parser.DefaultPropertyValueParser;
import in.testautomationstudio.commons.parser.PropertyValueParser;
import in.testautomationstudio.commons.util.PlaceholderResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Reflection-based implementation of {@link ConfigurationReader} that binds
 * property values from a classpath resource (properties file) into a target
 * bean's fields.
 *
 * <p>Behavior overview:</p>
 * <ul>
 *   <li>Determine the properties file to load from either the reader's
 *       constructor (if provided) or from the target bean's
 *       {@link Configuration#filePath()} annotation when present.</li>
 *   <li>Interpolate placeholders in the chosen file path using
 *       {@link PlaceholderResolver#resolvePlaceholders(String)} (supporting
 *       environment variables or other placeholders as implemented by that
 *       utility).</li>
 *   <li>Load the properties file from the runtime classpath using the
 *       {@link ClassLoader#getResourceAsStream(String)} mechanism. If the
 *       resource is missing a {@link FileNotFoundException} is thrown.</li>
 *   <li>For each non-static, non-final declared field on the bean annotated
 *       with {@link PropertyKey}, determine the property key and default
 *       value from the annotation, pick an appropriate parser and assign the
 *       converted value to the field.</li>
 * </ul>
 *
 * <h2>Parsing and conversion rules</h2>
 * <ul>
 *   <li>The reader maintains a small built-in map of parsers for common types
 *       (int, Integer, float, Float, double, Double, boolean, Boolean).
 *       These are used automatically when the field's type matches.</li>
 *   <li>If the {@link PropertyKey#parser()} element declares a custom
 *       {@link PropertyValueParser} (one other than
 *       {@link DefaultPropertyValueParser}), the reader will instantiate it via
 *       a no-argument constructor and use it to parse the property value.</li>
 *   <li>If no parser is specified and the field is an enum, the reader will
 *       resolve the enum constant using {@link Enum#valueOf(Class, String)}.
 *   <li>Otherwise the raw string value is assigned directly (suitable for
 *       {@link String} fields or for downstream conversion).</li>
 *   <li>If the resolved property value is {@code null} or blank (as defined
 *       by {@link org.apache.commons.lang3.StringUtils#isBlank(CharSequence)}),
 *       the reader will not modify the field (the existing value remains).
 *       Note: the annotation's {@code defaultValue} is supplied to
 *       {@link java.util.Properties#getProperty(String, String)} which means a
 *       non-empty default will be used when the property is absent.</li>
 * </ul>
 *
 * <h2>Errors and exceptions</h2>
 * <ul>
 *   <li>Missing property resource: {@link FileNotFoundException} wrapped in a
 *       {@link RuntimeException} is thrown.</li>
 *   <li>Failure to instantiate a custom parser: the reader throws a
 *       {@link RuntimeException} with details about the parser class and cause.</li>
 *   <li>Reflection errors when writing fields are wrapped in a
 *       {@link RuntimeException}.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // Example 1: use the annotation on the config class
 * @Configuration(filePath = "qa-configurations.properties")
 * public class QAConfig {
 *     @PropertyKey(key = "service.url", defaultValue = "http://localhost:8080")
 *     private String serviceUrl;
 * }
 *
 * QAConfig cfg = new QAConfig();
 * new PropertiesReader<QAConfig>().loadBean(cfg);
 * // cfg.serviceUrl now contains either the value from the properties file
 * // or the annotation default if the key was not present.
 *
 * // Example 2: pass a file path directly to the reader (overrides annotation)
 * PropertiesReader<QAConfig> reader = new PropertiesReader<>("test-configurations.properties");
 * reader.loadBean(cfg);
 *
 * // Example 3: custom parser
 * public class CommaSeparatedListParser implements PropertyValueParser<List<String>> {
 *     @Override
 *     public List<String> parse(String value) {
 *         if (value == null || value.isEmpty()) return Collections.emptyList();
 *         return Arrays.stream(value.split(","))
 *                      .map(String::trim)
 *                      .collect(Collectors.toList());
 *     }
 * }
 *
 * public class ListConfig {
 *     @PropertyKey(key = "items", parser = CommaSeparatedListParser.class)
 *     private List<String> items;
 * }
 *
 * ListConfig listCfg = new ListConfig();
 * new PropertiesReader<ListConfig>().loadBean(listCfg);
 * }</pre>
 *
 * <h2>Extension points</h2>
 * <ul>
 *   <li>Add more built-in parsers to the {@code PARSERS} map if you need
 *       automatic handling for additional primitive/wrapper types.</li>
 *   <li>Adjust blank/empty handling policies (for example, to treat empty
 *       values as explicit empty strings rather than "missing").</li>
 *   <li>Provide alternative resource loading strategies (file system,
 *       remote config service) by replacing the resource resolution logic or
 *       by subclassing and overriding {@link #loadBean(Object)}.</li>
 * </ul>
 *
 * @param <T> type of the configuration bean handled by this reader
 */
public class PropertiesReader<T> implements ConfigurationReader<T> {
    private static final Map<Class<?>, PropertyValueParser<?>> PARSERS = new HashMap<>();

    static {
        PARSERS.put(int.class, (PropertyValueParser<Integer>) Integer::parseInt);
        PARSERS.put(Integer.class, (PropertyValueParser<Integer>) Integer::parseInt);
        PARSERS.put(float.class, (PropertyValueParser<Float>) Float::parseFloat);
        PARSERS.put(Float.class, (PropertyValueParser<Float>) Float::parseFloat);
        PARSERS.put(double.class, (PropertyValueParser<Double>) Double::parseDouble);
        PARSERS.put(Double.class, (PropertyValueParser<Double>) Double::parseDouble);
        PARSERS.put(boolean.class, (PropertyValueParser<Boolean>) Boolean::parseBoolean);
        PARSERS.put(Boolean.class, (PropertyValueParser<Boolean>) Boolean::parseBoolean);
    }

    private String filePath;

    /**
     * Create an empty reader that will attempt to discover the properties
     * file via the {@link Configuration} annotation on the target bean's class.
     */
    public PropertiesReader() {
    }

    /**
     * Create a reader that uses the supplied {@code filePath} when loading
     * properties. This path is treated as a classpath resource and will be
     * resolved via the active {@link ClassLoader}.
     *
     * @param filePath path to the properties resource (relative to the classpath)
     */
    public PropertiesReader(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Populate the provided {@code bean} by reading property values from the
     * configured properties file and assigning them to fields annotated with
     * {@link PropertyKey}.
     *
     * <p>Algorithm summary:</p>
     * <ol>
     *   <li>If the reader was constructed without a file path, try to obtain
     *       one from the target class's {@link Configuration} annotation.
     *       (If both are absent, {@link PlaceholderResolver} will be invoked on
     *       a null/empty path which may result in an error.)</li>
     *   <li>Resolve placeholders in the file path via
     *       {@link PlaceholderResolver#resolvePlaceholders(String)}.</li>
     *   <li>Load the resolved resource from the classpath. A missing resource
     *       results in a {@link FileNotFoundException} wrapped by a
     *       {@link RuntimeException}.</li>
     *   <li>Iterate declared instance fields on the target bean; for fields
     *       annotated with {@link PropertyKey}, obtain the property value (or
     *       the annotation's default) and delegate to {@link #setFieldValue}
     *       for conversion and assignment.</li>
     * </ol>
     *
     * @param bean non-null instance whose annotated fields are to be populated
     * @throws RuntimeException wrapping IO or reflection failures
     */
    @Override
    public void loadBean(T bean) {
        Class<?> cls = bean.getClass();
        if (this.filePath == null && cls.isAnnotationPresent(Configuration.class)) {
            this.filePath = cls.getAnnotation(Configuration.class).filePath();
        }
        // Interpolate variables in filePath
        this.filePath = PlaceholderResolver.resolvePlaceholders(this.filePath);

        ClassLoader classLoader = PropertiesReader.class.getClassLoader();
        try (InputStream systemResource = classLoader.getResourceAsStream(filePath)) {
            if (systemResource == null) {
                throw new FileNotFoundException("File not found on classpath: " + this.filePath);
            }
            Properties properties = new Properties();
            properties.load(systemResource);
            for (Field field : cls.getDeclaredFields()) {
                // Ignore static and final fields
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                if (field.isAnnotationPresent(PropertyKey.class)) {
                    PropertyKey annotation = field.getAnnotation(PropertyKey.class);
                    String key = annotation.key();
                    String defaultValue = annotation.defaultValue();
                    Class<? extends PropertyValueParser<?>> parserClass = annotation.parser();
                    PropertyValueParser<?> customParser = null;
                    if (!parserClass.equals(DefaultPropertyValueParser.class)) {
                        try {
                            customParser = parserClass.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to instantiate custom PropertyValueParser: " + parserClass, e);
                        }
                    }
                    setFieldValue(field, bean, properties.getProperty(key, defaultValue), customParser);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the raw {@code value} and set it on the {@code target} bean's
     * {@code field}.
     *
     * <p>Conversion strategy:</p>
     * <ol>
     *   <li>If {@code customParser} is provided, use it to parse the string.
     *       Any exception thrown by the parser will propagate as a
     *       {@link RuntimeException} from the calling method.</li>
     *   <li>Otherwise, if a built-in parser exists for the field type (see
     *       {@code PARSERS}), use it.</li>
     *   <li>If the field is an enum type, resolve the enum constant by name.
     *   <li>For all other types, assign the raw string value directly (this is
     *       appropriate for {@link String} fields).</li>
     * </ol>
     *
     * <p>Note: this method will not modify the field when {@code value} is
     * {@code null} or blank according to {@link StringUtils#isBlank}.</p>
     *
     * @param field the field to be set (must be non-static, non-final)
     * @param target the bean instance whose field value is modified
     * @param value the raw value obtained from the properties file (may be null)
     * @param customParser optional parser to convert the string value
     * @throws RuntimeException wrapping reflection errors
     */
    private void setFieldValue(Field field, T target, Object value, PropertyValueParser<?> customParser) {
        // If the value is not provided then do not set the field
        if (value == null || StringUtils.isBlank((String) value)) {
            return;
        }
        try {
            Class<?> fieldType = field.getType();
            Object parsedValue;
            if (customParser != null) {
                parsedValue = customParser.parse((String) value);
            } else {
                PropertyValueParser<?> parser = PARSERS.get(fieldType);
                if (parser != null) {
                    parsedValue = parser.parse((String) value);
                } else if (fieldType.isEnum()) {
                    parsedValue = createEnumInstance(fieldType, (String) value);
                } else {
                    parsedValue = value;
                }
            }
            FieldUtils.writeField(field, target, parsedValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an enum instance of the supplied {@code enumType} by name.
     *
     * @param enumType enum class object
     * @param name name of the enum constant
     * @param <U> enum type parameter
     * @return enum constant of type {@code U}
     */
    @SuppressWarnings("unchecked")
    private <U extends Enum<U>> U createEnumInstance(Class<?> enumType, String name) {
        return Enum.valueOf((Class<U>) enumType, name);
    }
}
