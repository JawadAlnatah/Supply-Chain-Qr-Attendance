# Supply-Chain-Management-System-With-QR-Based-Attendance-Employee-Tracking-

# Supply Chain Management System - Setup Guide

## Project Structure Fix

Your project should have this structure:
```
C:\SWE PROJECT\supply-chain-system\
│
├── src/
│   └── main/
│       └── java/
│           ├── module-info.java
│           └── com/
│               └── team/
│                   └── supplychain/
│                       ├── Main.java
│                       ├── controllers/
│                       │   └── LoginController.java
│                       ├── dao/
│                       │   ├── UserDAO.java
│                       │   └── InventoryDAO.java
│                       ├── models/
│                       │   ├── User.java
│                       │   ├── Employee.java
│                       │   └── Inventory.java
│                       ├── utils/
│                       │   └── DatabaseConnection.java
│                       └── views/
│                           └── LoginScreen.java
├── lib/
│   ├── javafx-sdk-21/
│   │   └── lib/
│   │       ├── javafx.base.jar
│   │       ├── javafx.controls.jar
│   │       ├── javafx.fxml.jar
│   │       ├── javafx.graphics.jar
│   │       └── ... (other JavaFX jars)
│   └── mysql-connector-java-8.0.33.jar
├── bin/
├── .vscode/
│   ├── settings.json
│   └── launch.json
├── run.bat
├── run.ps1
└── database_setup.sql
```

## Setup Instructions

### 1. Fix JavaFX Path Issue

Since you have Java 25 but JavaFX SDK 21, you need to ensure compatibility:

1. **Download JavaFX 21** (if not already done):
   - Go to https://openjfx.io/
   - Download JavaFX 21 (LTS) for Windows
   - Extract to: `C:\SWE PROJECT\supply-chain-system\lib\javafx-sdk-21\`

2. **Download MySQL Connector**:
   - Download from: https://dev.mysql.com/downloads/connector/j/
   - Place in: `C:\SWE PROJECT\supply-chain-system\lib\mysql-connector-java-8.0.33.jar`

### 2. Database Setup

1. Open MySQL Workbench or command line
2. Run the `database_setup.sql` file
3. This creates the database and sample data

### 3. Fix File Locations

1. **Move LoginScreen.java** from `models` to `views` package
2. **Create Main.java** in the root package
3. **Update all files** with the corrected versions provided

### 4. Running the Application

#### Method 1: Using Batch Script
```batch
cd "C:\SWE PROJECT\supply-chain-system"
run.bat
```

#### Method 2: Using PowerShell
```powershell
cd "C:\SWE PROJECT\supply-chain-system"
powershell -ExecutionPolicy Bypass -File run.ps1
```

#### Method 3: Using VS Code
1. Copy the new `launch.json` and `settings.json` to `.vscode` folder
2. Press F5 to run

#### Method 4: Command Line
```batch
cd "C:\SWE PROJECT\supply-chain-system"

# Compile
javac --module-path "lib\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -cp "lib\mysql-connector-java-8.0.33.jar" -d bin src\main\java\module-info.java src\main\java\com\team\supplychain\*.java src\main\java\com\team\supplychain\models\*.java src\main\java\com\team\supplychain\dao\*.java src\main\java\com\team\supplychain\controllers\*.java src\main\java\com\team\supplychain\views\*.java src\main\java\com\team\supplychain\utils\*.java

# Run
java --module-path "lib\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib\mysql-connector-java-8.0.33.jar" com.team.supplychain.Main
```

## Common Issues and Solutions

### Issue 1: "JavaFX runtime components are missing"
**Solution**: Ensure `--module-path` points to the correct JavaFX lib folder and `--add-modules` includes all required modules.

### Issue 2: "Class not found: Main"
**Solution**: Create the Main.java file in the correct package location.

### Issue 3: "Module not found"
**Solution**: Add module-info.java to src/main/java folder.

### Issue 4: Java version mismatch
**Solution**: JavaFX 21 works with Java 11-21. If using Java 25, you might need to:
- Either downgrade to Java 21
- Or use JavaFX 22+ (check compatibility at https://openjfx.io/)

## Test Credentials
- Username: `admin`
- Password: `password123`

## Next Steps After Setup

1. ✅ Verify database connection works
2. ✅ Test login functionality  
3. 📝 Complete remaining DAO classes
4. 📝 Implement main dashboard
5. 📝 Add QR code generation
6. 📝 Implement inventory management screens

## Team Responsibilities (Week 9)

- **Jawad & Mustafa**: Fix database connection, implement remaining DAOs
- **Ahmed & Abdullah A.**: Create main dashboard after login works
- **Abdullah J.**: Design remaining screens in Scene Builder
- **Mohammad**: Write unit tests for DAOs, update documentation
