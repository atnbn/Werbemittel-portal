package com.miniportal.security.dto;

import com.miniportal.security.AuthenticatedUser;
import com.miniportal.security.Role;

public record MeResponse(String username, Role role) {

    public static MeResponse from(AuthenticatedUser user) {
        return new MeResponse(user.getUsername(), user.getRole());
    }
}
