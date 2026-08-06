package cc.shiyi.coleditor.user.table;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName(value = "system_user")
public class User {
    @TableField(value = "id", fill = FieldFill.INSERT)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "account")
    private String account;

    @TableField(value = "password")
    private String password;

    @TableField(value = "email")
    private String email;

    @TableField(value = "phone")
    private String phone;

    @TableField(value = "avatar")
    private String avatar;

    @TableField(value = "role")
    private String role;

    @TableField(value = "permission")
    private String permission;

    @TableField(value = "status")
    private String status;

}
