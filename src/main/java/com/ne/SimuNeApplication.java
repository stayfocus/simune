package com.ne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimuNeApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(SimuNeApplication.class);

    public static void main(String[] args) {
        logger.info("SimuNe start ...");
        SpringApplication.run(SimuNeApplication.class, args);
        logger.info("SimuNe started...");
    }
}
