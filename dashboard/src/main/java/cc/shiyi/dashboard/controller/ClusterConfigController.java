package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.db.table.ClusterConfig;
import cc.shiyi.dashboard.service.ClusterConfigService;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * K8s 集群配置管理接口：上传/激活/删除 kubeconfig，多集群单激活。
 */
@Tag(name = "Kubernetes集群配置接口", description = "上传kubeconfig到MySQL、多集群管理、单集群激活切换")
@RestController
@RequestMapping("/api/v1/k8s/cluster-config")
public class ClusterConfigController {

    @Autowired
    private ClusterConfigService clusterConfigService;

    @Operation(summary = "上传kubeconfig文件", description = "上传一个集群的kubeconfig文件，保存到MySQL，初始为未激活状态")
    @PostMapping("/upload")
    public ResponseWrapper<ClusterConfig> upload(
            @Parameter(description = "kubeconfig文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "集群显示名", required = true)
            @RequestParam("clusterName") String clusterName,
            @Parameter(description = "备注")
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            if (file == null || file.isEmpty()) {
                return new ResponseWrapper().fail("请选择kubeconfig文件");
            }
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Long userId = StpUtil.getLoginIdAsLong();
            ClusterConfig created = clusterConfigService.upload(clusterName, content, remark, userId);
            return new ResponseWrapper().success(created);
        } catch (IllegalArgumentException e) {
            return new ResponseWrapper().fail(e.getMessage());
        } catch (Exception e) {
            return new ResponseWrapper().fail("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "集群配置列表", description = "列出所有已上传的集群（不返回kubeconfig内容）")
    @GetMapping("/list")
    public ResponseWrapper<List<ClusterConfig>> list() {
        return new ResponseWrapper().success(clusterConfigService.list());
    }

    @Operation(summary = "当前激活的集群", description = "获取当前激活的集群信息，没有激活集群时返回null")
    @GetMapping("/active")
    public ResponseWrapper<ClusterConfig> active() {
        return new ResponseWrapper().success(clusterConfigService.getActive());
    }

    @Operation(summary = "激活指定集群", description = "激活该集群并重建Kubernetes客户端，其它集群自动取消激活")
    @PostMapping("/{id}/activate")
    public ResponseWrapper<ClusterConfig> activate(
            @Parameter(description = "集群配置ID", required = true)
            @PathVariable Long id) {
        try {
            return new ResponseWrapper().success(clusterConfigService.activate(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "测试集群连接", description = "临时构建客户端探测集群版本，验证kubeconfig是否可连通目标集群，不改变激活状态")
    @PostMapping("/{id}/test")
    public ResponseWrapper<ClusterConfig> testConnection(
            @Parameter(description = "集群配置ID", required = true)
            @PathVariable Long id) {
        try {
            return new ResponseWrapper().success(clusterConfigService.testConnection(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }

    @Operation(summary = "删除集群配置", description = "删除指定集群配置，激活中的集群不允许删除")
    @DeleteMapping("/{id}")
    public ResponseWrapper<Void> delete(
            @Parameter(description = "集群配置ID", required = true)
            @PathVariable Long id) {
        try {
            clusterConfigService.delete(id);
            return new ResponseWrapper().success(null);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseWrapper().fail(e.getMessage());
        }
    }
}
