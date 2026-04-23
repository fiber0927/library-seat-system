package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Violation;
import com.library.mapper.ViolationMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ViolationService extends ServiceImpl<ViolationMapper, Violation> {

    public boolean handleViolation(Long violationId, String handling, Long handledBy) {
        Violation violation = this.getById(violationId);
        if (violation == null) {
            return false;
        }

        violation.setStatus("handled");
        violation.setHandling(handling);
        violation.setHandledBy(handledBy);
        violation.setHandledAt(LocalDateTime.now());
        return this.updateById(violation);
    }

    public List<Violation> getAllViolations() {
        return this.list(new QueryWrapper<Violation>()
                .orderByDesc("create_time"));
    }

    public List<Violation> getPendingViolations() {
        return this.list(new QueryWrapper<Violation>()
                .eq("status", "pending")
                .orderByDesc("create_time"));
    }

    public List<Violation> getUserViolations(Long userId) {
        return this.list(new QueryWrapper<Violation>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    public Map<String, Object> getStatistics() {
        List<Violation> allViolations = this.list();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allViolations.size());
        stats.put("pending", allViolations.stream().filter(v -> "pending".equals(v.getStatus())).count());
        stats.put("handled", allViolations.stream().filter(v -> "handled".equals(v.getStatus())).count());

        Map<String, Long> reasonCount = new HashMap<>();
        allViolations.forEach(v -> {
            reasonCount.merge(v.getReason(), 1L, Long::sum);
        });
        stats.put("reasonCount", reasonCount);

        return stats;
    }

    public boolean deleteViolation(Long violationId) {
        return this.removeById(violationId);
    }
}
