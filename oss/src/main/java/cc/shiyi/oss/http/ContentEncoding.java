package cc.shiyi.oss.http;

/**
 * @program: minio-client
 * @description:
 * @author: shiyi
 * @create: 2021-02-18 16:52
 */
public enum ContentEncoding implements ContentHeader<String, String> {
    gzip("Content-Encoding", "gzip"),//表示Object采用Lempel-Ziv（LZ77）压缩算法以及32位CRC校验的编码方式
    compress("Content-Encoding", "compress"),//表示Object采用Lempel-Ziv-Welch（LZW）压缩算法的编码方式
    deflate("Content-Encoding", "deflate"),//表示Object采用zlib结构和deflate压缩算法的编码方式
    br("Content-Encoding", "br");//表示Object采用Brotli算法的编码方式

    private String type;
    private String value;

    ContentEncoding(String type, String value) {
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
