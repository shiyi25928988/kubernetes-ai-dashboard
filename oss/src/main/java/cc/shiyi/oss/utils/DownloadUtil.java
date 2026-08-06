package cc.shiyi.oss.utils;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.*;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @program: object-saved-tool
 * @description:
 * @author: shiyi
 * @create: 2021-03-09 08:51
 */
@Slf4j
public class DownloadUtil {

    private static final String tempDir = System.getProperty("user.dir");

    /**
     * @param url
     * @return
     * @throws IOException
     */
    public static File download(String url) throws IOException {
        AtomicReference<File> files = new AtomicReference<>();
        Request request = new Request.Builder().url(url).build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Response response = okHttpClient.newCall(request).execute();
        files.set(processDownloadResp(response, getHeaderFileName(response, url)));
        response.close();
        return files.get();
    }

    /**
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * @param response
     * @param fileName
     * @return
     */
    private static File processDownloadResp(Response response, String fileName) {
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        file.setReadable(true);
        file.setWritable(true);

        byte[] buf = new byte[1024];
        int len = 0;
        try (InputStream is = response.body().byteStream(); FileOutputStream fos = new FileOutputStream(file);) {
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * @param response
     * @param downloadUrl
     * @return
     */
    private static String getHeaderFileName(Response response, String downloadUrl) {
        String fileName = "";
        String dispositionHeader = response.header("Content-Disposition");

        if (!Strings.isNullOrEmpty(dispositionHeader)) {
            fileName = parseDispositionHeader(dispositionHeader);
        }
        if(Strings.isNullOrEmpty(fileName)) {
            fileName = downloadUrl;
            if (!Strings.isNullOrEmpty(fileName)) {
                for (; ; ) {
                    if (fileName.contains("?")) {
                        fileName = fileName.substring(0, fileName.indexOf("?"));
                    } else {
                        break;
                    }
                }
                String[] sub = fileName.split("/");
                fileName = sub[sub.length - 1];
            }
        }
        return fileName;
    }

    /**
     * @param dispositionHeader
     * @return
     */
    private static String parseDispositionHeader(String dispositionHeader) {
        String str = dispositionHeader;
        if(str.contains("attachment")){
            str = str.replace("attachment", "");
            str = str.trim();
        }else{
            return "";
        }
        if(str.contains(";")){
            str = str.replace(";", "");
            str = str.trim();
        }
        if(str.contains("filename")){
            str = str.replace("filename", "");
            str = str.trim();
        }else{
            return "";
        }
        if(str.contains("*")){
            str = str.replace("*", "");
            str = str.trim();
        }
        if(str.contains("=")){
            str = str.replace("=", "");
            str = str.trim();
        }
        if(str.contains("UTF-8") || str.contains("utf-8")){
            str = str.replace("UTF-8", "");
            str = str.replace("utf-8", "");
            str = str.trim();
            try {
                str = URLDecoder.decode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

}
