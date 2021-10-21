# Test Cointainer Demo For Spring Boot Using JUnit

## Overview
The purpose of this repository is to demonstrate the implementation of a fully automated integration test using JUnit + Testcontainers.

The sample application is a message driven insurance claim review process.

![Application Component Diagram](https://github.com/gavinklfong/spring-test-container-demo/blob/main/blob/Sample_App.png?raw=true)

## Build

Run this command to build and run all automated tests

`mvn clean install`

If you would like to run the integration test only, then run this command

`mvn -Dtest=ClaimProcessingIntegrationTest test`

## Integration Test Initialization

The sample JUnit test class is `integration.tests.ClaimProcessingIntegrationTest`. It extends from `integration.AbstractIntegrationTest` which is responsible for initialize rabbitmq and mongodb in test containers and bring up the Spring Boot application for testing.

As part of the test case setup, a queue is binded to the exchange and so the test execution will get message on the queue for the test verification.