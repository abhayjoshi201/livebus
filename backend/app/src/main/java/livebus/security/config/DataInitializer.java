package livebus.security.config;

import livebus.security.model.Role;
import livebus.security.model.User;
import livebus.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
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

        // Clean tables to force clean seed
        jdbcTemplate.update("TRUNCATE TABLE stops CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE trips CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE routes CASCADE");
        jdbcTemplate.update("TRUNCATE TABLE buses CASCADE");

        // Seed routes & stops
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM routes", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO routes (id, route_number, start_point, end_point) VALUES (?, ?, ?, ?)",
                    UUID.fromString("d1111111-1111-1111-1111-111111111111"), "D-1", "ISBT", "Clement Town Campus");

            jdbcTemplate.update("INSERT INTO routes (id, route_number, start_point, end_point) VALUES (?, ?, ?, ?)",
                    UUID.fromString("d2222222-2222-2222-2222-222222222222"), "D-2", "Clock Tower", "Clement Town Campus");

            jdbcTemplate.update("INSERT INTO routes (id, route_number, start_point, end_point) VALUES (?, ?, ?, ?)",
                    UUID.fromString("b1111111-1111-1111-1111-111111111111"), "B-1", "Haldwani", "Bhimtal Campus");

            jdbcTemplate.update("INSERT INTO routes (id, route_number, start_point, end_point) VALUES (?, ?, ?, ?)",
                    UUID.fromString("e1111111-1111-1111-1111-111111111111"), "H-1", "Lalkuan", "Haldwani Campus");

            // Seed Stops
            seedStopSql("d1111111-1111-1111-1111-111111111111", "ISBT Terminal", 30.2872, 77.9984, 1);
            seedStopSql("d1111111-1111-1111-1111-111111111111", "Turner Road Junction", 30.2862, 78.0012, 2);
            seedStopSql("d1111111-1111-1111-1111-111111111111", "Subhash Nagar Chowk", 30.2785, 78.0055, 3);
            seedStopSql("d1111111-1111-1111-1111-111111111111", "Clement Town Campus", 30.2700, 78.0084, 4);

            seedStopSql("d2222222-2222-2222-2222-222222222222", "Clock Tower", 30.3244, 78.0411, 1);
            seedStopSql("d2222222-2222-2222-2222-222222222222", "Prince Chowk", 30.3200, 78.0400, 2);
            seedStopSql("d2222222-2222-2222-2222-222222222222", "Saharanpur Road Crossing", 30.3160, 78.0380, 3);
            seedStopSql("d2222222-2222-2222-2222-222222222222", "ISBT Dehradun", 30.2872, 77.9984, 4);
            seedStopSql("d2222222-2222-2222-2222-222222222222", "Clement Town Campus", 30.2700, 78.0084, 5);

            seedStopSql("b1111111-1111-1111-1111-111111111111", "Haldwani Bus Station", 29.2170, 79.5180, 1);
            seedStopSql("b1111111-1111-1111-1111-111111111111", "Kathgodam Rly Station", 29.2670, 79.5380, 2);
            seedStopSql("b1111111-1111-1111-1111-111111111111", "Ranibagh Toll Plaza", 29.2970, 79.5420, 3);
            seedStopSql("b1111111-1111-1111-1111-111111111111", "Bhimtal Lake Crossing", 29.3450, 79.5490, 4);
            seedStopSql("b1111111-1111-1111-1111-111111111111", "Bhimtal Campus", 29.3510, 79.5480, 5);

            seedStopSql("e1111111-1111-1111-1111-111111111111", "Lalkuan Junction", 29.0770, 79.5090, 1);
            seedStopSql("e1111111-1111-1111-1111-111111111111", "Motahaldu Stop", 29.1370, 79.5100, 2);
            seedStopSql("e1111111-1111-1111-1111-111111111111", "Bareilly Road Crossing", 29.1970, 79.5120, 3);
            seedStopSql("e1111111-1111-1111-1111-111111111111", "Haldwani Campus", 29.2220, 79.5110, 4);

            System.out.println("Default routes and stops seeded successfully.");
        }

        // Seed buses
        if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM buses", Integer.class) == 0) {
            jdbcTemplate.update("INSERT INTO buses (id, license_plate, capacity, bus_type) VALUES (?, ?, ?, ?)",
                    UUID.fromString("11111111-1111-1111-1111-111111111111"), "UA-07-TA-2024", 40, "Standard");

            jdbcTemplate.update("INSERT INTO buses (id, license_plate, capacity, bus_type) VALUES (?, ?, ?, ?)",
                    UUID.fromString("22222222-2222-2222-2222-222222222222"), "UA-07-TA-4050", 40, "Standard");

            jdbcTemplate.update("INSERT INTO buses (id, license_plate, capacity, bus_type) VALUES (?, ?, ?, ?)",
                    UUID.fromString("33333333-3333-3333-3333-333333333333"), "UA-04-TC-8821", 50, "Standard");

            jdbcTemplate.update("INSERT INTO buses (id, license_plate, capacity, bus_type) VALUES (?, ?, ?, ?)",
                    UUID.fromString("44444444-4444-4444-4444-444444444444"), "UA-04-TB-5340", 30, "Standard");

            System.out.println("Default buses seeded successfully.");
        }
    }

    private void seedStopSql(String routeId, String stopName, double lat, double lon, int sequence) {
        jdbcTemplate.update(
            "INSERT INTO stops (id, route_id, stop_name, location, stop_sequence) VALUES (?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326), ?)",
            UUID.randomUUID(),
            UUID.fromString(routeId),
            stopName,
            lon, // X coordinate
            lat, // Y coordinate
            sequence
        );
    }
}
