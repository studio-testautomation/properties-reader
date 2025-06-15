package in.testautomationstudio.commons.reader;

import in.testautomationstudio.commons.enums.BrowserType;
import in.testautomationstudio.commons.pojo.TestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PropertiesReaderTest {
    private static final TestConfiguration TEST_CONFIGURATION = new TestConfiguration();

    @BeforeAll
    public static void loadBean() {
        ConfigurationReader<TestConfiguration> configurationReader = new PropertiesReader<>();
        configurationReader.loadBean(TEST_CONFIGURATION);
    }

    @Test
    void verifyStringPropertyValue() {
        Assertions.assertEquals("string", TEST_CONFIGURATION.getStringProperty());
    }

    @Test
    void verifyIntPropertyValue() {
        Assertions.assertEquals(1, TEST_CONFIGURATION.getIntProperty());
    }

    @Test
    void verifyIntegerWrapperPropertyValue() {
        Assertions.assertEquals(2, TEST_CONFIGURATION.getIntegerWrapperProperty());
    }

    @Test
    void verifyFloatPropertyValue() {
        Assertions.assertEquals(1.2f, TEST_CONFIGURATION.getFloatProperty());
    }

    @Test
    void verifyFloatWrapperPropertyValue() {
        Assertions.assertEquals(2.2f, TEST_CONFIGURATION.getFloatWrapperProperty());
    }

    @Test
    void verifyDoublePropertyValue() {
        Assertions.assertEquals(1.122, TEST_CONFIGURATION.getDoubleProperty());
    }

    @Test
    void verifyDoubleWrapperPropertyValue() {
        Assertions.assertEquals(3.1123, TEST_CONFIGURATION.getDoubleWrapperProperty());
    }

    @Test
    void verifyBooleanPropertyValue() {
        Assertions.assertTrue(TEST_CONFIGURATION.getBooleanProperty());
    }

    @Test
    void verifyBooleanWrapperPropertyValue() {
        Assertions.assertFalse(TEST_CONFIGURATION.getBooleanWrapperProperty());
    }

    @Test
    void verifyBrowserTypePropertyValue() {
        Assertions.assertEquals(BrowserType.CHROME, TEST_CONFIGURATION.getBrowserType());
    }
}