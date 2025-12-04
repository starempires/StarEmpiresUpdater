# Requirements Document

## Introduction

This feature enables empires in the Star Empires game to share scan data with other empires through AUTHORIZE and DENY orders. Empires SHALL be able to grant and revoke scan data access at three levels: by sector (with radius), by ship, and all data. The system SHALL maintain authorization state across turns and apply authorizations during scan data collection phases.

## Glossary

- **Order System**: The Star Empires command processing framework that parses, validates, and executes player commands
- **Scan Data**: Information about sectors that an empire has scanned, including scan status (unknown, stale, scanned, visible) and last turn scanned
- **Scan Status**: The visibility level of a sector (UNKNOWN, STALE, SCANNED, VISIBLE)
- **Scan Authorization**: Permission granted by one empire to another empire to access specific scan data
- **Authorizing Empire**: The empire that owns scan data and grants access to other empires
- **Authorized Empire**: The empire that receives permission to access another empire's scan data
- **Sector**: A location in the game map identified by coordinates (oblique, y)
- **RadialCoordinate**: A coordinate with an associated radius for area-based scan sharing
- **Ship**: A mobile unit owned by an Empire that can scan sectors
- **Empire**: A player-controlled faction in the game
- **TurnData**: The game state object containing all empires, ships, and scan data for a specific turn
- **Order Parameters**: The text string following the order type keyword that specifies the order's details

## Requirements

### Requirement 1

**User Story:** As a player, I want to authorize other empires to access my scan data by sector, so that I can share intelligence about specific locations

#### Acceptance Criteria

1. WHEN a player issues an AUTHORIZE order with sector coordinates and radius, THE Order System SHALL grant the specified empire access to scan data for all sectors within that radius from the center coordinate
2. WHEN a player specifies a sector by coordinate, THE Order System SHALL accept coordinates in the empire's local frame of reference and convert to galactic coordinates
3. WHEN a player specifies a sector by world name, THE Order System SHALL resolve the world's coordinate and grant access with the specified radius
4. WHEN a player specifies a sector by portal name, THE Order System SHALL resolve the portal's coordinate and grant access with the specified radius
5. WHEN a player specifies a sector by storm name, THE Order System SHALL resolve the storm's coordinate and grant access with the specified radius
6. WHEN a player specifies a radius, THE Order System SHALL require a non-negative integer value
7. WHEN a player omits the radius parameter, THE Order System SHALL mark the order as not ready and add an error message

### Requirement 2

**User Story:** As a player, I want to authorize other empires to access scan data from specific ships, so that I can share intelligence gathered by designated vessels

#### Acceptance Criteria

1. WHEN a player issues an AUTHORIZE order with ship names, THE Order System SHALL grant the specified empire access to scan data collected by those ships
2. WHEN a player specifies multiple ships, THE Order System SHALL accept space-separated ship names
3. WHEN a player specifies an unknown ship name, THE Order System SHALL mark the order as not ready and add an error message
4. WHEN a player specifies a ship not owned by the authorizing empire, THE Order System SHALL mark the order as not ready and add an error message
5. WHILE ships collect scan data during turn processing, THE Order System SHALL share that data with empires authorized for those ships

### Requirement 3

**User Story:** As a player, I want to authorize other empires to access all my scan data, so that I can establish complete intelligence sharing with allies

#### Acceptance Criteria

1. WHEN a player issues an AUTHORIZE order for all data, THE Order System SHALL grant the specified empire access to all current and future scan data
2. WHEN an empire has all-data authorization, THE Order System SHALL remove any existing sector-specific or ship-specific authorizations for that empire
3. WHEN an empire has all-data authorization, THE Order System SHALL share all scan data collected during turn processing
4. WHEN a player authorizes all data to an empire, THE Order System SHALL maintain that authorization across subsequent turns until explicitly denied
5. THE Order System SHALL store all-data authorizations in the shareEmpires collection

### Requirement 4

**User Story:** As a player, I want to deny other empires access to my scan data by sector, so that I can revoke intelligence sharing for specific locations

#### Acceptance Criteria

1. WHEN a player issues a DENY order with sector coordinates and radius, THE Order System SHALL revoke the specified empire's access to scan data for all sectors within that radius from the center coordinate
2. WHEN a player specifies a radius of zero, THE Order System SHALL revoke access only to the exact coordinate specified
3. WHEN a player specifies a sector by coordinate, THE Order System SHALL accept coordinates in the empire's local frame of reference and convert to galactic coordinates
4. WHEN a player specifies a sector by world name, THE Order System SHALL resolve the world's coordinate and revoke access with the specified radius
5. WHEN a player specifies a sector by portal name, THE Order System SHALL resolve the portal's coordinate and revoke access with the specified radius
6. WHEN a player specifies a sector by storm name, THE Order System SHALL resolve the storm's coordinate and revoke access with the specified radius
7. WHEN a player specifies a radius, THE Order System SHALL require a non-negative integer value
8. WHEN a player omits the radius parameter, THE Order System SHALL mark the order as not ready and add an error message

### Requirement 5

**User Story:** As a player, I want to deny other empires access to scan data from specific ships, so that I can revoke intelligence sharing from designated vessels

#### Acceptance Criteria

1. WHEN a player issues a DENY order with ship names, THE Order System SHALL revoke the specified empire's access to scan data collected by those ships
2. WHEN a player specifies multiple ships, THE Order System SHALL accept space-separated ship names
3. WHEN a player specifies an unknown ship name, THE Order System SHALL mark the order as not ready and add an error message
4. WHEN a player specifies a ship not owned by the denying empire, THE Order System SHALL mark the order as not ready and add an error message
5. WHEN scan data authorization is revoked for ships, THE Order System SHALL stop sharing data from those ships in future turns

### Requirement 6

**User Story:** As a player, I want to deny other empires access to all my scan data, so that I can completely revoke intelligence sharing

#### Acceptance Criteria

1. WHEN a player issues a DENY order for all data, THE Order System SHALL revoke all scan data access for the specified empire
2. WHEN all-data access is denied, THE Order System SHALL remove the empire from the shareEmpires collection
3. WHEN all-data access is denied, THE Order System SHALL remove all sector-specific authorizations for that empire
4. WHEN all-data access is denied, THE Order System SHALL remove all ship-specific authorizations for that empire
5. WHEN all-data access is denied, THE Order System SHALL stop sharing any scan data with that empire in future turns

### Requirement 7

**User Story:** As a player, I want AUTHORIZE and DENY orders to validate empire names, so that I receive clear feedback about invalid orders

#### Acceptance Criteria

1. WHEN a player specifies an unknown empire name, THE Order System SHALL mark the order as not ready and add an error message
2. WHEN a player attempts to authorize or deny their own empire, THE Order System SHALL mark the order as not ready and add an error message
3. WHEN a player specifies an empire that is not in their knownEmpires collection, THE Order System SHALL mark the order as not ready and add an error message
4. WHEN a player specifies multiple target empires, THE Order System SHALL validate each empire name independently
5. WHEN an order is successfully validated, THE Order System SHALL set the ready flag to true
6. WHEN an order has validation errors, THE Order System SHALL accumulate all error messages in the results list

### Requirement 8

**User Story:** As a player, I want AUTHORIZE and DENY orders to be serialized and persisted, so that my scan sharing preferences are maintained across game sessions

#### Acceptance Criteria

1. THE Order System SHALL serialize AUTHORIZE orders to JSON format with all authorization parameters
2. THE Order System SHALL serialize DENY orders to JSON format with all denial parameters
3. WHEN deserializing AUTHORIZE orders, THE Order System SHALL resolve empire, ship, and coordinate references through TurnData
4. WHEN deserializing DENY orders, THE Order System SHALL resolve empire, ship, and coordinate references through TurnData
5. THE Order System SHALL use @JsonInclude annotations to exclude empty collections from serialization

### Requirement 9

**User Story:** As a developer, I want new order types to integrate seamlessly with existing code, so that adding AUTHORIZE and DENY orders does not break existing functionality

#### Acceptance Criteria

1. WHEN AUTHORIZE and DENY order types are added to the OrderType enum, THE Order System SHALL continue to parse all existing order types without modification
2. WHEN AUTHORIZE and DENY order classes are created, THE Order System SHALL not require changes to the OrderParser class
3. WHEN AUTHORIZE and DENY orders are executed, THE Order System SHALL not modify the execution logic of existing PhaseUpdaters
4. WHEN AUTHORIZE and DENY orders are serialized, THE Order System SHALL use the existing CustomOrderDeserializer pattern
5. THE Order System SHALL maintain all existing order processing phases without modification
