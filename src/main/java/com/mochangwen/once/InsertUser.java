package com.mochangwen.once;


import com.mochangwen.mapper.UserMapper;
import com.mochangwen.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class InsertUser {

    @Resource
    private UserMapper userMapper;

    /**
     * 循环插入用户
     */
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )

    //@Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doInsertUser() {
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假沙鱼");
            user.setUserAccount("yusha");
            user.setAvatarUrl("shanghai.myqcloud.com/shayu931/shayu.png");
            user.setProfile("一条咸鱼");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("shayu-yusha@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("931");
            user.setTags("[]");
            userMapper.insert(user);
        }
    }
}

