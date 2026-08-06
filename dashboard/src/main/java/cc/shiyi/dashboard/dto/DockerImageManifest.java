package cc.shiyi.dashboard.dto;

import java.util.List;
import java.util.Map;

public class DockerImageManifest {

    private String name;
    private String tag;
    private String digest;
    private String mediaType;
    private long sizeBytes;
    private List<String> layers;
    private Map<String, String> annotations;
    private String architecture;
    private String os;
    private String created;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getDigest() { return digest; }
    public void setDigest(String digest) { this.digest = digest; }
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public List<String> getLayers() { return layers; }
    public void setLayers(List<String> layers) { this.layers = layers; }
    public Map<String, String> getAnnotations() { return annotations; }
    public void setAnnotations(Map<String, String> annotations) { this.annotations = annotations; }
    public String getArchitecture() { return architecture; }
    public void setArchitecture(String architecture) { this.architecture = architecture; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }
}
