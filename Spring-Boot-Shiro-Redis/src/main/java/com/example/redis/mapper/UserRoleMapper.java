package com.example.redis.mapper;

import com.example.redis.bean.Role;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/6 10:04
 */
@Mapper
@Repository
public interface UserRoleMapper {
    List<Role> findByUserName(String userName);
}
