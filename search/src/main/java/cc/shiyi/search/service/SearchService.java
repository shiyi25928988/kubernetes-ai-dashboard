package cc.shiyi.search.service;

import cc.shiyi.search.model.IndexedDocument;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    @Value("${lucene.storage.path}")
    String storagePath;

    public synchronized void loadAllParagraphs(){
//        QueryWrapper<Paragraph> queryWrapper = new QueryWrapper<>();
//        queryWrapper.ne("is_deleted", 1);
//        paragraphMapper.selectList(queryWrapper).forEach(paragraph -> {
//            try (Indexer indexer = new Indexer(storagePath)) {
//                IndexedDocument indexedDocument = new IndexedDocument();
//                indexedDocument.setDocId(paragraph.getDocId());
//                indexedDocument.setParagraphId(paragraph.getId());
//                indexedDocument.setParagraphTitle(paragraph.getTitle());
//                indexedDocument.setContent(paragraph.getContent());
//                indexedDocument.setCreatedTime(paragraph.getCreatedTime());
//                indexer.addDocument(indexedDocument);
//            }catch (Exception e) {
//                e.printStackTrace();
//            }
//        });

    }

    public synchronized void addDocuments(List<IndexedDocument> indexedDocuments) {
        try (Indexer indexer = new Indexer(storagePath)) {
            for (IndexedDocument indexedDocument : indexedDocuments) {
                indexer.addDocument(indexedDocument);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<IndexedDocument> fuzzySearch(String query, int pageNum, int pageSize) {
        try (Querier querier = new Querier(storagePath)) {
            return querier.fuzzySearch(query, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void clearAll(){
        try (Indexer indexer = new Indexer(storagePath)) {
            indexer.clearAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
