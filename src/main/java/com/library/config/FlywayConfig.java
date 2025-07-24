package com.library.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayConfig {

    @Autowired
    private Environment env;

    @Bean(initMethod = "migrate")
    @DependsOn("dataSource")
    @Profile("!prod") // Désactiver en production
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration", "classpath:db/repair")
                .baselineOnMigrate(true)
                .outOfOrder(true)
                .validateOnMigrate(false)
                .load();
        
        // Force repair before migration
        flyway.repair();
        
        return flyway;
    }
}