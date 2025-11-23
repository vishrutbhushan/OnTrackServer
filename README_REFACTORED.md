# OnTrack Email Processor - Object-Oriented Refactored Version

This is the refactored version of the OnTrack Server email processing module that has been converted from Spring Boot to a pure object-oriented Java application using Google Gmail API, Gemini AI, and Firebase Cloud Messaging.

## Architecture Overview

The application consists of three main service classes that work together:

### 1. EmailService
- **Purpose**: Handles Gmail API integration for fetching emails
- **Key Features**:
  - Uses Google Gmail API with OAuth2 authentication
  - Fetches emails based on configurable filters
  - Supports email archiving functionality
  - Server-side filtering to process only new emails

### 2. GeminiEmailAnalysisService
- **Purpose**: Analyzes email content using Google Gemini AI
- **Key Features**:
  - Uses Java 11+ HttpClient instead of Spring WebClient
  - Comprehensive email parsing for order information
  - Extracts order IDs, product details, prices, delivery dates
  - Maps shipment statuses and vendor information

### 3. NotificationService
- **Purpose**: Sends push notifications using Firebase Cloud Messaging
- **Key Features**:
  - Firebase Admin SDK integration
  - Configurable notification templates
  - User preference checking for notifications

### 4. EmailProcessingSchedulerService
- **Purpose**: Orchestrates the entire email processing workflow
- **Key Features**:
  - Scheduled execution using Java ScheduledExecutorService
  - Processes emails for each user periodically
  - Integrates all three services above
  - Object-oriented design with dependency injection through constructor

## Key Refactoring Changes

### Removed Spring Boot Dependencies
- ❌ `@Service`, `@Autowired`, `@Value`, `@Scheduled` annotations
- ❌ Spring Boot starters and repositories
- ❌ Spring WebClient

### Added Pure Java Alternatives
- ✅ Constructor-based dependency injection
- ✅ Java 11+ HttpClient for HTTP requests
- ✅ ScheduledExecutorService for scheduled tasks
- ✅ Properties file loading using standard Java I/O
- ✅ Direct JDBC model usage instead of Spring Data JPA

## Configuration

Update `src/main/resources/application.properties`:

```properties
# Email Processing Configuration
email.processing.schedule.enabled=true
email.processing.schedule.interval=10

# Gemini AI Configuration  
gemini.api.key=your_gemini_api_key_here

# Firebase Configuration
google.cloud.project-id=your_firebase_project_id

# Database Configuration
mysql.url=jdbc:mysql://localhost:3306/ontrack_schema
mysql.username=dbuser
mysql.password=your_password
```

## Required Files

1. **Gmail API Credentials**: Place `credentials.json` in `src/main/resources/`
2. **Firebase Service Account**: Place `service-account-key.json` in `src/main/resources/`

## Running the Application

### Using Maven:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.project.onTrackServer.OnTrackEmailProcessor"
```

### Using IDE:
Run the `OnTrackEmailProcessor` main class

## How It Works

1. **Initialization**: 
   - Main class creates instances of all three services
   - EmailProcessingSchedulerService receives them via constructor

2. **Scheduled Processing**:
   - Runs every X minutes (configurable)
   - Fetches all users from database
   - For each user with valid Gmail access:

3. **Email Processing Flow**:
   ```
   User → EmailService.fetchEmails() 
        → GeminiEmailAnalysisService.analyzeEmail()
        → NotificationService.sendNotification()
        → Archive email (if enabled)
   ```

4. **Order Management**:
   - Creates new orders or updates existing ones
   - Uses model classes with JDBC methods
   - Handles platform and category matching

## Object-Oriented Benefits

1. **Testability**: Each service can be unit tested independently
2. **Maintainability**: Clear separation of concerns
3. **Flexibility**: Easy to swap implementations
4. **Performance**: Removes Spring Boot overhead
5. **Dependencies**: Minimal external dependencies

## Model Classes Used

- `User`: User information and Gmail access tokens
- `Order`: Order details and status tracking  
- `Platform`: E-commerce platforms (Amazon, Flipkart, etc.)
- `Category`: User-defined product categories
- `UserConfig`: User preferences and settings

## Error Handling

- Graceful degradation when APIs are unavailable
- Comprehensive logging at DEBUG and INFO levels
- Safe fallback when Gemini AI analysis fails
- Retry mechanisms for temporary failures

## Shutdown Handling

The application includes proper shutdown hooks to:
- Stop the scheduled executor service
- Clean up resources
- Close database connections
- Log shutdown completion

This refactored version provides the same functionality as the Spring Boot version but with a cleaner, more maintainable object-oriented architecture.
