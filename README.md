# Project Guarana

Automatic model-based GUI generation framework for Java. Generate JavaFX user interfaces automatically from your domain models using reflection and introspection.

## Features

- **Automatic GUI Generation** - Create JavaFX forms and views from POJOs without manual UI coding
- **Reflection-Based** - Uses Java reflection to introspect classes and generate appropriate UI components
- **Configurable** - Extensive configuration system for customizing UI behavior
- **Collection Support** - Built-in support for lists, sets, and maps with appropriate editors
- **Java 21 Ready** - Modern Java features and JPMS module support

## Requirements

- **Java 21** or later (LTS)
- **Maven 3.8+**
- Platform-specific JavaFX native libraries (handled automatically by Maven)

## Quick Start

```bash
# Clone the repository
git clone https://github.com/ogerardin/project-guarana.git
cd project-guarana

# Build the project
mvn clean install -DskipTests

# Run the HR demo application
cd demo-javafx
mvn javafx:run
```

## Module Structure

```
project-guarana/
├── guarana-core/       # Core introspection, configuration, and UI abstraction
├── guarana-javafx/     # JavaFX-specific UI implementation
├── sample-business/    # Sample domain models (Employee, Event, Leave, etc.)
└── demo-javafx/        # Demo applications showcasing the framework
```

### Module Details

**guarana-core**
- Reflection-based class introspection
- Configuration management (Apache Commons Configuration)
- UI manager abstraction layer
- Collection and property handling

**guarana-javafx**
- JavaFX UI component generation
- Form builders and editors
- Custom JavaFX controls integration (JFXtras)
- FontAwesome icon support

**sample-business**
- Example domain models for testing and demos
- Employee, Event, Leave, and other sample classes
- Implements `Serializable` for persistence demos

**demo-javafx**
- **DemoJfxHR** - HR management demo with MapDB persistence
- **DemoJfxConfig** - Configuration system demo
- **DemoJfxWebsite** - Website management demo

## Usage Example

```java
// Create a UI manager
JfxUiManager uiManager = new JfxUiManager();

// Generate a form for any POJO
Employee employee = new Employee();
Pane form = uiManager.buildForm(employee);

// Show in your JavaFX application
Scene scene = new Scene(form);
stage.setScene(scene);
stage.show();
```

## Documentation

For detailed information about build commands, development workflows, and debugging:

**[See AGENTS.md](AGENTS.md)** - Complete developer guide including:
- Build and test commands
- Code style guidelines
- IntelliJ run configurations
- Debugging instructions
- Project conventions

## Development

### Build Commands

```bash
# Full build with tests
mvn clean install

# Fast build (skip tests)
mvn clean install -DskipTests

# Offline build (use cached dependencies)
mvn clean install -DskipTests -o
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ClassInformationTest
```

### Run Configurations

IntelliJ IDEA run configurations are provided in the `.run/` directory:
- **DemoJfxHR** - Main HR demo with debugging support
- **DemoJfxConfig** - Configuration demo
- **DemoJfxWebsite** - Website demo
- **CollectionFieldPojo** - Collection field test
- **CollectionBean** - Collection bean test

## Dependencies

- **JavaFX 21** - Modern UI toolkit
- **Lombok 1.18.40** - Boilerplate code reduction
- **MapDB 3.1.0** - Embedded database (demo only)
- **Apache Commons** - Configuration, Lang3, Collections
- **CGLIB 3.3.0** - Code generation for proxies
- **JUnit 4.13.2** - Unit testing

## License

This project is licensed under the Apache License 2.0 - see below for details:

```
Copyright 2015 Olivier Gérardin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Author

**Olivier Gérardin** - [ogerardin](https://github.com/ogerardin)

## Contributing

Contributions are welcome! Please ensure:
- Code follows the existing style (see AGENTS.md)
- Build passes: `mvn clean install -DskipTests`
- No TODO/FIXME comments in committed code

## Acknowledgments

- Built with JavaFX 21
- Uses Lombok for reducing boilerplate
- Inspired by the need for rapid UI prototyping in Java applications
