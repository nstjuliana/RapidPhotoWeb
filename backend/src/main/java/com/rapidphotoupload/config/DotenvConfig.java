package com.rapidphotoupload.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Application context initializer to load .env file before Spring Boot starts.
 * 
 * This allows running 'mvn spring-boot:run' directly without needing start.ps1.
 * The .env file is loaded and environment variables are set as system properties
 * before Spring Boot reads application.yml.
 */
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger logger = LoggerFactory.getLogger(DotenvConfig.class);
    private static boolean loaded = false;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (loaded) {
            return; // Only load once
        }

        try {
            // Load .env file from the backend directory (where pom.xml is)
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();

            // Set as system properties (Spring Boot reads ${VAR} from both env vars and system properties)
            int count = 0;
            for (var entry : dotenv.entries()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Only set if not already set (system/env vars take precedence)
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    count++;
                }
            }

            if (count > 0) {
                logger.info("Loaded {} environment variables from .env file", count);
            }
            loaded = true;
        } catch (Exception e) {
            logger.warn("Could not load .env file: {}. Using system environment variables.", e.getMessage());
        }
    }
}

