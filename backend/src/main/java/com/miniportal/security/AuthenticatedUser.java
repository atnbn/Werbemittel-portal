package com.miniportal.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails, CredentialsContainer {

    private final String username;
    private final Role role;
    private final Long partnerId;
    private String passwordHash;

    public AuthenticatedUser(String username, String passwordHash, Role role, Long partnerId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.partnerId = partnerId;
    }

    public Role getRole() {
        return role;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}
