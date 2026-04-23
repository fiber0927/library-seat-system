package com.library.controller;

import com.library.common.Result;
import com.library.entity.User;
import com.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginData, HttpServletRequest request) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        Map<String, Object> result = userService.login(username, password);

        if ((boolean) result.get("success")) {
            return Result.success(result);
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
            return Result.error("用户名、密码和邮箱不能为空");
        }

        Map<String, Object> result = userService.register(user);

        if ((boolean) result.get("success")) {
            return Result.success((String) result.get("message"));
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @GetMapping("/{id}")
    public Result<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.error("用户不存在");
        }
    }

    @GetMapping("/current")
    public Result<?> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.error("用户不存在");
        }
    }

    @GetMapping
    public Result<?> getAllUsers(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限访问");
        }

        List<User> users = userService.getAllUsers();
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PutMapping("/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody User user, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long currentUserId = (Long) request.getAttribute("userId");

        if (!"admin".equals(role) && !id.equals(currentUserId)) {
            return Result.error(403, "无权限修改此用户");
        }

        user.setId(id);
        user.setPassword(null);
        boolean success = userService.updateUser(user);

        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.error("更新失败");
        }
    }

    @PutMapping("/{id}/toggle-enabled")
    public Result<?> toggleEnabled(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限操作");
        }

        boolean success = userService.toggleEnabled(id);
        if (success) {
            return Result.success("操作成功");
        } else {
            return Result.error("操作失败");
        }
    }
}
