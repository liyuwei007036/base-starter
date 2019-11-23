package com.lc.core.component;

import com.baomidou.mybatisplus.core.MybatisPlusVersion;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author lc
 */
@ConditionalOnClass(MybatisPlusVersion.class)
@Component
public class MybatisMateObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.setInsertFieldValByName("version", 0L, metaObject);
        this.setInsertFieldValByName("deleted", false, metaObject);
        this.setInsertFieldValByName("createTime", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setUpdateFieldValByName("lastUpdateTime", new Date(), metaObject);
    }
}