package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("violation")
public class Violation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("seat_id")
    private String seatId;
    
    @TableField("reservation_id")
    private Long reservationId;
    
    private LocalDate date;
    
    private String reason;
    
    private String status;
    
    private String handling;
    
    @TableField("handled_by")
    private Long handledBy;
    
    @TableField("handled_at")
    private LocalDateTime handledAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
