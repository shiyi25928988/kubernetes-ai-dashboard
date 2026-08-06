package cc.shiyi.search.config;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class LuceneConfig {

    @Value("${lucene.storagePath:}")
    private String storagePath;

//    @Bean
//    public Directory directory() throws IOException {
//        return FSDirectory.open(Paths.get(storagePath));
//    }

//    @Bean
//    public Analyzer analyzer() {
//        return new IKAnalyzer(true);
//    }
}
