package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.ResourceOwnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "资源归属查询接口")
@RestController
@RequestMapping("/api/v1/resources")
public class ResourceOwnershipController {

    @Autowired
    private ResourceOwnershipService ownershipService;

    @Operation(summary = "查询资源归属记录")
    @GetMapping("/ownership")
    public ResponseWrapper<List<Map<String, Object>>> listOwnership(
            @Parameter(description = "资源类型（可选）：Namespace, Deployment, Service, Ingress, PVC, ConfigMap, Secret, Job, MPIJob")
            @RequestParam(required = false) String resourceType,
            @Parameter(description = "命名空间（可选）")
            @RequestParam(required = false) String namespace) {
        return new ResponseWrapper<List<Map<String, Object>>>()
                .success(ownershipService.listOwnershipRecords(resourceType, namespace));
    }
}
