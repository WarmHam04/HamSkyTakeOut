package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;

@Mapper
public interface UserMapper {

    /**
     * 获取用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入用户
     * @param user
     */
    void insertUser(User user);

    @Select("select * from user where userId = #{userId}")
    User getById(Long userId);

    Integer getNewUsers(HashMap map);

    /**
     * 根据map来返回在这个区间的user数量
     * @param map
     * @return
     */
    Integer countUsersByMap(HashMap map);
}
