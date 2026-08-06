package cc.shiyi.dashboard.advisor;

import cc.shiyi.dashboard.dto.a2a.AgentSkill;
import cc.shiyi.dashboard.dto.a2a.Message;
import cc.shiyi.dashboard.dto.a2a.Task;

import java.util.List;

/**
 * SolverProvider
 * 求解器提供者抽象接口，为 A2A Server 提供统一的求解器扩展点。
 *
 * <p>当前实现：{@link cc.shiyi.dashboard.advisor.MpiSolverProvider}（MPI Job 求解器）。
 * 后续新增其他求解器（如 CFD、结构力学、电磁仿真等）只需实现此接口并注册为 Spring Bean，
 * A2A Server 会自动发现并聚合所有 SolverProvider 的技能。</p>
 *
 * <p>扩展示例：</p>
 * <pre>{@code
 * @Service
 * public class CfdSolverProvider implements SolverProvider {
 *     public String getSolverType() { return "cfd"; }
 *     public List<AgentSkill> getSkills() { ... }
 *     public Task processMessage(Message message, String contextId) { ... }
 *     public Task getTask(String taskId) { ... }
 *     public Task cancelTask(String taskId) { ... }
 * }
 * }</pre>
 */
public interface SolverProvider {

    /**
     * 求解器类型标识，用于技能路由与日志追踪。
     *
     * @return 求解器类型字符串，如 "mpi"、"cfd"
     */
    String getSolverType();

    /**
     * 返回该求解器在 A2A 网络中暴露的技能列表。
     * A2A Server 会聚合所有 SolverProvider 的技能到 AgentCard 中。
     *
     * @return 技能列表
     */
    List<AgentSkill> getSkills();

    /**
     * 处理 A2A 消息，执行求解逻辑并返回任务结果。
     *
     * @param message  A2A 消息（含用户输入）
     * @param contextId 会话上下文 ID
     * @return 任务对象（含状态与产物）
     */
    Task processMessage(Message message, String contextId);

    /**
     * 查询任务状态。
     *
     * @param taskId 任务 ID（对应内部 Job ID）
     * @return 任务对象
     */
    Task getTask(String taskId);

    /**
     * 取消任务。
     *
     * @param taskId 任务 ID
     * @return 取消后的任务对象
     */
    Task cancelTask(String taskId);
}
