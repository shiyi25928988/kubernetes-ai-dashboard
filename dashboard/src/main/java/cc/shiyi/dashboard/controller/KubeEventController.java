package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Kubernetes Event管理接口", description = "查询集群Event")
@RestController
@RequestMapping("/api/v1/k8s")
public class KubeEventController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取所有Event列表", description = "查询所有命名空间中的所有Event")
    @GetMapping("/events")
    public ResponseWrapper<List<Event>> listAllEvents() {
        return new ResponseWrapper().success(kubernetesService.listAllEvents());
    }

    @Operation(summary = "获取指定命名空间下的Event列表", description = "查询某个命名空间中的Event")
    @GetMapping("/namespaces/{namespace}/events")
    public ResponseWrapper<List<Event>> listEvents(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace) {
        return new ResponseWrapper().success(kubernetesService.listEvents(namespace));
    }
}
