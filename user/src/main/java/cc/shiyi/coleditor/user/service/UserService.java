package cc.shiyi.coleditor.user.service;

import cc.shiyi.coleditor.user.converter.UserConverter;
import cc.shiyi.coleditor.user.dto.UserInfo;
import cc.shiyi.coleditor.user.dto.UserRegisterInfo;
import cc.shiyi.coleditor.user.mapper.UserMapper;
import cc.shiyi.coleditor.user.table.User;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.base.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Setter(onMethod_ = @Autowired)
public class UserService {

    UserMapper userMapper;

    public User getCurrentUser(){
        Long Id = StpUtil.getLoginIdAsLong();
        if(Objects.isNull(Id)){
            return null;
        }
        return userMapper.selectById(Id);
    }

    /**
     根据用户ID查询用户信息

     @param id 用户唯一标识ID
     @return 包含用户信息的UserInfo对象，若用户不存在则可能返回null */
    public UserInfo getUserInfoById(Long id) {
        User user = userMapper.selectById(id);
        return UserConverter.toUserInfo(user);
    }

    /**
     注册新用户

     @param userRegisterInfo 用户注册信息，包含账户、密码等必要字段
     @throws Exception 当用户注册信息为空时，抛出"user is null"异常；
     当账户已存在时，抛出"account is exist"异常；
     其他插入失败情况由底层异常向上抛出
     */
    public void registerUser(UserRegisterInfo userRegisterInfo) throws Exception {
        if (Objects.isNull(userRegisterInfo)) {
            throw new Exception("user is null");
        }
        if (isAccountExist(userRegisterInfo.getAccount())) {
            throw new Exception("account already exist");
        }
        User user = new User();
        user.setUsername(userRegisterInfo.getUsername());
        user.setAccount(userRegisterInfo.getAccount());
        user.setPassword(userRegisterInfo.getPassword());
        user.setEmail(userRegisterInfo.getEmail());
        user.setPhone(userRegisterInfo.getPhone());
        user.setAvatar(userRegisterInfo.getAvatar());
        userMapper.insert(user);
    }

    /**
     检查指定账号是否已存在

     @param account 待检查的账号字符串，不能为空或null
     @return 如果账号存在则返回true，否则返回false
     @throws Exception 当账号为空或null时抛出异常
     */
    private Boolean isAccountExist(String account) throws Exception {
        if (Strings.isNullOrEmpty(account)) {
            throw new Exception("account is null");
        }
        QueryWrapper<User>  queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        return Objects.nonNull(userMapper.selectOne(queryWrapper));
    }

    /**
     用户登录方法

     该方法用于处理用户登录逻辑，校验账号和密码的合法性，并在验证通过后执行登录操作。
     如果账号或密码为空，抛出异常提示信息。
     如果账号密码验证失败，抛出异常提示信息。
     验证通过后，通过账号获取用户ID并执行登录。

     @param account 用户账号，不能为空
     @param password 用户密码，不能为空
     @throws Exception 当账号或密码为空，或验证失败时抛出异常 */
    public UserInfo login(String account, String password) throws Exception {
        if(Strings.isNullOrEmpty(account) || Strings.isNullOrEmpty(password)){
            throw new Exception("用户名和密码不能为空！");
        }
        if(!validateAccountPassword(account, password)){
            throw new Exception("密码错误或者用户不存在！");
        }
        Long id = getIdByAccount(account);
        log.info("login id :{}", id);
        StpUtil.login(id);
        Long _id = StpUtil.getLoginIdAsLong();
        log.info("login _id :{}", _id);
        User user = userMapper.selectById(_id);
        UserInfo userInfo = UserConverter.toUserInfo(user);
        userInfo.setToken(StpUtil.getAnonTokenSession().getToken());
        return userInfo;
    }

    /**
     验证指定账户的密码是否正确。

     @param account 账户名，用于查找对应的密码信息
     @param password 待验证的密码
     @return 如果提供的密码与账户对应的密码一致，则返回 true；否则返回 false

     @note 该方法通过 getPasswordByAccount 方法获取账户对应的密码，并进行明文比较。
     注意：明文密码比较存在安全风险，建议使用安全的密码哈希比对方式。
     */
    private Boolean validateAccountPassword(String account, String password){
        String _password = getPasswordByAccount(account);
        if(Strings.isNullOrEmpty(_password)){
            return false;
        }
        return password.equals(_password);
    }

    /**
     根据账号查询对应的密码信息。

     @param account 用户账号，用于匹配数据库中的用户记录
     @return 返回对应账号的密码字符串；如果未找到用户，则可能返回 null */
    private String getPasswordByAccount(String account){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);

        User user = userMapper.selectOne(queryWrapper);
        if(Objects.nonNull(user)){
            return user.getPassword();
        } else {
            return null;
        }
    }

    /**
     根据用户账号查询并返回对应的用户ID。

     @param account 用户账号，用于匹配数据库中的账户字段
     @return 返回匹配到的用户ID，若无匹配记录则可能返回null */
    private Long getIdByAccount(String account){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", account);
        return userMapper.selectOne(queryWrapper).getId();
    }

    /**
     用户登出方法

     该方法调用StpUtil工具类的logout方法，实现当前用户的会话注销功能。
     执行后将清除用户的登录状态及相关会话信息。

     @throws Exception 可能抛出异常，具体取决于StpUtil.logout()的实现 */
    public void logout() throws Exception {
        StpUtil.logout();
    }

    /**
     更新当前登录用户的基本信息。

     @param userInfo 包含用户新信息的数据传输对象，包括头像、用户名、手机号和邮箱
     @throws Exception 当用户未登录或根据登录ID查询不到用户时抛出异常 */
    public void updateUserInfo(UserInfo userInfo) throws Exception {
        Long id = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new Exception("user is null");
        }
        if (!Strings.isNullOrEmpty(userInfo.getAvatar())) user.setAvatar(userInfo.getAvatar());
        if (!Strings.isNullOrEmpty(userInfo.getUsername())) user.setUsername(userInfo.getUsername());
        if (!Strings.isNullOrEmpty(userInfo.getPhone())) user.setPhone(userInfo.getPhone());
        if (!Strings.isNullOrEmpty(userInfo.getEmail())) user.setEmail(userInfo.getEmail());
        user.setId(id);
        userMapper.updateById(user);
    }

    public void updatePassword(String oldPassword, String newPassword) throws Exception {
        Long id = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(id);
        if (Objects.isNull(user)) {
            throw new Exception("user is null");
        }
        if (validateAccountPassword(user.getAccount(), oldPassword)) {
            user.setPassword(newPassword);
            userMapper.updateById(user);
        } else {
            throw new Exception("old password is not correct !");
        }
    }

    public List<User> searchUser(String userName){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("username", userName);
        return userMapper.selectList(queryWrapper);
    }

    //TODO: 删除用户
    public void deleteUser(Long id) throws Exception {}

    //TODO: 冻结用户
    public void suspendUser(Long id) throws Exception {}



}
