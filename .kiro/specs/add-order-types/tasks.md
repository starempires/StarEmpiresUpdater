# Implementation Plan

- [x] 1. Add FROM_TOKEN constant to Order base class
  - Add `FROM_TOKEN` constant similar to existing `TO_TOKEN`
  - Add `RADIUS_GROUP` and `RADIUS_CAPTURE_REGEX` constants for radius parameter parsing
  - _Requirements: 1.6, 4.7_

- [x] 2. Create AuthorizationType enum
  - Create enum with values: SECTOR, SHIP, ALL_DATA
  - Add toString() method for JSON serialization
  - _Requirements: 1.1, 2.1, 3.1_

- [x] 3. Create DenialType enum
  - Create enum with values: SECTOR, SHIP, ALL_DATA
  - Add toString() method for JSON serialization
  - _Requirements: 4.1, 5.1, 6.1_

- [x] 4. Create or verify EmpireBasedOrder class exists
  - Check if EmpireBasedOrder class exists
  - If not, create it extending Order with empire-specific helper methods
  - Add helper method for parsing target empire lists
  - _Requirements: 7.1, 7.2, 7.3, 7.4_

- [x] 5. Implement AuthorizeOrder class
- [x] 5.1 Create AuthorizeOrder class structure
  - Extend EmpireBasedOrder (or Order if EmpireBasedOrder doesn't exist)
  - Add Lombok annotations (@Getter, @SuperBuilder, @NoArgsConstructor)
  - Define fields: targetEmpires, authorizationType, coordinates, ships, locationText
  - Add @JsonInclude annotations to optional fields
  - _Requirements: 1.1, 2.1, 3.1, 8.1_

- [x] 5.2 Implement AuthorizeOrder regex patterns
  - Define SECTOR pattern with coordinate/location and radius
  - Define SHIP pattern with ship list
  - Define ALL pattern
  - Combine patterns with TO keyword
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 3.1_

- [x] 5.3 Implement AuthorizeOrder.parse() method
  - Create order builder with basic fields
  - Match parameters against regex pattern
  - Determine authorization type (SECTOR, SHIP, or ALL_DATA)
  - Parse target empires list
  - Validate target empires (exist, known, not self)
  - For SECTOR: parse coordinate/location and radius, create RadialCoordinate
  - For SHIP: parse ship names and validate ownership
  - Set ready flag if all validations pass
  - Return order object
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 2.2, 2.3, 2.4, 3.1, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 5.4 Implement AuthorizeOrder.parseReady() method
  - Create builder and call parent parseReady
  - Extract targetEmpires from JSON and resolve through TurnData
  - Extract authorizationType from JSON
  - Extract coordinates from JSON (if SECTOR type)
  - Extract ships from JSON (if SHIP type)
  - Extract locationText from JSON
  - Return built order
  - _Requirements: 8.1, 8.3, 8.5_

- [ ] 6. Implement DenyOrder class
- [ ] 6.1 Create DenyOrder class structure
  - Extend EmpireBasedOrder (or Order if EmpireBasedOrder doesn't exist)
  - Add Lombok annotations (@Getter, @SuperBuilder, @NoArgsConstructor)
  - Define fields: targetEmpires, denialType, coordinates, ships, locationText
  - Add @JsonInclude annotations to optional fields
  - _Requirements: 4.1, 5.1, 6.1, 8.2_

- [ ] 6.2 Implement DenyOrder regex patterns
  - Define SECTOR pattern with coordinate/location and radius
  - Define SHIP pattern with ship list
  - Define ALL pattern
  - Combine patterns with FROM keyword
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 5.1, 5.2, 6.1_

- [ ] 6.3 Implement DenyOrder.parse() method
  - Create order builder with basic fields
  - Match parameters against regex pattern
  - Determine denial type (SECTOR, SHIP, or ALL_DATA)
  - Parse target empires list
  - Validate target empires (exist, known, not self)
  - For SECTOR: parse coordinate/location and radius, create RadialCoordinate
  - For SHIP: parse ship names and validate ownership
  - Set ready flag if all validations pass
  - Return order object
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 5.1, 5.2, 5.3, 5.4, 6.1, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [ ] 6.4 Implement DenyOrder.parseReady() method
  - Create builder and call parent parseReady
  - Extract targetEmpires from JSON and resolve through TurnData
  - Extract denialType from JSON
  - Extract coordinates from JSON (if SECTOR type)
  - Extract ships from JSON (if SHIP type)
  - Extract locationText from JSON
  - Return built order
  - _Requirements: 8.2, 8.4, 8.5_

- [ ] 7. Update OrderType enum
  - Add AUTHORIZE(AuthorizeOrder.class, false) entry
  - Add DENY(DenyOrder.class, false) entry
  - Ensure entries are in alphabetical order with existing entries
  - _Requirements: 9.1_

- [ ] 8. Update CustomOrderDeserializer
  - Add case for "authorize" that calls AuthorizeOrder.parseReady()
  - Add case for "deny" that calls DenyOrder.parseReady()
  - _Requirements: 8.3, 8.4, 9.4_

- [ ] 9. Verify or implement AuthorizeScanDataPhaseUpdater
  - Check if AuthorizeScanDataPhaseUpdater class exists
  - If exists but empty, implement update() method
  - If doesn't exist, create class extending PhaseUpdater
  - Implement logic to process AUTHORIZE orders
  - For SECTOR type: call empire.addCoordinateScanAccess() for each target empire and coordinate
  - For SHIP type: call empire.addShipScanAccess() for each target empire
  - For ALL_DATA type: call empire.addEmpireScanAccess() for each target empire
  - Add news messages for each authorization
  - _Requirements: 1.1, 2.1, 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 10. Verify or implement DenyScanDataPhaseUpdater
  - Check if DenyScanDataPhaseUpdater class exists
  - If exists but empty, implement update() method
  - If doesn't exist, create class extending PhaseUpdater
  - Implement logic to process DENY orders
  - For SECTOR type: call empire.removeCoordinateScanAccess() for each target empire
  - For SHIP type: call empire.removeShipScanAccess() for each target empire
  - For ALL_DATA type: call empire.removeEmpireScanAccess() for each target empire
  - Add news messages for each denial
  - _Requirements: 4.1, 4.2, 5.1, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 11. Verify Phase enum and TurnUpdater integration
  - Verify Phase enum includes AUTHORIZE_SCAN_DATA and DENY_SCAN_DATA
  - Verify TurnUpdater calls these phases in correct order (early in turn)
  - If phases don't exist, add them to Phase enum
  - If TurnUpdater doesn't call them, add to phase execution sequence
  - _Requirements: 9.3, 9.5_


- [ ] 12. Write unit tests for AuthorizeOrder
- [ ] 12.1 Test valid AUTHORIZE SECTOR with coordinate
  - Create test with valid coordinate, radius, and target empires
  - Assert order is ready and fields are correctly populated
  - _Requirements: 1.1, 1.2, 1.6_

- [ ] 12.2 Test valid AUTHORIZE SECTOR with world
  - Create test with valid world name, radius, and target empires
  - Assert order is ready and coordinate is resolved correctly
  - _Requirements: 1.3_

- [ ] 12.3 Test valid AUTHORIZE SECTOR with portal
  - Create test with valid portal name, radius, and target empires
  - Assert order is ready and coordinate is resolved correctly
  - _Requirements: 1.4_

- [ ] 12.4 Test valid AUTHORIZE SECTOR with storm
  - Create test with valid storm name, radius, and target empires
  - Assert order is ready and coordinate is resolved correctly
  - _Requirements: 1.5_

- [ ] 12.5 Test valid AUTHORIZE SHIP
  - Create test with valid ship names and target empires
  - Assert order is ready and ships are correctly identified
  - _Requirements: 2.1, 2.2_

- [ ] 12.6 Test valid AUTHORIZE ALL
  - Create test with valid target empires
  - Assert order is ready and authorizationType is ALL_DATA
  - _Requirements: 3.1_

- [ ] 12.7 Test AUTHORIZE with multiple target empires
  - Create test authorizing multiple empires at once
  - Assert all empires are added to targetEmpires list
  - _Requirements: 7.4_

- [ ] 12.8 Test AUTHORIZE with unknown empire
  - Create test with non-existent empire name
  - Assert order is not ready and error message is present
  - _Requirements: 7.1_

- [ ] 12.9 Test AUTHORIZE with self empire
  - Create test attempting to authorize own empire
  - Assert order is not ready and error message is present
  - _Requirements: 7.2_

- [ ] 12.10 Test AUTHORIZE with not-known empire
  - Create test with empire not in knownEmpires collection
  - Assert order is not ready and error message is present
  - _Requirements: 7.3_

- [ ] 12.11 Test AUTHORIZE with unknown ship
  - Create test with non-existent ship name
  - Assert order is not ready and error message is present
  - _Requirements: 2.3_

- [ ] 12.12 Test AUTHORIZE with ship not owned
  - Create test with ship owned by different empire
  - Assert order is not ready and error message is present
  - _Requirements: 2.4_

- [ ] 12.13 Test AUTHORIZE with negative radius
  - Create test with negative radius value
  - Assert order is not ready and error message is present
  - _Requirements: 1.6_

- [ ] 12.14 Test AUTHORIZE with unknown location
  - Create test with non-existent world/portal/storm name
  - Assert order is not ready and error message is present
  - _Requirements: 1.3, 1.4, 1.5_

- [ ] 12.15 Test AuthorizeOrder serialization
  - Create valid order, serialize to JSON, deserialize with parseReady
  - Assert deserialized order matches original
  - _Requirements: 8.1, 8.3, 8.5_

- [ ] 13. Write unit tests for DenyOrder
- [ ] 13.1 Test valid DENY SECTOR with coordinate
  - Create test with valid coordinate, radius, and target empires
  - Assert order is ready and fields are correctly populated
  - _Requirements: 4.1, 4.3, 4.7_

- [ ] 13.2 Test valid DENY SECTOR with world
  - Create test with valid world name, radius, and target empires
  - Assert order is ready and coordinate is resolved correctly
  - _Requirements: 4.4_

- [ ] 13.3 Test valid DENY SECTOR with portal
  - Create test with valid portal name, radius, and target empires
  - Assert order is ready and coordinate is resolved correctly
  - _Requirements: 4.5_

- [ ] 13.4 Test valid DENY SECTOR with storm
  - Create test with valid storm name, radius, and target empires
  - Assert order is ready and coordinate is resolved correctly
  - _Requirements: 4.6_

- [ ] 13.5 Test valid DENY SHIP
  - Create test with valid ship names and target empires
  - Assert order is ready and ships are correctly identified
  - _Requirements: 5.1, 5.2_

- [ ] 13.6 Test valid DENY ALL
  - Create test with valid target empires
  - Assert order is ready and denialType is ALL_DATA
  - _Requirements: 6.1_

- [ ] 13.7 Test DENY with multiple target empires
  - Create test denying multiple empires at once
  - Assert all empires are added to targetEmpires list
  - _Requirements: 7.4_

- [ ] 13.8 Test DENY with unknown empire
  - Create test with non-existent empire name
  - Assert order is not ready and error message is present
  - _Requirements: 7.1_

- [ ] 13.9 Test DENY with self empire
  - Create test attempting to deny own empire
  - Assert order is not ready and error message is present
  - _Requirements: 7.2_

- [ ] 13.10 Test DENY with not-known empire
  - Create test with empire not in knownEmpires collection
  - Assert order is not ready and error message is present
  - _Requirements: 7.3_

- [ ] 13.11 Test DENY with unknown ship
  - Create test with non-existent ship name
  - Assert order is not ready and error message is present
  - _Requirements: 5.3_

- [ ] 13.12 Test DENY with ship not owned
  - Create test with ship owned by different empire
  - Assert order is not ready and error message is present
  - _Requirements: 5.4_

- [ ] 13.13 Test DENY with negative radius
  - Create test with negative radius value
  - Assert order is not ready and error message is present
  - _Requirements: 4.7_

- [ ] 13.14 Test DENY with unknown location
  - Create test with non-existent world/portal/storm name
  - Assert order is not ready and error message is present
  - _Requirements: 4.4, 4.5, 4.6_

- [ ] 13.15 Test DenyOrder serialization
  - Create valid order, serialize to JSON, deserialize with parseReady
  - Assert deserialized order matches original
  - _Requirements: 8.2, 8.4, 8.5_

- [ ] 14. Write integration tests for PhaseUpdaters
- [ ] 14.1 Test AuthorizeScanDataPhaseUpdater execution
  - Create TurnData with AUTHORIZE orders
  - Run AuthorizeScanDataPhaseUpdater
  - Assert Empire authorization collections are updated correctly
  - Assert news messages are generated
  - _Requirements: 1.1, 2.1, 3.1, 3.2, 3.3, 3.4, 3.5_

- [ ] 14.2 Test DenyScanDataPhaseUpdater execution
  - Create TurnData with DENY orders
  - Run DenyScanDataPhaseUpdater
  - Assert Empire authorization collections are updated correctly
  - Assert news messages are generated
  - _Requirements: 4.1, 5.1, 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 14.3 Test AUTHORIZE ALL overrides specific authorizations
  - Create empire with sector and ship authorizations
  - Execute AUTHORIZE ALL order
  - Assert shareEmpires contains target empire
  - Assert specific authorizations are removed
  - _Requirements: 3.2_

- [ ] 14.4 Test DENY ALL removes all authorizations
  - Create empire with all types of authorizations
  - Execute DENY ALL order
  - Assert all authorization collections are empty for target empire
  - _Requirements: 6.2, 6.3, 6.4, 6.5_

- [ ] 14.5 Test AUTHORIZE then DENY sequence
  - Execute AUTHORIZE order
  - Verify authorization is granted
  - Execute DENY order for same access
  - Verify authorization is revoked
  - _Requirements: 1.1, 4.1, 5.5_
