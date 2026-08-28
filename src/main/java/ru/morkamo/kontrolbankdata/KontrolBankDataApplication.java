package ru.morkamo.kontrolbankdata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KontrolBankDataApplication {

    public static void main(String[] args) {
        Path databaseConfig = Path.of("config", "database.yaml");

        if (Files.notExists(databaseConfig)) {
            createDatabaseConfig(databaseConfig);
            System.out.println("Created config/database.yaml.");
            return;
        }

        SpringApplication.run(KontrolBankDataApplication.class, args);
    }

    private static void createDatabaseConfig(Path databaseConfig) {
        String content = """
                spring:
                  datasource:
                    url: jdbc:mysql://localhost:3306/control?useSSL=false&useUnicode=true&characterEncoding=UTF-8
                    username: root
                    password: CHANGE_ME
                """;

        try {
            Files.createDirectories(databaseConfig.getParent());
            Files.writeString(databaseConfig, content);
        } catch (IOException exception) {
            throw new RuntimeException("Could not create config/database.yaml", exception);
        }
    }

}
