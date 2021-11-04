# Component Test Demo For Spring Boot Using JUnit

## Overview
The purpose of this repository is to demonstrate the implementation of a fully automated integration test using JUnit + API Mock Server (Wiremock) + Docker Test Containers.

The sample application is a message driven insurance claim review process. \

When a claim request is received from message queue, this service fetches customer record from customer service via REST API followed by system logic to determine the claim review result.
The claim review result will be sent to a message exchange for further distribution.

![Application Component Diagram](https://github.com/gavinklfong/spring-junit-test-container-demo/blob/main/blob/Sample_App.png?raw=true)

## Build

Run this command to build and run the sample automated test

`mvn -Dtest=ClaimProcessingAutoEnvTest clean test`

## Component Test Initialization

There are 3 component test cases for claim request handling for high risk customer, medium risk customer and low risk customer respectively.

The sample JUnit test class is `component.tests.HighRiskCustomerClaimProcessingIntegrationTest`. It extends from `integration.AbstractComponentTest` which is responsible for the following setup:
- Initialize rabbitmq and mongodb in test containers
- Start up API mock server (i.e. wiremock) and register it as a bean
- Bring up Spring Boot application for testing.

## Test Case Pattern

Since test scenarios are to submit clain request for different scenarios, all test cases share similar `given-when-then` pattern:
Given:
- Setup a message queue and bind to the output message exchange
- Setup mock customer API

When:
- Submit claim request to message queue

Then:
- Check claim result in database
- Verify message in the output queue
