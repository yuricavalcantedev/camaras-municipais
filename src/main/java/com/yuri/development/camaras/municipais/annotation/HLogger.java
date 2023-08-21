package com.yuri.development.camaras.municipais.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HLogger {

    int id() default  0;
    String description() default "";
    boolean hasUUID() default false;
}
