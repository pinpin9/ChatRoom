package org.example.server.service;

/**
 * 用户管理接口
 */
public interface UserService {

    /**
     * 登录
     * @param userId 用户名
     * @param password 密码
     * @return 登录成功返回 true, 否则返回 false
     */
    boolean login(String userId, String password);

    boolean register(String userId, String password);
}
