package org.example.server.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserServiceMemoryImpl implements UserService {
    private Map<String, String> allUserMap = new ConcurrentHashMap<>();

    {
        allUserMap.put("lzh", "123");
        allUserMap.put("lisi", "123");
        allUserMap.put("wangwu", "123");
        allUserMap.put("zhaoliu", "123");
        allUserMap.put("qianqi", "123");
    }

    @Override
    public boolean login(String userId, String password) {
        String pass = allUserMap.get(userId);
        if (pass == null) {
            return false;
        }
        return pass.equals(password);
    }

    @Override
    public boolean register(String userId, String password) {
        String exist = allUserMap.get(userId);
        if (exist == null) {
            allUserMap.put(userId, password);
            return true;
        }
        return false;
    }
}
