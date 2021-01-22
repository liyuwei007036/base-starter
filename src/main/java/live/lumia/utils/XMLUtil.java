package live.lumia.utils;

import lombok.extern.log4j.Log4j2;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

/**
 * XMl 和 json 互相转换工具类
 *
 * @author liyuwei
 */
@Log4j2
public class XMLUtil {

    /**
     * 将xml字符串<STRONG>转换</STRONG>为JSON字符串
     *
     * @param xmlString xml字符串
     * @return JSON<STRONG>对象</STRONG>
     */
    public static String xml2json(String xmlString) {
        org.json.JSONObject jsonObj = org.json.XML.toJSONObject(xmlString);
        return jsonObj.toString();
    }

    /**
     * JSON(数组)字符串<STRONG>转换</STRONG>成XML字符串
     *
     * @param jsonString
     * @return
     */
    public static String json2xml(String jsonString, String rootName) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        xmlSerializer.setRootName(rootName);
        xmlSerializer.setTypeHintsEnabled(false);
        return xmlSerializer.write(JSONSerializer.toJSON(jsonString));
    }
}