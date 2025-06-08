package com.library.enums;

public enum UserType {
    STUDENT("ROLE_STUDENT"),
    STAFF("ROLE_STAFF"),
    LIBRARIAN("ROLE_LIBRARIAN");

    private final String role;

    UserType(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static UserType fromString(String text) {
        for (UserType type : UserType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Type d'utilisateur non reconnu : " + text);
    }
}