package org.adch.multimodalparliamentexplorer.nlp;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import io.kubernetes.client.proto.V1;
import lombok.extern.slf4j.Slf4j;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class NLPDataImporter {

    private final String source;
    private final AtomicBoolean importRunning = new AtomicBoolean(false);

    public NLPDataImporter(@Value("${app.datasource.xmi-folder}") String source) {
        this.source = source;
    }

    public JCas importXmi(Path path) throws IOException, ResourceInitializationException, CASException, SAXException {

            JCas jCas = JCasFactory.createJCas();

            var metadata = new DocumentMetaData(jCas);
            String fileName = path.getFileName().toString();
            int dotIndex = fileName.indexOf('.');
            metadata.setDocumentId(dotIndex == -1 ? fileName: fileName.substring(0, dotIndex));
            metadata.addToIndexes();

            if(Files.exists(path)){
                var input = new GZIPInputStream(Files.newInputStream(path));
                XmiCasDeserializer.deserialize(input, jCas.getCas(), true);
                input.close();
            }

            return jCas;

    }


    public List<JCas> importAllXmis(){

        if (!importRunning.compareAndSet(false, true)) {
            log.warn("NLP import is already running");
            return List.of();
        }

        Path dir = Path.of(source);

        if (!Files.isDirectory(dir)) {
            log.warn("The provided path is not  directory");
            importRunning.set(false);
            return List.of();
        }

        log.info("Importing all XMI data from {}", dir);


        List<Path> xmiFiles;

        try (Stream<Path> fileStream = Files.list(dir)) {
            xmiFiles = fileStream
                    .filter(p -> p.toString().toLowerCase().endsWith(".xmi.gz"))
                    .toList();
        } catch (IOException e) {
            importRunning.set(false);
            throw new RuntimeException(e);
        }

        log.info("Found {} XMI files to process", xmiFiles.size());

        List<String> failedFiles = Collections.synchronizedList(new ArrayList<>());

        List<CompletableFuture<JCas>> futures = xmiFiles.stream()
                .map(p -> CompletableFuture.supplyAsync(() -> {
                    log.info("Importing NLP data from file {} ...", p.getFileName());
                    try {
                        return importXmi(p);
                    } catch (IOException | ResourceInitializationException | CASException | SAXException e) {
                        log.error("Failed to import file {}, skipping", p.getFileName(), e);
                        failedFiles.add(p.getFileName().toString());
                        return null; // don’t fail the whole batch
                    }
                }))
                .toList();

        List<JCas> result;
        try {
            result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream()
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .toList()
                    )
                    .join();
        } finally {
            importRunning.set(false);
        }

        log.info("Import finished. Total: {}, Successful: {}, Failed: {}",
                xmiFiles.size(),
                result.size(),
                failedFiles.size());

        if (!failedFiles.isEmpty()) {
            log.warn("Files that failed to import: {}", failedFiles);
        }

        return result;
    }


    public boolean isImportRunning() {
        return importRunning.get();
    }


}
