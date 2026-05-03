package org.adch.multimodalparliamentexplorer.nlp;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.speech.Speech;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIDockerDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;

@Component
@Slf4j
public class NLPAnalyser {

    private final DUUIComposer composer;
    private final DUUIConfiguration configuration;

    public NLPAnalyser(DUUIConfiguration configuration, @Value("${app.duui.mode}") String mode) throws Exception{
        this.configuration = configuration;

        this.composer = new DUUIComposer()
                .withLuaContext(new DUUILuaContext().withJsonLibrary())
                .withSkipVerification(true);

        this.composer
                .addDriver(new DUUIRemoteDriver(), new DUUIDockerDriver(), new DUUIUIMADriver());

        setupComponents(1, mode.equals("remote"));

        System.out.println("NLP analyser initialized");
    }


    private void setupComponents(int workers, boolean remote) throws Exception {
        composer.withWorkers(workers);
        if (remote) {
            composer.add(new DUUIRemoteDriver.Component(configuration.spacyRemoteUrl())
                    .withScale(workers)
                    .build());

            composer.add(new DUUIRemoteDriver.Component(configuration.gervaderRemoteUrl())
                    .withScale(workers)
                    .withParameter("selection", Sentence.class.getName())
                    .build());

            composer.add(new DUUIRemoteDriver.Component(configuration.parlbertRemoteUrl())
                    .withScale(workers)
                    .build());

            composer.add(new DUUIRemoteDriver.Component(configuration.sarcasmRemoteUrl())
                    .withScale(workers)
                    .withParameter("selection", Sentence.class.getName())
                    .build());

            return;
        }

        composer.add(new DUUIDockerDriver.Component(configuration.spacyDockerImage())
                .withScale(workers)
                .build());

        composer.add(new DUUIDockerDriver.Component(configuration.gervaderDockerImage())
                .withScale(workers)
                .withParameter("selection", Sentence.class.getName())
                .build());

        composer.add(new DUUIDockerDriver.Component(configuration.parlbertDockerImage())
                .withScale(workers)
                .build());

        composer.add(new DUUIDockerDriver.Component(configuration.sarcasmDockerImage())
                .withScale(workers)
                .withParameter("selection", Sentence.class.getName())
                .build());

    }

    public JCas analyse(Speech speech, String language) throws Exception {
        log.info("Analysing speech {} ...", speech.getId());
        JCas jCas = JCasFactory.createText(speech.getFullText(), language);

        var metadata = new DocumentMetaData(jCas);
        metadata.setDocumentId(speech.getId());
        metadata.addToIndexes();

        composer.run(jCas);

        return jCas;
    }


}
