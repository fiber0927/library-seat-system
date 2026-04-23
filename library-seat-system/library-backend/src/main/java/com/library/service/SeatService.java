package com.library.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.Seat;
import com.library.mapper.SeatMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatService extends ServiceImpl<SeatMapper, Seat> {

    public List<Seat> getSeatsByFloor(Integer floor) {
        return this.list(new QueryWrapper<Seat>()
                .eq("floor", floor)
                .eq("deleted", 0)
                .orderByAsc("area", "row_num", "col_num"));
    }

    public List<Seat> getSeatsByFloorAndArea(Integer floor, String area) {
        return this.list(new QueryWrapper<Seat>()
                .eq("floor", floor)
                .eq("area", area)
                .eq("deleted", 0)
                .orderByAsc("row_num", "col_num"));
    }

    public List<Seat> getAvailableSeats() {
        return this.list(new QueryWrapper<Seat>()
                .eq("status", "available")
                .eq("deleted", 0));
    }

    public boolean updateSeatStatus(String seatId, String status) {
        Seat seat = this.getById(seatId);
        if (seat != null) {
            seat.setStatus(status);
            return this.updateById(seat);
        }
        return false;
    }

    public boolean updateSeatStatusWithUser(String seatId, String status, Long userId) {
        Seat seat = this.getById(seatId);
        if (seat != null) {
            seat.setStatus(status);
            seat.setUserId(userId);
            return this.updateById(seat);
        }
        return false;
    }

    public boolean releaseSeat(String seatId) {
        Seat seat = this.getById(seatId);
        if (seat != null) {
            seat.setStatus("available");
            seat.setUserId(null);
            return this.updateById(seat);
        }
        return false;
    }

    public boolean addSeat(Seat seat) {
        Seat existingSeat = this.getById(seat.getId());
        if (existingSeat != null) {
            return false;
        }
        seat.setStatus("available");
        return this.save(seat);
    }

    public boolean deleteSeat(String seatId) {
        Seat seat = this.getById(seatId);
        if (seat != null) {
            seat.setDeleted(1);
            return this.updateById(seat);
        }
        return false;
    }

    public Map<String, Object> getStatistics() {
        List<Seat> allSeats = this.list(new QueryWrapper<Seat>().eq("deleted", 0));

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allSeats.size());
        stats.put("available", allSeats.stream().filter(s -> "available".equals(s.getStatus())).count());
        stats.put("reserved", allSeats.stream().filter(s -> "reserved".equals(s.getStatus())).count());
        stats.put("inUse", allSeats.stream().filter(s -> "in_use".equals(s.getStatus())).count());
        stats.put("away", allSeats.stream().filter(s -> "away".equals(s.getStatus())).count());
        stats.put("disabled", allSeats.stream().filter(s -> "disabled".equals(s.getStatus())).count());

        long occupiedCount = allSeats.size() - allSeats.stream().filter(s -> "available".equals(s.getStatus())).count();
        double usageRate = allSeats.isEmpty() ? 0 : ((double) occupiedCount / allSeats.size()) * 100;
        stats.put("usageRate", String.format("%.1f", usageRate));

        return stats;
    }

    public Map<String, Object> getFloorStatistics(Integer floor) {
        List<Seat> floorSeats = getSeatsByFloor(floor);

        Map<String, Object> stats = new HashMap<>();
        stats.put("floor", floor);
        stats.put("total", floorSeats.size());
        stats.put("available", floorSeats.stream().filter(s -> "available".equals(s.getStatus())).count());
        stats.put("reserved", floorSeats.stream().filter(s -> "reserved".equals(s.getStatus())).count());
        stats.put("inUse", floorSeats.stream().filter(s -> "in_use".equals(s.getStatus())).count());
        stats.put("away", floorSeats.stream().filter(s -> "away".equals(s.getStatus())).count());
        stats.put("disabled", floorSeats.stream().filter(s -> "disabled".equals(s.getStatus())).count());

        return stats;
    }

    public List<Seat> getSeatsByUserId(Long userId) {
        return this.list(new QueryWrapper<Seat>()
                .eq("user_id", userId)
                .eq("deleted", 0));
    }
}
