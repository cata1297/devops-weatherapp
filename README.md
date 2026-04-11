# devops-homework

Welcome, weary traveler! You have arrived at our homework for applicants of our DevOps Engineer position. Here are the instructions you should follow. Good luck, and may the odds be ever in your favour!

## The app

A simple Spring Boot 3 application running on Java 17. Here's a little documentation to get you started.

Config files:
1. `application.properties` — Spring config
2. `log4j2-weather.yml` — logging config

> **Note:** don't be afraid to edit the said two config files. On the contrary, that's part of the question.

Endpoints:
- `GET /` — calls a weather API and renders the result

## The exercise

1. **Fork this repository.**

2. **Build a GitHub Actions pipeline** triggered on push and pull request to the main branch, with the following steps:
   1. Unit tests run
   2. Docker image built
   3. Docker image pushed to an artifact repository
   4. _(Absolutely optional)_ SAST/DAST scans somewhere in the pipeline

3. **Deploy the application** to any cloud provider of your choice. Calling the `/` endpoint should show the weather conditions of **Miskolc**!
   1. _The catch:_ place a shell script in the container that runs every hour to check whether a specific env var is set in the container; if it's not, it should log to stderr. The name and value of the env var is totally up to you.
   2. _(Optional)_ Even with the application running, there might be a suspicious error message appearing in the logs after calling the endpoint. Feel free to investigate and fix it.

Please send us the URL of **the forked repository** and the **URL of the deployed web application**.

Thank you!

## Solution Overview

My goal for this homework was to keep the delivery flow reproducible, clear, and cost-conscious.

### Application changes

- The application configuration was adjusted so the `/` endpoint returns the weather for **Miskolc**, as required.
- The hardcoded OpenWeather API key was removed from the versioned application configuration.
- The logging configuration was fixed to remove the suspicious runtime logging issue caused by the original Log4j2 appender setup.
- The test suite was updated to work correctly with Spring Boot 3.

### Containerization

- The application is packaged with a multi-stage Docker build.
- A shell script is included in the container to check whether a chosen environment variable is present.
- That script is executed periodically from the container entrypoint, and it logs to `stderr` when the variable is missing.
- This keeps the application process and the periodic validation logic clearly separated.

### CI/CD design

The repository uses separate workflows for application delivery and infrastructure delivery.

#### Application workflow

The application pipeline is designed to:

- run unit tests,
- run a SAST dependency and filesystem scan,
- build the Docker image,
- push the image to Amazon ECR on pushes to the main branch,
- avoid running unnecessarily for Terraform-only changes.

I intentionally kept the application pipeline focused on validating and publishing the container image.

#### Terraform workflows

Infrastructure is handled separately with Terraform:

- `Terraform Plan` runs on pull requests, so infrastructure changes can be reviewed before merge.
- `Terraform Apply` is separated from planning and protected with manual approval through a GitHub environment.

This separation was chosen intentionally:

- it reduces the risk of accidental infrastructure changes,
- it keeps costs under control,
- and it provides a clearer review trail for infrastructure modifications.

### AWS architecture

The target runtime architecture is:

- Amazon ECR for the Docker image repository,
- Amazon ECS Fargate for running the application,
- an Application Load Balancer for a stable public URL,
- AWS Secrets Manager for the OpenWeather API key,
- CloudWatch Logs for container logs,
- Terraform for infrastructure provisioning.


### Security and secret handling

- Secrets are not committed to the repository.
- The OpenWeather API key is intended to be stored in AWS Secrets Manager.
- Terraform creates the secret resource, while the secret value can be populated separately to avoid storing sensitive values in Terraform state.

### Current delivery approach

The intended release flow is:

1. build and publish the image to ECR,
2. provision infrastructure with Terraform,
3. inject the OpenWeather secret through AWS Secrets Manager,
4. run the application on ECS Fargate behind the load balancer,
5. verify that the public endpoint serves the Miskolc weather page.

### Repository structure

The repository is split by responsibility:

- `src/` contains the Spring Boot application code.
- `docker/` contains the container helper scripts, including the periodic environment variable check required by the task.
- `.github/workflows/ci-cd.yml` contains the application delivery pipeline.
- `.github/workflows/terraform.yml` contains the Terraform validation and planning workflow for pull requests.
- `.github/workflows/terraform-apply.yml` contains the approved infrastructure apply workflow.
- `terraform/` contains the AWS infrastructure definitions, separated by concern such as ECR, IAM, ECS, ALB, logs, secrets, and outputs.

### Deployment notes

- The infrastructure is intentionally sized for a low-cost demo deployment.
- ECS Fargate is configured with a single small task rather than a scaled service.
- The ALB is kept because it provides a stable public URL for later verification.
- Secrets Manager is used for the OpenWeather API key so the key does not need to live in Git.
- The deployment flow is designed so that infrastructure changes are reviewable before they are applied.

