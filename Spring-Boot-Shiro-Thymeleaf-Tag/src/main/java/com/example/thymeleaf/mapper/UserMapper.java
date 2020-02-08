package com.example.thymeleaf.mapper;

import com.example.thymeleaf.bean.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author dengzhiming
 * @date 2020/2/5 11:46
 */
@Mapper
@Repository
public interface UserMapper {
    User findByUserName(String userName);
}
