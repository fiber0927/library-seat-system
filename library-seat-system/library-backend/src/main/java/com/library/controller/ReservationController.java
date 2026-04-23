package com.library.controller;

import com.library.common.Result;
import com.library.entity.Reservation;
import com.library.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public Result<?> createReservation(@RequestBody Map<String, Object> reservationData, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        String seatId = (String) reservationData.get("seatId");
        LocalDate date = LocalDate.parse((String) reservationData.get("date"));
        LocalTime startTime = LocalTime.parse((String) reservationData.get("startTime"));
        LocalTime endTime = LocalTime.parse((String) reservationData.get("endTime"));

        Map<String, Object> result = reservationService.createReservation(seatId, userId, date, startTime, endTime);

        if ((boolean) result.get("success")) {
            return Result.success((String) result.get("message"), result.get("reservation"));
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @PostMapping("/{id}/check-in")
    public Result<?> checkIn(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = reservationService.checkIn(id, userId);

        if ((boolean) result.get("success")) {
            boolean exceeded = (boolean) result.getOrDefault("exceeded", false);
            if (exceeded) {
                long minutes = (long) result.get("minutes");
                return Result.success("签到成功！已超时" + minutes + "分钟，但仍在30分钟宽限期内", result);
            } else {
                return Result.success("签到成功", result);
            }
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @PostMapping("/{id}/away")
    public Result<?> setAway(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = reservationService.setAway(id, userId);

        if ((boolean) result.get("success")) {
            return Result.success((String) result.get("message"));
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @PostMapping("/{id}/return")
    public Result<?> returnFromAway(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = reservationService.returnFromAway(id, userId);

        if ((boolean) result.get("success")) {
            return Result.success((String) result.get("message"));
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @PostMapping("/{id}/cancel")
    public Result<?> cancelReservation(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = reservationService.cancelReservation(id, userId);

        if ((boolean) result.get("success")) {
            return Result.success((String) result.get("message"));
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @PostMapping("/{id}/checkout")
    public Result<?> checkOut(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> result = reservationService.checkOut(id, userId);

        if ((boolean) result.get("success")) {
            return Result.success((String) result.get("message"));
        } else {
            return Result.error((String) result.get("message"));
        }
    }

    @GetMapping("/user/{userId}")
    public Result<?> getUserReservations(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (!"admin".equals(role) && !userId.equals(currentUserId)) {
            return Result.error(403, "无权限查看此用户的预约");
        }

        List<Reservation> reservations = reservationService.getUserReservations(userId);
        return Result.success(reservations);
    }

    @GetMapping("/user/{userId}/active")
    public Result<?> getActiveReservation(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (!"admin".equals(role) && !userId.equals(currentUserId)) {
            return Result.error(403, "无权限查看此用户的预约");
        }

        Reservation reservation = reservationService.getActiveReservation(userId);
        return Result.success(reservation);
    }

    @GetMapping
    public Result<?> getAllReservations(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限查看所有预约");
        }

        List<Reservation> reservations = reservationService.getAllReservations();
        return Result.success(reservations);
    }

    @GetMapping("/status/{status}")
    public Result<?> getReservationsByStatus(@PathVariable String status, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限查看预约");
        }

        List<Reservation> reservations = reservationService.getReservationsByStatus(status);
        return Result.success(reservations);
    }

    @GetMapping("/date/{date}")
    public Result<?> getReservationsByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"admin".equals(role)) {
            return Result.error(403, "无权限查看预约");
        }

        List<Reservation> reservations = reservationService.getReservationsByDate(date);
        return Result.success(reservations);
    }
}
