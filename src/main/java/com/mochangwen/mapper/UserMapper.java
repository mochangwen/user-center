package com.mochangwen.mapper;

import com.mochangwen.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 莫昌文
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-12-29 15:21:59
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




