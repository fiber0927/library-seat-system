package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private JwtUtils jwtUtils;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Map<String, Object> login(String username, String password) {
        User user = this.getOne(new QueryWrapper<User>()
                .eq("username", username)
                .eq("deleted", 0)
                .eq("enabled", 1));

        if (user == null) {
            return Map.of("success", false, "message", "用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Map.of("success", false, "message", "用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("role", user.getRole());
        userInfo.put("violations", user.getViolations());
        userInfo.put("createTime", user.getCreateTime());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("token", token);
        result.put("user", userInfo);

        return result;
    }

    public Map<String, Object> register(User user) {
        User existingUser = this.getOne(new QueryWrapper<User>()
                .eq("username", user.getUsername())
                .eq("deleted", 0));

        if (existingUser != null) {
            return Map.of("success", false, "message", "用户名已存在");
        }

        User existingEmail = this.getOne(new QueryWrapper<User>()
                .eq("email", user.getEmail())
                .eq("deleted", 0));

        if (existingEmail != null) {
            return Map.of("success", false, "message", "邮箱已被注册");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        user.setViolations(0);
        user.setEnabled(true);

        this.save(user);

        return Map.of("success", true, "message", "注册成功");
    }

    public User getUserById(Long id) {
        return this.getOne(new QueryWrapper<User>()
                .eq("id", id)
                .eq("deleted", 0));
    }

    public List<User> getAllUsers() {
        return this.list(new QueryWrapper<User>()
                .eq("deleted", 0)
                .orderByDesc("create_time"));
    }

    public boolean updateUser(User user) {
        return this.updateById(user);
    }

    public boolean incrementViolations(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setViolations(user.getViolations() + 1);
            return this.updateById(user);
        }
        return false;
    }

    public boolean toggleEnabled(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setEnabled(!user.getEnabled());
            return this.updateById(user);
        }
        return false;
    }
}
