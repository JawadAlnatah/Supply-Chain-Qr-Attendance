# Setup Instructions for Developers

## Prerequisites
- Java 17 or higher
- Apache Maven 3.9+
- TiDB Cloud account (free tier available)

## First-Time Setup

### 1. Clone the Repository
```bash
git clone https://github.com/JawadAlnatah/Integrated-Supply-Chain-Management-System-with-QR-Based-Employee-Attendance-Tracking-.git
cd Supply-Chain-Management-System-With-QR-Based-Attendance-Employee-Tracking-
```

### 2. Configure Database

**Step 2.1: Create config.properties**
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

**Step 2.2: Set up TiDB Cloud**
1. Sign up at https://tidbcloud.com/ (free tier available)
2. Create a new cluster
3. Note your connection details

**Step 2.3: Initialize Database**
Run the SQL scripts in order:
```bash
# Connect to your TiDB Cloud instance and run:
# 1. db/database_setup.sql
# 2. db/database_migration.sql
```

**Step 2.4: Update config.properties**
```properties
db.url=jdbc:mysql://YOUR_HOST:4000/supply_chain_qr?sslMode=VERIFY_IDENTITY&useSSL=true
db.username=YOUR_USERNAME
db.password=YOUR_PASSWORD
```

### 3. Build and Run
```bash
# Build project
mvn clean install 

# Run application
mvn javafx:run
```


### 4. Test Login.
Use these test credentials:
- **Admin**: username: `admin`, password: `password123`
- **Manager**: username: `manager1`, password: `password123`
- **Employee**: username: `employee1`, password: `employee123`

## Troubleshooting

### Database Connection Failed
- Verify internet connection
- Check TiDB Cloud cluster is active
- Confirm credentials in config.properties are correct
- Ensure SSL/TLS is enabled in database settings

### Build Errors
```bash
# Clean and rebuild
mvn clean install -U

# If dependencies fail, clear Maven cache
rm -rf ~/.m2/repository
mvn clean install
```

## Security Notes
- **NEVER commit config.properties** - it contains sensitive credentials
- The .gitignore file protects this file from being committed
- Rotate credentials if accidentally exposed
