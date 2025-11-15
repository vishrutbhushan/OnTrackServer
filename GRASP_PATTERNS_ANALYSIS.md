# GRASP Patterns Analysis - OnTrack Server

This document demonstrates how all **9 GRASP (General Responsibility Assignment Software Patterns)** are realized in the OnTrack Server codebase.

---

## 1. **Creator Pattern**
**Responsibility:** Who creates objects?

### Example: `UserService.saveUser()`
**Location:** `src/main/java/com/project/onTrackServer/service/UserService.java`

```java
public UserDTO saveUser(UserDTO userDTO) {
    String email = userDTO.getUserId();
    
    Optional<User> existingUser = userRepository.findByUserId(email);
    
    User user;
    if (existingUser.isPresent()) {
        user = existingUser.get();
        // ... update logic
    } else {
        user = new User();  // ← CREATOR: Service creates User objects
        user.setUserId(email);
        user.setEmail(email);
        user.setDisplayName(userDTO.getDisplayName());
        user.setAccessToken(userDTO.getAccessToken());
        user.setFcmToken(userDTO.getFcmToken());
    }
    
    user = userRepository.save(user);
    saveUserConfig(user, userDTO);  // ← Creates related UserConfig
    
    return new UserDTO(user);  // ← Creates DTO from model
}
```

**Rationale:** `UserService` creates `User` entities because it aggregates `User` and has all the necessary information. It also creates related `UserConfig` objects and `UserDTO` transfer objects.

---

## 2. **Information Expert Pattern**
**Responsibility:** Who knows how to perform a given operation?

### Example: `OrderService.getUserOrders()`
**Location:** `src/main/java/com/project/onTrackServer/service/OrderService.java`

```java
public List<Order> getUserOrders(String userId) {
    log.info("Fetching all orders for user: {}", userId);
    
    return userRepository.findByUserId(userId)
            .map(user -> orderRepository.findByUserAndIsDeletedFalse(user))
            .orElse(List.of());
}
```

**Rationale:** `OrderService` is the information expert:
- It knows how to retrieve orders (via `orderRepository`)
- It knows the business rule: "filter deleted orders" (`IsDeletedFalse`)
- It knows how to associate orders with users
- The controller delegates this responsibility rather than querying repositories directly

Another example - `AuditBase` as expert for audit operations:

```java
@MappedSuperclass
public abstract class AuditBase {
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();  // ← Expert in audit logic
    }
}
```

---

## 3. **Controller Pattern**
**Responsibility:** Who handles incoming requests and coordinates responses?

### Example: `OrderController`
**Location:** `src/main/java/com/project/onTrackServer/controller/OrderController.java`

```java
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Slf4j
public class OrderController {  // ← CONTROLLER

    @Autowired
    private OrderService orderService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String userId) {
        log.info("Get all orders for user: {}", userId);
        List<Order> orders = orderService.getUserOrders(userId);  // ← Delegates to service
        return ResponseEntity.ok(orders);
    }
}
```

**Rationale:** 
- `OrderController` acts as the facade/controller for HTTP requests
- It receives the request and delegates to the appropriate service
- It doesn't contain business logic, only orchestration

Similar example - `UserController`:

```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {  // ← CONTROLLER
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO userDTO) {
        UserDTO savedUser = userService.saveUser(userDTO);  // ← Delegates
        return ResponseEntity.ok(savedUser);
    }
}
```

---

## 4. **Low Coupling Pattern**
**Responsibility:** Reduce dependencies between objects

### Example: Layered Architecture
**Location:** Architecture across `controller`, `service`, `repository`, `model` layers

**Problem:** If controllers directly access repositories, they become tightly coupled.

**Solution:** Service layer as intermediary

```
OrderController (REST endpoint)
        ↓
    OrderService (business logic - DECOUPLING layer)
        ↓
    OrderRepository (data access)
```

Code example showing low coupling:

```java
// ✅ LOW COUPLING: OrderController depends only on OrderService
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;  // Only this dependency
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
}

// ✅ OrderService hides repository implementation details
@Service
@Slf4j
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;  // Internal detail
    @Autowired
    private UserRepository userRepository;    // Internal detail
    
    public List<Order> getUserOrders(String userId) {
        // OrderController doesn't need to know about these repositories
        return userRepository.findByUserId(userId)
                .map(user -> orderRepository.findByUserAndIsDeletedFalse(user))
                .orElse(List.of());
    }
}
```

**Benefits:**
- Controllers don't know about database queries
- Repository changes don't affect controllers
- Services can be tested independently with mock repositories

---

## 5. **High Cohesion Pattern**
**Responsibility:** Keep related functionality together

### Example: `AbstractCrudService<T>` - Cohesive CRUD operations
**Location:** `src/main/java/com/project/onTrackServer/service/AbstractCrudService.java`

```java
@Slf4j
public abstract class AbstractCrudService<T> implements CrudService<T> {
    
    protected abstract JpaRepository<T, Long> getRepository();
    
    // ✅ All CRUD operations together - HIGH COHESION
    public T create(String userId, T resourceData) { /* ... */ }
    
    public Optional<T> getResource(Long resourceId, String userId) { /* ... */ }
    
    public List<T> getUserResources(String userId) { /* ... */ }
    
    public void delete(Long resourceId, String userId) { /* ... */ }
    
    // ✅ Related helper methods for audit fields
    protected void markAsDeleted(T resource) { /* ... */ }
    protected void setUpdateUser(T resource, String userId) { /* ... */ }
    protected void setUser(T resource, User user) { /* ... */ }
    protected void setCreateUser(T resource, String userId) { /* ... */ }
    protected void applyResourceProperties(T resourceData, String userId) { /* ... */ }
}
```

### Example: `NotificationTemplates` - Cohesive notification logic
**Location:** `src/main/java/com/project/onTrackServer/service/NotificationTemplates.java`

```java
@Service
public class NotificationTemplates {
    
    public enum OrderStatus {  // ✅ Related status definitions
        ORDERED("ordered", "Order Placed"),
        SHIPPED("shipped", "Order Shipped"),
        OUT_OF_DELIVERY("out_of_delivery", "Out for Delivery"),
        DELIVERED("delivered", "Order Delivered"),
        CANCELLED("cancelled", "Order Cancelled");
        // ...
    }
    
    public static class NotificationTemplate {  // ✅ Related template structure
        private final String title;
        private final String body;
        // ...
    }
    
    // ✅ All notification template methods together
    public NotificationTemplate getTemplate(OrderStatus status, String orderId, String productName) { /* ... */ }
}
```

**Benefits:**
- Single Responsibility: Each class handles one cohesive area
- Easy to maintain and modify related functionality
- Clear dependencies between related functions

---

## 6. **Indirection Pattern**
**Responsibility:** Introduce intermediate objects to avoid direct coupling

### Example: Service Layer as Indirection
**Location:** Service layer between controllers and repositories

**Problem:** Controllers directly querying repositories = tight coupling

**Solution:** Service layer provides indirection

```java
// ✅ INDIRECTION: Interface-based service contract
public interface CrudService<T> {
    T create(String userId, T resourceData);
    Optional<T> getResource(Long resourceId, String userId);
    List<T> getUserResources(String userId);
    void delete(Long resourceId, String userId);
}

// ✅ CategoryService implements interface (indirection layer)
@Service
@Slf4j
public class CategoryService extends AbstractCrudService<Category> {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    protected JpaRepository<Category, Long> getRepository() {
        return categoryRepository;  // Implementation detail hidden
    }
}

// ✅ Controller uses interface, not implementation details
@RestController
public class CategoryController {
    @Autowired
    private CategoryService categoryService;  // Uses interface
    
    @PostMapping("/{userId}")
    public ResponseEntity<Category> create(
            @PathVariable String userId,
            @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.create(userId, category));
    }
}
```

### Example: DTO Pattern as Indirection
**Location:** `src/main/java/com/project/onTrackServer/dto/`

```java
// ✅ UserDTO acts as indirection between internal User model and API
@Data
@NoArgsConstructor
public class UserDTO {
    private String userId;
    private String displayName;
    private String accessToken;
    private String fcmToken;
    // Note: Internal 'id' field is NOT exposed
}

// ✅ Controllers work with DTO, not internal model
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String userId,
            @RequestBody UserDTO userDTO) {  // API contracts use DTO
        return ResponseEntity.ok(userService.saveUser(userDTO));
    }
}
```

---

## 7. **Polymorphism Pattern**
**Responsibility:** Handle variants without conditional logic

### Example: Service Implementations via Generic Abstract Class
**Location:** Service implementations extending `AbstractCrudService<T>`

```java
// ✅ Generic base with polymorphic subclasses
public abstract class AbstractCrudService<T> implements CrudService<T> {
    protected abstract JpaRepository<T, Long> getRepository();  // Polymorphic
    
    // Same implementation works for all entity types
    public List<T> getUserResources(String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> getRepository()  // ← Polymorphic call
                        .findByUserAndIsDeletedFalse(user))
                .orElse(List.of());
    }
}

// ✅ CategoryService (POLYMORPHIC VARIANT)
@Service
@Slf4j
public class CategoryService extends AbstractCrudService<Category> {
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    protected JpaRepository<Category, Long> getRepository() {
        return categoryRepository;  // Implementation specific to Category
    }
}

// ✅ PlatformService (POLYMORPHIC VARIANT)
@Service
@Slf4j
public class PlatformService extends AbstractCrudService<Platform> {
    @Autowired
    private PlatformRepository platformRepository;
    
    @Override
    protected JpaRepository<Platform, Long> getRepository() {
        return platformRepository;  // Implementation specific to Platform
    }
}

// ✅ No conditional logic needed:
// if (type == "Category") { ... }
// else if (type == "Platform") { ... }
```

### Example: Notification Status Handling
**Location:** `NotificationTemplates.java`

```java
// ✅ Enum-based polymorphism for order statuses
public enum OrderStatus {
    ORDERED("ordered", "Order Placed"),
    SHIPPED("shipped", "Order Shipped"),
    DELIVERED("delivered", "Order Delivered"),
    CANCELLED("cancelled", "Order Cancelled");
    
    public static OrderStatus fromCode(String code) {
        // Polymorphic behavior: different status = different template
        for (OrderStatus status : OrderStatus.values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        return null;
    }
}

// ✅ Switch based on status (no if-else chains)
public NotificationTemplate getTemplate(OrderStatus status, String orderId) {
    return switch(status) {
        case ORDERED -> new NotificationTemplate(
            "Order Confirmed",
            "Order " + orderId + " has been placed successfully");
        case SHIPPED -> new NotificationTemplate(
            "Order Shipped",
            "Your order " + orderId + " is on the way!");
        case DELIVERED -> new NotificationTemplate(
            "Order Delivered",
            "Your order " + orderId + " has been delivered");
        // ...
    };
}
```

---

## 8. **Pure Fabrication Pattern**
**Responsibility:** Create artificial classes for convenience that don't represent domain concepts

### Example: `NotificationTemplates` Service
**Location:** `src/main/java/com/project/onTrackServer/service/NotificationTemplates.java`

```java
/**
 * Pure Fabrication: NotificationTemplates is not a domain concept.
 * It's created purely for convenience and code organization.
 * Domain: User, Order, Platform (real business entities)
 * Pure Fabrication: NotificationTemplates (artificial helper)
 */
@Service
public class NotificationTemplates {
    
    // ✅ Exists for convenience, not in domain model
    public static class NotificationTemplate {
        private final String title;
        private final String body;
        
        public NotificationTemplate(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }
    
    // ✅ Provides pure utility/abstraction
    public NotificationTemplate getTemplate(OrderStatus status, 
                                           String orderId, 
                                           String productName) {
        return switch(status) {
            case ORDERED -> new NotificationTemplate(
                "Order Confirmed",
                "Your order for " + productName + " has been confirmed.");
            case SHIPPED -> new NotificationTemplate(
                "Order Shipped",
                "Your order " + orderId + " has shipped!");
            // ...
        };
    }
}
```

### Example: `ApiResponse` DTO - Pure Fabrication
**Location:** `src/main/java/com/project/onTrackServer/dto/ApiResponse.java`

```java
/**
 * Pure Fabrication: ApiResponse is not a domain concept.
 * It's purely for API convenience and standardized response wrapping.
 */
@Data
@NoArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
```

**Rationale:** 
- These classes don't represent real business entities
- Created for technical convenience and abstraction
- Improves API cleanliness and consistency
- Separates technical concerns from domain logic

---

## 9. **Protected Variations Pattern**
**Responsibility:** Isolate unstable elements behind stable interfaces

### Example: Firebase Integration Isolation
**Location:** `src/main/java/com/project/onTrackServer/service/NotificationService.java`

```java
/**
 * PROTECTED VARIATION: Encapsulates Firebase implementation details.
 * If Firebase is replaced with Twilio or AWS SNS, only this class changes.
 */
@Slf4j
@Service
public class NotificationService {

    private FirebaseApp firebaseApp;

    // ✅ STABLE INTERFACE
    public void sendNotification(String userId, String title, String messageBody) {
        try {
            // ✅ Internal Firebase details hidden behind this interface
            Optional<User> userOpt = userRepository.findByUserId(userId);
            
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found: " + userId);
            }
            
            User user = userOpt.get();
            String fcmToken = user.getFcmToken();
            
            // ✅ ISOLATED VARIATION: Firebase implementation
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(messageBody)
                    .build();
            
            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(fcmToken)
                    .build();
            
            String response = FirebaseMessaging.getInstance(firebaseApp)
                    .send(message);  // ← Firebase-specific code isolated here
            
            log.info("Notification sent: {}", response);
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            throw new RuntimeException("Notification delivery failed", e);
        }
    }

    // ✅ STABLE INITIALIZATION (hidden detail)
    @PostConstruct
    public void initializeFirebase() {
        // Firebase-specific initialization isolated here
        // Changes to Firebase config only affect this method
    }
}

// ✅ CALLERS work with stable interface, unaware of Firebase
@Service
public class EmailProcessingSchedulerService {
    @Autowired
    private NotificationService notificationService;  // Stable interface
    
    public void processEmailsForUser(User user) {
        // Uses public interface, Firebase hidden
        notificationService.sendNotification(
            user.getUserId(), 
            "New Order", 
            "You have a new order");
    }
}
```

### Example: Repository Pattern Isolation
**Location:** Service layer using repositories

```java
/**
 * PROTECTED VARIATION: Repositories isolate database implementation.
 * If we switch from MySQL to PostgreSQL or MongoDB, only repositories change.
 */
@Service
@Slf4j
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;  // ← Abstraction layer
    @Autowired
    private UserRepository userRepository;    // ← Abstraction layer
    
    // ✅ Business logic works with stable repository interface
    public List<Order> getUserOrders(String userId) {
        return userRepository.findByUserId(userId)
                .map(user -> orderRepository.findByUserAndIsDeletedFalse(user))
                .orElse(List.of());
    }
    
    // ✅ SQL queries, JPA implementations, etc. are PROTECTED behind interface
    // Callers don't know or care about database details
}

// ✅ Repository interface is stable
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUser(Long id, User user);
    List<Order> findByUserAndIsDeletedFalse(User user);
}
```

**Rationale:**
- Firebase, database, email services are implementation details
- If Firebase fails, we can swap in a different notification provider
- Services stay unchanged, only the implementation class changes
- Variations are protected behind stable interfaces

---

## Summary Table

| Pattern | Example | File | Key Benefit |
|---------|---------|------|------------|
| **Creator** | `UserService.saveUser()` creates `User` and `UserDTO` | `UserService.java` | Clear responsibility for object creation |
| **Information Expert** | `OrderService` knows how to fetch user's orders | `OrderService.java` | Business logic encapsulated with relevant data |
| **Controller** | `OrderController` handles REST requests | `OrderController.java` | HTTP layer separated from business logic |
| **Low Coupling** | Service layer between Controller and Repository | Architecture | Reduced dependencies across layers |
| **High Cohesion** | `AbstractCrudService<T>` groups related CRUD ops | `AbstractCrudService.java` | Related functionality stays together |
| **Indirection** | Service layer interfaces hide repository details | `CrudService<T>` interface | Decouples API contracts from implementations |
| **Polymorphism** | Generic `AbstractCrudService<T>` for all entity types | `CategoryService`, `PlatformService` | No conditional logic for entity variants |
| **Pure Fabrication** | `NotificationTemplates` and `ApiResponse` | `NotificationTemplates.java`, `ApiResponse.java` | Artificial classes for convenience and abstraction |
| **Protected Variations** | `NotificationService` isolates Firebase | `NotificationService.java` | Implementation details hidden behind stable interface |

---

## How These Patterns Work Together

The OnTrack Server demonstrates how GRASP patterns complement each other:

1. **Creator** & **Information Expert** work together: Services create objects they have information about
2. **Low Coupling** & **High Cohesion** are balanced: Each service has one focus, with minimal external dependencies
3. **Polymorphism** & **Pure Fabrication**: Generic services use polymorphism to avoid fabricating concrete implementations
4. **Protected Variations** & **Indirection**: Interfaces provide stable contracts while implementations vary
5. **Controller** pattern ties it all together: Controllers orchestrate services following all other patterns

This results in maintainable, testable, and flexible code.
