package com.library.controller;

import com.library.common.Result;
import com.library.entity.Violation;
import com.library.service.ViolationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/violations")
public class ViolationController {

    @Autowired
    private ViolationService violationService;

    @GetMapping
    public Result<?> getAllViolations(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限查看违规记录");
        }

        List<Violation> violations = violationService.getAllViolations();
        return Result.success(violations);
    }

    @GetMapping("/pending")
    public Result<?> getPendingViolations(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限查看违规记录");
        }

        List<Violation> violations = violationService.getPendingViolations();
        return Result.success(violations);
    }

    @GetMapping("/user/{userId}")
    public Result<?> getUserViolations(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (!"admin".equals(role) && !userId.equals(currentUserId)) {
            return Result.error(403, "无权限查看此用户的违规记录");
        }

        List<Violation> violations = violationService.getUserViolations(userId);
        return Result.success(violations);
    }

    @GetMapping("/statistics")
    public Result<?> getStatistics(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限查看统计");
        }

        Map<String, Object> stats = violationService.getStatistics();
        return Result.success(stats);
    }

    @PostMapping("/{id}/handle")
    public Result<?> handleViolation(@PathVariable Long id, @RequestBody Map<String, String> handlingData, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限处理违规");
        }

        String handling = handlingData.get("handling");
        Long handledBy = (Long) request.getAttribute("userId");

        boolean success = violationService.handleViolation(id, handling, handledBy);
        if (success) {
            return Result.success("处理成功");
        } else {
            return Result.error("处理失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteViolation(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限删除违规记录");
        }

        boolean success = violationService.deleteViolation(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
