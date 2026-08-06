package cc.shiyi.oss.items;


/**
 * @program: object-saved-tool
 * @description:
 * @author: shiyi
 * @create: 2021-04-22 11:03
 */
public class FileItem {

    private String name;
    private String bucket;
    private String folderName;
    private String downloadUrl;
    private boolean isFolder;
    private long size;
    private String filePath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "FileItem{" +
                "name='" + name + '\'' +
                ", bucket='" + bucket + '\'' +
                ", folderName='" + folderName + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", isFolder=" + isFolder +
                ", size=" + size +
                '}';
    }
}
