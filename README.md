[![Quality Gate Status](https://mathiasconradt.ngrok.io/api/project_badges/measure?project=mathiasconradt_JavaCoffeeShop_6849eab7-4f5e-47e8-bf46-d8ead5db6954&metric=alert_status&token=sqb_b75c387d44dc7257c85f6115330bed4134e48478)](https://mathiasconradt.ngrok.io/dashboard?id=mathiasconradt_JavaCoffeeShop_6849eab7-4f5e-47e8-bf46-d8ead5db6954)

# Java Coffee Shop

This application is an intentional vulnerable Java Spring-Boot application with Thymeleaf.
It is use for training purposes only.

The code is and layout of the application is originally based on https://github.com/hieutdo/waa-coffee-shop, however it is heavily edited, updated and includes security problems by design!

# Java Security Workshop

## Required software
- Java 11 or higher
- Maven installed
- IntelliJ Community (preferred) or VSCode
- unrestricted access to your work machine
- A Github account with unrestricted access

## Before we start

- Fork this repository to your own GitHub account
- Check out the forked repository
```
git clone https://github.com/<your_username>/JavaCoffeeShop.git 
```
- Sign up for a free Sonar account at https://sonarcloud.io (unless you already have one)
- Connect the forked project to your Sonar projects and leave it there.

## Run the application
- Go to the root folder of the application and run using Maven
```
mvn spring-boot:run
```
<!-- - If you run from you IDE, please set this JVM-parameter : `-Dcom.sun.jndi.ldap.object.trustURLCodebase=true` -->
- The application fills itself with data at startup wait until you see `READY` in the console.
- You can access the application on http://localhost:8081
- By default there are two users configured you can access

| Username | Password | User type |
|----------|----------|-----------|
| Admin    | admin    | ADMIN     |
| User     | user     | CUSTOMER  |

---
# Assignments

Before trying to exploit the application and/or fix vulnerabilities, play around in the app and make yourself familiar with the features.
Essentially it is an application to order coffee's and beer's if you have an account.
