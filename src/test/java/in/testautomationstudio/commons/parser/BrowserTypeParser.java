package in.testautomationstudio.commons.parser;

import in.testautomationstudio.commons.enums.BrowserType;

public class BrowserTypeParser implements PropertyValueParser<BrowserType> {

    @Override
    public BrowserType parse(String value) {
        if (value == null || value.isEmpty()) {
            return BrowserType.CHROME; // or throw an exception based on your requirements
        }

        for (BrowserType browserType : BrowserType.values()) {
            if (browserType.getBrowserName().equalsIgnoreCase(value)) {
                return browserType;
            }
        }

        throw new IllegalArgumentException("Unknown browser type: " + value);
    }
}
