# Short Link Service

A simple, high-performance URL shortener service built with Spring Boot and WebFlux.

## Features

- **Shorten URL**: Converts long URLs into 6-character alphanumeric short codes.
- **Redirection**: Redirects users from the short code to the original URL.
- **Info Endpoint**: Retrieve details about a short code, including the original URL and the short link.
- **Thread-safe & Fast**: Uses Caffeine cache for fast in-memory storage with concurrency support.
- **Robust Error Handling**: Centralized exception handling for 400 (Bad Request), 404 (Not Found), and 500 (Internal Server Error) scenarios.
- **Validation**: Ensures that only valid URLs are processed.
- **API Documentation**: Interactive API documentation using Swagger UI (SpringDoc).

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

The service will start on `http://localhost`.

## Configuration

### Custom Domain Setup (`short.ly`)

The application is configured to use `short.ly` as the host for generated short links. This is controlled by the `shortlink.host` property in `src/main/resources/application.properties`.

To use this domain locally:

1.  **Update your hosts file**:
    Add the following entry to your `/etc/hosts` (macOS/Linux) or `C:\Windows\System32\drivers\etc\hosts` (Windows) file:
    ```text
    127.0.0.1 short.ly
    ```

2.  **Application Port**:
    The application is configured to run on port `80` (see `server.port` in `application.properties`). Note that running on port 80 may require administrative privileges (e.g., `sudo ./gradlew bootRun`). Alternatively, you can change the port back to `8080`.

## Using the HTTP Client

The project includes a `short-link-api.http` file that can be used to test the API endpoints directly from your IDE (like IntelliJ IDEA with the HTTP Client plugin) or using `httpyac`/`rest-client`.

It contains requests for:
- Shortening a URL.
- Retrieving short code info.
- Testing the redirection.

## API Documentation

Once the application is running, you can access the interactive Swagger UI at:
- [http://localhost/swagger-ui.html](http://localhost/swagger-ui.html)

The OpenAPI spec in JSON format is available at:
- [http://localhost/v3/api-docs](http://localhost/v3/api-docs)

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
    "shortUrl": "http://short.ly/a1B2c3",
    "code": "a1B2c3"
  }
  ```

### 2. Get Short Code Info
- **Endpoint**: `GET /api/v1/urls/{code}/info`
- **Response** (200 OK):
  ```json
  {
    "originalUrl": "https://www.example.com/some/long/path",
    "shortUrl": "http://short.ly/a1B2c3",
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
