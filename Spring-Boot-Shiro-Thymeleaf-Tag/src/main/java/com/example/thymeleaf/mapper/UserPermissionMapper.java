package com.example.thymeleaf.mapper;

import com.example.thymeleaf.bean.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author dengzhiming
 * @date 2020/2/6 10:05
 */
@Mapper
@Repository
public interface UserPermissionMapper {
    List<Permission> findByUserName(String userName);
}
