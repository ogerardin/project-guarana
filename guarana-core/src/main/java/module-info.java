/*
 * Core module of the Guarana framework.
 * Provides introspection, configuration, and UI building capabilities.
 */
module guarana.core {
    // ===== Java Standard Modules =====
    requires java.desktop;  // Required for java.beans (PropertyChangeSupport, Introspector, etc.)
    requires java.sql;      // Required for persistence services
    
    // ===== Static (Compile-Only) Dependencies =====
    requires static lombok;  // Lombok annotation processor
    
    // ===== Third-Party Libraries (Automatic Modules) =====
    // Note: These are automatic modules derived from JAR files
    
    // Apache Commons - with Automatic-Module-Name
    requires org.apache.commons.configuration2;  // Configuration management
    requires static org.apache.commons.lang3;    // Utility functions (compile-only)
    
    // Google Guava - with Automatic-Module-Name
    requires static com.google.common;  // Common utilities (compile-only)
    
    // Apache Commons - JAR-derived module names (no Automatic-Module-Name)
    requires static commons.beanutils;  // Bean utilities (compile-only)
    
    // Other libraries - JAR-derived module names
    requires org.slf4j;                       // SLF4J API (logging facade)
    requires static cglib;                    // Code generation library (compile-only)
    requires static fast.classpath.scanner;   // Classpath scanning (compile-only)
    // Note: logback.classic is needed at runtime but not required here (logging implementation)
    
    // ===== JavaFX =====
    requires static javafx.base;  // For StringConverter (compile-only)
    
    // ===== Public API Exports =====
    // Export all public API packages for use by consumer modules
    exports com.ogerardin.guarana.core.annotations;
    exports com.ogerardin.guarana.core.config;
    exports com.ogerardin.guarana.core.introspection;
    exports com.ogerardin.guarana.core.metamodel;
    exports com.ogerardin.guarana.core.observability;
    exports com.ogerardin.guarana.core.persistence;
    exports com.ogerardin.guarana.core.persistence.basic;
    exports com.ogerardin.guarana.core.registry;
    exports com.ogerardin.guarana.core.ui;
    exports com.ogerardin.guarana.core.util;
    
    // ===== Service Loading (CRITICAL FOR FIX) =====
    // These 'uses' declarations enable JPMS service binding with 'provides' statements
    // in consumer modules. Without these, ServiceLoader cannot find service providers
    // when running in module-path mode (e.g., IntelliJ run configuration).
    
    uses com.ogerardin.guarana.core.config.AppConfigurationProvider;
    uses com.ogerardin.guarana.core.config.ToolkitConfigurationProvider;
}
