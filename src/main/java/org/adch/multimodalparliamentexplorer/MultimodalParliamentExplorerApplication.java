package org.adch.multimodalparliamentexplorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MultimodalParliamentExplorerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultimodalParliamentExplorerApplication.class, args);
    }

}
