package com.azuresdk.init;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.azuresdk.controller", "com.azuresdk.services.impl"})
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

}
