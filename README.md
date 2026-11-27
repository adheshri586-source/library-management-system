# Library Management System

A desktop Library Management System with a JavaFX frontend, separate Java backend, and separate file-backed database layer. The frontend uses JavaFX controls; no HTML or CSS frontend files are included.

## Folder layout

- frontend/ — JavaFX screens and user interaction
- backend/ — library rules, accounts, books, and circulation models
- database/ — local persistence and initial sample data

## Run

Install JDK 25+ and Maven. From the repository root run: mvn javafx:run

The first launch creates .library-data/library-data.ser and seeds sample books and accounts.

## Demo accounts

- Admin: admin@library.com / admin123
- Student: aarav@college.com / student123
- Faculty: neha@college.com / faculty123