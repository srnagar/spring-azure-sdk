# Spring Boot application with Azure SDK

## Requirements
+ JDK 8
+ Spring Boot 2.1.6.RELEASE
+ Azure SDK
  - Identity 1.0.0-preview.1
  - KeyVault Keys 4.0.0-preview.1

We will use IntelliJ for demonstrating how to create a simple RESTful service using 
spring boot and Maven to interact with Azure services through Azure SDK. As an example, 
we will use [KeyVault Keys](https://docs.microsoft.com/en-us/azure/key-vault/about-keys-secrets-and-certificates?redirectedfrom=MSDN#key-vault-keys) service.

## Create a new project 
![New project](./images/new_project.png)
 

## Maven and JDK 
![Maven](./images/maven_jdk8.png)

## Group and artifact
Provide maven group and artifact id to complete the project creation process.

## Project structure 
After creating the project, you should see the following directory structure
![Project](./images/project_dir.png)

## Dependencies
To configure dependencies, open `pom.xml` file. It should look similar to the one below
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.azuredemo</groupId>
  <artifactId>azuredemo</artifactId>
  <version>1.0-SNAPSHOT</version>
</project>
```
First, we need to make spring boot starter the parent of this project. Add the following snippet inside `<project>` scope.
```
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.6.RELEASE</version>
  </parent>
```

Now, we need to add the dependencies
+ Spring Boot web starter
+ Azure SDK
   - Identity
   - KeyVault Keys

Add the following xml snippet to your `<project>`

```
<dependencies>
    <!-- Spring Boot web starter -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Azure Identity -->
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-identity</artifactId>
      <version>1.0.0-preview.1</version>
    </dependency>

    <!-- Azure KeyVault - Keys -->
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-keyvault-keys</artifactId>
      <version>4.0.0-preview.1</version>
    </dependency>
  </dependencies>
```
After adding this, maven will download the necessary jars from 
[Maven repository](https://mvnrepository.com/repos/central)


## Spring Boot Application

#### Main
To run a simple REST service, create a java package `com.azuresdk.init` and a `Main` class. This 
class will be your starting point for your application to run. Annotate the class with 
`@SpringBootApplication`. Create a main method and write a single line of code to run the spring 
application as shown below.

```
package com.azuresdk.init;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

  public static void main(String[] args) {
    SpringApplication.run(Main.class);
  }
}

```
#### Controller and REST API
Create a controller class that will host your REST service API. If you put this in a separate 
package, make sure to include this package in spring package scan. To do that, go to your `Main` 
class and update the `@SpringBootApplication` to `@SpringBootApplication(scanBasePackages = 
{"com.azuresdk.controller"})`. A sample controller class which exposes a simple `GET` api to list 
KeyVault Keys is shown below:

```
package com.azuresdk.controller;

import com.azuresdk.services.KeyVaultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AzureController {
  private static final Logger LOGGER = LoggerFactory.getLogger(AzureController.class);
  
  @Autowired
  private KeyVaultService keyVaultService;

  @GetMapping(path = "/key_vault/list_keys")
  public String listKeys(@RequestParam(value = "async", required = false, defaultValue = "false") boolean isAsync) {
    LOGGER.info("Listing KeyVault keys using " + (isAsync ? "async" : "sync") + " key client");
    try {
      StringBuilder sb = new StringBuilder();
      if (isAsync) {
        keyVaultService
            .listKeysAsync()
            .doOnNext(keyBase -> sb.append(keyBase.name()).append(", "))
            .subscribe();
      } else {
        keyVaultService
            .listKeys()
            .forEach(keyBase -> sb.append(keyBase.name()).append(", "));
      }
      sb.setLength(sb.length() - 1);
      LOGGER.info("Successfully listed KeyVault keys");
      return sb.toString();
    } catch (Exception ex) {
      LOGGER.error("Failed to list KeyVault keys", ex.getMessage());
      return "Failed to list KeyVault keys";
    }
  }
}
```

You will need the following KeyVault Keys service interface and class. Note that in order for the 
`KeyVaultServiceImpl` class to be autowireable, the package should be added to the spring 
package scanner in your `Main` class.

``` 
package com.azuresdk.services;

import com.azure.security.keyvault.keys.models.KeyBase;
import reactor.core.publisher.Flux;

public interface KeyVaultService {

  Iterable<KeyBase> listKeys();

  Flux<KeyBase> listKeysAsync();

}

```

Replace `https://mykeyvault.vault.azure.net/` with your KeyVault endpoint.

```
package com.azuresdk.services.impl;

import com.azure.identity.credential.DefaultAzureCredential;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azuresdk.services.KeyVaultService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class KeyVaultServiceImpl implements KeyVaultService {

  @Override
  public Iterable<KeyBase> listKeys() {
    KeyClient keyClient = KeyClient.builder()
        .credential(new DefaultAzureCredential())
        .endpoint("https://mykeyvault.vault.azure.net/")
        .build();
    return keyClient.listKeys();
  }

  @Override
  public Flux<KeyBase> listKeysAsync() {
    KeyAsyncClient keyAsyncClient = KeyAsyncClient.builder()
        .credential(new DefaultAzureCredential())
        .endpoint("https://mykeyvault.vault.azure.net/")
        .build();
    return keyAsyncClient.listKeys();
  }
}
```

## Build and Run
With this setup, now we are almost ready to run the application. Before we can actually run this, 
there's one more thing we need to do in `pom.xml`. Setup the build plugin. Add the following
snippet to `<project` section.

``` 
<build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <executions>
          <execution>
            <id>build-info</id>
            <goals>
              <goal>build-info</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Now, we are ready to spin up the service. Go to your main class and run the application.  You should 
see logs similar to this:

``` 
2019-07-14 22:16:39,737 INFO  [main] o.s.b.w.e.tomcat.TomcatWebServer: Tomcat initialized with port(s): 8080 (http)
2019-07-14 22:16:39,794 INFO  [main] o.a.catalina.core.StandardService: Starting service [Tomcat]
2019-07-14 22:16:39,794 INFO  [main] o.a.catalina.core.StandardEngine: Starting Servlet engine: [Apache Tomcat/9.0.21]
2019-07-14 22:16:40,005 INFO  [main] o.a.c.c.C.[Tomcat].[localhost].[/]: Initializing Spring embedded WebApplicationContext
2019-07-14 22:16:40,005 INFO  [main] o.s.web.context.ContextLoader: Root WebApplicationContext: initialization completed in 2744 ms
2019-07-14 22:16:40,659 INFO  [main] o.s.s.c.ThreadPoolTaskExecutor: Initializing ExecutorService 'applicationTaskExecutor'
2019-07-14 22:16:41,059 INFO  [main] o.s.b.w.e.tomcat.TomcatWebServer: Tomcat started on port(s): 8080 (http) with context path ''
2019-07-14 22:16:41,081 INFO  [main] com.azuresdk.init.Main: Started Main in 5.13 seconds (JVM running for 7.894)
```

## Make a service call
 Use the following url http://localhost:8080/key_vault/list_keys for listing 
 keys using synchronous client. To list keys asynchronously, use http://localhost:8080/key_vault/list_keys?async=true.
 
## Logging

Azure SDK uses [slf4j](https://www.slf4j.org/) loggers. So, you can use any logging framework 
like `java.util.logging`, `logback` or `log4j`. More details on logging using slf4j can be found 
[here](https://www.slf4j.org/manual.html). 

#### Configuring log level for Azure SDK
In order for Azure SDK to attempt logging, set the logging level in your environment variable.
`export AZURE_LOG_LEVEL <n>` where `n` can range from 1 to 5. By default, the log level is set to
 5 (disabled). If a log message from Azure SDK has to be logged, first it has to be at a level 
 above the log level set in this environment variable. Next, the underlying logging framework should
 be enabled to log messages at this level.

Azure SDK log levels
```
1 - Verbose
2 - Info
3 - Warning
4 - Error
5 - Disabled
```

#### Configuring logback
[Logback](https://logback.qos.ch/manual/introduction.html) is one of the popular logging frameworks.
 To enable logback logging, create a file called `logback.xml` under `./src/main/resources` directory.
 This file will contain the logging configurations to customize your logging needs. More information 
 on configuring `logback.xml` can be found [here](https://logback.qos.ch/manual/configuration.html). 
 Create another file called `application.properties` under the same directory `./src/main/resources`.
 Spring looks at this file for various configurations including logging. You can configure your application
 to read logback configurations from any file. So, this is where you will link your `logback.xml` to 
 your spring application. Add the following line to do so:
 ```
logging.config=classpath:logback.xml
```

A simple logback configuration to log to console can be configured as follows:

``` 
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="Console"
    class="ch.qos.logback.core.ConsoleAppender">
    <layout class="ch.qos.logback.classic.PatternLayout">
      <Pattern>
        %black(%d{ISO8601}) %highlight(%-5level) [%blue(%t)] %blue(%logger{100}): %msg%n%throwable
      </Pattern>
    </layout>
  </appender>

  <root level="INFO">
    <appender-ref ref="Console" />
  </root>
</configuration>
```

To configure logging to a file which is rolled over after each hour and archived in gzip format:
```
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <property name="LOGS" value="./logs" />
  <appender name="RollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOGS}/spring-boot-logger.log</file>
    <encoder
      class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
      <Pattern>%d %p %C{1.} [%t] %m%n</Pattern>
    </encoder>

    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <!-- rollover hourly and gzip logs -->
      <fileNamePattern>${LOGS}/archived/spring-boot-logger-%d{yyyy-MM-dd-HH}.log.gz</fileNamePattern>
    </rollingPolicy>
  </appender>

  <!-- LOG everything at INFO level -->
  <root level="info">
    <appender-ref ref="RollingFile" />
  </root>
</configuration>
```
 
