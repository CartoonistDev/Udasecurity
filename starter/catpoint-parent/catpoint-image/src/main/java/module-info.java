module image.service{
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires org.slf4j;
    requires java.desktop;
    exports image.service;
}