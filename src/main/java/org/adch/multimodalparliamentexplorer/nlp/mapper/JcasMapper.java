package org.adch.multimodalparliamentexplorer.nlp.mapper;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.adch.multimodalparliamentexplorer.nlp.SpeechNlpData;
import org.adch.multimodalparliamentexplorer.nlp.entities.*;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.uima.type.GerVaderSentiment;
import org.hucompute.textimager.uima.type.category.CategoryCoveredTagged;
import org.texttechnologylab.annotation.AnnotationComment;
import org.texttechnologylab.annotation.Sarcasm;

import java.util.List;


public class JcasMapper {

    public static List<SentenceData> extractSentenceData(JCas jCas) {

        return JCasUtil.select(jCas, Sentence.class)
                .stream()
                .map(sentence -> {
                    var sentiment = JCasUtil.selectCovered(GerVaderSentiment.class, sentence)
                            .getFirst();
                    var sarcasm = JCasUtil.selectCovered(Sarcasm.class, sentence)
                            .getFirst();
                    var topics = JCasUtil.selectCovered(CategoryCoveredTagged.class, sentence)
                            .stream()
                            .map(topic -> new TopicData(topic.getValue(), topic.getScore()))
                            .toList();

                    var namedEntities = JCasUtil.selectCovered(NamedEntity.class, sentence)
                            .stream()
                            .map(namedEntity -> new NamedEntityData(namedEntity.getBegin(), namedEntity.getEnd(), namedEntity.getValue()))
                            .toList();

                    return SentenceData.builder()
                            .begin(sentence.getBegin())
                            .end(sentence.getEnd())
                            .positivSentiment(sentiment.getPos())
                            .neutralSentiment(sentiment.getNeu())
                            .negativSentiment(sentiment.getNeg())
                            .totalSentiment(sentiment.getSentiment())
                            .sarcasm(sarcasm.getSarcasm())
                            .nonSarcasm(sarcasm.getNonSarcasm())
                            .namedEntities(namedEntities)
                            .topics(topics)
                            .build();
                })
                .toList();

    }

    public static List<TokenData> extractTokenData(JCas jCas) {

        return JCasUtil.select(jCas, Token.class)
                .stream()
                .map(token ->
                        TokenData.builder()
                                .begin(token.getBegin())
                                .end(token.getEnd())
                                .stem(token.getStemValue())
                                .lemma(token.getLemmaValue())
                                .pos(token.getPosValue())
                                .coarseValue(token.getPos().getCoarseValue())
                                .gender(token.getMorph().getGender())
                                .number(token.getMorph().getNumber())
                                .casus(token.getMorph().getCase())
                                .tense(token.getMorph().getTense())
                                .verbForm(token.getMorph().getVerbForm())
                                .pronoun(token.getMorph().getPronType())
                                .build())
                .toList();
    }


    public static List<ParagraphData> extractParagraphData(JCas jCas) {

        var comments = JCasUtil.select(jCas, AnnotationComment.class)
                .stream()
                .filter(annotationComment -> annotationComment.getValue().equals("comment"))
                .toList();

        return JCasUtil.select(jCas, Paragraph.class)
                .stream()
                .map(paragraph -> {
                    int begin = paragraph.getBegin();
                    int end = paragraph.getEnd();
                    boolean comment = comments
                            .stream()
                            .anyMatch(annotationComment -> annotationComment.getReference().equals(paragraph));
                    return ParagraphData.builder()
                            .begin(begin)
                            .end(end)
                            .comment(comment)
                            .build();
                }).toList();

    }


    public static SpeechNlpData mapNlpData(JCas jCas) {

        String id = DocumentMetaData.get(jCas).getDocumentId();
        String fullText = jCas.getDocumentText();
        var sentenceData = extractSentenceData(jCas);
        var tokenData = extractTokenData(jCas);
        var paragraphData = extractParagraphData(jCas);

        return SpeechNlpData.builder()
                .id(id)
                .fullText(fullText)
                .sentenceData(sentenceData)
                .tokenData(tokenData)
                .paragraphData(paragraphData)
                .build();
    }

}
