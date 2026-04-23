package com.library.controller;

import com.library.common.Result;
import com.library.entity.Seat;
import com.library.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/seats")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @GetMapping
    public Result<?> getAllSeats() {
        List<Seat> seats = seatService.list();
        return Result.success(seats);
    }

    @GetMapping("/floor/{floor}")
    public Result<?> getSeatsByFloor(@PathVariable Integer floor) {
        List<Seat> seats = seatService.getSeatsByFloor(floor);
        return Result.success(seats);
    }

    @GetMapping("/floor/{floor}/area/{area}")
    public Result<?> getSeatsByFloorAndArea(@PathVariable Integer floor, @PathVariable String area) {
        List<Seat> seats = seatService.getSeatsByFloorAndArea(floor, area);
        return Result.success(seats);
    }

    @GetMapping("/available")
    public Result<?> getAvailableSeats() {
        List<Seat> seats = seatService.getAvailableSeats();
        return Result.success(seats);
    }

    @GetMapping("/{id}")
    public Result<?> getSeatById(@PathVariable String id) {
        Seat seat = seatService.getById(id);
        if (seat != null) {
            return Result.success(seat);
        } else {
            return Result.error("座位不存在");
        }
    }

    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        Map<String, Object> stats = seatService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/statistics/floor/{floor}")
    public Result<?> getFloorStatistics(@PathVariable Integer floor) {
        Map<String, Object> stats = seatService.getFloorStatistics(floor);
        return Result.success(stats);
    }

    @PostMapping
    public Result<?> addSeat(@RequestBody Seat seat, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限添加座位");
        }

        boolean success = seatService.addSeat(seat);
        if (success) {
            return Result.success("座位添加成功");
        } else {
            return Result.error("座位号已存在");
        }
    }

    @PutMapping("/{id}/status")
    public Result<?> updateSeatStatus(@PathVariable String id, @RequestBody Map<String, String> statusData, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限修改座位状态");
        }

        String status = statusData.get("status");
        boolean success = seatService.updateSeatStatus(id, status);
        if (success) {
            return Result.success("状态更新成功");
        } else {
            return Result.error("状态更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteSeat(@PathVariable String id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限删除座位");
        }

        boolean success = seatService.deleteSeat(id);
        if (success) {
            return Result.success("座位已删除");
        } else {
            return Result.error("删除失败");
        }
    }
}
