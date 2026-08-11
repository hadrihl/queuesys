# Queue Management System

A simple yet powerful queue management system built with Spring Boot. Users register with an email pincode, receive a tracking link, and monitor their queue status in real-time through a web interface.

## Features

✅ **User Registration** - Users register with name and email, verified with an emailed pincode  
✅ **Automatic Queue Assignment** - System assigns queue numbers automatically  
✅ **Email Notifications** - Users receive email with tracking link  
✅ **Real-time Status Tracking** - Web interface shows current queue position  
✅ **Admin Dashboard** - Manage queue, call users, and complete services  
✅ **Smart Link Management** - Tracking links become inactive after service completion  
✅ **Auto-refresh** - Status pages auto-refresh for latest information  

## Tech Stack

- **Backend**: Spring Boot 3.2.3
- **Database**: H2 (in-memory)
- **Frontend**: Thymeleaf, HTML, CSS, JavaScript
- **Java Version**: 17

## Project Structure

```
queuesys/
├── src/main/java/com/queuemgmt/
│   ├── QueueManagementApplication.java
│   ├── controller/
│   │   ├── QueueApiController.java
│   │   └── WebController.java
│   ├── dto/
│   │   ├── RegistrationRequest.java
│   │   ├── EmailRequest.java
│   │   └── QueueStatusResponse.java
│   ├── exception/
│   │   └── InvalidVerificationException.java
│   ├── model/
│   │   ├── QueueEntry.java
│   │   └── QueueStatus.java
│   ├── repository/
│   │   └── QueueEntryRepository.java
│   └── service/
│       ├── QueueService.java
│       ├── EmailService.java
│       └── EmailVerificationService.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/
│       ├── index.html
│       ├── track.html
│       ├── admin.html
│       └── error.html
└── pom.xml
```

## How It Works

### User Flow
1. **Request code**: User enters name + email on the home page and requests a verification pincode
2. **Email pincode**: System emails a 6-digit pincode
3. **Verify & Register**: User enters the pincode; once verified, the system creates the queue entry and emails a unique tracking link
4. **Track Status**: User clicks the link to view queue status in browser
5. **Notification**: User receives an email when it's their turn
6. **Service**: Admin manages service through dashboard
7. **Completion**: User receives a completion email, link becomes inactive

### Admin Flow
1. **View Queue**: Access admin dashboard to see all active entries
2. **Call Next**: Click button to call next person in queue
3. **Start Service**: Mark user as "In Service" when serving
4. **Complete**: Mark service as complete, deactivating user's link

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

1. **Navigate to project directory**
```bash
cd c:\Users\nrhdrhl\Desktop\queuesys
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the application**
- Home/Registration: http://localhost:8080/
- Admin Dashboard: http://localhost:8080/admin
- H2 Console: http://localhost:8080/h2-console

### H2 Database Console
- **URL**: `jdbc:h2:mem:queuedb`
- **Username**: `sa`
- **Password**: *(leave empty)*

## API Endpoints

### User Endpoints
- `POST /api/queue/send-code` - Send a verification pincode to an email address
- `POST /api/queue/register` - Verify the pincode and register the new user
- `GET /api/queue/status/{trackingNumber}` - Get status by tracking number
- `GET /api/queue/status/token/{accessToken}` - Get status by access token

### Admin Endpoints
- `GET /api/queue/active` - Get all active queue entries
- `POST /api/queue/call-next` - Call next person in queue
- `POST /api/queue/start-service/{trackingNumber}` - Start service
- `POST /api/queue/complete/{trackingNumber}` - Complete service

## Email Verification (Pincode)

Registration requires email verification: the server generates a 6-digit pincode, emails it via SMTP, and
the user enters it back to complete registration. The pincode is held in memory
(`EmailVerificationService`) for 10 minutes and is single-use.

### Setup

Configure SMTP credentials in `src/main/resources/application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.mail.from=YOUR_EMAIL@gmail.com
```

For Gmail, you must generate an [App Password](https://myaccount.google.com/apppasswords) (requires
2-Step Verification enabled) — your regular account password will not work. Any other SMTP provider
(SendGrid, Mailgun, your own mail server, etc.) works the same way; just change `spring.mail.host`/`port`
and credentials accordingly.

### How it works

1. User enters name + email on the registration page and clicks **Send Verification Code**.
2. The browser posts `{ email }` to `POST /api/queue/send-code`, which generates a pincode, stores it with
   a 10-minute expiry, and emails it via `EmailService.sendVerificationCode`.
3. User enters the pincode and clicks **Verify & Join Queue**.
4. The browser posts `{ userName, email, pincode }` to `POST /api/queue/register`. The server verifies the
   pincode (`EmailVerificationService.verifyCode`) and, if valid, creates the queue entry and emails a
   tracking link. An invalid/expired pincode returns `401 Unauthorized`.

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# Base URL (update for production)
app.base-url=http://localhost:8080

# Queue timeout (minutes)
app.queue.timeout-minutes=30
```

## Queue Status States

- **WAITING**: User is waiting in queue
- **CALLED**: User has been called (notification sent)
- **IN_SERVICE**: User is currently being served
- **COMPLETED**: Service completed, link deactivated
- **CANCELLED**: Entry cancelled or user no-show

## Features Deep Dive

### Smart Link Management
- Each user gets a unique access token
- Link remains active while service is in progress
- Automatically deactivated upon service completion
- Expired links show error page

### Auto-Refresh
- User status page refreshes every 30 seconds
- Admin dashboard refreshes every 15 seconds
- Can manually refresh anytime

### Real-time Queue Position
- Shows exact queue number
- Displays people ahead in queue
- Updates automatically as queue progresses

## Future Enhancements

- [ ] SMS notifications as backup
- [ ] Multiple service counters
- [ ] Queue categories/priorities
- [ ] Historical analytics
- [ ] User authentication
- [ ] Push notifications
- [ ] QR code generation

## Troubleshooting

**Port already in use**:
```properties
# Change port in application.properties
server.port=8081
```

**H2 Console not accessible**:
```properties
# Ensure these are set
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## License

This project is open source and available for educational purposes.

## Support

For issues or questions, please check the application logs in the console.

---

**Developed with Spring Boot** ❤️
