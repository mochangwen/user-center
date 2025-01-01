package com.mochangwen;

import com.mochangwen.model.domain.User;
import com.mochangwen.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
class UserCenterApplicationTests {

    @Autowired
    private UserService userService;
    @Test
    void contextLoads() {
        List<String> tagNameList = new ArrayList<>();
        Collections.addAll(tagNameList, "java", "python");
        List<User> users = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(users, "The list of users should not be null");
    }

}
