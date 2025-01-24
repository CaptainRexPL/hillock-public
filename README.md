# Hillock

A simple application written in spring boot. This repository is a public mirror since I wanted to show the progress to my friends without having to add them to the main repo.
Since we all like to play card games like poker, I decided to create this simple project to make it easier to manage them.

## Usage of the production version
Since there's no actual web app (yet), here's what you need to do to use the production version:
1. Open the swagger page
2. Generate an invite code with the /api/invites endpoint
3. Create a user with the /api/account/create endpoint
4. Verify your email (since mailgun is sometimes getting ratelimited, you might need to wait a bit)
5. Log in with the /api/account/login endpoint
6. That's it! You can now use almost all of the endpoints (some of them can only be used by the administrator)

# Credits

* CaptainRexPL
* [collarmc/collar](https://github.com/collarmc/collar) - email template

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

## Documentation

You can find api docs [here](http://hillock.live/swagger-ui/index.html)
