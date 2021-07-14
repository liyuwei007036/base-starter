package live.lumia.utils;

import live.lumia.dto.Account;
import live.lumia.enums.GlobalConstant;

/**
 * 获取当前用户工具类
 *
 * @author liyuwei
 * @date 2021/6/17 10:25
 **/
public class CurrentUserUtil {

    /**
     * 获取当前用户
     *
     * @return
     */
    public static Account getCurrentUser() {
        return GlobalRequestUtils.getData(GlobalConstant.SYS_USER, Account.class);
    }
}
