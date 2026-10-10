# ShapePuzzles

## Overview

ShapePuzzles is a full-stack application built with **Spring Boot** and **Angular**, featuring a collection of tangram-like geometric puzzles.

The main idea behind the project is extensibility. Each puzzle type consists of a set of geometric pieces and a collection of tasks to solve. New puzzle types can be introduced simply by adding new definitions to the database, without modifying the application code.

The backend is responsible for storing puzzle definitions, maintaining user data and validating puzzle solutions. The frontend handles game geometry and user interactions. Its mechanics are described in more detail in the frontend README.

The backend and frontend are maintained as separate projects.

**Frontend:** https://github.com/czmadzia2201/shape-puzzles-frontend

---

## History

The project was inspired by an old geometric puzzle game produced by the Polish company Polimer. The initial goal was simply to recreate the original worn and scribbled-over task booklet in digital form. This idea then gradually evolved into developing a digital version of the entire game.

The first implementation was frontend-only, built with JavaScript and CreateJS. It lacked support for different puzzle types, user management and a reliable solution validation algorithm. Attempts to add a backend layer, combined with a few bad architectural decisions, made the code difficult to maintain, and the project was eventually abandoned.

ShapePuzzles is a big comeback to the idea, built from scratch. With more experience and the help of AI tools, I believed — correctly, as it turned out — that I would avoid getting lost in the project's complexity and develop an automatic solution validation algorithm. The original Polimer puzzle is now one of the five puzzle types available in ShapePuzzles, included under the name **House**.

---

## Technologies

### Backend

- Java
- Spring Boot
- Maven
- PostgreSQL
- REST API
- JUnit 5
- Mockito

### Frontend

- Angular
- TypeScript
- HTML / CSS
- Konva.js

---

## Running the Project Locally

### Backend

Start PostgreSQL and configure the database connection in the application properties.

Then run:

```bash
mvn spring-boot:run
```

### Frontend

```bash
npm install
npm start
```

### Swagger
http://localhost:8080/shape-puzzles/swagger-ui/index.html

---

## Development

This project was built with some assistance from AI tools, primarily ChatGPT, which served as a discussion partner, helped evaluate architectural decisions and assisted with implementation details.

The overall architecture, design decisions, implementation, code reviews and final solutions were designed, evaluated and refined by the author.

---

## To Do List

- Expand the collection of available puzzle types and tasks.
- Publish the application online.