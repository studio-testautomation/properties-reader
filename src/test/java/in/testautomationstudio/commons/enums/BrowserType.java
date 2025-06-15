package in.testautomationstudio.commons.enums;

public enum BrowserType {
    CHROME("chrome"),
    FIREFOX("firefox");

    private final String browserName;

    BrowserType(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserName() {
        return browserName;
    }
}
