package com.example.authentication.util;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

/**
 * @author dengzhiming
 * @date 2020/2/5 13:47
 */
public class MD5Utils {
    private static final String SALT = "java-developer";
    private static final String ALGORITH_NAME = "md5";
    private static final int HASH_ITERATIONS = 2;
    public static String encrypt(String pswd){
        return new SimpleHash(ALGORITH_NAME,pswd, ByteSource.Util.bytes(SALT),HASH_ITERATIONS).toHex();
    }
    public static String encrypt(String username, String pswd){
        return new SimpleHash(ALGORITH_NAME,pswd,ByteSource.Util.bytes(username + SALT),HASH_ITERATIONS).toHex();
    }
    public static void main(String[] args) {
        System.out.println(MD5Utils.encrypt("conan", "123456"));
    }
}
