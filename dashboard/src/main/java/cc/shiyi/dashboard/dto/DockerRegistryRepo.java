package cc.shiyi.dashboard.dto;

import java.util.List;

public class DockerRegistryRepo {

    private String name;
    private List<String> tags;
    private int tagCount;
    private String registryHost;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public int getTagCount() { return tagCount; }
    public void setTagCount(int tagCount) { this.tagCount = tagCount; }
    public String getRegistryHost() { return registryHost; }
    public void setRegistryHost(String registryHost) { this.registryHost = registryHost; }
}
