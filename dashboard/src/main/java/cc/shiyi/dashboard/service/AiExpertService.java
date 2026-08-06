package cc.shiyi.dashboard.service;

import cc.shiyi.dashboard.db.table.AiExpert;
import cc.shiyi.dashboard.db.mapper.AiExpertMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * AI专家服务实现类
 */
@Service
public class AiExpertService extends ServiceImpl<AiExpertMapper, AiExpert> {

    /**
     * 初始化系统默认专家
     */
    @PostConstruct
    public void initSystemExperts() {
        LambdaQueryWrapper<AiExpert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiExpert::getIsSystem, true);
        long count = count(queryWrapper);

        if (count == 0) {
            createSystemExpert("K8S 专家", "Kubernetes 集群管理与运维专家", "☸️",
                    "你是一位资深的 Kubernetes 运维与架构专家，精通 K8s 集群管理、资源调度、故障排查与性能优化。你可以使用 Kubernetes 工具进行各种集群操作。请根据用户的问题提供专业、准确的解答和操作指导。",
                    1);
        }
    }

    private void createSystemExpert(String name, String description, String icon,
                                    String systemPrompt, int sortOrder) {
        AiExpert expert = new AiExpert();
        expert.setName(name);
        expert.setDescription(description);
        expert.setIcon(icon);
        expert.setSystemPrompt(systemPrompt);
        expert.setSortOrder(sortOrder);
        expert.setIsEnabled(true);
        expert.setIsSystem(true);
        expert.setIsDeleted(false);
        expert.setCreatedAt(new Date());
        expert.setUpdatedAt(new Date());
        save(expert);
    }

    /**
     * 获取用户可见的已启用专家（系统默认 + 用户自定义）
     */
    public List<AiExpert> getEnabledExperts(Long userId) {
        LambdaQueryWrapper<AiExpert> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiExpert::getIsEnabled, true)
                .eq(AiExpert::getIsDeleted, false)
                .and(w -> w.isNull(AiExpert::getUserId)
                        .or(eq -> eq.eq(AiExpert::getUserId, userId)))
                .orderByAsc(AiExpert::getSortOrder)
                .orderByAsc(AiExpert::getId);
        return list(queryWrapper);
    }

    /**
     * 创建自定义专家
     */
    public AiExpert createExpert(AiExpert expert, Long userId) {
        expert.setUserId(userId);
        expert.setIsEnabled(true);
        expert.setIsSystem(false);
        expert.setIsDeleted(false);
        expert.setCreatedAt(new Date());
        expert.setUpdatedAt(new Date());
        save(expert);
        return expert;
    }

    /**
     * 更新自定义专家（仅限自己的）
     */
    public boolean updateExpert(Long id, AiExpert expert, Long userId) {
        AiExpert existing = getById(id);
        if (existing == null || existing.getIsSystem() || !userId.equals(existing.getUserId())) {
            return false;
        }
        // 强制保护不可修改字段，避免恶意篡改
        expert.setId(id);
        expert.setUserId(existing.getUserId());
        expert.setIsSystem(existing.getIsSystem());
        expert.setIsDeleted(existing.getIsDeleted());
        expert.setCreatedAt(existing.getCreatedAt());
        expert.setUpdatedAt(new Date());
        return updateById(expert);
    }

    /**
     * 删除自定义专家（软删除，仅限自己的）
     */
    public boolean deleteExpert(Long id, Long userId) {
        AiExpert existing = getById(id);
        if (existing == null || existing.getIsSystem() || !userId.equals(existing.getUserId())) {
            return false;
        }
        existing.setIsDeleted(true);
        existing.setUpdatedAt(new Date());
        return updateById(existing);
    }
}