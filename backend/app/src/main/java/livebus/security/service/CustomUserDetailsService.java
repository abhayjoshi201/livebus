package livebus.security.service;

import livebus.security.model.User;
import livebus.security.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try loading from database first (for Admin, Drivers, and explicitly registered users)
        java.util.Optional<User> dbUser = userRepository.findByUsername(username);
        
        if (dbUser.isPresent()) {
            User user = dbUser.get();
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPasswordHash())
                    .roles(user.getRole().name())
                    .build();
        }

        // If not in database, treat any username that doesn't start with admin/driver
        // as a dynamic passenger/student user whose password defaults to their username (Student ID)
        if (username != null && !username.trim().isEmpty() && 
            !username.toLowerCase().startsWith("admin") && 
            !username.toLowerCase().startsWith("driver")) {
            
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = 
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
            
            return org.springframework.security.core.userdetails.User.builder()
                    .username(username)
                    .password(encoder.encode(username)) // password matches the username itself (Student ID)
                    .roles(livebus.security.model.Role.PASSENGER.name())
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}