package in.testautomationstudio.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class with the location of a properties (configuration) file to be
 * used when binding property values to fields annotated with {@link PropertyKey}.
 *
 * <p>Apply this annotation to a configuration holder class to declare which
 * properties file (relative to the application's resources root / classpath)
 * contains the values that should be injected into fields annotated with
 * {@code @PropertyKey}.</p>
 *
 * <h2>Semantics</h2>
 * <ul>
 *   <li>The {@code filePath} value is treated as a path relative to the
 *       runtime classpath/resources root. For example, {@code "qa-configurations.properties"}
 *       refers to {@code /resources/qa-configurations.properties}.</li>
 *   <li>Retention is {@link RetentionPolicy#RUNTIME} so annotation processors
 *       or reflection-based binders can discover it at runtime.</li>
 *   <li>This annotation targets {@link ElementType#TYPE} (classes and interfaces).
 *       It is typically placed on simple POJOs that act as containers for
 *       configuration values.</li>
 * </ul>
 *
 * <h2>Placeholder variables in {@code filePath}</h2>
 * <p>The {@code filePath} may include simple placeholder variables of the form
 * {@code ${KEY}}. A binding/reader implementation can resolve these placeholders
 * before attempting to load the resource by using the utility in
 * {@link in.testautomationstudio.commons.util.PlaceholderResolver} (see
 * {@code PlaceholderResolver.resolvePlaceholders(String)}).</p>
 *
 * <p>Resolution behavior (as implemented by {@code PlaceholderResolver}):
 * resolution first checks Java system properties ({@code System.getProperty}),
 * then environment variables ({@code System.getenv}). If a placeholder key is
 * not found an {@link IllegalArgumentException} is thrown by the resolver.
 * The annotation itself does not perform placeholder resolution — callers
 * must explicitly invoke the resolver or a reader that does so.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @Configuration(filePath = "${env}-configurations.properties")
 * public class TestConfiguration { ... }
 *
 * // A reader/binder might perform:
 * String resolvedPath = PlaceholderResolver.resolvePlaceholders(TestConfiguration.class.getAnnotation(Configuration.class).filePath());
 * // then load resolvedPath from the classpath
 * }</pre>
 *
 * <h2>Notes &amp; best practices</h2>
 * <ul>
 *   <li>Prefer a stable, predictable path structure under {@code src/main/resources}
 *       to avoid ambiguity across environments.</li>
 *   <li>When multiple configuration files exist (profiles/environments), keep
 *       them in separate directories (e.g. {@code env/dev/}, {@code env/prod/}).</li>
 *   <li>Document how the binder resolves missing files and how {@link PropertyKey#defaultValue}
 *       interacts with absent or empty properties.</li>
 *   <li>If you rely on placeholder resolution, ensure the environment variable
 *       or system property used by the placeholder is set in the runtime
 *       environment; otherwise {@code PlaceholderResolver} will throw an exception.</li>
 * </ul>
 *
 * @see PropertyKey
 * @see in.testautomationstudio.commons.util.PlaceholderResolver
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configuration {
    /**
     * @return File path relative to the application's resources/classpath root.
     *
     * <p>The returned string may contain simple placeholders of the form
     * {@code ${KEY}}. Consumers (for example a {@code PropertiesReader} or
     * other binder) that support placeholders should pass this value through
     * {@link in.testautomationstudio.commons.util.PlaceholderResolver#resolvePlaceholders(String)}
     * before attempting to locate and load the resource. Missing placeholder
     * keys will result in an {@link IllegalArgumentException} from the resolver.</p>
     */
    String filePath();
}
