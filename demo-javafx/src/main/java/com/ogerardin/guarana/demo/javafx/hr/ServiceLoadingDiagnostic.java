package com.ogerardin.guarana.demo.javafx.hr;

import com.ogerardin.guarana.core.config.AppConfigurationProvider;

import java.util.ServiceLoader;

/**
 * Diagnostic utility to test service loading in both Maven and IntelliJ modes.
 */
public class ServiceLoadingDiagnostic {
    
    public static void main(String[] args) {
        System.out.println("=== Service Loading Diagnostic ===\n");
        
        // Test 1: ServiceLoader
        System.out.println("1. Testing ServiceLoader.load(AppConfigurationProvider.class)...");
        ServiceLoader<AppConfigurationProvider> loader = ServiceLoader.load(AppConfigurationProvider.class);
        int count = 0;
        for (AppConfigurationProvider provider : loader) {
            count++;
            System.out.println("   Found provider #" + count + ": " + provider.getClass().getName());
            System.out.println("   Resource: " + provider.getPropertiesResourceName());
        }
        System.out.println("   Total providers found: " + count);
        System.out.println("   " + (count > 0 ? "✓ SUCCESS" : "✗ FAILURE") + "\n");
        
        // Test 2: Module system info
        System.out.println("2. Module System Information:");
        Module module = ServiceLoadingDiagnostic.class.getModule();
        System.out.println("   Current module: " + module.getName());
        System.out.println("   Module class loader: " + module.getClassLoader().getClass().getName());
        System.out.println("   Is named module: " + module.isNamed());
        System.out.println();
        
        // Test 3: ClassLoader info
        System.out.println("3. ClassLoader Information:");
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("   Context class loader: " + contextLoader.getClass().getName());
        ClassLoader classLoader = ServiceLoadingDiagnostic.class.getClassLoader();
        System.out.println("   Class class loader: " + classLoader.getClass().getName());
        System.out.println("   Are they same? " + (contextLoader == classLoader));
        System.out.println();
        
        System.out.println("=== Diagnostic Complete ===");
        System.out.println();
        System.out.println("EXPECTED: Total providers found should be 1");
        System.out.println("ACTUAL: Total providers found is " + count);
        System.out.println();
        if (count == 0) {
            System.err.println("❌ PROBLEM DETECTED: ServiceLoader is not finding the AppConfigurationProvider!");
            System.err.println("This means the 'uses'/'provides' mechanism in module-info.java is not working.");
            System.exit(1);
        } else {
            System.out.println("✅ Service loading is working correctly!");
            System.exit(0);
        }
    }
}
