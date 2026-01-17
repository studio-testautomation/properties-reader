package in.testautomationstudio.commons.parser;

/**
 * Strategy interface for converting raw property values (strings) into
 * application types.
 *
 * <p>This interface defines a simple contract used by property-binding
 * utilities: given the textual representation of a property value, return
 * an instance of the desired target type {@code T}.</p>
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>Implementations should be side-effect free and deterministic for a
 *       given input string.</li>
 *   <li>Implementations must handle {@code null} or empty input according to
 *       the needs of the consumer — either by returning {@code null}, a
 *       sensible default, or by throwing an unchecked exception. Document the
 *       chosen behavior in your implementation.</li>
 *   <li>Implementations are typically stateless and can be reused across
 *       multiple bindings; if state is required, ensure thread-safety.</li>
 * </ul>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * // A parser for integers
 * public class IntegerPropertyValueParser implements PropertyValueParser<Integer> {
 *     @Override
 *     public Integer parse(String value) {
 *         return value == null || value.isEmpty() ? null : Integer.valueOf(value);
 *     }
 * }
 *
 * // A parser for boolean values
 * public class BooleanPropertyValueParser implements PropertyValueParser<Boolean> {
 *     @Override
 *     public Boolean parse(String value) {
 *         return value == null ? null : Boolean.parseBoolean(value.trim());
 *     }
 * }
 *
 * // Registration/usage (conceptual):
 * // @PropertyKey(key = "app.port", parser = IntegerPropertyValueParser.class)
 * // private int port;
 * }</pre>
 *
 * @param <T> the target type produced by the parser
 */
public interface PropertyValueParser<T> {
    /**
     * Parse the provided string value into an instance of {@code T}.
     *
     * @param value the raw property value (may be {@code null} or empty)
     * @return the parsed value of type {@code T}
     */
    T parse(String value);
}