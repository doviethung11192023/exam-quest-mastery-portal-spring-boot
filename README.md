# Multiple Choice Exam Management System - Backend

This is the backend implementation of a Multiple Choice Exam Management System using Spring Boot. The system provides functionality for managing multiple choice exams, student records, teacher assignments, and exam results.

## Key Features

- User Authentication and Authorization
- Teacher Management (Giaovien)
- Student Management (Sinhvien)
- Class Management (Lop)
- Subject Management (Monhoc)
- Exam Question Bank Management (Bode)
- Exam Generation and Management
- Grade Recording and Management (BangDiem)
- Backup and Restore Functionality

## Technology Stack

- **Framework**: Spring Boot
- **Language**: Java
- **Database**: RDBMS (configured via DatabaseConfig)
- **Security**: Spring Security with custom authentication
- **Cross-Origin**: Custom CORS configuration

## Project Structure

```
src/main/java/com/example/demo/
├── config/          # Configuration classes
├── controller/      # REST API endpoints
├── dto/            # Data Transfer Objects
├── entity/         # Domain entities
├── enums/          # Enumerations
├── repository/     # Data access layer
└── service/        # Business logic layer
```

## Prerequisites

- Java 8 or higher
- Maven
- Compatible database server (as configured in application.properties)

## Setup and Installation

1. Clone the repository
2. Configure your database connection in `src/main/resources/application.properties`
3. Build the project:
   ```bash
   mvn clean install
   ```

## Running the Application

1. Start the application using Maven:
   ```bash
   mvn spring-boot:run
   ```
2. The application will be available at `http://localhost:8080` (or the configured port)

## API Endpoints

The application provides several REST endpoints:

- `/api/auth` - Authentication endpoints
- `/api/giaovien` - Teacher management
- `/api/sinhvien` - Student management
- `/api/lop` - Class management
- `/api/monhoc` - Subject management
- `/api/bode` - Question bank management
- `/api/bangdiem` - Grade management
- `/api/exam` - Exam management
- `/api/backup` - Backup and restore functionality

## Security

The application implements security through:

- Custom CORS configuration
- Header-based authentication
- Role-based access control

## Data Models

- **Giaovien**: Teacher information
- **Sinhvien**: Student information
- **Lop**: Class information
- **Monhoc**: Subject information
- **Bode**: Question bank entries
- **BangDiem**: Grade records
- **ChiTietBaiThi**: Exam details
- **GiaovienDangky**: Teacher assignments

## Services

The application provides various services for:

- User authentication and authorization
- Exam generation and management
- Grade recording and calculations
- Backup and restore operations
- Student and teacher management
