package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.mapper.KnowledgeDocMapper;
import cc.shiyi.dashboard.db.table.KnowledgeDoc;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库文档服务：MySQL 持久化 + Lucene 全文索引维护
 */
@Service
@Setter(onMethod_ = @Autowired)
public class KnowledgeDocService extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> {

    private KnowledgeIndexService knowledgeIndexService;

    /**
     * 启动时如果索引为空则按库全量重建（防止索引目录被误删后检索失联）
     */
    @PostConstruct
    public void bootstrapIndex() {
        new Thread(() -> {
            try {
                if (knowledgeIndexService.isIndexEmpty()) {
                    reindexAll();
                }
            } catch (Exception e) {
                // 依赖注入处于早期阶段时可能为空，忽略（下次保存文档时会再触发）
                org.slf4j.LoggerFactory.getLogger(KnowledgeDocService.class)
                        .warn("知识库索引启动重建未执行: {}", e.getMessage());
            }
        }, "knowledge-index-bootstrap").start();
    }

    /**
     * 从数据库全量重建索引
     */
    public void reindexAll() {
        List<KnowledgeIndexService.IndexedDoc> docs = list().stream()
                .map(d -> new KnowledgeIndexService.IndexedDoc(d.getId(), d.getTitle(), d.getCategory(), d.getContent()))
                .collect(Collectors.toList());
        knowledgeIndexService.reindexAll(docs);
    }

    /**
     * 分页查询（title/content 模糊关键字 + 分类过滤）。
     * 未配置 MyBatis-Plus 分页拦截器，手动 LIMIT 分页。
     */
    public Map<String, Object> listDocs(long pageNum, long pageSize, String keyword, String category) {
        LambdaQueryWrapper<KnowledgeDoc> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeDoc::getIsDeleted, false);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(KnowledgeDoc::getTitle, keyword).or().like(KnowledgeDoc::getContent, keyword));
        }
        if (StringUtils.hasText(category)) {
            qw.eq(KnowledgeDoc::getCategory, category);
        }
        long total = count(qw);
        long current = Math.max(pageNum, 1);
        long size = Math.min(Math.max(pageSize, 1), 100);
        qw.orderByDesc(KnowledgeDoc::getUpdatedAt)
                .last("LIMIT " + (current - 1) * size + ", " + size);
        List<KnowledgeDoc> records = list(qw);
        return Map.of(
                "records", records,
                "total", total,
                "page", current,
                "pageSize", size);
    }

    /**
     * 全部分类（去重）
     */
    public List<String> listCategories() {
        LambdaQueryWrapper<KnowledgeDoc> qw = new LambdaQueryWrapper<>();
        qw.eq(KnowledgeDoc::getIsDeleted, false)
                .select(KnowledgeDoc::getCategory)
                .isNotNull(KnowledgeDoc::getCategory)
                .ne(KnowledgeDoc::getCategory, "");
        return list(qw).stream()
                .map(KnowledgeDoc::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 创建文档并建索引
     */
    public KnowledgeDoc createDoc(KnowledgeDoc doc, Long userId) {
        doc.setId(null);
        doc.setCreatorId(userId);
        doc.setCreatedAt(new Date());
        doc.setUpdatedAt(new Date());
        if (doc.getIsDeleted() == null) {
            doc.setIsDeleted(false);
        }
        save(doc);
        knowledgeIndexService.indexDoc(doc.getId(), doc.getTitle(), doc.getCategory(), doc.getContent());
        return doc;
    }

    /**
     * 更新文档并重建索引
     */
    public boolean updateDoc(KnowledgeDoc doc) {
        KnowledgeDoc existing = getById(doc.getId());
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            return false;
        }
        doc.setUpdatedAt(new Date());
        boolean ok = updateById(doc);
        if (ok) {
            if (!StringUtils.hasText(doc.getTitle()) || !StringUtils.hasText(doc.getContent())) {
                // 未全量提交字段时按库里的完整数据重建索引
                KnowledgeDoc fresh = getById(doc.getId());
                knowledgeIndexService.indexDoc(fresh.getId(), fresh.getTitle(), fresh.getCategory(), fresh.getContent());
            } else {
                knowledgeIndexService.indexDoc(doc.getId(), doc.getTitle(), doc.getCategory(), doc.getContent());
            }
        }
        return ok;
    }

    /**
     * 删除文档（逻辑删除 + 移除索引）
     */
    public boolean deleteDoc(Long id) {
        KnowledgeDoc existing = getById(id);
        if (existing == null || Boolean.TRUE.equals(existing.getIsDeleted())) {
            return false;
        }
        existing.setIsDeleted(true);
        existing.setUpdatedAt(new Date());
        boolean ok = updateById(existing);
        if (ok) {
            knowledgeIndexService.removeDoc(id);
        }
        return ok;
    }
}
