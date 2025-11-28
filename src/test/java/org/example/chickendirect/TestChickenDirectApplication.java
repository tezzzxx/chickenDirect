package org.example.chickendirect;

import org.springframework.boot.SpringApplication;

public class TestChickenDirectApplication {

    public static void main(String[] args) {
        SpringApplication.from(ChickenDirectApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
