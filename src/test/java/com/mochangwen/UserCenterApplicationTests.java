package com.mochangwen;

import com.mochangwen.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserCenterApplicationTests {

    @Autowired
    private UserService userService;
    @Test
    void contextLoads() {
    }

}
