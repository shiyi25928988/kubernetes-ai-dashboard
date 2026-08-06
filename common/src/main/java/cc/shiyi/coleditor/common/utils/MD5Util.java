package cc.shiyi.coleditor.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@Slf4j
public class MD5Util {

    public static synchronized String caculateMD5(Object obj) throws JsonProcessingException {
        if(Objects.isNull(obj)) {
            return "";
        }
        String dataStr = JsonUtil.toJson(obj);
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(dataStr.getBytes());
            String md5Str = new BigInteger(md5.digest()).toString(16);
            log.info("MD5: " + md5Str);
            return md5Str;
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
        }
        return "";
    }

}
