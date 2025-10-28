# SchoolDay Backend

A comprehensive Spring Boot application for managing school operations, including user management, child tracking, and parent-teacher communication.

## Table of Contents
- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Quick Start](#quick-start)
- [Documentation](#documentation)
- [Project Structure](#project-structure)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [API Endpoints](#api-endpoints)
- [Contributing](#contributing)
- [License](#license)

---

## About

SchoolDay is a modern backend system designed to streamline school management operations. It provides REST APIs for managing users, children, parents, teachers, and daily reports with secure authentication and role-based access control.

### Key Capabilities
- User authentication and session management
- Role-based access control (Admin, Teacher, Parent)
- Child profile management
- Parent-child relationship tracking
- RESTful API design
- Secure database operations

---

## Features

### User Management
- User registration and authentication
- Session-based authentication with tokens
- Role assignment (ADMIN, TEACHER, PARENT)
- User profile management

### Child Management
- Child profile creation and updates
- Parent-child relationship tracking
- Multiple children per parent support
- Child status tracking

### Security
- Password encryption
- Token-based session management
- Role-based access control
- Secure credential storage (environment variables)

### Database
- MySQL 8 database
- JPA/Hibernate ORM
- Database migrations via SQL scripts
- Test database isolation

---

## Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM
- **Maven** - Build tool

### Database
- **MySQL 8** - Primary database
- **Podman/Docker** - Container runtime

### DevOps
- **Docker** - Containerization
- **AWS ECS Fargate** - Production deployment
- **AWS RDS** - Managed database
- **GitHub Actions** - CI/CD (optional)

---

## Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Podman or Docker
- MySQL 8 (via container)

### 1. Clone the Repository
```bash
git clone git@github.com:sanket1106/schoolday-be.git
cd schoolday-be
```

### 2. Configure Environment
```bash
# Copy environment template
cp infra/.env.example infra/.env

# Edit infra/.env and set your MySQL password
nano infra/.env
```

Set the following variables:
```bash
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=schoolday
MYSQL_HOST=localhost
MYSQL_PORT=3306
```

### 3. Initialize Database
```bash
# Initialize MySQL container, create schema, and seed data
./schoolday.sh dev init all
./schoolday.sh dev create mysql-schema
./schoolday.sh dev insert data

# Setup test database
./infra/db/setup_test_db.sh
```

### 4. Export Environment Variables
```bash
# Export variables for Spring Boot
export MYSQL_ROOT_PASSWORD=your_secure_password
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=schoolday
```

### 5. Run the Application
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/*.jar
```

### 6. Verify Application
```bash
# Check health
curl http://localhost:8081/actuator/health

# Test API (should return 401 without auth)
curl http://localhost:8081/api/users
```

**Application runs on:** http://localhost:8081

---

## Documentation

### Comprehensive Guides
- **[DATABASE_COMMANDS.md](DATABASE_COMMANDS.md)** - Complete database management guide
  - Database setup and initialization
  - All available commands
  - Troubleshooting
  - Configuration details

- **[AWS_ECS_DEPLOYMENT.md](AWS_ECS_DEPLOYMENT.md)** - AWS deployment guide
  - ECS Fargate deployment
  - Infrastructure setup
  - Cost optimization
  - CI/CD integration

### Quick Reference Docs
- **[infra/db/README_TEST_DB.md](infra/db/README_TEST_DB.md)** - Test database setup
- **[src/test/resources/sql/README.md](src/test/resources/sql/README.md)** - Test SQL scripts

---

## Project Structure

```
SchoolDay/
├── src/
│   ├── main/
│   │   ├── java/com/school/
│   │   │   ├── SchoolDayApplication.java          # Main application
│   │   │   ├── authentication/                    # Auth filters & services
│   │   │   ├── exceptions/                        # Custom exceptions
│   │   │   ├── feature/
│   │   │   │   ├── users/                         # User domain
│   │   │   │   │   ├── dao/                       # Repositories
│   │   │   │   │   └── entity/                    # JPA entities
│   │   │   │   └── report/                        # Report entities
│   │   │   ├── service/                           # Business logic
│   │   │   └── web/                               # REST controllers & DTOs
│   │   └── resources/
│   │       └── application.properties             # App configuration
│   └── test/                                      # Test code
│       ├── java/                                  # Unit & integration tests
│       └── resources/
│           ├── application.properties             # Test configuration
│           └── sql/                               # Test SQL scripts
├── infra/
│   ├── .env.example                               # Environment template
│   ├── containers/                                # Container scripts
│   │   ├── init_mysql.sh
│   │   ├── init_network.sh
│   │   └── start_mysql.sh
│   ├── db/                                        # Database scripts
│   │   ├── sql/
│   │   │   ├── schema.sql                         # Production schema
│   │   │   ├── basedata.sql                       # Seed data
│   │   │   ├── schema_test.sql                    # Test schema
│   │   │   └── basedata_test.sql                  # Test seed data
│   │   ├── create_schema.sh
│   │   ├── seed_data.sh
│   │   ├── setup_test_db.sh
│   │   └── reset_test_db.sh
│   └── deploy/                                    # Deployment config
│       ├── Dockerfile
│       ├── docker-compose.yml
│       ├── ecs-task-definition.json
│       ├── build-and-push.sh
│       └── deploy-to-ecs.sh
├── schoolday.sh                                   # Main orchestration script
├── pom.xml                                        # Maven configuration
├── DATABASE_COMMANDS.md                           # Database guide
├── AWS_ECS_DEPLOYMENT.md                          # Deployment guide
└── README.md                                      # This file
```

---

## Development

### Database Management

The project uses a centralized script for all database operations:

```bash
# Start MySQL
./schoolday.sh dev start mysql

# Stop MySQL
./schoolday.sh dev stop mysql

# Create schema
./schoolday.sh dev create mysql-schema

# Seed data
./schoolday.sh dev insert data

# Initialize everything
./schoolday.sh dev init all
```

See [DATABASE_COMMANDS.md](DATABASE_COMMANDS.md) for complete documentation.

### Local Development Workflow

```bash
# 1. Start MySQL
./schoolday.sh dev start mysql

# 2. Export environment variables
export MYSQL_ROOT_PASSWORD=your_password
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=schoolday

# 3. Run application
./mvnw spring-boot:run

# 4. Make changes and hot-reload (Spring DevTools)
# Application auto-restarts on code changes

# 5. Stop MySQL when done
./schoolday.sh dev stop mysql
```

### Database Schema

**Main Tables:**
- `user` - User accounts with authentication
- `role` - User roles (ADMIN, TEACHER, PARENT)
- `user_role` - User-to-role mappings
- `child` - Child profiles
- `parent_child` - Parent-child relationships
- `user_session` - Active user sessions

**Relationships:**
- Users can have multiple roles
- Parents can have multiple children
- Sessions track active user authentication

---

## Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test
```bash
./mvnw test -Dtest=UserServiceTest
```

### Test Database
Tests use a separate `schoolday_test` database:

```bash
# Setup test database (first time only)
./infra/db/setup_test_db.sh

# Reset test database (clean state)
./infra/db/reset_test_db.sh
```

### Test Configuration
- **Auto-cleanup:** Tests automatically clean data after execution
- **Base data:** Preserved between tests (10 users, 3 roles, 5 children)
- **Isolation:** Each test gets a clean slate via SQL scripts

See test documentation:
- [infra/db/README_TEST_DB.md](infra/db/README_TEST_DB.md)
- [src/test/resources/sql/README.md](src/test/resources/sql/README.md)

---

## Deployment

### Local Docker Deployment

Test the full stack locally with Docker Compose:

```bash
cd infra/deploy
docker-compose up --build

# Application: http://localhost:8081
# MySQL: localhost:3306
```

### AWS ECS Deployment

Deploy to production using AWS ECS Fargate:

```bash
# 1. Build and push Docker image
./infra/deploy/build-and-push.sh <aws-account-id> us-east-1 latest

# 2. Deploy to ECS
./infra/deploy/deploy-to-ecs.sh schoolday-cluster schoolday-service us-east-1
```

**Complete deployment guide:** [AWS_ECS_DEPLOYMENT.md](AWS_ECS_DEPLOYMENT.md)

**Includes:**
- Infrastructure setup (VPC, Security Groups, IAM)
- RDS MySQL configuration
- ECS Fargate deployment
- Monitoring and logging
- Cost optimization tips
- CI/CD integration

---

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout

### User Management
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Child Management
- `GET /api/children` - Get all children
- `GET /api/children/{id}` - Get child by ID
- `POST /api/children` - Create new child
- `PUT /api/children/{id}` - Update child
- `DELETE /api/children/{id}` - Delete child

### Parent Operations
- `GET /api/parents/{parentId}/children` - Get parent's children
- `POST /api/parents/{parentId}/children` - Add child to parent

**Base URL:** `http://localhost:8081/api`

**Authentication:** Session token-based (via `X-Auth-Token` header)

---

## Environment Variables

### Required Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | - | Yes |
| `MYSQL_DATABASE` | Database name | `schoolday` | No |
| `MYSQL_HOST` | MySQL hostname | `localhost` | No |
| `MYSQL_PORT` | MySQL port | `3306` | No |

### Configuration

**For shell scripts:** Configure in `infra/.env`
```bash
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=schoolday
MYSQL_HOST=localhost
MYSQL_PORT=3306
```

**For Spring Boot:** Export as environment variables
```bash
export MYSQL_ROOT_PASSWORD=your_password
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=schoolday
```

**Security Note:** Never commit passwords to git. The `infra/.env` file is excluded via `.gitignore`.

---

## Common Commands

### Database Operations
```bash
# Complete setup
./schoolday.sh dev init all && \
./schoolday.sh dev create mysql-schema && \
./schoolday.sh dev insert data

# Start/Stop
./schoolday.sh dev start mysql
./schoolday.sh dev stop mysql

# Reset test database
./infra/db/reset_test_db.sh
```

### Application
```bash
# Run application
./mvnw spring-boot:run

# Build JAR
./mvnw clean package

# Run tests
./mvnw test

# Skip tests during build
./mvnw clean package -DskipTests
```

### Docker
```bash
# Local testing
cd infra/deploy
docker-compose up --build

# Stop and clean
docker-compose down -v
```

---

## Troubleshooting

### Database Connection Issues

**Problem:** Application can't connect to MySQL

**Solution:**
```bash
# 1. Check if MySQL is running
podman ps | grep mysql8

# 2. Verify environment variables
echo $MYSQL_ROOT_PASSWORD

# 3. Test MySQL connection
mysql -h localhost -u root -p schoolday

# 4. Check logs
podman logs mysql8
```

### Test Failures

**Problem:** Tests fail due to database state

**Solution:**
```bash
# Reset test database
./infra/db/reset_test_db.sh

# Run tests again
./mvnw test
```

### Port Already in Use

**Problem:** Port 8081 or 3306 already in use

**Solution:**
```bash
# Find process using port
lsof -i :8081
lsof -i :3306

# Kill process or change port in application.properties
server.port=8082
```

**More troubleshooting:** See [DATABASE_COMMANDS.md](DATABASE_COMMANDS.md#troubleshooting)

---

## Performance Considerations

### Production Recommendations

1. **Database Connection Pool**
   - Configure HikariCP settings in `application.properties`
   - Set appropriate pool size based on load

2. **JVM Tuning**
   - Set heap size: `-Xmx512m -Xms256m`
   - Use G1GC: `-XX:+UseG1GC`

3. **Caching**
   - Enable Spring Cache for frequently accessed data
   - Use Redis for distributed caching

4. **Monitoring**
   - Enable Spring Boot Actuator endpoints
   - Integrate with CloudWatch/Prometheus

---

## Security Best Practices

✓ **Passwords:** Never hardcoded, use environment variables
✓ **Sessions:** Token-based with expiration
✓ **SQL Injection:** Protected via JPA/Hibernate
✓ **CORS:** Configure allowed origins in production
✓ **HTTPS:** Use SSL/TLS in production (ALB with ACM)
✓ **Secrets:** AWS Secrets Manager for production credentials

---

## Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `./mvnw test`
5. Commit: `git commit -m 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Use meaningful variable/method names
- Add comments for complex logic
- Write unit tests for new features

### Commit Messages
- Use present tense: "Add feature" not "Added feature"
- Be descriptive but concise
- Reference issues when applicable

---

## Resources

### Documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [MySQL 8.0 Reference](https://dev.mysql.com/doc/refman/8.0/en/)

### Project Documentation
- [Database Management Guide](DATABASE_COMMANDS.md)
- [AWS Deployment Guide](AWS_ECS_DEPLOYMENT.md)
- [Test Database Setup](infra/db/README_TEST_DB.md)

---

## License

This project is proprietary software. All rights reserved.

---

## Support

For issues, questions, or contributions:
- **GitHub Issues:** https://github.com/sanket1106/schoolday-be/issues
- **Email:** sanket1106@gmail.com

---

## Changelog

### v1.0.0 (Latest)
- Initial release
- User authentication and management
- Child profile management
- Parent-child relationships
- Role-based access control
- Database management scripts
- AWS ECS deployment configuration
- Comprehensive documentation

---

**Built with ❤️ using Spring Boot**
