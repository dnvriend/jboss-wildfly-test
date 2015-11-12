# jboss-wildfly-test
This is a study project into [WildFly](http://wildfly.org/), formerly known as JBoss AS, an application server developed by Red Hat. 
[WildFly](https://en.wikipedia.org/wiki/WildFly) is written in Java, and implements the [Java Platform, Enterprise Edition 7 specification](http://docs.oracle.com/javaee/7/index.html).
 
This study project will focus on some aspects of the application server, notably, new features, configuration and management of 
the application server; adding users. jdbc data sources and jms connections, the new classloader (using JBoss Modules) and how
to integrate our favorite programming language [Scala](http://www.scala-lang.org/) in the server to develop with ease. 

# Launching te project
The project uses the `launch.sh` script to build a custom wildfly distribution that adds an administrator user with
username `admin` and password `admin`. The administrator console is available at `http://boot2docker:9990/console`.

# Docker
JBoss distributes the Wildfy application server as a [docker container](https://hub.docker.com/r/jboss/wildfly/). We will
use this distribution for our tests. The source code of the Jboss Wildfy docker images is available [here](https://github.com/JBoss-Dockerfiles/wildfly).  

Wildfy is available in the `/opt/jboss/wildfly` directory, which is available in the `$JBOSS_HOME` environment variable and 
has the following [directory structure](https://docs.jboss.org/author/display/WFLY9/Getting+Started+Guide):
 
- __appclient__: configuration files, deployment content, and writable areas used by the application client container 
 are run from this installation.
- __bin__: all the server binaries, the startup script and also scripts that are used to setup the JVM startup parameters 
 such as memory, heap memory size, among others, are present here.
- __docs__: contains the license files, sample configurations, documentation, and other documents.
- __domain__: will only be used if the server is started in the domain mode. Inside are the settings files, 
 deployments contents, and areas of writing used by the server to store log files and temporary files.
- __modules__: The WildFly classloading is structured in a modular way. All the necessary modules are stored in this 
 directory. Also, the new modules that are installed should be stored here following the directory pattern. This directory 
 should only be modified if you know what you're doing to avoid classloading issues.
- __standalone__: will only be used when the server is started in the standalone mode. Here are configuration files, 
 deployment contents, and areas of writing used by the server to store temporary files and logs, as well as the domain mode.
- __welcome-content__: This is an area of internal use of the server that should not be used by end users unless they want 
 to overwrite and insert their own application in the / context. Its alteration can influence the functioning of WildFly, 
 welcome pages, or error messages that are present in this directory.
 
# Two modes of operation
WildFly has two modes of operation: `standalone` and `domain`. The main difference between the two modes is scalability. 
The standalone mode works alone and the domain mode works with one or more servers (cluster mode). By default, the docker
container will launch the `standalone` mode using the script `$JBOSS_HOME/bin/standalone.sh`, but you can override
this behavior by changing the `wildfly/Dockerfile` description.
 
The `standalone` and `domain` mode each have their own directory; `$JBOSS_HOME/standalone` and `$JBOSS_HOME/domain`. 
These directories have the following structure:

- __configuration__: contains all the configuration files that are necessary to run this installation
- __data__: files written by the server during startup and their lifecycle; directory is not present is domain mode
- __deployments__: place your deployment content (e.g. war, ear, jar, sar files) to have it automatically deployed into the server runtime
- __lib/ext__: location for installed library jars referenced by applications using the extension-list mechanism
- __log__: the server's log files
- __tmp__: temporary files written by the server

# Standalone mode
We will only focus on the `standalone` mode. The following files are present in the `$JBOSS_HOME/standalone/configuration` directory:
 
- __application-roles.properties__: contains the user permissions to access the applications
- __application-users.properties__: contains user information and password to access applications
- __mgmt-users.properties__: contains user information and password for access to the management console
- __mgmt-groups.properties__: contains group information and administration permissions that the users have. 
- __logging.properties__: contains the initial bootstrap logging configuration for the AS instance. This boostrap logging 
 configuration is replaced with the logging configuration specified in the `standalone.xml` file once the server boot has 
 reached the point where that configuration is available.
- __standalone-full-ha.xml__: It is a Java Enterprise Edition 6 certified full-profile configuration file with a high 
 availability and the full list of services.
- __standalone-ha.xml__: It is a Java Enterprise Edition 6 certified full-profile configuration file with high availability 
 and the basic services.
- __standalone-full.xml__: It is a Java Enterprise Edition 6 full-profile certified configuration file that includes all the 
 technologies required by the full-profile specification, including most other OSGI with the full services.
- __standalone.xml__: It is a Java Enterprise Edition 6 full-profile certified configuration file that includes all the 
 technologies required by the Full Profile specification including most other OSGI with the full services
 
See [wikipedia](https://en.wikipedia.org/wiki/Java_Platform,_Enterprise_Edition#Web_profile) for information about the web profiles.

# Scripts for everyday use
The `$JBOSS_HOME/bin` directory contains the following scripts for everyday use:

- __add-user.sh__: used to create an application or management user. 
- __domain.sh__: the domain mode startup script
- __domain.conf__: the boot configuration file that is used by domain.sh to set the boot parameters
- __standalone.sh__: the standalone mode startup script
- __standalone.conf__: the startup configuration file that is used by standalone.sh to set the boot parameters

# Configurating the application server
WildFly has all your settings centralized in a single file that is divided into subsystems that can be changed according 
to your needs. All management features are exposed through a Command Line Interface a web management interface, a native Java API, 
and through the Rest API-based HTTP / JSON and JMX.
 
## Command-Line Interface
The `$JBOSS_HOME/bin/jboss-cli.sh` script is used to configure Wildfly. To connect to the application server type:

```bash
$ cd $JBOSS_HOME/bin
# ./jboss-cli.sh --connect
[standalone@localhost:9990 /] 
```

> __Note:__ When running locally to the WildFly process the CLI will silently authenticate against the server by exchanging tokens on the file system, the purpose of this exchange is to verify that the client does have access to the local file system. If the CLI is connecting to a remote WildFly installation then you will be prompted to enter the username and password of a user already added to the realm.

Once connected you can add, modify, remove resources and deploy or undeploy applications. It is also possible to
put all the commands you wish to execute in a file and run all commands (one command per line) sequentially.

# Modular 
Hierarchical classloading often causes several problems, mainly library conflicts between different versions of the same library, 
but this ended in WildFly thanks to its modular structure that only links the JAR file to your application when it needs it.

# Installing a postgres datasource
Installing a datasource consists of the following steps:

- Packaging a JDBC4 compatible database driver,
- Adding a module that will define the postgres driver, and the dependencies,
- Adding a jdbc-driver definition that points to a module to use, and defines a the class names for the driver,
- Adding a datasource configuration that contains all the connection specifics

Using the `$JBOSS_HOME/bin/jboss-cli.sh` script, that can execute commands from a file, these steps can be executed
when building the docker image. With the `execute.sh` script I copied from [Marek Goldmann's wildfly docker configuration](https://github.com/goldmann/wildfly-docker-configuration) project,
it will launch Wildfly and then configure it when building the docker image. Below is an example of such a commands script.


```
# Add a module that contains the Postgres driver
module add --name=org.postgres --resources=/opt/jdbc/postgresql-9.4-1205.jdbc42.jar --dependencies=javax.api,javax.transaction.api

# Add postgres driver
/subsystem=datasources/jdbc-driver=postgres:add(driver-name="postgres",driver-module-name="org.postgres",driver-class-name=org.postgresql.Driver,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)

# Add the datasource
data-source add --name=PostgresDS --driver-name=postgres --jndi-name=java:jboss/datasources/PostgresDS --connection-url=jdbc:postgresql://postgres:5432/test --user-name=postgres --password=secret --use-ccm=true --max-pool-size=5 --blocking-timeout-wait-millis=5000 --enabled=true --driver-class=org.postgresql.Driver --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter --jta=true --use-java-context=true --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker

# Execute the batch
run-batch
```
 
# Adding a user
By default WildFly is distributed with security enabled for the management interfaces, this means that before you connect 
using the administration console or remotely using the CLI you will need to add a new user, this can be achieved simply 
by using the `add-user.sh` script in the bin folder.

The `$JBOSS_HOME/bin/add-user.sh` script is used to add an application or management user. By default it adds a `management` user. 
Use the `-a` option to add an application user. To add a management user type:
 
```bash
# adds a user 'admin' with password '123'
cd $JBOSS_HOME/bin
$ ./add-user.sh admin 123
```

To add an application user type:

```bash
# adds an application user 'foo' with password '123'
cd $JBOSS_HOME/bin
$ ./add-user.sh foo 123
```
 
You can also type `add-user.sh` without any options to guide you through the process adding a user.  
 
# Application and management ports
Wildfy only has has two ports in use, an `application` port, which is `8080` and a `management` port, which is `9990`.
The `application` port is used for HTTP (servlet, JAX-RS, JAX-WS), Web Sockets, HTTP Upgraded Remoting (EJB Invocation, Remote JNDI). 
The `management` port is used for HTTP/JSON Management, HTTP Upgraded Remoting (Native Management & JMX), Web Administration Console.

This means that you have the following two urls to deal with:

- __Application__: http://boot2docker:8080
- __Management__: http://boot2docker:9990/console

# Logging
https://goldmann.pl/blog/2014/07/18/logging-with-the-wildfly-docker-image/

# Application deployment
https://docs.jboss.org/author/display/WFLY8/Application+deployment
http://tools.jboss.org/blog/2015-03-02-getting-started-with-docker-and-wildfly.html
https://goldmann.pl/blog/2014/07/23/customizing-the-configuration-of-the-wildfly-docker-image/#_using_code_jboss_cli_sh_code