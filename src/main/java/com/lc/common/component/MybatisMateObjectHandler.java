package com.lc.common.component;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MybatisMateObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Object version = getFieldValByName("version", metaObject);
        if (version == null) {
            this.setFieldValByName("version", 0L, metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object version = getFieldValByName("version", metaObject);
        if (version == null) {
            this.setFieldValByName("version", 0L, metaObject);
        }
    }
}