# AcciSense - Accident Detection & Sensing System

A real-time accident detection system built in pure Java (Swing GUI + SQLite + JDBC).

## Problem Statement

Road accidents cause approximately **1.35 million deaths worldwide every year** (WHO),
and the biggest killer is not the crash itself but the **delay in emergency response**.
If medical help arrives within the **"Golden Hour"** (first 60 minutes after the crash),
survival rates increase by **60-70%**.

After an accident - especially on highways or in remote areas - there is often
**no one to report the crash**: the victim may be unconscious and no witnesses may be
present. Existing approaches (manual phone calls, airbag sensors, GPS fleet trackers,
smartphone apps, insurance telematics) either depend on human action, do not notify
anyone outside the vehicle, or analyse data only after the fact.

**Result: preventable deaths caused by late detection and late reporting.**

## Proposed Solution

AcciSense **removes the human from the detection loop**. It continuously monitors
simulated vehicle sensors and reacts automatically the instant a crash occurs:

1. **SENSE** - Accelerometer, Gyroscope and Impact sensors stream data every 500 ms.
2. **DETECT** - 2-of-3 threshold confirmation (Impact > 40 kN, Accel > 20 m/s2, Gyro > 120 deg/s).
3. **CLASSIFY** - Weighted severity score -> LOW / MEDIUM / HIGH / CRITICAL.
4. **ALERT** - Audible beep + simulated SMS to emergency contacts within 500 ms.
5. **PERSIST** - Every accident stored in SQLite via JDBC for later analysis.
6. **VISUALIZE** - Animated Swing dashboard with live waveform, impact gauge and accident log.

Unlike existing systems, AcciSense needs **no conscious human**, sends **external alerts**,
provides **severity information**, analyses **in real time**, and offers a
**live monitoring view** - all in one pure-Java application.

## Screenshots

![Dashboard](screenshots/dashboard.png)

![Crash Alert](screenshots/crash_alert.png)

![Database Table](screenshots/database_table.png)

## Features
- Real-time sensor simulation (Accelerometer, Gyroscope, Impact)
- Threshold-based accident detection (2-of-3 trigger confirmation)
- Severity classification: LOW / MEDIUM / HIGH / CRITICAL
- Sound alert (Java Sound API) + SMS simulation
- SQLite storage via JDBC (PreparedStatement, ResultSet, CRUD)
- Animated Swing dashboard: glowing cards, impact gauge, live waveform, flashing alerts

## OOP Concepts (Rubric Coverage)
| Concept | Where |
|---|---|
| Inheritance | BaseSensor -> AccelerometerSensor / GyroscopeSensor / ImpactSensor |
| Polymorphism | read() / readAll() overridden, runtime dispatch |
| Interfaces | SensorListener, AccidentListener, AlertListener |
| Exception Handling | SensorException, DatabaseException, try-catch |
| Collections & Generics | ArrayList, HashMap, ConcurrentLinkedQueue, Comparable |
| Multithreading & Synchronization | Runnable sensor thread, synchronized blocks, SwingUtilities.invokeLater |
| JDBC | DriverManager, PreparedStatement, ResultSet, DatabaseManager CRUD |

## Project Structure
- src/com/accisense/main - AcciSenseApp.java (entry point, wiring)
- src/com/accisense/model - SensorData, AccidentRecord
- src/com/accisense/sensor - BaseSensor + 3 sensors + SensorSimulator
- src/com/accisense/detection - AccidentDetector
- src/com/accisense/alert - AlertManager
- src/com/accisense/exception - SensorException, DatabaseException
- src/com/accisense/storage - DatabaseManager (JDBC)
- src/com/accisense/gui - Dashboard (animated Swing UI)

## Requirements
- JDK 17+
- JARs in lib/ (sqlite-jdbc, slf4j-api, slf4j-nop) - already included in repo

## How to Run
1. Compile:
   javac -cp "lib/*" -d out src/com/accisense/*/*.java
2. Run (Windows):
   java -cp "out;lib/*" com.accisense.main.AcciSenseApp
   (Linux/Mac: use : instead of ; in classpath)
3. Click SIMULATE CRASH / SIMULATE ROLLOVER to test detection.