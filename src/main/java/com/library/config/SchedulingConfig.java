package com.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling in the application.
 * This allows the use of @Scheduled annotations to run tasks at specific intervals.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // No additional configuration needed, the @EnableScheduling annotation does the work
}