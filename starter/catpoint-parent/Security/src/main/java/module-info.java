module security{
    requires transitive com.udacity.catpoint.Image;
    requires transitive java.desktop;
    requires transitive java.sql;
    requires miglayout;
    requires guava;
    requires com.google.gson;
    requires transitive java.prefs;
    opens security.service to com.google.gson;
}