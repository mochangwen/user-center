package com.mochangwen.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochangwen.common.BaseResponse;
import com.mochangwen.common.ErrorCode;
import com.mochangwen.common.ResultUtils;
import com.mochangwen.exception.BusinessException;
import com.mochangwen.model.domain.User;
import com.mochangwen.model.dto.UserLoginRequest;
import com.mochangwen.model.dto.UserRegisterRequest;
import com.mochangwen.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.mochangwen.constant.UserConstant.ADMIN_ROLE;
import static com.mochangwen.constant.UserConstant.userLoginStatus;

@RestController
@RequestMapping("/user")
@Slf4j
//允许跨域
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@Api(tags = "用户接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 用户注册请求
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        // 判断账户密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)){
            throw new RuntimeException("请输入账号或密码或星球号");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        if (result<=0){
           throw new RuntimeException("注册失败");
        }
        return ResultUtils.success(result,"注册成功");
    }

    /**
     * 用户登录
     * @param userLoginRequest 用户登录请求
     */

    @PostMapping("/login")
    public BaseResponse<User> login(@RequestBody UserLoginRequest userLoginRequest , HttpServletRequest request){
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 判断账户密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR, "请输入账号或密码");
        }
        //return ResultUtils.success(userService.userLogin(userAccount, userPassword, request),"登录成功");
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user,"登录成功");
    }

    /**
     * 根据标签查询用户
     */
    @GetMapping("/search/tags")
    @ApiOperation(value = "根据标签查询用户")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        List<User> list = userList.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list,"查询成功");
    }
    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id, HttpServletRequest request){
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        if (id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的id");
        }
        return ResultUtils.success(userService.removeById(id),"删除成功");
    }

    /**
     * 查寻用户
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(@RequestParam(required = false) String username,HttpServletRequest request){
        //获取用户角色
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        log.info("searchUsers username:{}",username);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(username)){
            wrapper.like(User::getUsername,username);
        }
        List<User> list = userService.list(wrapper);
        List<User> userList = list.stream().map(user -> userService.getSafeUser(user)).collect(Collectors.toList());
        return ResultUtils.success(userList);
    }

    /**
     * 获取登录信息
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(userLoginStatus);
        User currentUser = (User) userObj;
        if (currentUser == null){
            log.error("user not login");
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        Long userId = currentUser.getId();
        User user = userService.getById(userId);
        if (user == null){
            log.error("user not exist");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        return ResultUtils.success(userService.getSafeUser(user));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<String> userLogout(HttpServletRequest request){
        request.getSession().removeAttribute(userLoginStatus);
        return ResultUtils.success("退出成功");
    }


    /**
     * 是否为管理员
     *
     */
    private Boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(userLoginStatus);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
