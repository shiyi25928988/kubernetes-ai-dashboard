package cc.shiyi.message.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SiteMessageUpdateDTO {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer priority;
    private Boolean isActive;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
}
