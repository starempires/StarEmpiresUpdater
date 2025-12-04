# Java Coding Guidelines

This document outlines the coding standards and best practices for the StarEmpiresUpdater Java project.

## Library Usage

### Prefer Standard Libraries
- **Use Google and Apache Commons libraries** for common operations instead of writing custom implementations
- **Validation**: Use `Validate.*` methods from Apache Commons Lang instead of writing manual null checks
  ```java
  // Preferred
  Validate.notNull(orderClass, "Order class cannot be null");
  Validate.notNull(turnData, "TurnData cannot be null");
  
  // Avoid
  if (orderClass == null) {
      throw new IllegalArgumentException("Order class cannot be null");
  }
  ```

### Recommended Libraries
- **Apache Commons Lang**: For validation, string utilities, and common operations
- **Google Guava**: For collections, caching, and utility functions
- **Apache Commons Collections**: For advanced collection operations
- **Lombok**: For reducing boilerplate code with annotations
- **Mockito**: For mocking in unit tests

## Control Flow Guidelines

### Positive Logic in Conditionals
- **Favor "positive" logic** in IF-THEN-ELSE statements where the IF condition expresses "if true" logic rather than "if not true"
- This improves readability and reduces cognitive load

```java
// Preferred - positive logic
if (order.isReady()) {
    processOrder(order);
} else {
    handleNotReadyOrder(order);
}

// Avoid - negative logic
if (!order.isReady()) {
    handleNotReadyOrder(order);
} else {
    processOrder(order);
}
```

```java
// Preferred - positive logic
if (result != null) {
    return processResult(result);
} else {
    throw new OrderCreationException("Result cannot be null");
}

// Avoid - negative logic  
if (result == null) {
    throw new OrderCreationException("Result cannot be null");
} else {
    return processResult(result);
}
```

### Exception Handling
- Use positive logic when checking for valid conditions before proceeding
- Handle the "happy path" first, then handle exceptions

```java
// Preferred
if (parseMethod != null && Modifier.isStatic(parseMethod.getModifiers())) {
    return invokeParseMethod(parseMethod, turnData, empire, parameters);
} else {
    throw new OrderCreationException("Invalid parse method");
}
```

## Method Design

### Parameter Validation
- Always validate method parameters at the beginning of public methods
- Use Apache Commons `Validate` class for consistent validation
- Provide clear, descriptive error messages

```java
public Order createOrder(Class<? extends Order> orderClass, TurnData turnData, 
                        Empire empire, String parameters) throws OrderCreationException {
    Validate.notNull(orderClass, "Order class cannot be null");
    Validate.notNull(turnData, "TurnData cannot be null");
    Validate.notNull(empire, "Empire cannot be null");
    // parameters can be null - that's valid
    
    // Method implementation...
}
```

### Return Value Handling
- Check for positive conditions first
- Use early returns for error conditions when appropriate

```java
// Preferred
public boolean isValidOrder(Order order) {
    if (order != null && order.isReady()) {
        return validateOrderDetails(order);
    }
    return false;
}
```

## Error Messages

### Descriptive Error Messages
- Provide context about what went wrong
- Include relevant object names or identifiers
- Use consistent formatting

```java
// Preferred
throw new OrderCreationException(
    "Order creation failed - parse method returned null for class: " + orderClass.getSimpleName());

// Avoid
throw new OrderCreationException("Parse method returned null");
```

## Lombok Usage

### Immutable Objects
- **Use Lombok annotations** to implement immutable objects with getters but no setters
- **Use `@Value`** for completely immutable classes
- **Use `@Getter`** for classes that need only getters

```java
// Preferred - Immutable class with Lombok
@Value
@Builder
public class OrderRegistration {
    String orderName;
    Class<? extends Order> orderClass;
    boolean gmOnly;
    Method parseMethod;
}

// Avoid - Manual getter implementation
public class OrderRegistration {
    private final String orderName;
    private final Class<? extends Order> orderClass;
    private final boolean gmOnly;
    private final Method parseMethod;
    
    public String getOrderName() { return orderName; }
    public Class<? extends Order> getOrderClass() { return orderClass; }
    // ... more boilerplate
}
```

### Builder Patterns
- **Use `@Builder`** annotation for complex object construction
- **Use `@SuperBuilder`** for inheritance hierarchies
- Combine with `@Value` or `@Data` for complete immutable objects

```java
// Preferred - Lombok builder
@Value
@Builder
public class OrderConfiguration {
    String orderName;
    Class<? extends Order> orderClass;
    boolean gmOnly;
    Set<String> requiredPermissions;
}

// Usage
OrderConfiguration config = OrderConfiguration.builder()
    .orderName("BUILD")
    .orderClass(BuildOrder.class)
    .gmOnly(false)
    .requiredPermissions(Set.of("CONSTRUCTION"))
    .build();
```

## Testing Guidelines

### JUnit Unit Tests
- **Always use JUnit** to write unit tests
- **Use the most succinct JUnit assert methods** instead of simple `assertTrue`/`assertFalse`
  - Use `assertNull(value)` instead of `assertTrue(value == null)`
  - Use `assertNotNull(value)` instead of `assertFalse(value == null)`
  - Use `assertThrows(ExceptionClass.class, () -> method())` instead of try-catch blocks
  - Use `assertInstanceOf(ExpectedClass.class, object)` instead of `assertTrue(object instanceof ExpectedClass)`
  - Use `assertSame(expected, actual)` for reference equality
  - Use `assertNotSame(expected, actual)` for reference inequality

### Mockito for Unit Tests
- **Use Mockito** for mocking dependencies in unit tests
- **Use `@Mock`** annotation for mock objects
- **Use `@MockitoAnnotations.openMocks(this)`** in test setup
- **Prefer `when().thenReturn()`** for stubbing method calls

```java
// Preferred - JUnit with succinct assertions
@Test
void testOrderCreation() throws OrderCreationException {
    Order result = factory.createOrder("BUILD", "SHIP Destroyer AT 10,20");
    
    // Use assertInstanceOf instead of assertTrue(result instanceof BuildOrder)
    BuildOrder buildOrder = assertInstanceOf(BuildOrder.class, result);
    assertEquals("SHIP", buildOrder.getType());
    assertNotNull(buildOrder.getName());
}

@Test
void testInvalidInput() {
    // Use assertThrows instead of try-catch blocks
    OrderCreationException exception = assertThrows(OrderCreationException.class,
        () -> factory.createOrder(null, "parameters"));
    
    assertNotNull(exception.getMessage());
    assertTrue(exception.getMessage().contains("cannot be null"));
}

// Preferred - Mockito usage
class OrderParserTest extends BaseTest {
    @Mock
    private OrderRegistry mockRegistry;
    
    @Mock
    private OrderFactory mockFactory;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock behavior
        when(mockRegistry.isRegistered("BUILD")).thenReturn(true);
        when(mockRegistry.getOrderClass("BUILD")).thenReturn(BuildOrder.class);
    }
    
    @Test
    void testOrderParsing() {
        // Test implementation using mocks
        verify(mockRegistry).isRegistered("BUILD");
        verify(mockFactory).createOrder(any(), any(), any(), any());
    }
}
```

### Mock Verification
- **Use `verify()`** to ensure mock interactions occurred
- **Use `never()`** to verify methods were not called
- **Use argument matchers** like `any()`, `eq()`, `anyString()` for flexible verification

```java
// Verify specific interactions
verify(mockRegistry).isRegistered("BUILD");
verify(mockRegistry).getOrderClass("BUILD");

// Verify method was not called
verify(mockFactory, never()).createOrder(any(), any(), any(), any());

// Verify with argument matchers
verify(mockFactory).createOrder(eq(BuildOrder.class), any(TurnData.class), 
                               any(Empire.class), eq("test parameters"));
```

## Code Organization

### Method Structure
1. Parameter validation (using positive logic)
2. Main logic (using positive conditions first)
3. Error handling
4. Return statements

### Comments and Documentation
- Use JavaDoc for public methods
- Explain the "why" not just the "what"
- Document any non-obvious business logic

## Examples

### Before (Avoid)
```java
public Order createOrder(Class<? extends Order> orderClass, TurnData turnData, 
                        Empire empire, String parameters) throws OrderCreationException {
    if (orderClass == null) {
        throw new OrderCreationException("Order class cannot be null");
    }
    if (turnData == null) {
        throw new OrderCreationException("TurnData cannot be null");
    }
    
    Method parseMethod = getParseMethod(orderClass);
    Object result = parseMethod.invoke(null, turnData, empire, parameters);
    
    if (result == null) {
        throw new OrderCreationException("Parse method returned null");
    }
    if (!(result instanceof Order)) {
        throw new OrderCreationException("Wrong return type");
    }
    
    return (Order) result;
}
```

### After (Preferred)
```java
public Order createOrder(Class<? extends Order> orderClass, TurnData turnData, 
                        Empire empire, String parameters) throws OrderCreationException {
    Validate.notNull(orderClass, "Order class cannot be null");
    Validate.notNull(turnData, "TurnData cannot be null");
    Validate.notNull(empire, "Empire cannot be null");
    
    String safeParameters = parameters != null ? parameters : "";
    
    try {
        Method parseMethod = getParseMethod(orderClass);
        Object result = parseMethod.invoke(null, turnData, empire, safeParameters);
        
        if (result != null && result instanceof Order) {
            Order order = (Order) result;
            log.debug("Successfully created order: {} for empire: {}", 
                     orderClass.getSimpleName(), empire.getName());
            return order;
        } else if (result == null) {
            throw new OrderCreationException(
                "Order creation failed - parse method returned null for class: " + orderClass.getSimpleName());
        } else {
            throw new OrderCreationException(
                "Order creation failed - parse method returned non-Order instance: " +
                result.getClass().getName() + " for class: " + orderClass.getSimpleName());
        }
    } catch (Exception e) {
        // Handle exceptions...
    }
}
```

## Dependencies

Ensure these dependencies are available in your project:

```xml
<!-- Apache Commons -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>

<!-- Google Guava -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>

<!-- Mockito for testing -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```