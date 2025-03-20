package mains.fixer;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** How long the python language detector should be kept alive for without interaction. */
@BindingAnnotation
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface DetectLanguageTimeout {
}
