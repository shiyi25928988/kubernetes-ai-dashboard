package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.KnowledgeDoc;
import cc.shiyi.dashboard.service.KnowledgeDocService;
import cc.shiyi.dashboard.service.KnowledgeIndexService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理接口
 */
@RestController
@RequestMapping("/api/v1/knowledge")
@Setter(onMethod_ = @Autowired)
@Tag(name = "知识库管理-KnowledgeDocController")
public class KnowledgeDocController {

    private KnowledgeDocService knowledgeDocService;
    private KnowledgeIndexService knowledgeIndexService;

    @Operation(summary = "分页查询文档（keyword 模糊标题/内容，category 分类过滤）")
    @GetMapping("/docs")
    public ResponseWrapper<Map<String, Object>> listDocs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return new ResponseWrapper<>().success(knowledgeDocService.listDocs(page, pageSize, keyword, category));
    }

    @Operation(summary = "文档详情")
    @GetMapping("/docs/{id}")
    public ResponseWrapper<KnowledgeDoc> getDoc(@PathVariable Long id) {
        KnowledgeDoc doc = knowledgeDocService.getById(id);
        return new ResponseWrapper<>().success(doc);
    }

    @Operation(summary = "全部分类列表（去重）")
    @GetMapping("/categories")
    public ResponseWrapper<List<String>> listCategories() {
        return new ResponseWrapper<>().success(knowledgeDocService.listCategories());
    }

    @Operation(summary = "全文检索（Lucene+IK，返回高亮片段）")
    @GetMapping("/search")
    public ResponseWrapper<List<KnowledgeIndexService.KnowledgeHit>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        return new ResponseWrapper<>().success(knowledgeIndexService.search(keyword, limit));
    }

    @Operation(summary = "创建文档")
    @PostMapping("/docs")
    public ResponseWrapper<KnowledgeDoc> createDoc(@RequestBody KnowledgeDoc doc) {
        Long userId = StpUtil.getLoginIdAsLong();
        return new ResponseWrapper<>().success(knowledgeDocService.createDoc(doc, userId));
    }

    @Operation(summary = "更新文档")
    @PutMapping("/docs")
    public ResponseWrapper<Boolean> updateDoc(@RequestBody KnowledgeDoc doc) {
        return new ResponseWrapper<>().success(knowledgeDocService.updateDoc(doc));
    }

    @Operation(summary = "删除文档（逻辑删除）")
    @PostMapping("/docs/{id}/delete")
    public ResponseWrapper<Boolean> deleteDoc(@PathVariable Long id) {
        return new ResponseWrapper<>().success(knowledgeDocService.deleteDoc(id));
    }
}
