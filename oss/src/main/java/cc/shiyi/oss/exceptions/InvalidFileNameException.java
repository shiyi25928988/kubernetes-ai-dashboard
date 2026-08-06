package cc.shiyi.oss.exceptions;

/**
 * @program: minio-client
 * @description:
 * @author: shiyi
 * @create: 2021-02-20 11:39
 */
public class InvalidFileNameException extends Exception {

    public InvalidFileNameException(String message) {
        super(message);
    }
}
