package in.testautomationstudio.commons.reader;

import in.testautomationstudio.commons.annotation.Configuration;
import in.testautomationstudio.commons.annotation.PropertyKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Properties;

public class PropertiesReader<T> implements ConfigurationReader<T> {
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
                    setFieldValue(field, bean, properties.getProperty(key, defaultValue));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFieldValue(Field field, T target, Object value) {
        // If the value is not provided then do not set the field
        if (value == null || StringUtils.isBlank((String) value)) {
            return;
        }
        try {
            Class<?> clazz = field.getType();
            if (clazz.isEnum()) {
                value = createEnumInstance(clazz, (String) value);
            } else if (clazz.isAssignableFrom(Integer.class)) {
                value = Integer.parseInt((String) value);
            } else if (clazz == int.class) {
                value = Integer.parseInt((String) value);
                setIntegerValue(field, (Integer) value, target);
                return;
            } else if (clazz.isAssignableFrom(Float.class)) {
                value = Float.parseFloat((String) value);
            } else if (clazz == float.class) {
                value = Float.parseFloat((String) value);
                setFloatValue(field, (Float) value, target);
                return;
            } else if (clazz.isAssignableFrom(Double.class)) {
                value = Double.parseDouble((String) value);
            } else if (clazz == double.class) {
                value = Double.parseDouble((String) value);
                setDoubleValue(field, (Double) value, target);
                return;
            } else if (clazz.isAssignableFrom(Boolean.class)) {
                value = Boolean.parseBoolean((String) value);
            } else if (clazz == boolean.class) {
                value = Boolean.parseBoolean((String) value);
                setBooleanValue(field, (Boolean) value, target);
            }
            FieldUtils.writeField(field, target, value, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setIntegerValue(Field field, int value, Object bean) throws IllegalAccessException {
        field.setAccessible(true);
        field.setInt(bean, value);
    }

    private void setFloatValue(Field field, float value, Object bean) throws IllegalAccessException {
        field.setAccessible(true);
        field.setFloat(bean, value);
    }

    private void setDoubleValue(Field field, double value, Object bean) throws IllegalAccessException {
        field.setAccessible(true);
        field.setDouble(bean, value);
    }

    private void setBooleanValue(Field field, boolean value, Object bean) throws IllegalAccessException {
        field.setAccessible(true);
        field.setBoolean(bean, value);
    }

    @SuppressWarnings("unchecked")
    private <U extends Enum<U>> U createEnumInstance(Type enumType, String name) {
        return Enum.valueOf((Class<U>) enumType, name);
    }
}
