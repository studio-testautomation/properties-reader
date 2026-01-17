package in.testautomationstudio.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import in.testautomationstudio.commons.parser.DefaultPropertyValueParser;
import in.testautomationstudio.commons.parser.PropertyValueParser;

/**
 * Annotation used to bind a Java field to a key in a properties source.
 *
 * <p>Place this annotation on a field to indicate that the value for that
 * field should be obtained from a properties file (or other property source)
 * using the given property key. At runtime a properties-binding routine can
 * scan a class for fields annotated with {@code @PropertyKey} and populate
 * them by reading the corresponding property and converting it to the field's
 * type using a {@link PropertyValueParser}.</p>
 *
 * <h2>Elements</h2>
 * <ul>
 *   <li>{@link #key()} - the property key to read from the properties store.</li>
 *   <li>{@link #defaultValue()} - an optional default value to use if the
 *       property is missing or its value is empty. The default is the empty
 *       string which means "no default provided"; interpretation of an empty
 *       default depends on the binding implementation.</li>
 *   <li>{@link #parser()} - a {@link PropertyValueParser} implementation used to
 *       convert the raw property string to the field's type. If omitted,
 *       {@link DefaultPropertyValueParser} will be used.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public class AppConfig {
 *     // Bind primitive or object fields
 *     @PropertyKey(key = "app.name", defaultValue = "MyApp")
 *     private String appName;
 *
 *     // Use a parser that converts the string to an Integer
 *     @PropertyKey(key = "app.port", defaultValue = "8080",
 *                  parser = in.testautomationstudio.commons.parser.IntegerPropertyValueParser.class)
 *     private int port;
 *
 *     // For custom conversion, provide your own parser implementing PropertyValueParser<T>
 *     @PropertyKey(key = "app.launchDate", parser = com.example.parser.DatePropertyValueParser.class)
 *     private java.time.LocalDate launchDate;
 * }
 *
 * // Loading code (conceptual):
 * // PropertiesReader.bind(new AppConfig(), loadedProperties);
 * }</pre>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>This annotation has runtime retention so it can be discovered via reflection.</li>
 *   <li>It targets fields only.</li>
 *   <li>Implementations reading property values should honor {@code defaultValue}
 *       when the property is not present or empty.</li>
 * </ul>
 *
 * @see PropertyValueParser
 * @see DefaultPropertyValueParser
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyKey {
    String key();
    String defaultValue() default "";
    Class<? extends PropertyValueParser<?>> parser() default DefaultPropertyValueParser.class;
}
