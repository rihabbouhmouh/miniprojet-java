package com.eventmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class DatabaseConfig {
    // Configuration JPA additionnelle si nécessaire
}