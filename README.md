# Overview
This project shows how http request and response can be logged in a DynamoDB table. Sometime an application have requirement to record access to its uris in a database. Storing access requests in database serves multiple purpose:
1. Review who accessed what in the application
2. Which functions are most used in the application and by whom

This project is a spring boot application which utilizes filters to extract useful information from HTTP request and response and store in DynamoDB table.

# Setup
Create a DynamoDB table in region us-west-2:
1. Table name:	AUDIT_LOG
2. Primary partition key:	app_id (String)
3. Primary sort key:	timestamp (Number)

Follow AWS SDK [instructions](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html) to configure user credentials.

# How to run
Run "MovieApplication" Spring Boot application.

Using basic auth username and password setup in UserDetailsService invoke API in Postman: http://localhost:8080/movies

Browse to DynamoDB table in AWS console to view logged items. 

Following attributes are stored in DynamoDB table:

* app_id
* timestamp
* request_time 
* response_time 
* time_taken = (response_time - request_time) milliseconds
* status = HTTP Status
* uri = GET /movies
* user = authenticated username

# Metrics using MicroMeter
This application also uses micrometer to generate custom metric - count uri by user. As URI and user are dynamic, code uses Counter.builder and tags to dynamically count requests made by user.

pom.xml
```
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

AuditLoggerFilter.java
* Use Counter.builder to create a requestCounter builder

```
requestCounter = Counter.builder("api.request");
```

* Use tags to add user and uri requested
```
requestCounter.tags("uri", req.getRequestURI(), "user", currentUserName).register(registry).increment();
```

Now this metric is available to view at (http://localhost:8080/actuator/prometheus)

Example:
```
# HELP api_request_total  
# TYPE api_request_total counter
api_request_total{app_id="my-audit-logger",uri="/v1/movies",user="gabbar",} 7.0
api_request_total{app_id="my-audit-logger",uri="/actuator/prometheus",user="-",} 10.0
api_request_total{app_id="my-audit-logger",uri="/actuator/prometheus",user="gabbar",} 2.0
```

You can add this metric to your grafana dashboard.

![grafana image](https://github.com/arvindhiman/auditlogger/blob/main/grafana.png?raw=true)






