package com.example.protection.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author dengzhiming
 * @date 2020/2/6 10:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    private Integer id;
    private String name;
    private String memo;
}
