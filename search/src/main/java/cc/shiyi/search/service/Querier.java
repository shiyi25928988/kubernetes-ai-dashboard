package cc.shiyi.search.service;

import cc.shiyi.coleditor.common.utils.ListPageUtil;
import cc.shiyi.search.model.IndexedDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Querier implements AutoCloseable{

    private IndexReader reader;
    private IndexSearcher searcher;

    public Querier(String storagePath) throws IOException {
        this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(storagePath)));
        this.searcher = new IndexSearcher(reader);
    }

    public List<IndexedDocument> fuzzySearch(String query, int pageNum, int pageSize) throws IOException {
        List<IndexedDocument> result = new ArrayList<>();
        Term term = new Term("content", query);
        FuzzyQuery fuzzyQuery = new FuzzyQuery(term);
        TopDocs topDocs = searcher.search(fuzzyQuery, 1000);
        List<ScoreDoc> scoreDocs = ListPageUtil.page(List.of(topDocs.scoreDocs), pageNum, pageSize);
        for (ScoreDoc scoreDoc : scoreDocs) {
            try {
                org.apache.lucene.document.Document doc = searcher.storedFields().document(scoreDoc.doc);
                IndexedDocument indexedDocument = new IndexedDocument();
                indexedDocument.setDocId(Long.parseLong(doc.get("doc_id")));
                indexedDocument.setDocTitle(doc.get("doc_title"));
                indexedDocument.setParagraphId(Long.parseLong(doc.get("paragraph_id")));
                indexedDocument.setParagraphTitle(doc.get("Paragraph_title"));
                indexedDocument.setUrl(doc.get("url"));
                indexedDocument.setContent(doc.get("content"));
                indexedDocument.setCreatedTime(new java.util.Date(Long.parseLong(doc.get("created_Time"))));
                result.add(indexedDocument);
            } catch (IOException e) {
                log.info("查询异常{}",e.getMessage());
            }
        }
        return result;
    }


    @Override
    public void close() throws Exception {
        this.reader.close();
        this.searcher = null;
    }
}
