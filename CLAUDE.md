# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
mvn clean compile          # Compile
mvn clean package          # Build uber JAR (maven-shade)
mvn test                   # Run all tests
mvn test -Dtest=ShipTest   # Run a single test class
mvn test jacoco:report     # Run tests with coverage (report: target/site/jacoco/index.html)
mvn spotbugs:check         # Static analysis
```

Running specific entry points:
```bash
mvn exec:java@SessionCreator   -Dexec.args="..."
mvn exec:java@SnapshotGenerator -Dexec.args="..."
mvn exec:java@OrderParser      -Dexec.args="..."
mvn exec:java@TurnUpdater      -Dexec.args="..."
```

## What This Project Does

Star Empires Updater is a turn-based space strategy game engine. It processes empire orders, manages galactic objects (ships, worlds, portals, storms), and maintains game state across turns. The game runs in sessions stored either on AWS S3 or local JSON files.

**Four main programs** (each has a CLI variant and an AWS Lambda handler):
1. **SessionCreator** – bootstraps a new game session (empires, worlds, portals, storms)
2. **SnapshotGenerator** – produces per-empire JSON snapshots filtered to what that empire can see
3. **OrderParser** – parses text-format orders submitted by a player into structured JSON
4. **TurnUpdater** – executes all turn phases in sequence and advances game state

## Architecture

### Central Data Structure

`TurnData.java` is the single object that holds all game state for a turn: maps of empires, worlds, ships, portals, storms, and all submitted orders organized by `OrderType`. Every phase updater reads from and writes to a `TurnData` instance.

### Turn Processing Pipeline

`TurnUpdater` runs a fixed sequence of ~50 named phases, each implemented as a `PhaseUpdater`. The phase registry (lines 36–90 of `TurnUpdater.java`) maps each `Phase` enum value to a factory function. Phases are grouped into stages executed in order:

1. **ADMINISTRATION** – Add/remove/modify map objects and ships (GM orders)
2. **LOGISTICS** – Load/unload cargo, deploy devices
3. **ASTRONOMICS** – Portal traversal and storm drift
4. **COMBAT** – Firing, damage resolution, ownership changes
5. **MOVEMENT** – Ship movement and navigation
6. **RESEARCH** – Design salvage, tech transfer
7. **MAINTENANCE** – Construction, repair, concealment
8. **INCOME** – Resource production and pooling
9. **SCANNING** – Recon and intel sharing between empires

### Key Packages

| Package | Role |
|---|---|
| `com.starempires.objects` | Domain model: `Empire`, `Ship`, `World`, `Portal`, `Storm`, `Fleet`, `Coordinate`, `ShipClass`, `ScanData`, etc. |
| `com.starempires.updater` | ~50 `PhaseUpdater` implementations, one per phase |
| `com.starempires.orders` | `OrderType` enum (~40 types), one `Order` subclass per type, `OrderParser` |
| `com.starempires.creator` | `SessionCreator` – world generation, wormnet construction, homeworld setup |
| `com.starempires.generator` | `SnapshotGenerator` – empire-scoped view with frame-of-reference coordinate transforms |
| `com.starempires.dao` | `S3StarEmpiresDAO` (primary) and `JsonStarEmpiresDAO` (file-based) for persisting `TurnData` |
| `com.starempires.aws` | Lambda handlers wrapping each main program |
| `com.starempires.constants` | `Constants.java` – game balance constants (repair multipliers, world generation params, etc.) |

### Coordinate System

`Coordinate.java` uses a **hexagonal grid**. `FrameOfReference.java` transforms between absolute coordinates and each empire's local coordinate system (empires see the galaxy relative to their homeworld).

### Scan / Knowledge System

`ScanData` and `ScanRecord` track what each empire knows about objects. `SnapshotGenerator` filters `TurnData` through each empire's scan records before writing the snapshot JSON.

### Persistence

`StarEmpiresDAO` interface abstracts storage. In production, `S3StarEmpiresDAO` stores `TurnData` and orders as JSON objects in S3. `JsonStarEmpiresDAO` writes to local files and is used in development and tests.

### Testing

`BaseTest.java` in `src/test/java/com/starempires/util/` is the superclass for most tests. It loads fixtures from `src/test/resources/test/` (hull parameters, ship classes, a baseline `test.0.turn-data.json`) and provides helper methods to build empires, worlds, portals, storms, and ships.

## Tech Stack

- **Java 25**, **Maven 3**
- **Lombok** (builders, getters, `@Slf4j`)
- **Jackson** for all JSON serialization/deserialization; custom `CustomOrderDeserializer` handles polymorphic `Order` objects
- **AWS SDK v2** (S3, DynamoDB), **AWS Lambda** Java runtime
- **Apache Commons CLI** for command-line argument parsing
- **JUnit Jupiter 6 + Mockito 5** for tests
- **Log4j2** (level overridable via `LOG_LEVEL` env var)
