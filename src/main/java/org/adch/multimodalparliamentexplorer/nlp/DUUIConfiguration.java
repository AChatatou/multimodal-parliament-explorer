package org.adch.multimodalparliamentexplorer.nlp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.duui")
public record DUUIConfiguration(
         String spacyRemoteUrl,
         String spacyDockerImage,
         String gervaderRemoteUrl,
         String gervaderDockerImage,
         String parlbertRemoteUrl,
         String parlbertDockerImage,
         String sarcasmRemoteUrl,
         String sarcasmDockerImage,
         String whisperxRemoteUrl,
         String whisperxDockerImage
) {}
