/*
 * Copyright (c) 2025 Olivier Gérardin
 */

package com.ogerardin.guarana.demo.javafx.test;

import com.ogerardin.guarana.core.config.AppConfigurationProvider;
import com.ogerardin.guarana.core.config.ToolkitConfigurationProvider;
import java.util.ServiceLoader;

/**
 * Test to verify that JPMS service loading works correctly with module-info.java
 * and provides/uses declarations.
 *
 * This test helps diagnose ServiceLoader issues when running in module-path mode.
 *
 * @author Olivier Gérardin
 * @since 1.0
 */
public class ModuleServiceTest {
    
    public static void main(String[] args) {
        System.out.println("=== JPMS ServiceLoader Test ===\n");
        
        // Test AppConfigurationProvider
        testServiceProvider(
            "AppConfigurationProvider",
            AppConfigurationProvider.class
        );
        
        // Test ToolkitConfigurationProvider
        testServiceProvider(
            "ToolkitConfigurationProvider",
            ToolkitConfigurationProvider.class
        );
        
        System.out.println("\n=== Test Complete ===");
    }
    
    private static <T> void testServiceProvider(String serviceName, Class<T> serviceClass) {
        System.out.println("Testing " + serviceName + ":");
        System.out.println("  Service class: " + serviceClass.getName());
        
        ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
        int count = 0;
        
        for (T provider : loader) {
            count++;
            System.out.println("  Provider #" + count + ":");
            System.out.println("    Class: " + provider.getClass().getName());
            
            // Try to get the resource name
            if (provider instanceof AppConfigurationProvider) {
                AppConfigurationProvider appProvider = (AppConfigurationProvider) provider;
                System.out.println("    Resource: " + appProvider.getPropertiesResourceName());
            } else if (provider instanceof ToolkitConfigurationProvider) {
                ToolkitConfigurationProvider toolkitProvider = (ToolkitConfigurationProvider) provider;
                System.out.println("    Resource: " + toolkitProvider.getPropertiesResourceName());
            }
        }
        
        if (count == 0) {
            System.err.println("  ✗ ERROR: No providers found!");
            System.err.println("  This indicates a ServiceLoader configuration problem.");
            System.err.println("  Check that:");
            System.err.println("    1. module-info.java has 'uses' declaration in consumer module");
            System.err.println("    2. module-info.java has 'provides' declaration in provider module");
            System.err.println("    3. META-INF/services file exists as backup for classpath mode");
        } else {
            System.out.println("  ✓ SUCCESS: Found " + count + " provider(s)");
        }
        
        System.out.println();
    }
}
