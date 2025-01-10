package dev.codeclub.hillock.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final dev.codeclub.hillock.database.model.User user;

    public CustomUserDetails(dev.codeclub.hillock.database.model.User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getHashedpassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !user.getDisabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getDisabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.getDisabled();
    }

    @Override
    public boolean isEnabled() {
        return user.getEmailverified();
    }

    public dev.codeclub.hillock.database.model.User getUser() {
        return user;
    }
}