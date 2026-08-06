package cc.shiyi.search.service;

import cc.shiyi.search.db.SearchFrequency;
import cc.shiyi.search.db.SearchFrequencyMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Suggester implements AutoCloseable{

    private static final String DATA_FILE = "suggest.dat";

    @Autowired
    private SearchFrequencyMapper searchFrequencyMapper;

    @Value("${lucene.storage.path}")
    String storagePath;

    private Directory directory;

    private Dictionary dictionary;

    public Suggester(String storagePath) throws IOException {
        this.directory = this.directory = FSDirectory.open(Paths.get(storagePath));
        File file = new File(storagePath, DATA_FILE);
        com.google.common.io.Files.touch(file);
        this.dictionary = new FileDictionary(new FileInputStream(file));
    }

    public void rebuildFromDatabase() throws IOException {
        AnalyzingSuggester analyzingSuggester = new AnalyzingSuggester(directory, "sugg_", new StandardAnalyzer());
        List<SearchFrequency> searchFrequencies = searchFrequencyMapper.getAll();
        Map<CharSequence, Float> map = new HashMap<>();
        for (SearchFrequency searchFrequency : searchFrequencies) {
            map.put(searchFrequency.getTerm(), (float)Math.log(searchFrequency.getFrequency())+1);
        }
        if(!map.isEmpty()) {
//            dictionary.getEntryIterator().
//            analyzingSuggester.build();
        }
    }

    public void updateFrequency(String term) {
        searchFrequencyMapper.updateFrequency(term);
    }

    public List<SearchFrequency> getAll() {
        return searchFrequencyMapper.getAll();
    }

    @Override
    public void close() throws Exception {
        if(Objects.nonNull(this.directory)) {
            this.directory.close();
        }
    }
}
