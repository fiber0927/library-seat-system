package com.library.scheduler;

import com.library.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationCleanupTask {

    @Autowired
    private ReservationService reservationService;

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredReservations() {
        try {
            reservationService.cleanupExpiredReservations();
        } catch (Exception e) {
            System.err.println("清理超时预约失败: " + e.getMessage());
        }
    }
}
