# Agent Guidelines for Project Guarana

## Project Overview
JavaFX-based automatic GUI generation framework using reflection and introspection. Multi-module Maven project with Java 21.

## Build Commands

```bash
# Full build
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Build offline (fastest, uses local cache)
mvn clean install -DskipTests -o

# Compile only
mvn clean compile

# Single module build
cd guarana-core && mvn clean install -DskipTests
```

## Test Commands

```bash
# Run all tests
mvn test

# Run tests in specific module
cd guarana-core && mvn test

# Run single test class
mvn test -Dtest=ClassInformationTest

# Run single test method
mvn test -Dtest=ClassInformationTest#testConstructors

# Run with offline mode
mvn test -o
```

## Run Applications

```bash
# Run DemoJfxHR (default)
cd demo-javafx && mvn javafx:run

# Run with specific profile
cd demo-javafx && mvn javafx:run -PDemoJfxConfig
cd demo-javafx && mvn javafx:run -PDemoJfxWebsite

# Run guarana-javafx test classes
cd guarana-javafx && mvn javafx:run -PCollectionFieldPojo
cd guarana-javafx && mvn javafx:run -PCollectionBean
```

## Code Style Guidelines

### Java Version
- **Java 21** (LTS)
- Use modern features: `var`, `Optional`, streams, pattern matching where appropriate

### Imports
- Group imports: `java.*`, `javax.*`, third-party, project
- No wildcard imports (except Lombok)
- Static imports allowed for test assertions and constants

### Formatting
- 4-space indentation (no tabs)
- Opening brace on same line
- Max line length: 120 characters
- One blank line between methods

### Naming Conventions
- **Classes**: `PascalCase` (e.g., `DomainManagerMapDBImpl`)
- **Methods**: `camelCase` (e.g., `getAllEmployees()`)
- **Fields**: `camelCase`, private with getters/setters
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `CORE_PROPERTIES`)
- **Interfaces**: `PascalCase`, no I- prefix
- **Generic types**: Single uppercase (e.g., `<T>`, `<C>`)

### Lombok Usage
**Required annotations for model classes:**
```java
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Employee { ... }
```

- Always use Lombok for boilerplate: getters, setters, toString, equals/hashCode
- Add `@NoArgsConstructor` for classes that need default instantiation
- Use `@Slf4j` for logging (not manual logger creation)

### Type Safety
- Prefer `Optional<T>` over null returns
- Use `final` for parameters and local variables where appropriate
- Avoid raw types; use generics properly
- Prefer interfaces in declarations (e.g., `List<String>` not `ArrayList<String>`)

### Error Handling
- Use checked exceptions for recoverable errors
- Use unchecked exceptions for programming errors
- Log exceptions with context before throwing
- Never catch and ignore exceptions without logging

### Documentation
- Javadoc for public APIs with `@author` and `@since`
- Use `{@code ...}` for inline code
- Document thread-safety and null handling

## Module Structure

```
project-guarana/
├── guarana-core/       # Core introspection and configuration
├── guarana-javafx/     # JavaFX UI implementation
├── sample-business/    # Sample domain models (Employee, Event, etc.)
└── demo-javafx/        # Demo applications
```

## Key Dependencies
- **JavaFX 21**: UI framework
- **Lombok 1.18.40**: Code generation
- **MapDB 3.1.0**: Embedded database (demo only)
- **JUnit 4.13.2**: Testing
- **Apache Commons**: Configuration, Lang3, Collections

## Testing Guidelines
- Test classes end with `Test` suffix
- Use JUnit 4 (`@Test`, `Assert.assertEquals`)
- Place tests in `src/test/java` parallel to main code
- Test domain classes in `guarana-core/src/test/java/.../test/domain/`

## IntelliJ Run Configurations

Run configurations are in `.run/` directory. **Important:** These configurations have been updated to use JAR files from `target/` directories instead of `target/classes` because automatic modules (JARs without module-info) must be loaded from JAR files, not exploded class directories.

**Available Configurations:**
- **DemoJfxHR** - Main HR demo application (with debug agent on port 5005, `suspend=n`)
- **DemoJfxConfig** - Configuration demo
- **DemoJfxWebsite** - Website demo
- **CollectionFieldPojo** - Collection field test
- **CollectionBean** - Collection bean test

**Note:** IntelliJ run configurations require all dependencies to be listed in the module-path. The configurations currently include core dependencies but may need additional dependencies for full functionality. For guaranteed working execution, use Maven commands below.

## Debugging JavaFX Applications

### Option 1: Using Run Configurations (Recommended)
The **DemoJfxHR** run configuration has debugging enabled on port 5005 with `suspend=n` (non-blocking).

**Steps:**
1. Select **DemoJfxHR** configuration
2. Click **Debug** (not Run) - this starts the app with debugger attached immediately

**Note:** If you need to debug startup code (main method, static initializers), use the Maven command line option below with `suspend=y`.

### Option 2: Command Line Debugging
```bash
cd demo-javafx
# Start with debug agent
mvn javafx:run -Djavafx.jvmArgs="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Then attach IntelliJ debugger to localhost:5005
```

### Option 3: Debug Profile
Add to `demo-javafx/pom.xml` in profiles section:
```xml
<profile>
    <id>debug</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.openjfx</groupId>
                <artifactId>javafx-maven-plugin</artifactId>
                <configuration>
                    <mainClass>com.ogerardin.guarana.demo.javafx/com.ogerardin.guarana.demo.javafx.hr.DemoJfxHR</mainClass>
                    <options>
                        <option>--add-opens</option>
                        <option>java.base/java.lang=cglib</option>
                    </options>
                    <jvmArgs>
                        <jvmArg>-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005</jvmArg>
                    </jvmArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

Then run:
```bash
cd demo-javafx
mvn javafx:run -Pdebug
```



## Git Workflow
- No specific branch naming convention
- Build must pass (`mvn clean install -DskipTests`) before committing
- No TODO/FIXME comments in committed code (use issue tracker)

## Performance Notes
- Use `final` for fields where possible
- Prefer `StringBuilder` over String concatenation in loops
- Use `Stream` API judiciously (avoid in hot paths)
- Lazy initialization pattern used for expensive objects

## Known Issues
- Repository `shamanblackout.com` may be unreachable (non-critical, cached deps)
- Lombok uses deprecated `sun.misc.Unsafe` (cosmetic warning)
- JavaFX native access warnings (JavaFX 21 internal, safe to ignore)
