package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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


    @Select("select count(1) from user where create_time > #{begin} and create_time < #{end}")
    Integer selectNewUserCountByCreateTime(@Param("begin") LocalDateTime localDateTimeBegin,
                                        @Param("end") LocalDateTime localDateTimeEnd);


    @Select("select count(1) from user where create_time < #{date} ")
    Integer selectBeforeUserCountByCreateTime(@Param("date") LocalDateTime localDateTimeEnd);
}
