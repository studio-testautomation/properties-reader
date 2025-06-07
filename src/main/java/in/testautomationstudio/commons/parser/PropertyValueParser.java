package in.testautomationstudio.commons.parser;

public interface PropertyValueParser<T> {
    T parse(String value);
}