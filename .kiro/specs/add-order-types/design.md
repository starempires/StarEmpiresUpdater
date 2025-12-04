# Design Document

## Overview

This design implements AUTHORIZE and DENY order types that enable empires to share scan data with other empires. The implementation follows the existing Star Empires order pattern: orders are parsed during submission, validated against game state, and executed during turn processing phases.

Scan data sharing operates at three levels:
1. **By Sector**: Share scan data for specific coordinates with a radius
2. **By Ship**: Share scan data collected by specific ships
3. **All Data**: Share all scan data unconditionally

The Empire class already contains the necessary data structures for storing authorizations (shareCoordinates, shareShips, shareEmpires), so the orders primarily manipulate these existing collections.

## Architecture

### Order Flow

```
Player submits AUTHORIZE/DENY order text
    ↓
OrderParser routes to AuthorizeOrder/DenyOrder.parse()
    ↓
Order validates parameters and target empires
    ↓
Order marked as ready (or not ready with errors)
    ↓
Order serialized to JSON in TurnData
    ↓
AuthorizeScanDataPhaseUpdater executes AUTHORIZE orders
    ↓
DenyScanDataPhaseUpdater executes DENY orders
    ↓
Empire authorization collections updated
    ↓
ShareScanDataPhaseUpdater applies authorizations during scan sharing
```

### Integration with Existing Phases

The system already has phase updaters for scan data:
- `AuthorizeScanDataPhaseUpdater`: Executes AUTHORIZE orders (already exists but needs implementation)
- `DenyScanDataPhaseUpdater`: Executes DENY orders (already exists but needs implementation)
- `ShareScanDataPhaseUpdater`: Applies authorizations to share scan data (already exists)

## Components and Interfaces

### 1. AuthorizeOrder Class

**Purpose**: Parse and validate AUTHORIZE orders for scan data sharing

**Base Class**: Extends `EmpireBasedOrder` (operates at empire level, not ship/world specific)

**Fields**:
- `targetEmpires`: List<Empire> - Empires receiving scan data access
- `authorizationType`: AuthorizationType enum (SECTOR, SHIP, ALL_DATA)
- `coordinates`: List<RadialCoordinate> - For sector-based authorization
- `ships`: List<Ship> - For ship-based authorization
- `locationText`: String - Original location text for error messages

**Order Syntax Patterns**:
1. By Sector (coordinate): `AUTHORIZE SECTOR (oblique,y) radius TO empire1 empire2 ...`
2. By Sector (world): `AUTHORIZE SECTOR @WorldName radius TO empire1 empire2 ...`
3. By Sector (portal): `AUTHORIZE SECTOR @PortalName radius TO empire1 empire2 ...`
4. By Sector (storm): `AUTHORIZE SECTOR @StormName radius TO empire1 empire2 ...`
5. By Ship: `AUTHORIZE SHIP ship1 ship2 ... TO empire1 empire2 ...`
6. All Data: `AUTHORIZE ALL TO empire1 empire2 ...`

**Regex Pattern**:
```java
// Pattern supports three authorization types
"(?:" +
  "(?:SECTOR" + SPACE_REGEX + "(?:" + COORDINATE_CAPTURE_REGEX + "|" + LOCATION_CAPTURE_REGEX + ")" + SPACE_REGEX + RADIUS_CAPTURE_REGEX + ")|" +
  "(?:SHIP" + SPACE_REGEX + SHIP_LIST_CAPTURE_REGEX + ")|" +
  "(?:ALL)" +
")" + SPACE_REGEX + TO_TOKEN + SPACE_REGEX + RECIPIENT_LIST_CAPTURE_REGEX
```

**Validation Logic**:
1. Validate all target empire names exist and are known to authorizing empire
2. Validate target empires are not the authorizing empire itself
3. For SECTOR type: validate coordinate/location exists and radius is non-negative
4. For SHIP type: validate all ships exist and are owned by authorizing empire
5. Mark order as ready if all validations pass

### 2. DenyOrder Class

**Purpose**: Parse and validate DENY orders to revoke scan data sharing

**Base Class**: Extends `EmpireBasedOrder`

**Fields**:
- `targetEmpires`: List<Empire> - Empires losing scan data access
- `denialType`: DenialType enum (SECTOR, SHIP, ALL_DATA)
- `coordinates`: List<RadialCoordinate> - For sector-based denial
- `ships`: List<Ship> - For ship-based denial
- `locationText`: String - Original location text for error messages

**Order Syntax Patterns**:
1. By Sector (coordinate): `DENY SECTOR (oblique,y) radius FROM empire1 empire2 ...`
2. By Sector (world): `DENY SECTOR @WorldName radius FROM empire1 empire2 ...`
3. By Sector (portal): `DENY SECTOR @PortalName radius FROM empire1 empire2 ...`
4. By Sector (storm): `DENY SECTOR @StormName radius FROM empire1 empire2 ...`
5. By Ship: `DENY SHIP ship1 ship2 ... FROM empire1 empire2 ...`
6. All Data: `DENY ALL FROM empire1 empire2 ...`

**Regex Pattern**:
```java
// Pattern supports three denial types
"(?:" +
  "(?:SECTOR" + SPACE_REGEX + "(?:" + COORDINATE_CAPTURE_REGEX + "|" + LOCATION_CAPTURE_REGEX + ")" + SPACE_REGEX + RADIUS_CAPTURE_REGEX + ")|" +
  "(?:SHIP" + SPACE_REGEX + SHIP_LIST_CAPTURE_REGEX + ")|" +
  "(?:ALL)" +
")" + SPACE_REGEX + FROM_TOKEN + SPACE_REGEX + RECIPIENT_LIST_CAPTURE_REGEX
```

**Note**: Need to add `FROM_TOKEN` constant to Order base class (similar to existing `TO_TOKEN`)

**Validation Logic**:
1. Validate all target empire names exist and are known to denying empire
2. Validate target empires are not the denying empire itself
3. For SECTOR type: validate coordinate/location exists and radius is non-negative
4. For SHIP type: validate all ships exist and are owned by denying empire
5. Mark order as ready if all validations pass


### 3. EmpireBasedOrder Class (if needed)

**Purpose**: Base class for orders that operate at empire level

**Note**: If this class doesn't exist, AuthorizeOrder and DenyOrder can extend Order directly. Check if EmpireBasedOrder exists; if not, create it as a simple extension of Order with empire-specific helper methods.

**Fields**:
- Inherits all fields from Order

**Helper Methods**:
- `validateTargetEmpires(TurnData, Empire, List<String>)`: Validates empire names and returns Empire objects
- `getEmpireList(TurnData, String)`: Parses space-separated empire names

### 4. OrderType Enum Updates

**Changes Required**:
```java
public enum OrderType {
    // ... existing entries ...
    AUTHORIZE(AuthorizeOrder.class, false),  // Player-accessible
    DENY(DenyOrder.class, false),            // Player-accessible
    // ... existing entries ...
}
```

### 5. PhaseUpdater Classes

#### AuthorizeScanDataPhaseUpdater

**Purpose**: Execute AUTHORIZE orders during turn processing

**Phase**: AUTHORIZE_SCAN_DATA (early in turn, before scan collection)

**Execution Logic**:
```java
@Override
public void update() {
    List<Order> orders = turnData.getOrders(OrderType.AUTHORIZE);
    orders.forEach(o -> {
        AuthorizeOrder order = (AuthorizeOrder) o;
        addOrderText(order);
        Empire empire = order.getEmpire();
        
        switch (order.getAuthorizationType()) {
            case SECTOR:
                order.getTargetEmpires().forEach(targetEmpire -> {
                    order.getCoordinates().forEach(radialCoord -> {
                        empire.addCoordinateScanAccess(targetEmpire, radialCoord);
                        addNews(order, "Authorized " + targetEmpire + " to access scan data for sector " + 
                                empire.toLocal(radialCoord) + " radius " + radialCoord.getRadius());
                    });
                });
                break;
                
            case SHIP:
                order.getTargetEmpires().forEach(targetEmpire -> {
                    empire.addShipScanAccess(targetEmpire, order.getShips());
                    addNews(order, "Authorized " + targetEmpire + " to access scan data from " + 
                            order.getShips().size() + " ship(s)");
                });
                break;
                
            case ALL_DATA:
                order.getTargetEmpires().forEach(targetEmpire -> {
                    empire.addEmpireScanAccess(targetEmpire);
                    addNews(order, "Authorized " + targetEmpire + " to access all scan data");
                });
                break;
        }
    });
}
```

#### DenyScanDataPhaseUpdater

**Purpose**: Execute DENY orders during turn processing

**Phase**: DENY_SCAN_DATA (early in turn, before scan collection)

**Execution Logic**:
```java
@Override
public void update() {
    List<Order> orders = turnData.getOrders(OrderType.DENY);
    orders.forEach(o -> {
        DenyOrder order = (DenyOrder) o;
        addOrderText(order);
        Empire empire = order.getEmpire();
        
        switch (order.getDenialType()) {
            case SECTOR:
                order.getTargetEmpires().forEach(targetEmpire -> {
                    empire.removeCoordinateScanAccess(targetEmpire, order.getCoordinates());
                    addNews(order, "Denied " + targetEmpire + " access to scan data for " + 
                            order.getCoordinates().size() + " sector(s)");
                });
                break;
                
            case SHIP:
                order.getTargetEmpires().forEach(targetEmpire -> {
                    empire.removeShipScanAccess(targetEmpire, order.getShips());
                    addNews(order, "Denied " + targetEmpire + " access to scan data from " + 
                            order.getShips().size() + " ship(s)");
                });
                break;
                
            case ALL_DATA:
                order.getTargetEmpires().forEach(targetEmpire -> {
                    empire.removeEmpireScanAccess(targetEmpire);
                    addNews(order, "Denied " + targetEmpire + " all scan data access");
                });
                break;
        }
    });
}
```

## Data Models

### AuthorizationType Enum

```java
public enum AuthorizationType {
    SECTOR,    // Authorize by coordinate with radius
    SHIP,      // Authorize by ship
    ALL_DATA   // Authorize all scan data
}
```

### DenialType Enum

```java
public enum DenialType {
    SECTOR,    // Deny by coordinate with radius
    SHIP,      // Deny by ship
    ALL_DATA   // Deny all scan data
}
```

### RadialCoordinate Usage

The existing `RadialCoordinate` class stores a coordinate with a radius:
- `oblique`: X coordinate
- `y`: Y coordinate  
- `radius`: Radius from center coordinate

For AUTHORIZE/DENY by sector, we create RadialCoordinate objects and store them in Empire's `shareCoordinates` multimap.

### Empire Authorization Storage

The Empire class already has these collections:
- `shareCoordinates`: Multimap<Empire, RadialCoordinate> - Sector-based authorizations
- `shareShips`: Multimap<Empire, Ship> - Ship-based authorizations
- `shareEmpires`: Set<Empire> - All-data authorizations

## Error Handling

### Parsing Errors

**Unknown Empire**:
```java
if (targetEmpire == null) {
    order.addError("Unknown empire: " + empireName);
    return order;
}
```

**Self-Authorization**:
```java
if (targetEmpire.equals(empire)) {
    order.addError("Cannot authorize your own empire");
    return order;
}
```

**Unknown Empire (Not Encountered)**:
```java
if (!empire.isKnownEmpire(targetEmpire)) {
    order.addError("Unknown empire: " + targetEmpire + " (not yet encountered)");
    return order;
}
```

**Invalid Radius**:
```java
if (radius < 0) {
    order.addError("Radius must be non-negative: " + radius);
    return order;
}
```

**Unknown Ship**:
```java
if (ship == null) {
    order.addError("Unknown ship: " + shipName);
    return order;
}
```

**Ship Not Owned**:
```java
if (!ship.getOwner().equals(empire)) {
    order.addError(ship, "You do not own this ship");
    return order;
}
```

**Unknown Location**:
```java
if (coordinate == null) {
    order.addError("Unknown location: " + locationText);
    return order;
}
```

### Execution Errors

During execution, PhaseUpdaters should validate that:
- Target empires still exist
- Ships still exist and are owned by the empire
- Coordinates are still valid

If validation fails, add news message but don't fail the entire turn.

## Testing Strategy

### Unit Tests for AuthorizeOrder

**Test Cases**:
1. `testAuthorizeBySectorWithCoordinate()` - Valid sector authorization with coordinate
2. `testAuthorizeBySectorWithWorld()` - Valid sector authorization with world name
3. `testAuthorizeBySectorWithPortal()` - Valid sector authorization with portal name
4. `testAuthorizeBySectorWithStorm()` - Valid sector authorization with storm name
5. `testAuthorizeByShip()` - Valid ship-based authorization
6. `testAuthorizeAllData()` - Valid all-data authorization
7. `testAuthorizeMultipleEmpires()` - Authorize multiple empires at once
8. `testAuthorizeUnknownEmpire()` - Error for unknown empire
9. `testAuthorizeSelfEmpire()` - Error for self-authorization
10. `testAuthorizeNotKnownEmpire()` - Error for empire not in knownEmpires
11. `testAuthorizeUnknownShip()` - Error for unknown ship
12. `testAuthorizeShipNotOwned()` - Error for ship not owned
13. `testAuthorizeInvalidRadius()` - Error for negative radius
14. `testAuthorizeUnknownLocation()` - Error for unknown location
15. `testAuthorizeSerialization()` - Round-trip JSON serialization

### Unit Tests for DenyOrder

**Test Cases**:
1. `testDenyBySectorWithCoordinate()` - Valid sector denial with coordinate
2. `testDenyBySectorWithWorld()` - Valid sector denial with world name
3. `testDenyByShip()` - Valid ship-based denial
4. `testDenyAllData()` - Valid all-data denial
5. `testDenyMultipleEmpires()` - Deny multiple empires at once
6. `testDenyUnknownEmpire()` - Error for unknown empire
7. `testDenySelfEmpire()` - Error for self-denial
8. `testDenyNotKnownEmpire()` - Error for empire not in knownEmpires
9. `testDenyUnknownShip()` - Error for unknown ship
10. `testDenyShipNotOwned()` - Error for ship not owned
11. `testDenyInvalidRadius()` - Error for negative radius
12. `testDenyUnknownLocation()` - Error for unknown location
13. `testDenySerialization()` - Round-trip JSON serialization

### Integration Tests

**Test Cases**:
1. `testAuthorizeAndDenySequence()` - Authorize then deny same access
2. `testAuthorizeAllDataOverridesSpecific()` - All-data authorization removes specific authorizations
3. `testDenyAllDataRemovesAll()` - All-data denial removes all authorizations
4. `testPhaseUpdaterExecution()` - Verify PhaseUpdaters update Empire collections correctly
5. `testScanDataSharing()` - Verify authorized empires receive scan data

## Implementation Patterns

### Pattern: Parsing Multiple Empires

```java
private List<Empire> parseTargetEmpires(TurnData turnData, Empire empire, String empiresText, Order order) {
    List<Empire> targetEmpires = Lists.newArrayList();
    String[] empireNames = empiresText.split(SPACE_REGEX);
    
    for (String empireName : empireNames) {
        Empire targetEmpire = turnData.getEmpire(empireName);
        
        if (targetEmpire == null) {
            order.addError("Unknown empire: " + empireName);
        } else if (targetEmpire.equals(empire)) {
            order.addError("Cannot authorize your own empire");
        } else if (!empire.isKnownEmpire(targetEmpire)) {
            order.addError("Unknown empire: " + empireName + " (not yet encountered)");
        } else {
            targetEmpires.add(targetEmpire);
        }
    }
    
    return targetEmpires;
}
```

### Pattern: Parsing Sector with Radius

```java
private RadialCoordinate parseSectorWithRadius(TurnData turnData, Empire empire, String locationText, int radius, Order order) {
    Coordinate coordinate = null;
    
    if (locationText.matches(COORDINATE_REGEX)) {
        // Parse as coordinate (oblique, y)
        Coordinate localCoord = Coordinate.parse(locationText);
        coordinate = empire.toGalactic(localCoord);
    } else {
        // Parse as named location - check world, portal, or storm
        String mapObjectName = locationText.replace("@", "");
        
        // Try world first
        World world = turnData.getWorld(mapObjectName);
        if (world != null && empire.isKnownWorld(world)) {
            coordinate = world.getCoordinate();
        } else {
            // Try portal
            Portal portal = turnData.getPortal(mapObjectName);
            if (portal != null && empire.isKnownPortal(portal)) {
                coordinate = portal.getCoordinate();
            } else {
                // Try storm
                Storm storm = turnData.getStorm(mapObjectName);
                if (storm != null && empire.isKnownStorm(storm)) {
                    coordinate = storm.getCoordinate();
                }
            }
        }
        
        if (coordinate == null) {
            order.addError("Unknown location: " + mapObjectName);
            return null;
        }
    }
    
    if (radius < 0) {
        order.addError("Radius must be non-negative: " + radius);
        return null;
    }
    
    return new RadialCoordinate(coordinate.getOblique(), coordinate.getY(), radius);
}
```

### Pattern: Parsing Ships

```java
private List<Ship> parseShips(Empire empire, String shipsText, Order order) {
    List<Ship> ships = Lists.newArrayList();
    String[] shipNames = shipsText.split(SPACE_REGEX);
    
    for (String shipName : shipNames) {
        Ship ship = empire.getShip(shipName);
        
        if (ship == null) {
            order.addError("Unknown ship: " + shipName);
        } else if (!ship.getOwner().equals(empire)) {
            order.addError(ship, "You do not own this ship");
        } else {
            ships.add(ship);
        }
    }
    
    return ships;
}
```

## Integration Points

### CustomOrderDeserializer Updates

Add cases for AUTHORIZE and DENY in the deserializer:

```java
case "authorize":
    return AuthorizeOrder.parseReady(node, turnData);
case "deny":
    return DenyOrder.parseReady(node, turnData);
```

### Phase Enum Updates

Ensure Phase enum includes:
- `AUTHORIZE_SCAN_DATA`
- `DENY_SCAN_DATA`

These phases should execute early in the turn, before scan data collection.

### TurnUpdater Integration

The TurnUpdater should already call PhaseUpdaters in sequence. Verify that AUTHORIZE_SCAN_DATA and DENY_SCAN_DATA phases are included in the execution order.

## Design Decisions and Rationales

### Why Extend EmpireBasedOrder?

**Decision**: Create or use EmpireBasedOrder as base class

**Rationale**:
- AUTHORIZE and DENY operate at empire level, not ship or world level
- Provides consistent pattern with ShipBasedOrder and WorldBasedOrder
- Allows shared validation logic for empire-related operations

### Why Use Enums for Authorization/Denial Types?

**Decision**: Use AuthorizationType and DenialType enums

**Rationale**:
- Type-safe representation of authorization modes
- Enables switch statements in PhaseUpdaters
- Clear documentation of supported authorization types
- Easy to extend with new types in future

### Why Parse During Order Submission?

**Decision**: Parse and validate orders immediately when submitted

**Rationale**:
- Provides immediate feedback to players
- Catches errors before turn processing
- Consistent with existing order pattern
- Allows orders to be saved and loaded

### Why Separate AUTHORIZE and DENY Orders?

**Decision**: Create two distinct order types instead of one with a flag

**Rationale**:
- Clearer player intent
- Simpler parsing logic
- Separate PhaseUpdaters for better organization
- Consistent with game's command structure

### Why Store RadialCoordinate Instead of Coordinate?

**Decision**: Use RadialCoordinate for sector-based authorizations

**Rationale**:
- Radius is integral to sector authorization
- RadialCoordinate already exists in codebase
- Empire.shareCoordinates already uses RadialCoordinate
- Simplifies authorization checking during scan sharing

## Summary

This design implements AUTHORIZE and DENY orders by:
1. Creating AuthorizeOrder and DenyOrder classes that parse and validate order parameters
2. Updating OrderType enum to register the new order types
3. Implementing PhaseUpdaters to execute the orders and update Empire authorization collections
4. Leveraging existing Empire data structures (shareCoordinates, shareShips, shareEmpires)
5. Following established patterns for order parsing, validation, and execution

The implementation requires no changes to existing orders or core infrastructure, maintaining backward compatibility while adding new functionality.
