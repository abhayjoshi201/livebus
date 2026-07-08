package livebus.security.config;

import livebus.security.model.Role;
import livebus.security.model.User;
import livebus.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default Admin user if none exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            String adminPassword = System.getenv("ADMIN_PASSWORD");
            if (adminPassword == null || adminPassword.isEmpty()) {
                adminPassword = "admin123";
            }
            User admin = new User("admin", passwordEncoder.encode(adminPassword), Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Default admin user created successfully.");
        }

        // Seed default Driver user if none exists
        if (userRepository.findByUsername("driver").isEmpty()) {
            User driver = new User("driver", passwordEncoder.encode("driver123"), Role.DRIVER);
            userRepository.save(driver);
            System.out.println("Default driver user created successfully.");
        }
    }
}
