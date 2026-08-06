package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.PresetPrompt;
import cc.shiyi.dashboard.service.PresetPromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/preset")
@Setter(onMethod_ = @Autowired)
@Tag(name = "预设指令管理-PresetPromptController")
public class PresetPromptController {

    private PresetPromptService presetPromptService;

    @Operation(summary = "获取所有启用的预设指令")
    @GetMapping("/list")
    public ResponseWrapper<List<PresetPrompt>> getEnabledPresets() {
        return new ResponseWrapper<>().success(presetPromptService.getEnabledPresets());
    }

    @Operation(summary = "按分类获取预设指令")
    @GetMapping("/list/{category}")
    public ResponseWrapper<List<PresetPrompt>> getPresetsByCategory(@PathVariable String category) {
        return new ResponseWrapper<>().success(presetPromptService.getPresetsByCategory(category));
    }

    @Operation(summary = "创建预设指令")
    @PostMapping("/create")
    public ResponseWrapper<PresetPrompt> createPreset(@RequestBody PresetPrompt preset) {
        return new ResponseWrapper<>().success(presetPromptService.createPreset(preset));
    }

    @Operation(summary = "更新预设指令")
    @PostMapping("/update/{id}")
    public ResponseWrapper<Boolean> updatePreset(@PathVariable Long id, @RequestBody PresetPrompt preset) {
        return new ResponseWrapper<>().success(presetPromptService.updatePreset(id, preset));
    }

    @Operation(summary = "删除预设指令")
    @PostMapping("/delete/{id}")
    public ResponseWrapper<Boolean> deletePreset(@PathVariable Long id) {
        return new ResponseWrapper<>().success(presetPromptService.deletePreset(id));
    }
}
