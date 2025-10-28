# SchoolDay Database Management Guide

This guide provides a comprehensive reference for all database and container management commands available in the SchoolDay project.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Main Command Interface](#main-command-interface)
- [Container Management](#container-management)
- [Database Operations](#database-operations)
- [Test Database Management](#test-database-management)
- [Direct Script Commands](#direct-script-commands)
- [Common Workflows](#common-workflows)
- [Database Configuration](#database-configuration)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Environment Configuration

**IMPORTANT:** All scripts now use environment variables for sensitive information like passwords. You must configure these before running any commands.

1. **Copy the example environment file:**
   ```bash
   cp infra/.env.example infra/.env
   ```

2. **Edit `infra/.env` and set your MySQL password:**
   ```bash
   # MySQL Configuration
   MYSQL_ROOT_PASSWORD=your_secure_password_here
   MYSQL_DATABASE=schoolday
   MYSQL_PORT=3306
   MYSQL_HOST=localhost
   ```

3. **Export environment variables for Spring Boot application:**
   ```bash
   export MYSQL_ROOT_PASSWORD=your_secure_password_here
   export MYSQL_HOST=localhost
   export MYSQL_PORT=3306
   export MYSQL_DATABASE=schoolday
   ```

   Or add them to your shell profile (`~/.bashrc`, `~/.zshrc`, etc.)

**Security Note:** The `infra/.env` file is excluded from git via `.gitignore`. Never commit passwords to version control.

---

## Quick Start

### Complete Setup from Scratch
```bash
# 1. Configure environment (see Prerequisites above)
cp infra/.env.example infra/.env
# Edit infra/.env and set MYSQL_ROOT_PASSWORD

# 2. Initialize infrastructure
./schoolday.sh dev init all           # Initialize network and MySQL container
./schoolday.sh dev create mysql-schema # Create database schema
./schoolday.sh dev insert data         # Seed initial data
```

### Start/Stop MySQL
```bash
./schoolday.sh dev start mysql        # Start MySQL container
./schoolday.sh dev stop mysql         # Stop MySQL container
```

---

## Main Command Interface

The primary interface for managing infrastructure is **schoolday.sh** (located at project root).

### Command Syntax
```bash
./schoolday.sh <ENV> <ACTION> <ENTITY>
```

**Parameters:**
- `<ENV>` - Environment name (e.g., `dev`, `local`, `prod`)
- `<ACTION>` - Action to perform (see below)
- `<ENTITY>` - Target entity (see below)

### Available Actions

#### 1. Initialize (`init`)
Creates and configures new containers and networks.

```bash
./schoolday.sh dev init mysql         # Initialize MySQL container only
./schoolday.sh dev init all           # Initialize network + MySQL container
```

**What it does:**
- Creates a Podman/Docker network (`dev-schoolday-net`)
- Creates MySQL 8 container with persistent storage
- Configures root password and default database

#### 2. Create Schema (`create`)
Creates database schema (tables, constraints, indexes).

```bash
./schoolday.sh dev create mysql-schema
```

**What it does:**
- Creates `schoolday` database if it doesn't exist
- Creates tables: `user`, `role`, `child`, `user_role`, `parent_child`, `user_session`
- Sets up foreign key constraints and indexes
- Uses SQL script: [infra/db/sql/schema.sql](infra/db/sql/schema.sql)

#### 3. Insert Data (`insert`)
Seeds the database with initial base data.

```bash
./schoolday.sh dev insert data
```

**What it does:**
- Inserts 10 test users (user_1 through user_10)
- Inserts 3 roles: ADMIN, TEACHER, PARENT
- Creates user-role assignments
- Inserts 5 test children
- Uses SQL script: [infra/db/sql/basedata.sql](infra/db/sql/basedata.sql)

#### 4. Start (`start`)
Starts existing containers.

```bash
./schoolday.sh dev start mysql        # Start MySQL container
./schoolday.sh dev start all          # Start all containers
```

#### 5. Stop (`stop`)
Stops running containers.

```bash
./schoolday.sh dev stop mysql         # Stop MySQL container
./schoolday.sh dev stop all           # Stop all containers
```

#### 6. Remove (`rm`)
Removes containers (data in volumes is preserved).

```bash
./schoolday.sh dev rm mysql           # Remove MySQL container
./schoolday.sh dev rm all             # Remove all containers
```

**Warning:** This removes the container but preserves the `mysql-data` volume. To completely reset, you must also remove the volume manually.

---

## Container Management

### Initialize MySQL Container
**Script:** [infra/containers/init_mysql.sh](infra/containers/init_mysql.sh)

```bash
./infra/containers/init_mysql.sh mysql8 dev-schoolday-net
```

**Configuration:**
- **Image:** `mysql:8`
- **Container Name:** `mysql8`
- **Port:** `3306:3306` (host:container)
- **Root Password:** Configured in `infra/.env` (`MYSQL_ROOT_PASSWORD`)
- **Database:** Configured in `infra/.env` (`MYSQL_DATABASE`, default: `schoolday`)
- **Volume:** `mysql-data:/var/lib/mysql` (persistent storage)
- **Network:** Custom Podman network

**Note:** This script loads credentials from `infra/.env` file.

### Initialize Network
**Script:** [infra/containers/init_network.sh](infra/containers/init_network.sh)

```bash
./infra/containers/init_network.sh dev-schoolday-net
```

Creates a Podman/Docker network for container communication.

### Start MySQL Container
**Script:** [infra/containers/start_mysql.sh](infra/containers/start_mysql.sh)

```bash
./infra/containers/start_mysql.sh
```

Starts the MySQL container if it exists.

### Generic Container Start
**Script:** [infra/containers/start_container.sh](infra/containers/start_container.sh)

```bash
./infra/containers/start_container.sh mysql8
```

Generic utility to start any container by name.

---

## Database Operations

### Create Database Schema
**Script:** [infra/db/create_schema.sh](infra/db/create_schema.sh)

```bash
./infra/db/create_schema.sh
```

**Connection Details:**
- Host: From `MYSQL_HOST` (default: `localhost`)
- User: `root`
- Password: From `MYSQL_ROOT_PASSWORD` in `infra/.env`
- Database: From `MYSQL_DATABASE` in `infra/.env` (default: `schoolday`)

**Note:** This script loads credentials from `infra/.env` file.

**Tables Created:**
1. **user** - User accounts (email, name, password, status)
2. **role** - User roles (ADMIN, TEACHER, PARENT) with permissions JSON
3. **user_role** - Many-to-many mapping between users and roles
4. **child** - Child profiles with date of birth
5. **parent_child** - Links parents to children with relationship type
6. **user_session** - Session tokens for active user sessions

**Schema Features:**
- Foreign key constraints with CASCADE on delete
- Unique constraints on emails and tokens
- Timestamp tracking (created, updated)
- Status fields for soft deletes
- JSON field for role permissions

### Seed Database Data
**Script:** [infra/db/seed_data.sh](infra/db/seed_data.sh)

```bash
./infra/db/seed_data.sh
```

**Base Data Inserted:**
- **Users:** 10 test users (user_1@example.com through user_10@example.com)
  - All passwords: `password123`
  - First names: User_1 through User_10
  - Last names: Lastname_1 through Lastname_10
- **Roles:** 3 roles
  - ADMIN (role_1) - Full permissions
  - TEACHER (role_2) - Teaching permissions
  - PARENT (role_3) - Parent permissions
- **Children:** 5 test children (Child_1 through Child_5)
  - Ages range from 5 to 13 years old
- **Assignments:** User-role mappings

---

## Test Database Management

The test database (`schoolday_test`) is used for running Spring Boot tests.

### Setup Test Database
**Script:** [infra/db/setup_test_db.sh](infra/db/setup_test_db.sh)

```bash
./infra/db/setup_test_db.sh
```

**What it does:**
- Creates `schoolday_test` database
- Applies identical schema as production
- Seeds with base test data

**Usage:** Run this once before running tests.

### Create Test Schema
**Script:** [infra/db/create_test_schema.sh](infra/db/create_test_schema.sh)

```bash
./infra/db/create_test_schema.sh
```

**What it does:**
- Creates `schoolday_test` database
- Applies schema from [infra/db/sql/schema_test.sql](infra/db/sql/schema_test.sql)
- Seeds data from [infra/db/sql/basedata_test.sql](infra/db/sql/basedata_test.sql)

### Reset Test Database
**Script:** [infra/db/reset_test_db.sh](infra/db/reset_test_db.sh)

```bash
./infra/db/reset_test_db.sh
```

**What it does:**
1. Drops existing `schoolday_test` database completely
2. Creates fresh `schoolday_test` database
3. Applies clean schema
4. Seeds fresh base data

**Use Cases:**
- Test database is corrupted
- Need clean state for debugging
- Schema changes require full reset
- Tests are failing due to data inconsistencies

**Warning:** This is destructive and removes ALL test data.

### Test Data Cleanup Scripts

These SQL scripts are used by Spring tests via `@Sql` annotation.

#### Clear Test Data (Preserve Base Data)
**Script:** [src/test/resources/sql/clear_test_data.sql](src/test/resources/sql/clear_test_data.sql)

```java
@Sql(scripts = "/sql/clear_test_data.sql",
     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
```

**What it does:**
- Clears test-created data while preserving base data
- Keeps base users (user_1 through user_10)
- Keeps base roles (role_1, role_2, role_3)
- Keeps base children (child_1 through child_5)

**Usage:** Automatic cleanup after each test method.

#### Clear All Data
**Script:** [src/test/resources/sql/clear_all_data.sql](src/test/resources/sql/clear_all_data.sql)

**What it does:**
- Completely clears ALL data including base data
- Temporarily disables foreign key checks
- Truncates all tables

**Usage:** When complete isolation between tests is needed.

---

## Direct Script Commands

All scripts support both Podman and Docker (auto-detected).

### Container Scripts
```bash
# Initialize components
./infra/containers/init_network.sh <network-name>
./infra/containers/init_mysql.sh <container-name> <network-name>

# Start containers
./infra/containers/start_mysql.sh
./infra/containers/start_container.sh <container-name>
```

### Database Scripts
```bash
# Production database
./infra/db/create_schema.sh          # Create schema
./infra/db/seed_data.sh              # Seed data

# Test database
./infra/db/setup_test_db.sh          # Setup test database
./infra/db/create_test_schema.sh     # Create test schema only
./infra/db/reset_test_db.sh          # Reset test database
```

---

## Common Workflows

### 1. First Time Setup (New Developer)
```bash
# Step 1: Initialize infrastructure
./schoolday.sh dev init all

# Step 2: Create database schema
./schoolday.sh dev create mysql-schema

# Step 3: Seed base data
./schoolday.sh dev insert data

# Step 4: Setup test database
./infra/db/setup_test_db.sh

# Step 5: Run application
./mvnw spring-boot:run
```

### 2. Daily Development Workflow
```bash
# Start MySQL
./schoolday.sh dev start mysql

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Stop MySQL (end of day)
./schoolday.sh dev stop mysql
```

### 3. Reset Everything (Clean Slate)
```bash
# Stop and remove all containers
./schoolday.sh dev stop all
./schoolday.sh dev rm all

# Remove MySQL volume (optional - loses all data)
podman volume rm mysql-data

# Reinitialize
./schoolday.sh dev init all
./schoolday.sh dev create mysql-schema
./schoolday.sh dev insert data
./infra/db/setup_test_db.sh
```

### 4. Schema Changes Workflow
```bash
# 1. Update schema.sql and schema_test.sql files

# 2. Reset production database
./schoolday.sh dev stop mysql
./schoolday.sh dev rm mysql
podman volume rm mysql-data
./schoolday.sh dev init mysql
./schoolday.sh dev create mysql-schema
./schoolday.sh dev insert data

# 3. Reset test database
./infra/db/reset_test_db.sh

# 4. Run tests to verify
./mvnw test
```

### 5. Debugging Test Failures
```bash
# Reset test database to clean state
./infra/db/reset_test_db.sh

# Run specific test
./mvnw test -Dtest=YourTestClass

# If needed, connect to test database manually
mysql -h localhost -u root -p schoolday_test
# Enter password from your MYSQL_ROOT_PASSWORD env var when prompted
```

### 6. Production-like Environment Setup
```bash
# Use 'prod' environment
./schoolday.sh prod init all
./schoolday.sh prod create mysql-schema
./schoolday.sh prod insert data

# Start with production profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## Database Configuration

### Environment Variables

All database credentials are now configured via environment variables for security:

| Variable | Description | Default |
|----------|-------------|---------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | *Required - must be set* |
| `MYSQL_DATABASE` | Database name | `schoolday` |
| `MYSQL_HOST` | MySQL host | `localhost` |
| `MYSQL_PORT` | MySQL port | `3306` |

**Configuration Files:**
- **Shell scripts:** Load from `infra/.env`
- **Spring Boot app:** Uses environment variables

### Production Database
- **Database Name:** From `MYSQL_DATABASE` env var (default: `schoolday`)
- **Host:** From `MYSQL_HOST` env var (default: `localhost`)
- **Port:** From `MYSQL_PORT` env var (default: `3306`)
- **User:** `root`
- **Password:** From `MYSQL_ROOT_PASSWORD` env var
- **Connection URL:** `jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}`

### Test Database
- **Database Name:** `schoolday_test` (hardcoded)
- **Host:** From `MYSQL_HOST` env var (default: `localhost`)
- **Port:** From `MYSQL_PORT` env var (default: `3306`)
- **User:** `root`
- **Password:** From `MYSQL_ROOT_PASSWORD` env var
- **Connection URL:** `jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/schoolday_test`

### Container Details
- **Container Name:** `mysql8`
- **Image:** `mysql:8`
- **Volume:** `mysql-data` (persistent storage)
- **Network:** `<env>-schoolday-net` (e.g., `dev-schoolday-net`)

### Application Configuration

**Production:** [src/main/resources/application.properties](src/main/resources/application.properties)
```properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:schoolday}
spring.datasource.username=root
spring.datasource.password=${MYSQL_ROOT_PASSWORD}
```

**Test:** [src/test/resources/application.properties](src/test/resources/application.properties)
```properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/schoolday_test
spring.datasource.username=root
spring.datasource.password=${MYSQL_ROOT_PASSWORD}
```

**Note:** Spring Boot will read these environment variables at runtime. Make sure to export them in your shell or IDE.

---

## Troubleshooting

### Container Issues

#### Container doesn't start
```bash
# Check if container exists
podman ps -a | grep mysql8

# Check logs
podman logs mysql8

# Remove and recreate
./schoolday.sh dev rm mysql
./schoolday.sh dev init mysql
```

#### Port 3306 already in use
```bash
# Find process using port 3306
lsof -i :3306

# Kill the process or change port in init_mysql.sh
```

#### Network issues
```bash
# List networks
podman network ls

# Remove and recreate network
podman network rm dev-schoolday-net
./schoolday.sh dev init all
```

### Database Issues

#### Cannot connect to database
```bash
# Verify container is running
podman ps | grep mysql8

# Check if MySQL is ready (you'll be prompted for password)
podman exec mysql8 mysqladmin ping -h localhost -u root -p

# Wait 30 seconds for MySQL to fully start
sleep 30
./infra/db/create_schema.sh
```

#### Schema creation fails
```bash
# Check MySQL logs
podman logs mysql8

# Try connecting manually (you'll be prompted for password)
mysql -h localhost -u root -p

# Verify SQL script syntax
cat infra/db/sql/schema.sql
```

#### Tests fail due to database state
```bash
# Reset test database
./infra/db/reset_test_db.sh

# Clear test data manually (you'll be prompted for password)
mysql -h localhost -u root -p schoolday_test < src/test/resources/sql/clear_all_data.sql

# Verify base data is present (you'll be prompted for password)
mysql -h localhost -u root -p -e "SELECT COUNT(*) FROM schoolday_test.user;"
```

### Permission Issues

#### Script not executable
```bash
# Make scripts executable
chmod +x schoolday.sh
chmod +x infra/containers/*.sh
chmod +x infra/db/*.sh
```

#### Podman/Docker permission denied
```bash
# For Podman (rootless)
# Usually works without sudo

# For Docker
sudo usermod -aG docker $USER
newgrp docker
```

### Data Issues

#### Need to completely reset MySQL data
```bash
# Stop and remove container
./schoolday.sh dev stop mysql
./schoolday.sh dev rm mysql

# Remove volume (THIS DELETES ALL DATA)
podman volume rm mysql-data

# Reinitialize everything
./schoolday.sh dev init mysql
./schoolday.sh dev create mysql-schema
./schoolday.sh dev insert data
```

#### Base data is missing or corrupted
```bash
# Re-run seed script
./infra/db/seed_data.sh

# Or reset completely
./infra/db/reset_test_db.sh  # For test database
```

### Maven/Spring Boot Issues

#### Tests cannot connect to database
1. Verify test database exists (you'll be prompted for password):
```bash
mysql -h localhost -u root -p -e "SHOW DATABASES;" | grep schoolday_test
```

2. Setup test database if missing:
```bash
./infra/db/setup_test_db.sh
```

3. Check application-test.properties configuration

#### Application cannot start
1. Check if MySQL is running:
```bash
podman ps | grep mysql8
```

2. Verify environment variables are exported:
```bash
echo $MYSQL_ROOT_PASSWORD
echo $MYSQL_HOST
echo $MYSQL_PORT
```

3. Export variables if needed:
```bash
export MYSQL_ROOT_PASSWORD=your_password_here
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=schoolday
```

4. Check application logs for specific errors

---

## Additional Resources

### Documentation Files
- [infra/db/README_TEST_DB.md](infra/db/README_TEST_DB.md) - Detailed test database documentation
- [src/test/resources/sql/README.md](src/test/resources/sql/README.md) - Test SQL scripts documentation

### SQL Script Files
- **Production Schema:** [infra/db/sql/schema.sql](infra/db/sql/schema.sql)
- **Production Data:** [infra/db/sql/basedata.sql](infra/db/sql/basedata.sql)
- **Test Schema:** [infra/db/sql/schema_test.sql](infra/db/sql/schema_test.sql)
- **Test Data:** [infra/db/sql/basedata_test.sql](infra/db/sql/basedata_test.sql)

### Direct MySQL Access
```bash
# Connect to production database
mysql -h localhost -u root -p schoolday
# Enter password from your MYSQL_ROOT_PASSWORD env var when prompted

# Connect to test database
mysql -h localhost -u root -p schoolday_test
# Enter password from your MYSQL_ROOT_PASSWORD env var when prompted

# Execute from container
podman exec -it mysql8 mysql -u root -p schoolday
# Enter password from your MYSQL_ROOT_PASSWORD env var when prompted
```

### Useful MySQL Commands
```sql
-- Show all databases
SHOW DATABASES;

-- Show all tables
SHOW TABLES;

-- Describe table structure
DESCRIBE user;

-- Count records
SELECT COUNT(*) FROM user;

-- View base users
SELECT id, email, first_name, last_name FROM user;

-- View roles
SELECT id, name, permissions FROM role;
```

---

## Summary of Key Commands

| Purpose | Command |
|---------|---------|
| Complete setup | `./schoolday.sh dev init all && ./schoolday.sh dev create mysql-schema && ./schoolday.sh dev insert data` |
| Start MySQL | `./schoolday.sh dev start mysql` |
| Stop MySQL | `./schoolday.sh dev stop mysql` |
| Create schema | `./schoolday.sh dev create mysql-schema` |
| Seed data | `./schoolday.sh dev insert data` |
| Setup test DB | `./infra/db/setup_test_db.sh` |
| Reset test DB | `./infra/db/reset_test_db.sh` |
| Run tests | `./mvnw test` |
| Run application | `./mvnw spring-boot:run` |
| Connect to DB | `mysql -h localhost -u root -p schoolday` |

---

**Need Help?** Check the [Troubleshooting](#troubleshooting) section or review the detailed documentation in the `infra/db/` directory.
