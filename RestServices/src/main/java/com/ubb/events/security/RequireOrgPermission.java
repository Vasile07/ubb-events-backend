package com.ubb.events.security;

import com.ubb.events.EffectiveOrgRole;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOrgPermission {
    EffectiveOrgRole value() default EffectiveOrgRole.MEMBER;
}
