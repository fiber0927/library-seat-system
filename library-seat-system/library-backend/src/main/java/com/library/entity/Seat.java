package com.library.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("seat")
public class Seat {
    
    @TableId
    private String id;
    
    private Integer floor;
    
    private String area;
    
    @TableField("row_num")
    private Integer rowNum;
    
    @TableField("col_num")
    private Integer colNum;
    
    private String status;
    
    private String features;
    
    @TableField("user_id")
    private Long userId;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
