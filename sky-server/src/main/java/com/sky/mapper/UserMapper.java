package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/10 20:41
 */
@Mapper
public interface UserMapper {


    @Select("select * from user where openid = #{openId}")
    User selectByOpenId(String openId);

    Integer insert(User user);


    @Select("select * from user where id = #{id}")
    User selectById(Long id);

}
