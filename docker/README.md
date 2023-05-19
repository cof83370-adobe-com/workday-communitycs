# Local Docker Stack

## Pre-start requirements
* Ensure AWS CLI is setup and configured locally.
    * If you are a contractor, reach out for special AWS credentials otherwise proceed with the following step.
    * Run `okta2aws login` or use your provided AWS credentials in your AWS CLI config
* Set the environmental variable for your AWS Profile. This must be set even if you use `[default]` in your aws credentials/config files for the container to build correctly. 
  ```bash
  export AWS_PROFILE=<Your role name>
  ```
* Add the following environmental variable to your shell or rerun it if outputting the variable shows no value
    ```bash
    export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain community-workday --domain-owner 210939474461 --region us-west-2 --query authorizationToken --output text`
    ```
* Setup XCode (required for using the [Makefile](../Makefile) on mac)
    ```bash
    # Install xcode
    xcode-select --install
    # Verify Installation
    xcode-select -p
    # If you run a Make command and get an error then the following should fix the issue
    xcode-select --reset
    ```

## Getting Started
1. AWS CLI must be authenticated and CodeArtifact token must be less than 12 hours old 
2. Run the standard `docker compose` commands or use the [Makefile](../Makefile) in the root directory for simplified commands. 

***If the docker image is not already on your local machine, or during the first `docker compose up/docker compose build` command you may get the error` ✘ publisher Error`. 
This error can be ignored, it will still eventually create and start the container once the image build is completed.***

Refer to the primary project [README.md](../README.md) for additional information on how to interact with documentation.

### Reference Urls:

* [AEM Local Development Environment Set up](https://experienceleague.adobe.com/docs/experience-manager-learn/cloud-service/local-development-environment-set-up/overview.html?lang=en)
* [Set up your IDE for AEM](https://experienceleague.adobe.com/docs/experience-manager-learn/foundation/development/set-up-a-local-aem-development-environment.html?lang=en#set-up-an-integrated-development-environment)
