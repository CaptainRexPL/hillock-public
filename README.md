# Hillock

A simple application written in spring boot. This repository is a public mirror since I wanted to show the progress to my friends without having to add them to the main repo.
Since we all like to play card games like poker, I decided to create this simple project to make it easier to manage them.


Since the production version of Hillock is meant to be private (and the frontend is not ready yet) in order to create an account you would have to request an invite code from the administrator. In other words, as of right now, there's not much I can show beside the swagger api docs.

# Warning for anyone who decides to try to use the endponts on the production server: All instances of unauthorised attempts of using any of the endpoints are logged (the info logged includes ip addresses) as a way of preventing break-ins. 

# Credits

* CaptainRexPL
* [collarmc/collar](https://github.com/collarmc/collar) - email template, current code that handles token generation and password hashing

# Requirements

* PostgreSQL database with the schema (check out db-structure.md for details)
* A mailgun account if you want to automatically verify accounts (in theory you could just use local emails and verify them manually)
* Java 21 and maven 3

## Setting up

### Set up the database schema
If you haven't done so already, set up the database server and the database schema

### Clone the repository
```bash
git clone https://github.com/CaptainRexPL/Hillock
```

### Create the application.properties and fill it out

Create a file called `application.properties` in the `src/main/resources` directory. An example file can be in application.properties.example. Fill out the properties with your own values.


### Compile and run the project
Use either
```bash
mvn clean install
```
and then
```bash
java -jar hillock-0.0.1-SNAPSHOT.jar
```

Or just use
```
mvn spring-boot:run
```


## TODO
* add a way to reset your password
* add an example application.properties file to the public repository
