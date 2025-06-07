package in.testautomationstudio.commons.reader;

import in.testautomationstudio.commons.annotation.Configuration;
import in.testautomationstudio.commons.annotation.PropertyKey;
import in.testautomationstudio.commons.parser.DefaultPropertyValueParser;
import in.testautomationstudio.commons.parser.PropertyValueParser;
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

public class PropertiesReader<T> implements ConfigurationReader<T> {
    private static final Map<Class<?>, PropertyValueParser<?>> PARSERS = new HashMap<>();
    private static final String PLACEHOLDER_START = "${";
    private static final String PLACEHOLDER_END = "}";

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

    public PropertiesReader() {
    }

    public PropertiesReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void loadBean(T bean) {
        Class<?> cls = bean.getClass();
        if (this.filePath == null && cls.isAnnotationPresent(Configuration.class)) {
            this.filePath = cls.getAnnotation(Configuration.class).filePath();
        }
        // Interpolate variables in filePath
        this.filePath = resolvePlaceholders(this.filePath);

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

    private static String resolvePlaceholders(String text) {
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

    @SuppressWarnings("unchecked")
    private <U extends Enum<U>> U createEnumInstance(Class<?> enumType, String name) {
        return Enum.valueOf((Class<U>) enumType, name);
    }
}
