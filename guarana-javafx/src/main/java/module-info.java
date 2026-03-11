/*
 * JavaFX implementation module of the Guarana framework.
 * Provides JavaFX-specific UI components and configuration.
 */
module guarana.javafx {
    // ===== Guarana Modules =====
    requires guarana.core;  // Core framework (required)
    
    // ===== Static (Compile-Only) Dependencies =====
    requires static lombok;  // Lombok annotation processor
    
    // ===== Java Standard Modules =====
    requires java.desktop;  // For java.beans and javax.imageio
    
    // ===== JavaFX Modules =====
    requires javafx.controls;   // JavaFX controls
    requires javafx.graphics;   // JavaFX graphics
    
    // ===== Third-Party Libraries (Automatic Modules) =====
    // FontAwesome icons - JAR-derived module names
    requires fontawesomefx.commons;      // FontAwesome commons (runtime required)
    requires fontawesomefx.fontawesome;  // FontAwesome icons (runtime required)
    requires fontawesomefx.icons525;     // Additional icons (runtime required)
    
    // JFXtras - JAR-derived module names
    requires jfxtras.labs;      // Additional JavaFX controls (runtime required)
    
    // SLF4J (logging)
    requires org.slf4j;  // SLF4J API
    
    // Apache Commons
    requires static org.apache.commons.lang3;       // Utility functions (compile-only)
    requires static commons.beanutils;              // Bean utilities (compile-only)
    
    // ===== Public API Exports =====
    // Export all public API packages for use by demo applications
    exports com.ogerardin.guarana.javafx;
    exports com.ogerardin.guarana.javafx.binding;
    exports com.ogerardin.guarana.javafx.binding.strategies;
    exports com.ogerardin.guarana.javafx.config;
    exports com.ogerardin.guarana.javafx.ui;
    exports com.ogerardin.guarana.javafx.ui.impl;
    exports com.ogerardin.guarana.javafx.ui.impl.embedded;
    exports com.ogerardin.guarana.javafx.util;
    
    // ===== Service Provision (CRITICAL FOR FIX) =====
    // Provides implementation of ToolkitConfigurationProvider service.
    // This allows guarana-core to discover and use JavaFX-specific configuration.
    
    provides com.ogerardin.guarana.core.config.ToolkitConfigurationProvider
        with com.ogerardin.guarana.javafx.config.JavaFxConfigurationProvider;
    
    // Also declare 'uses' for future-proofing and consistency
    uses com.ogerardin.guarana.core.config.ToolkitConfigurationProvider;
}
