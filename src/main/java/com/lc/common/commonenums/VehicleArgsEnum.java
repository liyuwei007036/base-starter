package com.lc.common.commonenums;

import lombok.Getter;

public enum VehicleArgsEnum {

    PHOTO_METER("中控仪表", 1),
    PHOTO_SEAT("座椅内饰", 1),
    PHOTO_WHOLE("整体外观", 1),
    PHOTO_CERT("行驶证", 1),
    PHOTO_PROC("手续", 1),
    PHOTO_OTHER("其他", 1);

    @Getter
    private String name;

    // 值类型 1=车辆图片
    private Integer type;

    VehicleArgsEnum(String name, Integer type) {
        this.name = name;
        this.type = type;
    }


}
