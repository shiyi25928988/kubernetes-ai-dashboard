package cc.shiyi.message.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SiteMessageCreateDTO {
    private String title;
    private String content;
    private String type;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
}
