# Short Link Service

A simple, high-performance URL shortener service built with Spring Boot and WebFlux.

## Features

- **Shorten URL**: Converts long URLs into 6-character alphanumeric short codes.
- **Redirection**: Redirects users from the short code to the original URL.
- **Info Endpoint**: Retrieve details about a short code, including the original URL and the short link.
- **Thread-safe & Fast**: Uses Caffeine cache for fast in-memory storage with concurrency support.
- **Robust Error Handling**: Centralized exception handling for 400 (Bad Request), 404 (Not Found), and 500 (Internal Server Error) scenarios.
- **Validation**: Ensures that only valid URLs are processed.

## Tech Stack

- **Java 17**
- **Spring Boot 3.4.2**
- **Spring WebFlux** (Reactive web framework)
- **Caffeine Cache** (In-memory caching)
- **JUnit 5 & Mockito** (Testing)
- **Gradle** (Build tool)

## Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle (optional, wrapper provided)

### Running the Application

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd short-link
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

The service will start on `http://localhost:8080`.

## API Endpoints

### 1. Shorten URL
- **Endpoint**: `POST /api/v1/urls/shorten`
- **Request Body**:
  ```json
  {
    "url": "https://www.example.com/some/long/path"
  }
  ```
- **Response** (201 Created):
  ```json
  {
    "originalUrl": "https://www.example.com/some/long/path",
    "shortUrl": "http://localhost:8080/a1B2c3",
    "code": "a1B2c3"
  }
  ```

### 2. Get Short Code Info
- **Endpoint**: `GET /api/v1/urls/{code}/info`
- **Response** (200 OK):
  ```json
  {
    "originalUrl": "https://www.example.com/some/long/path",
    "shortUrl": "http://localhost:8080/a1B2c3",
    "code": "a1B2c3"
  }
  ```

### 3. Redirect to Original URL
- **Endpoint**: `GET /{code}`
- **Response**: `302 Found` with `Location` header containing the original URL.

## Testing

The project includes unit tests for services and controllers, as well as full-cycle integration tests.

To run all tests:
```bash
./gradlew test
```

## Architecture Notes

- **Concurrency**: The `UrlShortenerService` uses Caffeine cache's atomic `get` operation to ensure that multiple requests for the same URL result in only one short code being generated.
- **Cache Capacity**: The in-memory cache is currently configured with a maximum capacity of 10,000 entries. For production use, a distributed storage like Redis is recommended.
- **Base URL**: The service dynamically determines the base URL for the `shortUrl` field based on the incoming request.
