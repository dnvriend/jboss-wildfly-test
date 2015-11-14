# jboss-wildfly-test
This is a study project on [WildFly](http://wildfly.org/), formerly known as JBoss AS, an application server developed by Red Hat. 
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
[WildFly](http://wildfly.org/) has two modes of operation: `standalone` and `domain`. The main difference between the two modes is scalability. 
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

- __jboss-cli.sh__: used to configure Wildfly
- __add-user.sh__: used to create an application or management user 
- __standalone.sh__: the standalone mode startup script

# Configurating the application server
[WildFly](http://wildfly.org/) has all your settings centralized in a single file that is divided into subsystems that can be changed according 
to your needs. All management features are exposed through a Command Line Interface a web management interface, a native Java API, 
and through the Rest API-based HTTP / JSON and JMX.

# Wildfly configurations
[WildFly](http://wildfly.org/) 8 is the latest release in a series of JBoss open-source application server offerings. 
WildFly 8 is an exceptionally fast, lightweight and powerful implementation of the 
[Java Platform, Enterprise Edition 7 specification](http://docs.oracle.com/javaee/7/index.html). 

The state-of-the-art architecture built on the Modular Service Container enables services on-demand when your application 
requires them. The table on [this](https://docs.jboss.org/author/display/WFLY9/Getting+Started+Guide) page lists the 
Java Enterprise Edition 7 technologies and the technologies available in Wildfly 8 server configuration profiles:

- __standalone.xml (default)__: Java Enterprise Edition 7 web profile certified configuration with the required 
 technologies. 
- __standalone-ha.xml__: Java Enterprise Edition 7 web profile certified configuration with high availability
- __standalone-full.xml__: Java Enterprise Edition 7 full profile certified configuration including all the required EE 7 technologies
- __standalone-full-ha.xml__: Java Enterprise Edition 7 full profile certified configuration with high availability
 
> __Note__: The WildFly Web Profile doesn't include JMS (provided by HornetQ) by default, and the JMS module APIs in the runtime. If you want to use messaging, make sure you start the server using the "Full Profile" configuration (*-full.xml). 
 
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

# JBoss Modules (Modular Classloading) 
[JBoss Modules](https://docs.jboss.org/author/display/MODULES/Introduction) is a standalone implementation of a modular 
(non-hierarchical) class loading and execution environment for Java. In other words, rather than a single class loader which 
loads all JARs into a flat class path, each library becomes a module which only links against the exact modules it depends on, 
and nothing more. It implements a thread-safe, fast, and highly concurrent delegating class loader model, coupled to an 
extensible module resolution system, which combine to form a unique, simple and powerful system for application execution 
and distribution.

[JBoss Modules](https://docs.jboss.org/author/display/MODULES/Introduction) is designed to work with any existing library 
or application without changes, and its simple naming and resolution strategy is what makes that possible. Unlike OSGi, 
JBoss Modules does not implement a container; rather, it is a thin bootstrap wrapper for executing an application in a modular 
environment. The moment your application takes control, the modular environment is ready to load and link modules as needed. 
Furthermore, modules are never loaded (not even for resolution purposes) until required by a dependency, meaning that the 
performance of a modular application depends only on the number of modules actually used (and when they are used), 
rather than the total number of modules in the system. And, they may be unloaded by the user at any time.

Since module definition is essentially pluggable, a module can be defined in many different ways.  However, JBoss Modules 
ships with two basic implemented strategies which are most commonly utilized.

## Defining a module
The `first strategy` is the `static filesystem repository` approach. Modules are organized in a directory hierarchy on the 
filesystem which is derived from the name and version of the module. The content of the module's specific directory is 
comprised of a simple module descriptor and all of the content itself (JARs or loose files).

The `second strategy` is designed for `direct JAR execution`. It uses JAR MANIFEST.MF information to define simple dependencies 
and other module information, and is designed for executing JARs from the command line as well as situations where a JAR 
may be deployed in a container such as the JBoss Application Server.

For more information, please read [JBoss Modules - Module Descriptors](https://docs.jboss.org/author/display/MODULES/Module+descriptors)

# Installing a postgres datasource
Installing a datasource consists of the following steps:

- Packaging a JDBC4 compatible database driver,
- Adding a module that will define the postgres driver, and the dependencies,
- Adding a jdbc-driver definition that points to a module to use, and defines a the class names for the driver,
- Adding a datasource configuration that contains all the connection specifics

Using the `$JBOSS_HOME/bin/jboss-cli.sh` script, that can execute commands from a file, these steps can be executed
when building the docker image. With the `execute.sh` script I copied from [Marek Goldmann's wildfly docker configuration](https://github.com/goldmann/wildfly-docker-configuration) project,
it will launch Wildfly and then configure it when building the docker image. Below is an example of such a commands script.

Also see: [this](https://github.com/wildfly/quickstart/blob/d33768c2a2211f03c5d80d7c39e66ea82cba3d68/configure-postgresql.cli)
```
# Add a module that contains the Postgres driver
module add --name=org.postgres --resources=/opt/jdbc/postgresql-9.4-1205.jdbc42.jar --dependencies=javax.api,javax.transaction.api

# Add postgres driver
/subsystem=datasources/jdbc-driver=postgres:add(driver-name="postgres",driver-module-name="org.postgres",driver-class-name=org.postgresql.Driver,driver-xa-datasource-class-name=org.postgresql.xa.PGXADataSource)

# Add the datasource
data-source add --name=PostgresDS --driver-name=postgres --jndi-name=java:jboss/datasources/PostgresDS --connection-url=jdbc:postgresql://postgres:5432/test --user-name=postgres --password=secret --use-ccm=true --max-pool-size=5 --blocking-timeout-wait-millis=5000 --enabled=true --driver-class=org.postgresql.Driver --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter --jta=true --use-java-context=true --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker
```
 
You could also read [this](https://github.com/jboss-developer/jboss-developer-shared-resources/blob/master/guides/CONFIGURE_POSTGRESQL_EAP7.md#configure-the-postgresql-database-for-use-with-the-quickstarts) explanation.

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

# Resource Adapter
RAR (Resource Adapter Archive): is an archive that is defined in the [Java EE Connector Architecture (JCA) specification](https://www.jcp.org/en/jsr/detail?id=322) 
as the valid format for deployment of resource adapters on application servers. You can deploy a RAR on the AS as a 
standalone component or as part of a larger application. In both cases, the adapter is available to all applications 
using a lookup procedure.

[JCA](https://en.wikipedia.org/wiki/Java_EE_Connector_Architecture) is a Java-based technology solution for connecting 
application servers and enterprise information systems (EIS) as part of enterprise application integration (EAI) solutions. 
While JDBC is specifically used to connect Java EE applications to databases, JCA is a more generic architecture for 
connection to legacy systems.

http://repo1.maven.org/maven2/org/apache/activemq/activemq-rar/5.10.0/

# Logging
https://goldmann.pl/blog/2014/07/18/logging-with-the-wildfly-docker-image/

# Application deployment
https://docs.jboss.org/author/display/WFLY8/Application+deployment
http://tools.jboss.org/blog/2015-03-02-getting-started-with-docker-and-wildfly.html
https://goldmann.pl/blog/2014/07/23/customizing-the-configuration-of-the-wildfly-docker-image/#_using_code_jboss_cli_sh_code

# Postgres
https://blog.starkandwayne.com/2015/05/23/uuid-primary-keys-in-postgresql/

# ActiveMQ
https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6/html/Administration_and_Configuration_Guide/Configure_a_Deployed_Resource_Adapter1.html
http://repo1.maven.org/maven2/org/apache/activemq/activemq-rar/5.10.0/
https://developer.jboss.org/wiki/HowToUseOutOfProcessActiveMQWithWildFly
https://developer.jboss.org/message/883239
http://www.mastertheboss.com/jboss-server/jboss-script/installing-a-jboss-as-7-module-using-the-cli
