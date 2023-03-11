package com.caspar.eservicemall.common.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
// 可以同时指定多个不同的校验器
@Constraint(
        validatedBy = {ListValueConstraintValidator.class }
)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
/**
 * 自定义校验注解
 * **/
public @interface ListValue {
    String message() default "{com.caspar.eservicemall.common.valid.ListValue.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int[] vals() default {};
}
