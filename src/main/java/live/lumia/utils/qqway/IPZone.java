package live.lumia.utils.qqway;

import live.lumia.utils.ObjectUtil;
import lombok.Data;

@Data
public class IPZone {

    private final String ip;
    private String mainInfo;
    private String subInfo;

    public IPZone(final String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return ObjectUtil.getString(mainInfo) + " " + ObjectUtil.getString(subInfo);
    }

}
