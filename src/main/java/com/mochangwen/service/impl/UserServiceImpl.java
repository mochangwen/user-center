package com.mochangwen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mochangwen.common.ErrorCode;
import com.mochangwen.exception.BusinessException;
import com.mochangwen.model.domain.User;
import com.mochangwen.service.UserService;
import com.mochangwen.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;


import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.mochangwen.constant.UserConstant.ADMIN_ROLE;
import static com.mochangwen.constant.UserConstant.userLoginStatus;

/**
* @author 莫昌文
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-12-29 15:21:59
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{


    private static final String ACCOUNT_REGEX = "^[a-zA-Z0-9_]{6,20}$";
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     *
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        //1.校验参数
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入");
        }
        //2.校验账号是否合法
        if(!isValidAccount(userAccount)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号格式为6-20位字母数字下划线");
        }
        //.校验 planetCode 是否合法
        if (planetCode.length()>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号格式为5位");
        }
        //3.校验密码是否合法
        if(userPassword.length()<8 || checkPassword.length()<8 || !userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确密码");
        }
        //4.查询数据库，判断账号是否重复
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount,userAccount);
        User user = getOne(wrapper);
        if(user!=null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        //5.planetCode不能重复
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPlanetCode,planetCode);
        user = getOne(wrapper);
        if(user!=null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号已存在");
        }
        //6.插入数据
        userPassword = DigestUtils.md5DigestAsHex(userPassword.getBytes());
        user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(userPassword);
        user.setPlanetCode(planetCode);
        boolean save = save(user);
        if(!save){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 密码
     * @param request 请求
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验参数
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入账号密码");
        }
        //2.校验账号是否合法
        if(!isValidAccount(userAccount)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号格式为6-20位字母数字下划线");
        }
        //3.查询数据
        userPassword = DigestUtils.md5DigestAsHex(userPassword.getBytes());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserAccount,userAccount);
        wrapper.eq(User::getUserPassword,userPassword);
        User user = getOne(wrapper);
        if (user==null){
            log.info("用户不存在");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        //4.脱敏返回数据
        user = getSafeUser(user);
        request.getSession().setAttribute(userLoginStatus,user);
        return user;
    }

    /**
     * 根据标签查询用户
     * @param tagNameList 标签列表
     * @return
     */

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        //1.判断参数是否为空
        if (CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.查询数据库
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        tagNameList.forEach(tagName -> {
            wrapper.like(User::getTags,tagName);
        });
        List<User> userList = list(wrapper);
        return userList.stream().map(user -> getSafeUser(user)).collect(Collectors.toList());

//        //在内存中查寻
//        List<User> userList = this.list();
//        Gson gson = new Gson();
//        //2.判断内存中是否包含要求的标签
//        return userList.stream().filter(user -> {
//            String tagstr = user.getTags();
//            if (StringUtils.isBlank(tagstr)){
//                return false;
//            }
//            Set<String> tempTagNameSet =  gson.fromJson(tagstr,new TypeToken<Set<String>>(){}.getType());
//            for (String tagName : tagNameList){
//                if (!tempTagNameSet.contains(tagName)){
//                    return false;
//                }
//            }
//            return true;
//        }).map(this::getSafeUser).collect(Collectors.toList());
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request==null){
            return null;
        }
        User user = (User) request.getSession().getAttribute(userLoginStatus);
        if (user == null){
            log.info("用户未登录");
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        return getSafeUser(user);
    }

    /**
     * 更新用户信息
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的id");
        }
        if (!isAdmin(loginUser)&& !userId.equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }
        User OlderUser = this.getById(userId);
        if (OlderUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        //修改用户
        int result = userMapper.updateById(user);
        if (result<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改失败");
        }
        return 1;
    }

    @Override
    public User getSafeUser(User user) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setUsername(user.getUsername());
        safeUser.setUserAccount(user.getUserAccount());
        safeUser.setAvatarUrl(user.getAvatarUrl());
        safeUser.setGender(user.getGender());
        safeUser.setPhone(user.getPhone());
        safeUser.setEmail(user.getEmail());
        safeUser.setUserStatus(user.getUserStatus());
        safeUser.setCreateTime(user.getCreateTime());
        safeUser.setUpdateTime(user.getUpdateTime());
        safeUser.setUserRole(user.getUserRole());
        safeUser.setPlanetCode(user.getPlanetCode());
        safeUser.setTags(user.getTags());
        return safeUser;
    }

    public static Boolean isValidAccount(String account) {
        return account != null && account.matches(ACCOUNT_REGEX);
    }

    /**
     * 判断是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(userLoginStatus);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断登录用户是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }
}




