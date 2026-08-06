package cc.shiyi.dashboard.dto.a2a;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TaskState
 * A2A 协议定义的任务状态枚举。
 *
 * <p>A2A 1.0 线上枚举值为 {@code TASK_STATE_*} 前缀（见 {@code @JsonProperty}），
 * Java 常量名保持简短以方便代码引用；序列化/反序列化均使用 1.0 线名。</p>
 */
public enum TaskState {

    /**
     * 未指定/未知
     */
    @JsonProperty("TASK_STATE_UNSPECIFIED")
    UNSPECIFIED,

    /**
     * 任务已提交
     */
    @JsonProperty("TASK_STATE_SUBMITTED")
    SUBMITTED,

    /**
     * 任务执行中
     */
    @JsonProperty("TASK_STATE_WORKING")
    WORKING,

    /**
     * A2A 1.0：任务正在运行（WORKING 的细化，可选使用）
     */
    @JsonProperty("TASK_STATE_RUNNING")
    RUNNING,

    /**
     * 需要额外输入
     */
    @JsonProperty("TASK_STATE_INPUT_REQUIRED")
    INPUT_REQUIRED,

    /**
     * 任务已完成
     */
    @JsonProperty("TASK_STATE_COMPLETED")
    COMPLETED,

    /**
     * 任务已取消
     */
    @JsonProperty("TASK_STATE_CANCELED")
    CANCELED,

    /**
     * 任务失败
     */
    @JsonProperty("TASK_STATE_FAILED")
    FAILED,

    /**
     * A2A 1.0：任务被拒绝
     */
    @JsonProperty("TASK_STATE_REJECTED")
    REJECTED,

    /**
     * A2A 1.0：需要认证
     */
    @JsonProperty("TASK_STATE_AUTH_REQUIRED")
    AUTH_REQUIRED,

    /**
     * 未知状态（兼容旧值，映射到 UNSPECIFIED 的语义）
     */
    @JsonProperty("TASK_STATE_UNKNOWN")
    UNKNOWN
}
