package com.library.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充。
 *
 * 实体里的 createTime / updateTime 标了 @TableField(fill = ...)，但只有注册了
 * MetaObjectHandler 才会真正被填充。否则这两个字段会以 null 参与 insert，
 * 而数据库列是 NOT NULL DEFAULT CURRENT_TIMESTAMP，直接抛
 * "Column 'create_time' cannot be null"。
 *
 * 数据库的 DEFAULT / ON UPDATE 仍然保留，双保险。
 */
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
