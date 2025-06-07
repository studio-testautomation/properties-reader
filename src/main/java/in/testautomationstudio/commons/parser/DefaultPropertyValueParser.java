package in.testautomationstudio.commons.parser;

public class DefaultPropertyValueParser implements PropertyValueParser<Object> {
    @Override
    public Object parse(String value) {
        return value;
    }
}