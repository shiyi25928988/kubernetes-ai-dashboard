package cc.shiyi.oss.http;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * @author yshi
 */
public enum ContentType implements ContentHeader<String, String> {
    /**
     *
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
     * */

    /**
     * Default file type
     */
    APPLICATION_OCTET_STREAM("application/octet-stream", ""),

    /**
     * TEXT
     */
    TEXT_PLAIN("text/plain", "txt"),
    TEXT_HTML("text/html", "html"),
    TEXT_HTM("text/html", "htm"),
    TEXT_CSS("text/css", "css"),
    TEXT_CSV("text/csv", "csv"),
    TEXT_XML("text/xml", "xml"),
    TEXT_CALENDAR("text/calendar", "ics"),
    TEXT_MARKDOWN("text/markdown", "md"),

    /**
     * Common http application.
     */
    APPLICATION("application/*", ""),
    APPLICATION_JAVASCRIPT("application/javascript", "js"),
    APPLICATION_JAVASCRIPT_MJS("application/javascript", "mjs"),
    APPLICATION_JSON("application/json", "json"),
    APPLICATION_LD_JSON("application/ld+json", "jsonld"),
    APPLICATION_XML("application/xml", "xml"),
    APPLICATION_XHTML_XML("application/xhtml+xml", "xhtml"),
    APPLICATION_X_SHOCKWAVE_FLASH("application/x-shockwave-flash", "swf"),
    APPLICATION_RTF("application/rtf", "rtf"),
    APPLICATION_VND_MS_FONTOBJECT("application/vnd.ms-fontobject", "eot"),

    /**
     * Document
     */
    APPLICATION_PDF("application/pdf", "pdf"),
    APPLICATION_DOC("application/msword", "doc"),
    APPLICATION_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    APPLICATION_PPT("application/vnd.ms-powerpoint", "ppt"),
    APPLICATION_PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
    APPLICATION_XLS("application/vnd.ms-excel", "xls"),
    APPLICATION_XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    APPLICATION_VND_VISIO("application/vnd.visio", "vsd"),
    APPLICATION_X_ABIWORD("application/x-abiword", "abw"),
    APPLICATION_VND_AMAZON_EBOOK("application/vnd.amazon.ebook", "azw"),
    APPLICATION_VND_MOZILLA_XUL_XML("application/vnd.mozilla.xul+xml", "xul"),

    /**
     * Compression type
     */
    APPLICATION_ZIP("application/zip", "zip"),
    APPLICATION_GZIP("application/gzip", "gz"),
    APPLICATION_RAR("application/vnd.rar", "rar"),
    APPLICATION_JAR("application/java-archive", "jar"),
    APPLICATION_TAR("application/x-tar", "tar"),
    APPLICATION_7Z("application/x-7z-compressed", "7z"),
    APPLICATION_X_BZIP2("application/x-bzip2", "bz2"),
    APPLICATION_X_BZIP("application/x-bzip", "bz"),
    APPLICATION_X_FREEARC("application/x-freearc", "arc"),

    /**
     * shell script type.
     */
    APPLICATION_SHELL("application/x-sh", "sh"),
    APPLICATION_X_CSH("application/x-csh", "csh"),

    /**
     * Image type.
     */
    IMAGE_JPEG("image/jpeg", "jpeg"),
    IMAGE_JPG("image/jpeg", "jpg"),
    IMAGE_PNG("image/png", "png"),
    IMAGE_GIF("image/gif", "gif"),
    IMAGE_BMP("image/bmp", "bmp"),
    IMAGE_ICON("image/vnd.microsoft.icon", "icon"),
    IMAGE_SVG("image/svg+xml", "svg"),
    IMAGE_TIFF("image/tiff", "tif"),
    IMAGE_WEBP("image/webp", "webp"),

    /**
     * Media type.
     */
    VIDEO_MP4("video/mp4", "mp4"),
    VIDEO_AVI("video/x-msvideo", "avi"),
    VIDEO_MPEG("video/mpeg", "mpeg"),
    VEDIO_WEBM("video/webm", "webm"),
    VIDEO_3GPP("video/3gpp", "3gp"),
    VIDEO_3GPP2("video/3gpp2", "3g2"),
    VIDEO_MP2T("video/mp2t", "ts"),
    AUDIO_AAC("audio/aac", "aac"),
    AUDIO_WEBM("audio/webm", "weba"),
    AUDIO_MP3("audio/mpeg", "mp3"),
    AUDIO_WAV("audio/wav", "wav"),

    /**
     * Font type
     */
    FONT_WOFF2("font/woff2", "woff2"),
    FONT_WOFF("font/woff", "woff"),
    FONT_TTF("font/ttf", "ttf");

    String value;
    String extension;

    ContentType(String value, String extension) {
        this.value = value;
        this.extension = extension;
    }

    public static String getTypeByFileName(String fileName) {
		AtomicReference<String> ret = new AtomicReference<>(APPLICATION_OCTET_STREAM.value);
		Stream.of(ContentType.values()).forEach(mimeType -> {
			if(fileName.trim().toLowerCase().endsWith(mimeType.extension)){
				ret.set(mimeType.value);
			}
		});
		return ret.get();
    }

    public static ContentType getContentTypeByFileName(String fileName) {
        AtomicReference<ContentType> ret = new AtomicReference<>(APPLICATION_OCTET_STREAM);
        Stream.of(ContentType.values()).forEach(mimeType -> {
            if(fileName.trim().toLowerCase().endsWith(mimeType.extension)){
                ret.set(mimeType);
            }
        });
        return ret.get();
    }

    @Override
    public String getType() {
        return "Content-Type";
    }

    @Override
    public String getValue() {
        return this.value;
    }
}
