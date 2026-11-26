# Library Management System

A JavaFX desktop library app built with Java 25+ and Maven. It also includes an optional standalone browser demo in [index.html](index.html).

## Run the JavaFX app

Install JDK 25 or newer and Maven, clone this repository, then run from the repository root:

```sh
mvn javafx:run
```

The JavaFX source is `LibraryManagementSystem.java`; Maven configures that root-level source file and packages `styles.css` as the app stylesheet.

## Included features

- Admin, student, and faculty sign-in and registration
- Book catalog search and administration
- Borrowing, returns, due dates, fines, and transaction history
- Local file persistence

## Demo accounts

- Admin: `admin@library.com` / `admin123`
- Student: `aarav@college.com` / `student123`
- Faculty: `neha@college.com` / `faculty123`
