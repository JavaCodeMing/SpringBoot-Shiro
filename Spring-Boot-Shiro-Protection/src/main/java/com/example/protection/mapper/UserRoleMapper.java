package com.example.protection.mapper;

import com.example.protection.bean.Role;
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
