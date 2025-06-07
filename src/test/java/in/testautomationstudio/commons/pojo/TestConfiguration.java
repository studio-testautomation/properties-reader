package in.testautomationstudio.commons.pojo;

import in.testautomationstudio.commons.annotation.Configuration;
import in.testautomationstudio.commons.annotation.PropertyKey;
import in.testautomationstudio.commons.enums.BrowserType;
import in.testautomationstudio.commons.parser.BrowserTypeParser;

@Configuration(filePath = "test-configurations.properties")
public class TestConfiguration {
    @PropertyKey(key = "string.property")
    private String stringProperty;

    @PropertyKey(key = "int.property")
    private int intProperty;

    @PropertyKey(key = "integer.wrapper.property")
    private Integer integerWrapperProperty;

    @PropertyKey(key = "float.property")
    private float floatProperty;

    @PropertyKey(key = "float.wrapper.property")
    private Float floatWrapperProperty;

    @PropertyKey(key = "double.property")
    private double doubleProperty;

    @PropertyKey(key = "double.wrapper.property")
    private Double doubleWrapperProperty;

    @PropertyKey(key = "boolean.property")
    private boolean booleanProperty;

    @PropertyKey(key = "boolean.wrapper.property")
    private Boolean booleanWrapperProperty;

    @PropertyKey(key = "browser.name", defaultValue = "chrome", parser = BrowserTypeParser.class)
    private BrowserType browserType;

    public String getStringProperty() {
        return stringProperty;
    }

    public int getIntProperty() {
        return intProperty;
    }

    public Integer getIntegerWrapperProperty() {
        return integerWrapperProperty;
    }

    public float getFloatProperty() {
        return floatProperty;
    }

    public Float getFloatWrapperProperty() {
        return floatWrapperProperty;
    }

    public double getDoubleProperty() {
        return doubleProperty;
    }

    public Double getDoubleWrapperProperty() {
        return doubleWrapperProperty;
    }

    public boolean getBooleanProperty() {
        return booleanProperty;
    }

    public Boolean getBooleanWrapperProperty() {
        return booleanWrapperProperty;
    }

    public BrowserType getBrowserType() {
        return browserType;
    }
}
