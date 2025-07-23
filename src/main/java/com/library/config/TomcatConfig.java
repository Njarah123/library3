package com.library.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                // Configuration directe sur le connecteur pour désactiver TOUTES les limites
                connector.setMaxPostSize(-1);
                connector.setMaxSavePostSize(-1);
                
                // Configuration via propriétés système pour forcer la désactivation
                System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "-1");
                System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax", "-1");
                System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax", "-1");
                
                // Configuration des propriétés du connecteur
                connector.setProperty("maxParameterCount", "-1");
                connector.setProperty("maxFileCount", "-1");
                connector.setProperty("maxHttpHeaderSize", "65536");
                connector.setProperty("maxSwallowSize", "-1");
                connector.setProperty("maxPostSize", "-1");
                connector.setProperty("maxSavePostSize", "-1");
                
                // Configuration du protocole avec limites désactivées
                Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
                protocol.setMaxConnections(10000);
                protocol.setAcceptCount(1000);
                protocol.setConnectionTimeout(60000);
                protocol.setMaxHttpHeaderSize(65536);
                
                // Force la désactivation des limites au niveau du protocole
                try {
                    // Utilisation de la réflexion pour accéder aux propriétés internes
                    java.lang.reflect.Field field = protocol.getClass().getDeclaredField("maxParameterCount");
                    field.setAccessible(true);
                    field.set(protocol, -1);
                } catch (Exception e) {
                    // Ignore si la propriété n'existe pas
                }
                
                try {
                    java.lang.reflect.Field field = protocol.getClass().getDeclaredField("maxFileCount");
                    field.setAccessible(true);
                    field.set(protocol, -1);
                } catch (Exception e) {
                    // Ignore si la propriété n'existe pas
                }
            });
            
            // Configuration supplémentaire au niveau de la factory
            factory.addContextCustomizers(context -> {
                // Désactiver les limites au niveau du contexte
                context.setSwallowOutput(true);
            });
        };
    }
}