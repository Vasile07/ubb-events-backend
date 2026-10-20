package com.ubb.events;

public enum EffectiveOrgRole {
    NONE, MEMBER, MANAGER;

    public boolean atLeast(EffectiveOrgRole minimum) {
        return ordinal() >= minimum.ordinal();
    }
}
