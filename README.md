# OnTrack Server

A Spring Boot backend server for the OnTrack email tracking application. This server provides REST APIs for user authentication, Gmail integration, email fetching, and push notifications.

## 🚀 Features

- **User Authentication**: OAuth token management for Gmail access
- **Gmail Integration**: Fetch emails using Gmail API with proper OAuth authentication
- **Push Notifications**: Firebase Cloud Messaging (FCM) integration for real-time notifications
- **Email Storage**: MySQL database for storing user emails and tokens
- **RESTful APIs**: Clean, organized REST endpoints for all operations

## 🏗️ Architecture

### Controllers
- **TokenController**: Manages user authentication tokens
- **ItemController**: Handles email items and Gmail operations
- **NotificationController**: Manages push notifications and FCM tokens

### Services
- **GmailService**: Integrates with Gmail API to fetch user emails
- **NotificationService**: Handles FCM notifications

### Models
- **Token**: User authentication tokens (access/refresh tokens)
- **Item**: Email items (subject, snippet, sender, etc.)
- **FCMToken**: Firebase Cloud Messaging tokens
- **ApiResponse**: Standard API response wrapper

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Firebase project with Admin SDK credentials

## 🛠️ Setup

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE ontrack_schema;

-- Create user (optional)
CREATE USER 'db_user'@'localhost' IDENTIFIED BY 'Root@Password@123';
GRANT ALL PRIVILEGES ON ontrack_schema.* TO 'db_user'@'localhost';
FLUSH PRIVILEGES;
```

Run the provided `database_schema.sql` to create the required tables.

### 2. Firebase Configuration

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Generate a service account key:
   - Go to Project Settings → Service Accounts
   - Click "Generate new private key"
   - Save the JSON file as `service-account-key.json`
3. Place the file in `src/main/resources/`

### 3. Application Configuration

Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/ontrack_schema
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

# Google Cloud Configuration
google.cloud.project-id=your-firebase-project-id
```

### 4. Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/ontrack-server-1.0.0.jar
```

The server will start on `http://localhost:8080`

## 📖 API Documentation

### Authentication Endpoints

#### Save Token
```
POST /api/tokens
Content-Type: application/json

{
  "userId": "user123",
  "accessToken": "oauth_access_token",
  "refreshToken": "oauth_refresh_token",
  "email": "user@example.com"
}
```

#### Get Token
```
GET /api/tokens/{userId}
```

#### Check Token Exists
```
GET /api/tokens/exists/{userId}
```

### Email Endpoints

#### Get User Emails
```
GET /api/items/user/{userId}
```

#### Fetch Emails from Gmail
```
POST /api/items/fetch/{userId}
```

### Notification Endpoints

#### Store FCM Token
```
POST /api/notifications/fcm-token/{userId}?fcmToken=your_fcm_token
```

#### Send Test Notification
```
POST /api/notifications/test/{userId}
```

#### Send Custom Notification
```
POST /api/notifications/send?userId=user123&title=Title&message=Message
```

## 🗂️ Project Structure

```
src/
├── main/
│   ├── java/com/project/onTrackServer/
│   │   ├── controller/           # REST controllers
│   │   │   ├── TokenController.java
│   │   │   ├── ItemController.java
│   │   │   └── NotificationController.java
│   │   ├── service/              # Business logic
│   │   │   ├── GmailService.java
│   │   │   └── NotificationService.java
│   │   ├── model/                # Data models
│   │   │   ├── Token.java
│   │   │   ├── Item.java
│   │   │   ├── FCMToken.java
│   │   │   └── ApiResponse.java
│   │   ├── repository/           # Data access layer
│   │   │   ├── TokenRepository.java
│   │   │   ├── ItemRepository.java
│   │   │   └── FCMTokenRepository.java
│   │   └── OnTrackServerApplication.java
│   └── resources/
│       ├── application.properties
│       └── service-account-key.json
├── database_schema.sql
└── pom.xml
```

## 🔒 Security Notes

- Keep `service-account-key.json` secure and never commit it to version control
- Use environment variables for sensitive configuration in production
- Implement proper authentication and authorization for production use
- Regularly rotate OAuth tokens and Firebase credentials

## 🚀 Deployment

### Docker (Recommended)

```dockerfile
FROM openjdk:17-jdk-alpine
VOLUME /tmp
COPY target/ontrack-server-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Environment Variables

Set these environment variables in production:

```bash
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ontrack_schema
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
GOOGLE_CLOUD_PROJECT_ID=your-project-id
```

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

For support and questions, please create an issue in the repository.
