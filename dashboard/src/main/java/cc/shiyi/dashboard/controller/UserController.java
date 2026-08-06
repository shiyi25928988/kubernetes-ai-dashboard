package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.coleditor.user.dto.UserInfo;
import cc.shiyi.coleditor.user.dto.UserRegisterInfo;
import cc.shiyi.coleditor.user.service.UserService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Setter(onMethod_ = @Autowired)
@Tag(name = "用户管理-UserController")
public class UserController {

    UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ResponseWrapper<Void> registerUser(@RequestBody UserRegisterInfo userRegisterInfo) {
        try {
            userService.registerUser(userRegisterInfo);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
        return new ResponseWrapper().success("注册成功！");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ResponseWrapper<UserInfo> login(String account, String password) throws Exception {
        try{
            return new ResponseWrapper().success(userService.login(account, password));
        }catch(Exception e){
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ResponseWrapper<Void> logout() throws Exception {
        userService.logout();
        return new ResponseWrapper().success();
    }

    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public ResponseWrapper<Void> updateUserInfo(@RequestBody UserInfo userInfo) throws Exception {
        userService.updateUserInfo(userInfo);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "更新用户密码")
    @PostMapping("/updatePassword")
    public ResponseWrapper<Void> updatePassword(String oldPassword, String newPassword) throws Exception {
        userService.updatePassword(oldPassword, newPassword);
        return new ResponseWrapper().success();
    }

    @Operation(summary = "获取当前登陆人信息")
    @GetMapping("/getCurrentUserInfo")
    public ResponseWrapper<UserInfo> getCurrentUserInfo() throws Exception {
        return new ResponseWrapper().success(userService.getUserInfoById(StpUtil.getLoginIdAsLong()));
    }
}
