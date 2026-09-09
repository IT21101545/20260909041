# Training Management System

A Spring Boot application for managing officer nominations for training programmes.

This project focuses on two main business requirements:

- Prevent duplicate nominations
- Manage limited training capacity using a FIFO waiting list

---

## Features

- Manage officers
- Manage departments
- Manage training programmes
- Create training nominations
- Prevent duplicate nominations
- Automatically confirm nominations when seats are available
- Automatically add nominations to the waiting list when training is full
- FIFO (First In, First Out) waiting-list management
- Automatically promote the next waiting officer when a confirmed officer cancels
- Store nomination date and time
- Database-level duplicate protection
- REST APIs
- Simple web interface

---

## Technologies Used

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Maven
- Microsoft SQL Server
- HTML
- CSS
- JavaScript
- JUnit 5
- Mockito

---

## Project Structure

```text
training-management-system/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/trainingmanagement/
│   │   │       │
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── TrainingManagementApplication.java
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── app.js
│   │       │   └── style.css
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│
├── pom.xml
└── README.md
