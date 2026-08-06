package cc.shiyi.coleditor.user.service;

import cc.shiyi.coleditor.user.mapper.UserMapper;
import cc.shiyi.coleditor.user.table.User;
import cn.dev33.satoken.stp.StpInterface;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter(onMethod_ = @Autowired)
public class StpInterfaceImpl implements StpInterface {

    UserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        User user = userMapper.selectById(userId);
        return List.of(user.getPermission());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        User user = userMapper.selectById(userId);
        return List.of(user.getRole());
    }
}
