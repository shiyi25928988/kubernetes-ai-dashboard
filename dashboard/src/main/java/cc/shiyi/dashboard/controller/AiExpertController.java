package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.AiExpert;
import cc.shiyi.dashboard.service.AiExpertService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/expert")
@Setter(onMethod_ = @Autowired)
@Tag(name = "AI专家管理-AiExpertController")
public class AiExpertController {

    private AiExpertService aiExpertService;

    @Operation(summary = "获取已启用的专家列表（系统默认 + 用户自定义）")
    @GetMapping("/list")
    public ResponseWrapper<List<AiExpert>> getEnabledExperts() {
        long userId = StpUtil.getLoginIdAsLong();
        return new ResponseWrapper<>().success(aiExpertService.getEnabledExperts(userId));
    }

    @Operation(summary = "创建自定义专家")
    @PostMapping("/create")
    public ResponseWrapper<AiExpert> createExpert(@RequestBody AiExpert expert) {
        long userId = StpUtil.getLoginIdAsLong();
        return new ResponseWrapper<>().success(aiExpertService.createExpert(expert, userId));
    }

    @Operation(summary = "更新自定义专家")
    @PostMapping("/update/{id}")
    public ResponseWrapper<Boolean> updateExpert(@PathVariable Long id, @RequestBody AiExpert expert) {
        long userId = StpUtil.getLoginIdAsLong();
        return new ResponseWrapper<>().success(aiExpertService.updateExpert(id, expert, userId));
    }

    @Operation(summary = "删除自定义专家")
    @PostMapping("/delete/{id}")
    public ResponseWrapper<Boolean> deleteExpert(@PathVariable Long id) {
        long userId = StpUtil.getLoginIdAsLong();
        return new ResponseWrapper<>().success(aiExpertService.deleteExpert(id, userId));
    }
}