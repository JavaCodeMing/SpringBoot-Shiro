package com.example.logintypes.mapper;

import com.example.logintypes.bean.User;
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
    User findByPhone(String phone);
}
