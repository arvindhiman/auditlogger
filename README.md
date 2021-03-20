# Overview
This project shows how http request and response can be logged in a DynamoDB table. Sometime an application have requirement to record access in a database. Storing access requests in database serves multiple purpose:
1. Review who accessed what in the application
2. Which functions are most used in the application and by whom

This project is a spring boot application which utilizes filters to extract useful information from HTTP request and response. It uses DynamoDB client to store extracted information in AWS DynamoDB table.

# Setup
Create a DynamoDB table in region us-west-2:
1. Table name:	AUDIT_LOG
2. Primary partition key:	app_id (String)
3. Primary sort key:	timestamp (Number)

Follow AWS SDK [instructions](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html) to configure user credentials.

# How to run
Run "MovieApplication" Spring Boot application.

Using basic auth username and password setup in UserDetailsService invoke API in Postman: http://localhost:8080/movies

Browse to DynamoDB table in AWS console to view logged items. This project stores following attributes in DynamoDB table:

* app_id
* timestamp
* request_time
* response_time
* time_taken
* status
* uri
* user



