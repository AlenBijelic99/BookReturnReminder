# BookReturnReminder

An Android application to remind you to return the book you borrowed from the library.

This application was developed as a part of the course "Développement Mobile Avancé" at the HEIG-VD.

![BookReturnReminder](./figures/BookReturnReminder.png)

## Features

- View the list of borrowed books.
- Scan a book's barcode to add it to the list of borrowed books.
- Set a reminder for each book.
- Receive a notification when the book is due.
- Receive a notification with a list of books that are due when you pass by the library.

## Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Build the project.
4. Run the project on an Android device.

## Usage

1. Open the application.
2. Grant the necessary permissions.
3. Add a book by scanning its barcode.
4. Set a reminder for the book.
5. Set book as returned when you return it to the library by clicking on the book in the list.
6. Change return date if needed.

## Development

### Database

Usually we want to use a database that contains many books and their information. But for this project, we will use a local database to store a small number of books that are available for the user to scan and borrow.

The application uses a SQLite database to store the list of books. When a book is borrowed, it will set the return date and the book is represented as a borrowed book. When the book is returned, the return date is removed.

In a public library, we would use a database that contains all the library's books to avoid storing all the books in the smartphone's database. We were time-limited and decided to use a local database for this project.

## Scanning

### ISBN

ISNB (International Standard Book Number) is a unique identifier of 13 digits that is used to identify books (same format as EAN-13). The application uses the ISBN to identify which book is scanned.

Each of the 13 digits of the ISBN has a specific meaning:

![ISBN exemple from Wikipedia](./figures/EAN-13-ISBN-13.svg.png)

We only use the ISBN to identify the book. We do not use the information to retrieve publisher, author, title, etc.

## Notification with iBeacon

The application uses a foreground service that detects iBeacon device representing the library. When the user is near the library, a notification is sent to the user with a list of books that are due.

A notification is also sent to the user when a book is due on the same day.