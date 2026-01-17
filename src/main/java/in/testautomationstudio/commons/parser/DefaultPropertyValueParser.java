package in.testautomationstudio.commons.parser;

/**
 * Default implementation of {@link PropertyValueParser} used when no specific
 * parser is provided for a {@link in.testautomationstudio.commons.annotation.PropertyKey}.
 *
 * <p>This parser performs a simple pass-through: it returns the raw string
 * value as-is (an {@code Object} reference). This is useful for bindings
 * where the target field is a {@link java.lang.String} or when a caller wants
 * to receive the unmodified property text and perform conversion later.</p>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>If the input {@code value} is {@code null}, {@code parse} returns
 *       {@code null}.</li>
 *   <li>Empty strings are returned unchanged (empty {@code String}).
 *       Binding implementations should consider {@link in.testautomationstudio.commons.annotation.PropertyKey#defaultValue}
 *       before invoking this parser if they want to substitute defaults for
 *       missing or empty properties.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * // Field will receive the exact string read from the property file
 * @PropertyKey(key = "greeting")
 * private String greeting; // will be assigned the property text
 * }</pre>
 *
 * @see PropertyValueParser
 */
public class DefaultPropertyValueParser implements PropertyValueParser<Object> {
    @Override
    public Object parse(String value) {
        return value;
    }
}