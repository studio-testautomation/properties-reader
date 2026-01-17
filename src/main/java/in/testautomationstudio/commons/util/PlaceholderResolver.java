package in.testautomationstudio.commons.util;

/**
 * Utility for resolving simple placeholders in text using system properties
 * and environment variables.
 *
 * <p>This class is intentionally small and focused: it scans an input
 * string for placeholders of the form {@code ${KEY}} and replaces each
 * occurrence with the value resolved from the following sources (in order):</p>
 *
 * <ol>
 *   <li>Java system properties ( {@code System.getProperty(KEY)} )</li>
 *   <li>Operating system environment variables ( {@code System.getenv(KEY)} )</li>
 * </ol>
 *
 * <p>Design notes and semantics:</p>
 * <ul>
 *   <li>If the input text is {@code null}, {@link #resolvePlaceholders(String)}
 *       returns {@code null} immediately.</li>
 *   <li>If a placeholder key cannot be resolved from either system properties
 *       or environment variables, an {@link IllegalArgumentException} is
 *       thrown identifying the missing key and the original input text.</li>
 *   <li>Placeholders are not recursive: the resolver does not attempt to
 *       resolve placeholders inside values obtained from system properties or
 *       environment variables. If nested/recursive resolution is required,
 *       callers should perform an additional pass or extend this utility.
 *   </li>
 *   <li>Unterminated placeholders (a {@code ${} } with no matching closing
 *       brace) are treated as literal text: the resolver appends the remainder
 *       of the string without modification.</li>
 *   <li>The implementation is stateless and thread-safe.</li>
 * </ul>
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * // If the environment variable ENV is set to "dev":
 * PlaceholderResolver.resolvePlaceholders("config-${ENV}.properties")
 * // returns "config-dev.properties"
 *
 * // If a Java system property 'user' exists:
 * PlaceholderResolver.resolvePlaceholders("home-${user}")
 * // returns "home-alice" (assuming user=alice)
 *
 * // Missing placeholder key -> IllegalArgumentException
 * PlaceholderResolver.resolvePlaceholders("file-${MISSING}.properties");
 * }</pre>
 *
 * <h2>Limitations &amp; possible improvements</h2>
 * <ul>
 *   <li>No support for default values within placeholders (e.g. {@code ${KEY:default}}).
 *       You can implement that if you need a fallback without throwing an exception.</li>
 *   <li>No support for recursive or nested placeholder resolution. If a
 *       resolved value itself contains placeholders they will not be expanded
 *       by this single call.</li>
 * </ul>
 */
public final class PlaceholderResolver {
    private static final String PLACEHOLDER_START = "${";
    private static final String PLACEHOLDER_END = "}";

    private PlaceholderResolver() {
    }

    /**
     * Resolve placeholders of the form {@code ${KEY}} contained in the
     * supplied {@code text}.
     *
     * <p>Resolution search order: system properties first, then environment
     * variables. If a key is not found, this method throws an
     * {@link IllegalArgumentException} describing the missing key and the
     * original input string.</p>
     *
     * @param text input text which may contain zero or more placeholders; may be {@code null}
     * @return a new string with all placeholders replaced, or {@code null} if the input was {@code null}
     * @throws IllegalArgumentException if a placeholder key cannot be resolved
     */
    public static String resolvePlaceholders(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            int start = text.indexOf(PLACEHOLDER_START, i);
            if (start == -1) {
                sb.append(text.substring(i));
                break;
            }
            sb.append(text, i, start);
            int end = text.indexOf(PLACEHOLDER_END, start);
            if (end == -1) {
                sb.append(text.substring(start));
                break;
            }
            String key = text.substring(start + 2, end);
            String value = System.getProperty(key);
            if (value == null) {
                value = System.getenv(key);
            }
            if (value == null) {
                throw new IllegalArgumentException("No environment variable or system property found for placeholder: " + key + " in filePath: " + text);
            }
            sb.append(value);
            i = end + 1;
        }
        return sb.toString();
    }
}
