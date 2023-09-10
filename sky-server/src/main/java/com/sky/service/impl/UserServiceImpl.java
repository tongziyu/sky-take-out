package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @Author: Ian
 * @Date: 2023/9/10 18:23
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    // 微信服务接口地址
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;


    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 调用微信接口服务,获得当前微信用户的 openid
        String openId = getOpenId(userLoginDTO.getCode());
        //判断openid是否为空,如果为空 表示登录失败,则抛出业务异常
        log.info("openid: {}",openId);

        if (openId == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 如果是新用户,自动完成注册
        // 查询是不是新用户
        User user = userMapper.selectByOpenId(openId);

        log.info("查询到的user:{}",user);
        if (user == null){
            user = User
                    .builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();


            // 新建用户 并返回主键
            userMapper.insert(user);
        }
        return user;

    }


    /**
     * 用户登录微信小程序
     * @param code
     * @return
     */
    private String getOpenId(String code){
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);

        // 返回
        String openid = jsonObject.getString("openid");

        return openid;
    }
}
