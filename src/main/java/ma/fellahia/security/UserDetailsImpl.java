package ma.fellahia.security;

import ma.fellahia.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserDetailsImpl implements UserDetails {

    private final UUID id;
    private final String phone;
    private final String password;
    private final String role;
    private final boolean verified;

    public UserDetailsImpl(User user) {
        this.id       = user.getId();
        this.phone    = user.getPhone();
        this.password = user.getPassword();
        this.role     = user.getRole().name();
        this.verified = user.isVerified();
    }

    public UUID getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public String getPassword()               { return password; }
    @Override public String getUsername()               { return phone; }
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return verified; }
}
