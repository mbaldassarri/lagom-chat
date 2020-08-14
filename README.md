# Distributed Systems Project - Lagom Chat

## Lagom: study of the Framework and implementation of a Microservices based project

# Lagom Chat
[Lagom](https://www.lagomframework.com/) is an open source framework for building reactive microservice systems in Java or Scala. 
This Lagom project contains three services
* ***User service*** that serves as an API for login and logout a user
* ***Channel service*** that served as an API for creating updating and interacting with chat channels (also known as 'rooms').This service contains Event Sourcing implementation.
* ***Message-Dispatcher service*** that is responsible of listening to the changings of the ChannelService as well as sending chat messages. 
A client can use this microservice to handle messages and show them in a nice UI. 

## Setup
Install sbt with macOS
```
brew install sbt
```

Navigate to the project and run `$ sbt`

Start up the project by executing `$ runAll`

Debugging and testing microservices is a very hot topic in Distributed Systems world. 
If you want to debug a API call just run  :

`sbt -jvm-debug 5005 runAll`

Please help yourself with [this guide](https://stackoverflow.com/questions/4150776/debugging-scala-code-with-simple-build-tool-sbt-and-intellij) for a complete setup of project debugging using IntelliJ.

 
## Importing the project in an IDE
Import the project as an sbt project in your IDE.

* Note 1: This project uses the [Immutables](https://immutables.github.io) library, be sure to consult [Set up Immutables in your IDE](https://www.lagomframework.com/documentation/1.6.x/java/ImmutablesInIDEs.html).
*Setting up the IDE to run Immutables library is essential to make the project work*.

If you are using IntelliJ, make sure to properly set the generated classes path in `Compiler > Annotation Processor`

* Note 2: This project needs PostgreSQL to be installed locally in order to run. You can find Database configuration settings at
`user-impl > resources.conf`. 
A manual initial setup of the database may be needed in order to properly run the project. 
Current settings use a database named **user_db** containing a **usermodel** schema.
This schema has 2 colums: **id:varchar** and **username:varchar**.
Please refer to the [official documentation](https://www.postgresql.org/docs/12/sql-createdatabase.html) about creating a database and tables.

* Note 3: `Channel Service` uses Cassandra database. No manual configuration is needed. 
SBT and Lagom do the dirty work for you under the hood. Enjoy :)

## Other Notes
* This project lacks of the frontend, which is essential for logging in, interacting with the Channels and sending messages without using Postman.

* The only way to do integration tests on **/api/message/live** endpoint in Message-Dispatcher service
is using a WebSocket client and manually verify the response. 



