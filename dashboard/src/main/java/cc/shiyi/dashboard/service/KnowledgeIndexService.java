package cc.shiyi.dashboard.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.wltea.analyzer.lucene.IKAnalyzer;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库全文索引服务（Lucene + IK 中文分词）。
 * 索引目录独立于库内容存储（lucene.storage.path/knowledge），
 * 数据以 MySQL 为准（knowledge_doc 表），索引仅用于检索。
 */
@Slf4j
@Service
public class KnowledgeIndexService {

    private static final String FIELD_ID = "id";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_CATEGORY = "category";
    private static final String FIELD_CONTENT = "content";

    @Value("${lucene.storage.path:D:/data/lucene}")
    private String storagePath;

    /** 索引/查询共用 IK 分词器（true=智能切分；细粒度切分召回更好，这里用细粒度） */
    private final Analyzer analyzer = new IKAnalyzer(false);
    private final Analyzer fallbackAnalyzer = new StandardAnalyzer();

    /** 索引更新串行化（IndexWriter 目录级互斥） */
    private final Object writeLock = new Object();

    /**
     * 检索结果
     */
    public record KnowledgeHit(Long id, String title, String category, String snippet, float score) {
    }

    private Path indexDir() {
        return Paths.get(storagePath, "knowledge");
    }

    private FSDirectory openDirectory() throws IOException {
        return FSDirectory.open(indexDir());
    }

    /**
     * 索引是否存在（不存在或为空时返回 true，用于启动时全量重建）
     */
    public boolean isIndexEmpty() {
        try (FSDirectory dir = openDirectory()) {
            return !DirectoryReader.indexExists(dir);
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 新增/更新文档索引（按 id upsert）
     */
    public void indexDoc(Long id, String title, String category, String content) {
        Document doc = new Document();
        doc.add(new StringField(FIELD_ID, String.valueOf(id), Field.Store.YES));
        doc.add(new TextField(FIELD_TITLE, nvl(title), Field.Store.YES));
        doc.add(new StringField(FIELD_CATEGORY, nvl(category), Field.Store.YES));
        doc.add(new TextField(FIELD_CONTENT, nvl(content), Field.Store.YES));
        doWrite(writer -> {
            writer.deleteDocuments(new Term(FIELD_ID, String.valueOf(id)));
            writer.addDocument(doc);
        });
    }

    /**
     * 从索引中移除文档
     */
    public void removeDoc(Long id) {
        doWrite(writer -> writer.deleteDocuments(new Term(FIELD_ID, String.valueOf(id))));
    }

    /**
     * 全量重建索引（先清空再逐条写入）
     */
    public void reindexAll(List<IndexedDoc> docs) {
        synchronized (writeLock) {
            try (FSDirectory dir = openDirectory();
                 IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(analyzer))) {
                writer.deleteAll();
                for (IndexedDoc d : docs) {
                    Document doc = new Document();
                    doc.add(new StringField(FIELD_ID, String.valueOf(d.id()), Field.Store.YES));
                    doc.add(new TextField(FIELD_TITLE, nvl(d.title()), Field.Store.YES));
                    doc.add(new StringField(FIELD_CATEGORY, nvl(d.category()), Field.Store.YES));
                    doc.add(new TextField(FIELD_CONTENT, nvl(d.content()), Field.Store.YES));
                    writer.addDocument(doc);
                }
                writer.commit();
                log.info("知识库索引全量重建完成，文档数: {}", docs.size());
            } catch (Exception e) {
                log.error("知识库索引全量重建失败: {}", e.getMessage(), e);
            }
        }
    }

    private interface WriteAction {
        void apply(IndexWriter writer) throws Exception;
    }

    private void doWrite(WriteAction action) {
        synchronized (writeLock) {
            try (FSDirectory dir = openDirectory();
                 IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(analyzer))) {
                action.apply(writer);
                writer.commit();
            } catch (Exception e) {
                log.error("知识库索引更新失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 关键词检索，返回命中列表（含 <em> 高亮片段）
     */
    public List<KnowledgeHit> search(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        List<KnowledgeHit> hits = new ArrayList<>();
        try (FSDirectory dir = openDirectory()) {
            if (!DirectoryReader.indexExists(dir)) {
                return hits;
            }
            DirectoryReader reader = DirectoryReader.open(dir);
            try {
                IndexSearcher searcher = new IndexSearcher(reader);
                Query query = buildQuery(keyword);
                TopDocs topDocs = searcher.search(query, limit);
                Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<em>", "</em>"), new QueryScorer(query));
                highlighter.setTextFragmenter(new SimpleFragmenter(120));
                var storedFields = reader.storedFields();
                for (ScoreDoc sd : topDocs.scoreDocs) {
                    Document doc = storedFields.document(sd.doc);
                    String snippet = null;
                    try {
                        String[] fragments = highlighter.getBestFragments(analyzer, FIELD_CONTENT, nvl(doc.get(FIELD_CONTENT)), 2);
                        if (fragments != null && fragments.length > 0) {
                            snippet = String.join(" ... ", fragments);
                        }
                    } catch (Exception ignore) {
                        // 高亮失败时退回片段截取
                    }
                    if (snippet == null || snippet.isBlank()) {
                        String content = nvl(doc.get(FIELD_CONTENT));
                        snippet = content.length() > 120 ? content.substring(0, 120) : content;
                    }
                    hits.add(new KnowledgeHit(
                            Long.parseLong(doc.get(FIELD_ID)),
                            doc.get(FIELD_TITLE),
                            doc.get(FIELD_CATEGORY),
                            snippet,
                            sd.score));
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            log.error("知识库检索失败: {}", e.getMessage(), e);
        }
        return hits;
    }

    /**
     * 构造检索查询：title 加权，IK 词法分析普通词组（fallback 为标准分词器解析）
     */
    private Query buildQuery(String keyword) throws Exception {
        try {
            MultiFieldQueryParser parser = new MultiFieldQueryParser(
                    new String[]{FIELD_TITLE, FIELD_CONTENT}, analyzer);
            parser.setDefaultOperator(MultiFieldQueryParser.Operator.OR);
            Query q = parser.parse(escape(keyword));
            if (q != null) {
                return q;
            }
        } catch (Exception e) {
            log.debug("IK 查询解析失败，退回标准分词: {}", e.getMessage());
        }
        MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new String[]{FIELD_TITLE, FIELD_CONTENT}, fallbackAnalyzer);
        parser.setDefaultOperator(MultiFieldQueryParser.Operator.OR);
        return parser.parse(escape(keyword));
    }

    /**
     * 转义 QueryParser 保留字符，按空格拆分的所有词之间为 OR 关系
     */
    private String escape(String keyword) {
        StringBuilder sb = new StringBuilder();
        for (String token : keyword.trim().split("\\s+")) {
            StringBuilder escaped = new StringBuilder();
            for (char c : token.toCharArray()) {
                if ("+-=&|<>!(){}[]^\"~*?:\\".indexOf(c) >= 0) {
                    escaped.append('\\');
                }
                escaped.append(c);
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(escaped);
        }
        return sb.toString();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    @PreDestroy
    public void close() throws IOException {
        // IKAnalyzer 实现 close()
        analyzer.close();
        fallbackAnalyzer.close();
    }

    /**
     * 待全量重建的文档 DTO
     */
    public record IndexedDoc(Long id, String title, String category, String content) {
    }
}
