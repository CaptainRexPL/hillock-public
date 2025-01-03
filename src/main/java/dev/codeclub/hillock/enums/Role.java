package dev.codeclub.hillock.enums;

import java.util.Arrays;

public enum Role {

    /**
     * GUEST - new user of the service
     * MEMBER - regular/trusted user
     * MODERATOR - user with moderation permissions
     * DEVELOPER - user with developer permissions
     * ADMIN - owner of the service
     */

    GUEST(1),
    MEMBER(2),
    POKER_MANAGER(3),
    MODERATOR(100),
    DEVELOPER(999),
    ADMIN(1000);

    public final int level;

    Role(int level) {
        this.level = level;
    }

    public static boolean contains(String test) {
        return Arrays.stream(Role.values())
                .anyMatch(role -> role.name().equals(test));
    }
}