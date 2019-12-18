package com.xuecheng.ucenter.dao;


import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component()
public interface XcMenuMapper {

    //根据用户id查询用户的权限
    List<XcMenu> selectPermissionByUserId(String userId);

}
