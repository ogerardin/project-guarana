module com.ogerardin.guarana.demo.javafx {
    requires javafx.controls;
    requires javafx.graphics;
    requires guarana.core;
    requires guarana.javafx;
    requires sample.business;
    requires mapdb;
    requires static lombok;
    requires java.sql;
    requires cglib;
    
    exports com.ogerardin.guarana.demo.javafx.config;
    exports com.ogerardin.guarana.demo.javafx.hr;
    exports com.ogerardin.guarana.demo.javafx.website;
    
    opens com.ogerardin.guarana.demo.javafx.config;
    opens com.ogerardin.guarana.demo.javafx.hr;
    opens com.ogerardin.guarana.demo.javafx.hr.adapters;
    opens com.ogerardin.guarana.demo.javafx.website;
}