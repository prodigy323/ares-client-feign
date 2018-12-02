# ares-client-feign
Client application using feign to make API calls

## ToDo
- [ ] set up client UI to manage data

----

### Main dependencies and components
* Actuator
* Web
* DevTools
* Lombok
* Open Feign
* Netflix Ribbon
* HATEOAS
* Data Rest
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <java.version>1.8</java.version>
    <spring-cloud-services.version>2.0.2.RELEASE</spring-cloud-services.version>
    <spring-cloud.version>Greenwich.M1</spring-cloud.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-hateoas</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-rest</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>io.pivotal.spring.cloud</groupId>
            <artifactId>spring-cloud-services-dependencies</artifactId>
            <version>${spring-cloud-services.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>

<repositories>
    <repository>
        <id>spring-milestones</id>
        <name>Spring Milestones</name>
        <url>https://repo.spring.io/milestone</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>
</repositories>
```

### bootstrap.yml
```yaml
server:
  port: 8091
  
spring:
  application:
    name: ares-client-feign
  cloud:
    config:
      uri: http://localhost:8900
```

### ares-client-feign.yml
```yaml
ares-service-h2:
  ribbon:
    eureka:
      enabled: false
    listOfServers: ares-service-h2
```

### Main Application class
```java
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableFeignClients
@SpringBootApplication
public class AresClientFeignApplication {

	public static void main(String[] args) {
		SpringApplication.run(AresClientFeignApplication.class, args);
	}
}
```

### FeignClient.java
<pre>
Response types:
Resource - for single object
Resources - for multiple objects (Collection[])
</pre>
```java
@org.springframework.cloud.openfeign.FeignClient("ares-service-h2")
public interface FeignClient {

    @RequestMapping(path = "/heroes", method = RequestMethod.GET)
    Resources<Hero> getHeroes();

}
```

### FeignConfiguration.java
> not sure what this does - thought it increases log level for feign operations
```java
@Configuration
public class FeignConfiguration {

    @Bean
    public Logger.Level logLevel(){
        return Logger.Level.FULL;
    }

}
```

### Model class for response object
```java
@Data
public class Hero {

    private String firstName;
    private String lastName;
    private String codeName;
    private String email;
    private String team;

    public Hero() {}

    public Hero(String firstName, String lastName, String codeName, String email, String team) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.codeName = codeName;
        this.email = email;
        this.team = team;
    }

}
```

### Controller class
```java
@RestController
@Slf4j
@RequestMapping("/hero")
public class HeroController {

    @Autowired
    private FeignClient feignClient;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Collection<Hero> getHero() {
        return feignClient.getHeroes().getContent();
    }

    // TODO: CRUD operations

    // TODO: UI javascript to present data
}
```

### Dockerfile
```dockerfile
FROM openjdk:8-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
EXPOSE 8900
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```

### Jenkinsfile
```groovy
def mvnTool
def prjName = "ares-service-mysql"
def imageTag = "latest"

pipeline {
    agent { label 'maven' }
    options {
        buildDiscarder(logRotator(numToKeepStr: '2'))
        disableConcurrentBuilds()
    }
    stages {
        stage('Build && Test') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    script {
                        mvnTool = tool 'Maven'
                        sh "${mvnTool}/bin/mvn -B clean verify sonar:sonar -Prun-its,coverage"
                    }
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(execPattern: 'target/jacoco.exec')
                }
            }
        }
        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage('Release && Publish Artifact') {

        }
        stage('Create Image') {
            steps {
                sh "docker build --build-arg JAR_FILE=target/${prjName}-${releaseVersion}.jar -t ${prjName}:${releaseVersion}"
            }
        }
        stage('Publish Image') {
            steps {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'JENKINS_ID', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    sh """
                        docker login -u ${USERNAME} -p ${PASSWORD} dockerRepoUrl
                        docker push ...
                    """
                }
            }
        }
    }
}
```