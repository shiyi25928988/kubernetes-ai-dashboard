package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.KubernetesService;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Kubernetes Job管理接口")
@RestController
@RequestMapping("/api/v1/k8s")
public class KubeJobController {

    @Autowired
    private KubernetesService kubernetesService;

    @Operation(summary = "获取指定命名空间下的Job列表")
    @GetMapping("/namespaces/{namespace}/jobs")
    public ResponseWrapper<List<Job>> listJobs(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace) {
        return new ResponseWrapper().success(kubernetesService.listJobs(namespace));
    }

    @Operation(summary = "获取单个Job详情")
    @GetMapping("/namespaces/{namespace}/jobs/{name}")
    public ResponseWrapper<Job> getJob(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "Job名称", required = true)
            @PathVariable String name) {
        return new ResponseWrapper().success(kubernetesService.getJob(namespace, name));
    }

    @Operation(summary = "创建Job")
    @PostMapping("/namespaces/{namespace}/jobs")
    public ResponseWrapper<Job> createJob(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @RequestBody Job job) {
        return new ResponseWrapper().success(kubernetesService.createJob(job, namespace));
    }

    @Operation(summary = "删除Job")
    @DeleteMapping("/namespaces/{namespace}/jobs/{name}")
    public ResponseWrapper<Void> deleteJob(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "Job名称", required = true)
            @PathVariable String name) {
        kubernetesService.deleteJob(namespace, name);
        return new ResponseWrapper().success(null);
    }

    @Operation(summary = "获取Job日志，返回Pod名称到日志内容的映射")
    @GetMapping("/namespaces/{namespace}/jobs/{name}/logs")
    public ResponseWrapper<Map<String, String>> getJobLogs(
            @Parameter(description = "命名空间名称", required = true)
            @PathVariable String namespace,
            @Parameter(description = "Job名称", required = true)
            @PathVariable String name) {
        return new ResponseWrapper().success(kubernetesService.getJobLogs(namespace, name));
    }
}
