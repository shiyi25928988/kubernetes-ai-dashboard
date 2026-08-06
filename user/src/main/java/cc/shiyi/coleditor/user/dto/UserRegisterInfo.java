package cc.shiyi.coleditor.user.dto;

import lombok.Data;

@Data
public class UserRegisterInfo {
    private String username;
    private String account;
    private String password;
    private String email;
    private String phone;
    private String avatar;
}
