package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.table.PresetPrompt;
import cc.shiyi.dashboard.db.mapper.PresetPromptMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 预设指令服务实现类
 */
@Service
public class PresetPromptService extends ServiceImpl<PresetPromptMapper, PresetPrompt> {

    /**
     * 初始化系统预设指令
     */
    @PostConstruct
    public void initSystemPresets() {
        // 检查是否已存在系统预设
        LambdaQueryWrapper<PresetPrompt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PresetPrompt::getIsSystem, true);
        long count = count(queryWrapper);

        if (count == 0) {
            // 创建默认的系统预设
            createSystemPreset("代码助手", "帮助编写、优化和解释代码", "💻",
                "请帮我完成以下代码相关的任务：", "代码", 1);
            createSystemPreset("K8S专家", "提供Kubernetes相关的帮助和建议", "☸️",
                "我需要关于Kubernetes的帮助，请帮我：", "K8S", 2);
            createSystemPreset("MPI优化", "优化MPI作业的性能和配置", "⚡",
                "请帮我优化以下MPI作业：", "MPI", 3);
            createSystemPreset("写作助手", "帮助撰写和润色文档、邮件等", "✍️",
                "请帮我撰写或润色以下内容：", "写作", 4);
            createSystemPreset("问题排查", "帮助排查和分析系统问题", "🔍",
                "我遇到了以下问题，请帮我排查：", "排查", 5);
            createSystemPreset("翻译助手", "提供多语言翻译服务", "🌐",
                "请帮我翻译以下内容：", "翻译", 6);
        }
    }

    private void createSystemPreset(String name, String description, String icon,
                                   String promptContent, String category, int sortOrder) {
        PresetPrompt preset = new PresetPrompt();
        preset.setName(name);
        preset.setDescription(description);
        preset.setIcon(icon);
        preset.setPromptContent(promptContent);
        preset.setCategory(category);
        preset.setSortOrder(sortOrder);
        preset.setIsEnabled(true);
        preset.setIsSystem(true);
        preset.setCreatedAt(new Date());
        preset.setUpdatedAt(new Date());
        save(preset);
    }

    /**
     * 获取所有启用的预设指令
     */
    public List<PresetPrompt> getEnabledPresets() {
        LambdaQueryWrapper<PresetPrompt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PresetPrompt::getIsEnabled, true)
                .orderByAsc(PresetPrompt::getSortOrder);
        return list(queryWrapper);
    }

    /**
     * 按分类获取预设指令
     */
    public List<PresetPrompt> getPresetsByCategory(String category) {
        LambdaQueryWrapper<PresetPrompt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PresetPrompt::getIsEnabled, true)
                .eq(PresetPrompt::getCategory, category)
                .orderByAsc(PresetPrompt::getSortOrder);
        return list(queryWrapper);
    }

    /**
     * 创建新的预设指令
     */
    public PresetPrompt createPreset(PresetPrompt preset) {
        preset.setIsEnabled(true);
        preset.setIsSystem(false);
        preset.setCreatedAt(new Date());
        preset.setUpdatedAt(new Date());
        save(preset);
        return preset;
    }

    /**
     * 更新预设指令
     */
 public boolean updatePreset(Long id, PresetPrompt preset) {
        preset.setId(id);
        preset.setUpdatedAt(new Date());
        return updateById(preset);
    }

    /**
     * 删除预设指令（只能删除非系统预设）
     */
    public boolean deletePreset(Long id) {
        PresetPrompt preset = getById(id);
        if (preset != null && !preset.getIsSystem()) {
            return removeById(id);
        }
        return false;
    }
}
