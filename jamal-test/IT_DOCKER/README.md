
# Integration Tests using Docker

This directory contains the integration tests.
Integration tests are executed using Docker containers.

The shell script `integrationtest` is created from the source `integrationtest.jam`.
The Jamal source is the skeleton of the integration test script.
It includes other scripts so that after the processing there will be one giant monolithic shell script.
It eases the maintenance of the docker image.

The included script file names are `test_ ... .sh.jam`.
The files named `test_ ... .txt.jam` are processed during the test.
Their result is compared to the expected result.
These files are `test_ ... .txt`.


Some integration tests are included from other subprojects.
Those files are developed testing their functionality locally on the development machine.
When they work they are included into the top level `integrationtest.jam` file.


## Executing the integration tests

The tests can be created and executed using the `test.sh` script.
This script creates the test scripts from the local Jamal sources, builds the Docker image and runs the tests.

Prerequisites:

* Docker must be installed and running.

* The script must be executed from the directory containing the `integrationtest` and the `Dockerfile`.

* The docker instance running should have internet connection

  ** to GitHub for cloning Jamal source

  ** Maven central for building Jamal

Note that the processing of the Jamal sources to prepare the integration test is done with the locally installed development version, but the integration tests will be executed cloning the Jamal repository from GitHub.

## Integration tests implemented

This section describes the integration tests implemented.
Each subsection is a test, part of, and executed by the `integrationtest` script.


### Basic Maven Compilation

The first step is to build Maven.
Because the container does not have a local repo it will provide a fresh build downloading all dependencies from central.

### Setting Security

When the macro `maven:load` is used, it requires that the configuration in `~/jamal` is secure.
It must not be read or modified by other users.
The test sets the file permissions to the directory and the configuration file in different ways and runs tests.

### Command Line execution

The test runs the command line tool `jamal` with different parameters.
