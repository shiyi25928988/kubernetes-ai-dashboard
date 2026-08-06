package cc.shiyi.oss.http;

/**
 * @program: minio-client
 * @description:
 * @author: shiyi
 * @create: 2021-02-18 16:53
 */
public enum ContentDisposition implements ContentHeader<String, String> {
    inline("Content-Disposition", "inline"),//直接在浏览器中打开Object
    attachment("Content-Disposition", "attachment")//将Object下载到本地
    ;

    private String type;
    private String value;

    ContentDisposition(String type, String value) {
        this.type = type;
        this.value = value;
    }


    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
