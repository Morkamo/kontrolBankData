package ru.morkamo.kontrolbankdata.security;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@NullMarked
public class AppUserPrincipal implements UserDetails {

    @Getter
    private final Short id;
    private final String username;
    private final String password;
    @Getter
    private final Integer departmentId;

    public AppUserPrincipal(Short id, String username, String password, Integer departmentId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.departmentId = departmentId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
