package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("reservation")
public class Reservation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("seat_id")
    private String seatId;
    
    @TableField("user_id")
    private Long userId;
    
    private LocalDate date;
    
    @TableField("start_time")
    private LocalTime startTime;
    
    @TableField("end_time")
    private LocalTime endTime;
    
    private String status;
    
    @TableField("check_in_time")
    private LocalDateTime checkInTime;
    
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;
    
    @TableField("cancel_time")
    private LocalDateTime cancelTime;
    
    @TableField("cancel_reason")
    private String cancelReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
