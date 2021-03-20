# Overview
This project shows how http request and response can be logged in a DynamoDB table. Sometime an application have requirement to record access in a database. Storing access requests in database serves multiple purpose:
1. Review who accessed what in the application
2. Which functions are most used in the application and by whom

This project is a spring boot application which utilizes filters to extract useful information from HTTP request and response. It uses DynamoDB client to store extracted information in AWS DynamoDB table.
