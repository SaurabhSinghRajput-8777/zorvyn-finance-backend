package com.saurabh.finance.user.service;

import com.saurabh.finance.common.enums.Role;
import com.saurabh.finance.common.enums.UserStatus;
import com.saurabh.finance.user.entity.User;
import com.saurabh.finance.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User defaultAdmin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(defaultAdmin);
            log.info("System initialized: Default admin user created (username: 'admin').");
        } else {
            log.debug("Database already seeded, skipping default admin creation.");
        }
    }
}
