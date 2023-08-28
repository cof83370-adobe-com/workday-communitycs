# Workday Community AEM

This is an AEM project of Wokday Community site. This repository contains community AEM components and integrations of community react library and related systems.

This project is generated with AEM [Archetype 40](https://confluence.workday.com/display/WEBS/Regeneration+of+community+AEM+repository+Archetype+40)

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* it.tests: Java based integration tests
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, and templates
* ui.content: contains sample content using the components from the ui.apps
* ui.config: contains runmode specific OSGi configs for the project
* ui.frontend: an optional dedicated front-end build mechanism (Angular, React or general Webpack project)
* ui.tests: Selenium based UI tests
* all: a single content package that embeds all of the compiled modules (bundles and content packages) including any vendor dependencies
* analyse: this module runs analysis on the project which provides additional validation for deploying into AEMaaCS

## How to build

### BEFORE running maven commands please do the following

* Ensure AWS CLI is setup and configured locally.
  * If you are a contractor, reach out for special AWS credentials otherwise proceed with the following step.
  * Run `okta2aws login` or use your provided AWS credentials in your AWS CLI config  
* Set the environmental variable for your role.
  ```bash
  export AWS_PROFILE=<Your role name>
  ```
* Add the following environmental variable to your shell or rerun it if outputting the variable shows no value
    ```bash
     export CODEARTIFACT_AUTH_TOKEN=`aws codeartifact get-authorization-token --domain community-workday --domain-owner 210939474461 --region us-west-2 --query authorizationToken --output text`
    ```
* **For all local maven commands use `mvn -s .cloudmanager/maven/settings.xml <your maven command>` or link the settings.xml to your local mvn installation (usually .m2 directory)**

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

To build all the modules and deploy the `all` package to a local instance of AEM, run in the project root directory the following command:

    mvn clean install -PautoInstallSinglePackage

Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallSinglePackagePublish

Or alternatively

    mvn clean install -PautoInstallSinglePackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

Or to deploy only a single content package, run in the sub-module directory (i.e `ui.apps`)

    mvn clean install -PautoInstallPackage

## Testing

There are three levels of testing contained in the project:

### Unit tests

This show-cases classic unit testing of the code contained in the bundle. To
test, execute:

    mvn clean test

### Integration tests

This allows running integration tests that exercise the capabilities of AEM via
HTTP calls to its API. To run the integration tests, run:

    mvn clean verify -Plocal

Test classes must be saved in the `src/main/java` directory (or any of its
subdirectories), and must be contained in files matching the pattern `*IT.java`.

The configuration provides sensible defaults for a typical local installation of
AEM. If you want to point the integration tests to different AEM author and
publish instances, you can use the following system properties via Maven's `-D`
flag.

| Property | Description | Default value |
| --- | --- | --- |
| `it.author.url` | URL of the author instance | `http://localhost:4502` |
| `it.author.user` | Admin user for the author instance | `admin` |
| `it.author.password` | Password of the admin user for the author instance | `admin` |
| `it.publish.url` | URL of the publish instance | `http://localhost:4503` |
| `it.publish.user` | Admin user for the publish instance | `admin` |
| `it.publish.password` | Password of the admin user for the publish instance | `admin` |

The integration tests in this archetype use the [AEM Testing
Clients](https://github.com/adobe/aem-testing-clients) and showcase some
recommended [best
practices](https://github.com/adobe/aem-testing-clients/wiki/Best-practices) to
be put in use when writing integration tests for AEM.

## AEM Pipeline Check
AEM cloud pipelines run a set of three maven commands. The first two perform a dependency check, the third is the package build.
Running these locally will help reduce the frequency of pipeline failures.

```bash
mvn --batch-mode org.apache.maven.plugins:maven-dependency-plugin:3.1.2:resolve-plugins
mvn --batch-mode org.apache.maven.plugins:maven-clean-plugin:3.1.0:clean -Dmaven.clean.failOnError=false
mvn --batch-mode org.jacoco:jacoco-maven-plugin:prepare-agent package
```
**Special Note:** The package build may show errors, this is expected for a local run. Give it a minute to finish and the pipeline should still successfully build and show something similar to the text below.
```bash
[INFO] Workday Community Site ............................. SUCCESS [  0.178 s]
[INFO] Workday Community Site - Core ...................... SUCCESS [01:30 min]
[INFO] Workday Community Site - UI Frontend ............... SUCCESS [ 16.253 s]
[INFO] Workday Community Site - Repository Structure Package SUCCESS [  0.507 s]
[INFO] Workday Community Site - UI apps ................... SUCCESS [  1.826 s]
[INFO] Workday Community Site - UI content ................ SUCCESS [  1.532 s]
[INFO] Workday Community Site - UI config ................. SUCCESS [  0.063 s]
[INFO] Workday Community Site - All ....................... SUCCESS [  0.850 s]
[INFO] Workday Community Site - Integration Tests ......... SUCCESS [  4.091 s]
[INFO] Workday Community Site - Dispatcher ................ SUCCESS [  0.021 s]
[INFO] Workday Community Site - UI Tests .................. SUCCESS [  0.032 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
```

## Static Analysis

The `analyse` module performs static analysis on the project for deploying into AEMaaCS. It is automatically
run when executing

    mvn clean install

from the project root directory. Additional information about this analysis and how to further configure it
can be found here https://github.com/adobe/aemanalyser-maven-plugin

### UI tests

They will test the UI layer of your AEM application using Selenium technology. 

To run them locally:

    mvn clean verify -Pui-tests-local-execution

This default command requires:
* an AEM author instance available at http://localhost:4502 (with the whole project built and deployed on it, see `How to build` section above)
* Chrome browser installed at default location

Check README file in `ui.tests` module for more details.

## ClientLibs

The frontend module is made available using an [AEM ClientLib](https://helpx.adobe.com/experience-manager/6-5/sites/developing/using/clientlibs.html). When executing the NPM build script, the app is built and the [`aem-clientlib-generator`](https://github.com/wcm-io-frontend/aem-clientlib-generator) package takes the resulting build output and transforms it into such a ClientLib.

A ClientLib will consist of the following files and directories:

- `css/`: CSS files which can be requested in the HTML
- `css.txt` (tells AEM the order and names of files in `css/` so they can be merged)
- `js/`: JavaScript files which can be requested in the HTML
- `js.txt` (tells AEM the order and names of files in `js/` so they can be merged
- `resources/`: Source maps, non-entrypoint code chunks (resulting from code splitting), static assets (e.g. icons), etc.

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html

## Maven Profiles

AEM has its own unique way of handling profiles. Unfortunately due to this we may have near identical duplicate entries.
This is due to AEM relying on special environmental variables that are used in the AEM cloud build pipelines. The intent is to remove this and the need for the three maven commands check upon completion of WCDEVOPS-5602.
See the following documentation for reference:
**https://experienceleague.adobe.com/docs/experience-manager-cloud-manager/content/getting-started/project-creation/project-setup.html?lang=en**
