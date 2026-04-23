package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Reservation;
import com.library.entity.Seat;
import com.library.entity.Violation;
import com.library.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService extends ServiceImpl<ReservationMapper, Reservation> {

    @Autowired
    private SeatService seatService;

    @Autowired
    private UserService userService;

    @Autowired
    private ViolationService violationService;

    @Value("${system.check-in-grace-period:30}")
    private int gracePeriodMinutes;

    @Value("${system.reservation.max-per-user:1}")
    private int maxReservationsPerUser;

    @Transactional
    public Map<String, Object> createReservation(String seatId, Long userId, LocalDate date,
                                                 LocalTime startTime, LocalTime endTime) {
        Seat seat = seatService.getById(seatId);
        if (seat == null) {
            return Map.of("success", false, "message", "座位不存在");
        }

        if (!"available".equals(seat.getStatus())) {
            return Map.of("success", false, "message", "座位不可预约");
        }

        Reservation existingReservation = this.getOne(new QueryWrapper<Reservation>()
                .eq("user_id", userId)
                .in("status", "pending", "active", "away"));

        if (existingReservation != null) {
            return Map.of("success", false, "message", "您当前已有预约，不可重复预约");
        }

        List<Reservation> conflictingReservations = this.list(new QueryWrapper<Reservation>()
                .eq("seat_id", seatId)
                .eq("date", date)
                .and(w -> w.le("start_time", endTime).ge("end_time", startTime))
                .notIn("status", "cancelled", "completed", "violated"));

        if (!conflictingReservations.isEmpty()) {
            return Map.of("success", false, "message", "该时段已被预约");
        }

        Reservation reservation = new Reservation();
        reservation.setSeatId(seatId);
        reservation.setUserId(userId);
        reservation.setDate(date);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setStatus("pending");

        if (this.save(reservation)) {
            seatService.updateSeatStatusWithUser(seatId, "reserved", userId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "预约成功！仅可预约1个座位，请按时签到后生效");
        result.put("reservation", reservation);

        return result;
    }

    @Transactional
    public Map<String, Object> checkIn(Long reservationId, Long userId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            return Map.of("success", false, "message", "预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "无权操作此预约");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = reservation.getDate().atTime(reservation.getStartTime());
        long diffMinutes = java.time.Duration.between(startDateTime, now).toMinutes();

        if (diffMinutes > gracePeriodMinutes) {
            reservation.setStatus("violated");
            this.updateById(reservation);
            seatService.releaseSeat(reservation.getSeatId());

            Violation violation = new Violation();
            violation.setUserId(userId);
            violation.setSeatId(reservation.getSeatId());
            violation.setReservationId(reservationId);
            violation.setDate(reservation.getDate());
            violation.setReason("预约后未在" + gracePeriodMinutes + "分钟内签到");
            violation.setStatus("pending");
            violationService.save(violation);

            userService.incrementViolations(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "签到已超时（超过" + gracePeriodMinutes + "分钟宽限期），预约已取消");
            result.put("exceeded", true);
            result.put("minutes", diffMinutes);

            return result;
        }

        reservation.setStatus("active");
        reservation.setCheckInTime(now);
        this.updateById(reservation);
        seatService.updateSeatStatusWithUser(reservation.getSeatId(), "in_use", userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("exceeded", diffMinutes > 0);
        result.put("minutes", diffMinutes);

        return result;
    }

    @Transactional
    public Map<String, Object> setAway(Long reservationId, Long userId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            return Map.of("success", false, "message", "预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "无权操作此预约");
        }

        if (!"active".equals(reservation.getStatus())) {
            return Map.of("success", false, "message", "无法暂离");
        }

        reservation.setStatus("away");
        this.updateById(reservation);
        seatService.updateSeatStatus(reservation.getSeatId(), "away");

        return Map.of("success", true, "message", "已设为暂离");
    }

    @Transactional
    public Map<String, Object> returnFromAway(Long reservationId, Long userId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            return Map.of("success", false, "message", "预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "无权操作此预约");
        }

        if (!"away".equals(reservation.getStatus())) {
            return Map.of("success", false, "message", "无法取消暂离");
        }

        reservation.setStatus("active");
        this.updateById(reservation);
        seatService.updateSeatStatus(reservation.getSeatId(), "in_use");

        return Map.of("success", true, "message", "已取消暂离");
    }

    @Transactional
    public Map<String, Object> cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            return Map.of("success", false, "message", "预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "无权操作此预约");
        }

        String currentStatus = reservation.getStatus();
        String seatId = reservation.getSeatId();
        
        seatService.releaseSeat(seatId);
        
        this.removeById(reservationId);
        
        String message;
        switch (currentStatus) {
            case "pending":
                message = "预约已取消（待签到状态），座位已释放";
                break;
            case "active":
                message = "预约已取消（使用中状态），座位已释放";
                break;
            case "away":
                message = "预约已取消（暂离状态），座位已释放";
                break;
            default:
                message = "预约已取消，座位已释放";
        }

        return Map.of("success", true, "message", message);
    }

    @Transactional
    public Map<String, Object> checkOut(Long reservationId, Long userId) {
        Reservation reservation = this.getById(reservationId);
        if (reservation == null) {
            return Map.of("success", false, "message", "预约记录不存在");
        }

        if (!reservation.getUserId().equals(userId)) {
            return Map.of("success", false, "message", "无权操作此预约");
        }

        if (!"active".equals(reservation.getStatus()) && !"away".equals(reservation.getStatus())) {
            return Map.of("success", false, "message", "无法结束使用");
        }

        String seatId = reservation.getSeatId();
        
        seatService.releaseSeat(seatId);
        
        this.removeById(reservationId);

        return Map.of("success", true, "message", "已结束使用，感谢使用");
    }

    public List<Reservation> getUserReservations(Long userId) {
        return this.list(new QueryWrapper<Reservation>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    public Reservation getActiveReservation(Long userId) {
        return this.getOne(new QueryWrapper<Reservation>()
                .eq("user_id", userId)
                .in("status", "pending", "active", "away"));
    }

    public List<Reservation> getAllReservations() {
        return this.list(new QueryWrapper<Reservation>()
                .orderByDesc("create_time"));
    }

    public List<Reservation> getReservationsByStatus(String status) {
        return this.list(new QueryWrapper<Reservation>()
                .eq("status", status)
                .orderByDesc("create_time"));
    }

    public List<Reservation> getReservationsByDate(LocalDate date) {
        return this.list(new QueryWrapper<Reservation>()
                .eq("date", date)
                .orderByAsc("start_time"));
    }

    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime graceDeadline = LocalDateTime.now().minusMinutes(gracePeriodMinutes);

        List<Reservation> expiredReservations = this.list(new QueryWrapper<Reservation>()
                .eq("status", "pending")
                .eq("date", LocalDate.now())
                .lt("start_time", graceDeadline.toLocalTime()));

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus("violated");
            this.updateById(reservation);
            seatService.releaseSeat(reservation.getSeatId());

            Violation violation = new Violation();
            violation.setUserId(reservation.getUserId());
            violation.setSeatId(reservation.getSeatId());
            violation.setReservationId(reservation.getId());
            violation.setDate(reservation.getDate());
            violation.setReason("预约后未在" + gracePeriodMinutes + "分钟内签到");
            violation.setStatus("pending");
            violationService.save(violation);

            userService.incrementViolations(reservation.getUserId());
        }
    }

    @Transactional
    public Map<String, Object> resetAllReservations(Long adminId) {
        int deletedCount = 0;

        List<Reservation> activeReservations = this.list(new QueryWrapper<Reservation>()
                .in("status", "pending", "active", "away"));

        for (Reservation reservation : activeReservations) {
            seatService.releaseSeat(reservation.getSeatId());
            this.removeById(reservation.getId());
            deletedCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "清零成功！已清空" + deletedCount + "条预约记录，所有座位已重置为「空闲」状态");
        result.put("deletedCount", deletedCount);

        return result;
    }
}
