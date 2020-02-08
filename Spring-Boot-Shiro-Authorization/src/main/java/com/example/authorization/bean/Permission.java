package com.example.authorization.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author dengzhiming
 * @date 2020/2/6 10:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission implements Serializable {
    private Integer id;
    private String url;
    private String name;
}
