package cc.shiyi.coleditor.user.converter;

import cc.shiyi.coleditor.user.dto.UserInfo;
import cc.shiyi.coleditor.user.dto.UserRegisterInfo;
import cc.shiyi.coleditor.user.table.User;

import java.util.Objects;

public final class UserConverter {

    public static UserInfo toUserInfo(User user){
        if(Objects.isNull(user)) {
            return new UserInfo();
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setAccount(user.getAccount());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRole(user.getRole());
        userInfo.setPermission(user.getPermission());
        userInfo.setStatus(user.getStatus());
        return userInfo;
    }

    public User toUser(UserRegisterInfo userRegisterInfo){
        if(Objects.isNull(userRegisterInfo)){
            return new User();
        }
        User user = new User();
        user.setUsername(userRegisterInfo.getUsername());
        user.setAccount(userRegisterInfo.getAccount());
        user.setPassword(userRegisterInfo.getPassword());
        user.setEmail(userRegisterInfo.getEmail());
        user.setPhone(userRegisterInfo.getPhone());
        user.setAvatar(userRegisterInfo.getAvatar());
        user.setRole("user");
        user.setPermission("");
        user.setStatus("normal");
        return user;
    }

}
