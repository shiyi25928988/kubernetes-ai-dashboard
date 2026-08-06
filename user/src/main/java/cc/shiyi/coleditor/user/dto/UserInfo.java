package cc.shiyi.coleditor.user.dto;

import lombok.Data;

@Data
public class UserInfo {
    private Long id;
    private String username;
    private String account;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private String permission;
    private String status;
    private String token;
}
