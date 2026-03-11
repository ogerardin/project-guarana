/*
 * Module definition for sample-business domain models.
 * Provides simple business objects for HR, website, and configuration examples.
 */
module sample.business {
    // Static (compile-only) dependency for Lombok annotations
    requires static lombok;
    
    // Export all domain model packages for use by consumer modules
    exports com.ogerardin.business.sample.config.model;
    exports com.ogerardin.business.sample.hr.model;
    exports com.ogerardin.business.sample.hr.service;
    exports com.ogerardin.business.sample.website.model;
    
    // Open packages for reflection (required by guarana-core introspection)
    opens com.ogerardin.business.sample.config.model;
    opens com.ogerardin.business.sample.hr.model;
    opens com.ogerardin.business.sample.hr.service;
    opens com.ogerardin.business.sample.website.model;
}
