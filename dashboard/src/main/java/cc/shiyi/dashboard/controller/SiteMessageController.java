package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.message.dto.SiteMessageCreateDTO;
import cc.shiyi.message.dto.SiteMessageUpdateDTO;
import cc.shiyi.message.service.SiteMessageService;
import cc.shiyi.message.table.SiteMessage;
import cc.shiyi.dashboard.db.table.ClusterInspectionConfig;
import cc.shiyi.dashboard.config.ClusterMonitorScheduleConfig;
import cc.shiyi.dashboard.service.ClusterInspectionConfigService;
import cc.shiyi.dashboard.service.K8sClusterMonitorService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@Setter(onMethod_ = @Autowired)
@Tag(name = "Site message - SiteMessageController")
public class SiteMessageController {

    SiteMessageService siteMessageService;
    K8sClusterMonitorService k8sClusterMonitorService;
    ClusterInspectionConfigService inspectionConfigService;
    ClusterMonitorScheduleConfig inspectionScheduler;

    @Operation(summary = "Create a new message (admin only)")
    @PostMapping("/create")
    public ResponseWrapper<SiteMessage> createMessage(@RequestBody SiteMessageCreateDTO dto) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            SiteMessage message = siteMessageService.createMessage(dto, userId);
            return new ResponseWrapper().success(message);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Update a message (admin only)")
    @PostMapping("/update")
    public ResponseWrapper<SiteMessage> updateMessage(@RequestBody SiteMessageUpdateDTO dto) {
        try {
            SiteMessage message = siteMessageService.updateMessage(dto);
            if (message == null) {
                return new ResponseWrapper().fail("Message not found");
            }
            return new ResponseWrapper().success(message);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Delete a message (admin only)")
    @PostMapping("/delete/{id}")
    public ResponseWrapper<Void> deleteMessage(@PathVariable Long id) {
        try {
            boolean deleted = siteMessageService.deleteMessage(id);
            if (!deleted) {
                return new ResponseWrapper().fail("Message not found");
            }
            return new ResponseWrapper().success();
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Get message by ID")
    @GetMapping("/{id}")
    public ResponseWrapper<SiteMessage> getMessageById(@PathVariable Long id) {
        try {
            SiteMessage message = siteMessageService.getMessageById(id);
            if (message == null) {
                return new ResponseWrapper().fail("Message not found");
            }
            return new ResponseWrapper().success(message);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Get all messages (admin only)")
    @GetMapping("/all")
    public ResponseWrapper<List<SiteMessage>> getAllMessages() {
        try {
            List<SiteMessage> messages = siteMessageService.getAllMessages();
            return new ResponseWrapper().success(messages);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Get active messages (for all users)")
    @GetMapping("/active")
    public ResponseWrapper<List<SiteMessage>> getActiveMessages() {
        try {
            List<SiteMessage> messages = siteMessageService.getActiveMessages();
            return new ResponseWrapper().success(messages);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Get unread messages for current user")
    @GetMapping("/unread")
    public ResponseWrapper<List<SiteMessage>> getUnreadMessages() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<SiteMessage> messages = siteMessageService.getUnreadMessagesForUser(userId);
            return new ResponseWrapper().success(messages);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Get active messages with read status for current user")
    @GetMapping("/active-with-status")
    public ResponseWrapper<Map<String, Object>> getActiveMessagesWithStatus() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<SiteMessage> activeMessages = siteMessageService.getActiveMessages();
            List<Long> readMessageIds = siteMessageService.getReadMessageIdsForUser(userId);

            Map<String, Object> result = new HashMap<>();
            result.put("messages", activeMessages);
            result.put("readMessageIds", readMessageIds);
            return new ResponseWrapper().success(result);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Mark a message as read")
    @PostMapping("/mark-read/{id}")
    public ResponseWrapper<Void> markAsRead(@PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            siteMessageService.markAsRead(id, userId);
            return new ResponseWrapper().success();
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Mark all active messages as read")
    @PostMapping("/mark-all-read")
    public ResponseWrapper<Void> markAllAsRead() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            siteMessageService.markAllAsRead(userId);
            return new ResponseWrapper().success();
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Check if a message is read by current user")
    @GetMapping("/is-read/{id}")
    public ResponseWrapper<Boolean> isRead(@PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean read = siteMessageService.isMessageRead(id, userId);
            return new ResponseWrapper().success(read);
        } catch (Exception e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "Manually trigger K8s cluster health check (admin only)")
    @PostMapping("/cluster/check")
    public ResponseWrapper<Void> manualClusterCheck() {
        try {
            k8sClusterMonitorService.checkAndAlert();
            return new ResponseWrapper().success("Cluster health check completed.");
        } catch (Exception e) {
            return new ResponseWrapper().fail("Check failed: " + e.getMessage());
        }
    }

    @Operation(summary = "Get cluster auto inspection settings")
    @GetMapping("/cluster/inspection-config")
    public ResponseWrapper<ClusterInspectionConfig> getInspectionConfig() {
        try {
            return new ResponseWrapper<>().success(inspectionConfigService.getConfig());
        } catch (Exception e) {
            return new ResponseWrapper<>().fail("Failed to load inspection config: " + e.getMessage());
        }
    }

    @Operation(summary = "Update cluster auto inspection settings (enabled / interval / AI agent)")
    @PostMapping("/cluster/inspection-config")
    public ResponseWrapper<ClusterInspectionConfig> updateInspectionConfig(@RequestBody ClusterInspectionConfig body) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            ClusterInspectionConfig config = inspectionConfigService.updateConfig(body, userId);
            // 保存成功后立即重排定时巡检任务（无需重启）
            inspectionScheduler.reschedule();
            return new ResponseWrapper<>().success(config);
        } catch (IllegalArgumentException e) {
            return new ResponseWrapper<>().fail(e.getMessage());
        } catch (Exception e) {
            return new ResponseWrapper<>().fail("Failed to update inspection config: " + e.getMessage());
        }
    }
}
