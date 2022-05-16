module security{
    requires transitive image.service;
    requires transitive java.desktop;
    requires transitive java.sql;
    requires miglayout;
    requires guava;
    requires gson;
    requires transitive java.prefs;
    opens security.data to gson;
    opens security.application to gson;
    opens security.service to gson;
    exports security.service;
    exports security.data;
    exports security.application;
}