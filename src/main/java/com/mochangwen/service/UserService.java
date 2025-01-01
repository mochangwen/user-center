package com.mochangwen.service;

import com.mochangwen.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 莫昌文
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-12-29 15:21:59
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */

    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 用户登录
     */

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前脱敏用户
     * @param user
     * @return
     */
    User getSafeUser(User user);

    /**
     * 根据标签查询用户
     */
    List<User> searchUsersByTags(List<String> tagNameList);
}
