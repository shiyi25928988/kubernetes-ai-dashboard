package cc.shiyi.dashboard.db.table;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 镜像仓库源配置（页面管理，存 MySQL）。
 * 支持多仓库源并存：docker (Docker Registry V2) / harbor (Harbor 1.x)。
 */
@Data
@TableName("registry_config")
public class RegistryConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 仓库源显示名，唯一 */
    private String name;

    /** 仓库类型：docker = Docker Registry V2；harbor = Harbor（1.x 老API + V2 manifest） */
    private String type;

    /** 仓库地址，如 http://10.113.16.240 */
    private String url;

    /** 认证用户名（docker 仓库源可为空） */
    private String username;

    /** 认证密码 */
    private String password;

    /** 备注说明 */
    private String remark;

    /** 创建用户 ID */
    private Long creatorId;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 最后更新时间 */
    private LocalDateTime updatedTime;

    /** 是否已设置密码（不落库，接口返回用；密码本身不回传） */
    @TableField(exist = false)
    private Boolean hasPassword;
}
