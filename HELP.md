# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.7/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.7/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.7/reference/web/servlet.html)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.4.7/reference/using/devtools.html)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/3.4.7/specification/configuration-metadata/annotation-processor.html)
* [JDBC API](https://docs.spring.io/spring-boot/3.4.7/reference/data/sql.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.7/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Data JDBC](https://docs.spring.io/spring-boot/3.4.7/reference/data/sql.html#data.sql.jdbc)
* [Flyway Migration](https://docs.spring.io/spring-boot/3.4.7/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway)
* [Resilience4J](https://docs.spring.io/spring-cloud-circuitbreaker/reference/spring-cloud-circuitbreaker-resilience4j.html)
* [OpenFeign](https://docs.spring.io/spring-cloud-openfeign/reference/)
* [WebSocket](https://docs.spring.io/spring-boot/3.4.7/reference/messaging/websockets.html)
* [Spring Web Services](https://docs.spring.io/spring-boot/3.4.7/reference/io/webservices.html)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/3.4.7/reference/data/nosql.html#data.nosql.mongodb)
* [Cloud Bootstrap](https://docs.spring.io/spring-cloud-commons/reference/spring-cloud-commons/application-context-services.html)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/3.4.7/reference/actuator/index.html)
* [Spring Data Redis (Access+Driver)](https://docs.spring.io/spring-boot/3.4.7/reference/data/nosql.html#data.nosql.redis)
* [Validation](https://docs.spring.io/spring-boot/3.4.7/reference/io/validation.html)
* [Spring Batch](https://docs.spring.io/spring-boot/3.4.7/how-to/batch.html)
* [MyBatis Framework](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
* [Java Mail Sender](https://docs.spring.io/spring-boot/3.4.7/reference/io/email.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Relational Data using JDBC with Spring](https://spring.io/guides/gs/relational-data-access/)
* [Managing Transactions](https://spring.io/guides/gs/managing-transactions/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Using Spring Data JDBC](https://github.com/spring-projects/spring-data-examples/tree/master/jdbc/basics)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Using WebSocket to build an interactive web application](https://spring.io/guides/gs/messaging-stomp-websocket/)
* [Producing a SOAP web service](https://spring.io/guides/gs/producing-web-service/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Messaging with Redis](https://spring.io/guides/gs/messaging-redis/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Creating a Batch Service](https://spring.io/guides/gs/batch-processing/)
* [MyBatis Quick Start](https://github.com/mybatis/spring-boot-starter/wiki/Quick-Start)

### Additional Links
These additional references should also help you:

* [Declarative REST calls with Spring Cloud OpenFeign sample](https://github.com/spring-cloud-samples/feign-eureka)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

