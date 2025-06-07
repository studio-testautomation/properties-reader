package in.testautomationstudio.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import in.testautomationstudio.commons.parser.DefaultPropertyValueParser;
import in.testautomationstudio.commons.parser.PropertyValueParser;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyKey {
    String key();
    String defaultValue() default "";
    Class<? extends PropertyValueParser<?>> parser() default DefaultPropertyValueParser.class;
}
