# Properties Reader

Reads Java properties files and binds property values to POJOs using annotations and pluggable value parsers.

This project provides a light-weight, annotation-driven utility to map keys from a properties file to fields on a Java
object. It supports built-in parsing for common primitive and wrapper types, enum resolution, custom parsers, and
placeholder-based file path resolution.

Contents

- Overview
- Quick start
- Annotations
    - `@Configuration`
    - `@PropertyKey`
- Using `PropertiesReader`
    - Basic example
    - Overriding the file path
    - Using placeholders (e.g. `${env}`)
    - Custom parser example
- Parsers
    - `PropertyValueParser` (interface)
    - `DefaultPropertyValueParser`
- Error handling & behavior
- API reference (short)

Overview
--------

The library converts entries in Java properties files into values set on POJO fields.
Fields are annotated with `@PropertyKey` to indicate the property key to read and optionally a parser and default value.
A configuration class can be annotated with `@Configuration` to declare the properties resource to use. The
`PropertiesReader` (an implementation of `ConfigurationReader`) performs reflection, reads the properties file, converts
values, and assigns them to the annotated fields.

Quick start
-----------
Use the library as a dependency in your project.

Maven:
```xml
<dependency>
    <groupId>in.testautomationstudio</groupId>
    <artifactId>properties-reader</artifactId>
    <version>1.0.1</version>
</dependency>
```

Gradle:
```groovy
dependencies {
    implementation 'in.testautomationstudio:properties-reader:1.0.1'
}
```

Annotations
-----------

`@Configuration`

- Place on a class to declare the classpath-relative properties file path: `filePath`.
- The `filePath` may include simple placeholders of the form `${KEY}`.

`@PropertyKey`

- Place on individual fields to indicate which property key maps to the field.
- Elements:
    - `key()` - property name in the properties file.
    - `defaultValue()` - optional default to use when the property is missing (empty string means no default provided).
    - `parser()` - a `PropertyValueParser` class to convert the raw string into the field's type. If omitted, the
      `DefaultPropertyValueParser` will be used or the reader will attempt built-in conversions for common types.

Using `PropertiesReader`
-----------------------

Basic example

Configuring POJO class:

```java
import in.testautomationstudio.commons.annotation.Configuration;
import in.testautomationstudio.commons.annotation.PropertyKey;

@Configuration(filePath = "app-configurations.properties")
public class AppConfig {
  @PropertyKey(key = "service.url", defaultValue = "http://localhost:8080")
  private String serviceUrl;

  @PropertyKey(key = "service.timeout", defaultValue = "30")
  private int timeoutSeconds;

  // getters
  public String getServiceUrl() {
    return serviceUrl;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }
}
```
Reading values:

```java
import in.testautomationstudio.commons.reader.ConfigurationReader;

public class Main {
    static void main(String[] args) {
      AppConfig appConfig = new AppConfig();

      ConfigurationReader<AppConfig> configurationReader = new PropertiesReader<>();
      configurationReader.loadBean(appConfig);
      // appConfig fields are now populated from app-configurations.properties (or defaults)
    }
}
```

Using placeholders in `@Configuration.filePath`

The `filePath` in `@Configuration` can contain simple placeholders like `${env}`. These are resolved automatically
by the annotation:

```java
import in.testautomationstudio.commons.annotation.Configuration;
import in.testautomationstudio.commons.annotation.PropertyKey;

@Configuration(filePath = "${env}-configurations.properties")
public class AppConfig {
  @PropertyKey(key = "service.url", defaultValue = "http://localhost:8080")
  private String serviceUrl;

  @PropertyKey(key = "service.timeout", defaultValue = "30")
  private int timeoutSeconds;

  // getters
  public String getServiceUrl() {
    return serviceUrl;
  }

  public int getTimeoutSeconds() {
    return timeoutSeconds;
  }
}
```

Note: While loading the values, `PropertiesReader` first checks Java system properties (`System.getProperty`) for `env` then
environment variables (`System.getenv`). If the placeholder key (`env`) is not found then resolver throws
IllegalArgumentException.

Custom parser example

If you need to convert property strings to custom types, implement `PropertyValueParser<T>` and reference it from
`@PropertyKey(parser = YourParser.class)`.

```java
import in.testautomationstudio.commons.parser.PropertyValueParser;

public class CommaSeparatedListParser implements PropertyValueParser<List<String>> {
    @Override
    public List<String> parse(String value) {
        if (value == null || value.isEmpty()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}

public class ListConfig {
    @PropertyKey(key = "items", parser = CommaSeparatedListParser.class)
    private List<String> items;
}
```

Parsers
-------

`PropertyValueParser<T>`

- Interface used to convert raw string values to target types.
- Implementations should return `null` for `null` inputs and may return `null` or a specific value for empty strings
  depending on desired semantics.

`DefaultPropertyValueParser`

- Returns the raw string as an `Object` (pass-through). Useful when the field is `String` or when the caller wants the
  raw value.

Built-in conversions

- `PropertiesReader` contains built-in parsers for common primitive and wrapper types: int/Integer, float/Float,
  double/Double, boolean/Boolean.
- Enums are resolved with `Enum.valueOf(Class, String)` when no custom parser is provided.

Error handling & behavior
------------------------

- Missing resource file: a `FileNotFoundException` wrapped in a `RuntimeException` is thrown when the reader cannot load
  the properties resource.
- Custom parser instantiation failure: a `RuntimeException` is thrown describing the parser class and cause.
- Reflection errors when setting fields are wrapped in a `RuntimeException`.
- When a property value is missing or blank, the reader receives the `@PropertyKey.defaultValue()` and uses it with
  `Properties.getProperty(key, defaultValue)` semantics.
- If the resolved property value is `null` or blank (as defined by `StringUtils.isBlank`), the reader will not modify
  the field (the existing field value remains).

API reference (short)
---------------------

- `in.testautomationstudio.commons.annotation.Configuration` — annotate classes to declare which properties file to use.
- `in.testautomationstudio.commons.annotation.PropertyKey` — annotate fields with property keys, optional default value
  and parser.
- `in.testautomationstudio.commons.parser.PropertyValueParser<T>` — interface for value parsers.
- `in.testautomationstudio.commons.parser.DefaultPropertyValueParser` — pass-through parser.
- `in.testautomationstudio.commons.reader.ConfigurationReader<T>` — reader interface.
- `in.testautomationstudio.commons.reader.PropertiesReader<T>` — implementation that reads properties and binds to
  annotated POJOs.
- `in.testautomationstudio.commons.util.PlaceholderResolver` — utility to resolve simple `${KEY}` placeholders against
  system properties and environment variables.
