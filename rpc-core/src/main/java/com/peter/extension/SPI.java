package com.peter.extension;

import java.lang.annotation.*;

/**
 * @author Peter
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {
    String value() default "";
}
