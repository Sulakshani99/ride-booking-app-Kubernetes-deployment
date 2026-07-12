package com.ridebooking.auth.model;

public enum AccountRole {
    USER,
    DRIVER,
    ADMIN;

    public String landingPage() {
        return switch (this) {
            case ADMIN -> "/admin";
            case DRIVER -> "/driver";
            case USER -> "/";
        };
    }
}