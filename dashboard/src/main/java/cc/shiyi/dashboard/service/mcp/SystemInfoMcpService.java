package cc.shiyi.dashboard.service.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统信息MCP服务
 * 提供获取当前服务器操作系统、硬件资源使用情况等信息的接口
 */
@Slf4j
@Service
public class SystemInfoMcpService {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long GB = MB * 1024;

    /**
     * 获取系统基本信息
     * @return 包含系统信息的Map
     */
    @Tool(description = "获取当前服务器的基本系统信息，包括操作系统名称、版本、CPU、内存、磁盘、IP等基础信息")
    public Map<String, Object> getBasicSystemInfo() {
        log.info("MCP调用-获取系统基本信息");
        Map<String, Object> info = new HashMap<>();

        try {
            // 操作系统信息
            info.put("osName", System.getProperty("os.name"));
            info.put("osVersion", System.getProperty("os.version"));
            info.put("osArch", System.getProperty("os.arch"));
            info.put("userName", System.getProperty("user.name"));
            info.put("userDir", System.getProperty("user.dir"));

            // 主机信息
            InetAddress localHost = InetAddress.getLocalHost();
            info.put("hostName", localHost.getHostName());
            info.put("hostAddress", localHost.getHostAddress());

            // CPU信息
            info.put("availableProcessors", Runtime.getRuntime().availableProcessors());

            // 内存信息
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                info.put("totalPhysicalMemory", formatSize(sunOsBean.getTotalPhysicalMemorySize()));
                info.put("freePhysicalMemory", formatSize(sunOsBean.getFreePhysicalMemorySize()));
                info.put("totalSwapSpace", formatSize(sunOsBean.getTotalSwapSpaceSize()));
                info.put("freeSwapSpace", formatSize(sunOsBean.getFreeSwapSpaceSize()));
            }

            // JVM信息
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("jvmTotalMemory", formatSize(Runtime.getRuntime().totalMemory()));
            info.put("jvmFreeMemory", formatSize(Runtime.getRuntime().freeMemory()));
            info.put("jvmMaxMemory", formatSize(Runtime.getRuntime().maxMemory()));

            // 磁盘信息
            File[] roots = File.listRoots();
            if (roots != null && roots.length > 0) {
                Map<String, Object> diskInfo = new HashMap<>();
                for (File root : roots) {
                    Map<String, String> disk = new HashMap<>();
                    disk.put("totalSpace", formatSize(root.getTotalSpace()));
                    disk.put("freeSpace", formatSize(root.getFreeSpace()));
                    disk.put("usableSpace", formatSize(root.getUsableSpace()));
                    diskInfo.put(root.getAbsolutePath(), disk);
                }
                info.put("diskInfo", diskInfo);
            }

        } catch (UnknownHostException e) {
            log.warn("获取主机信息失败: {}", e.getMessage());
            info.put("error", "获取主机信息失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("获取系统信息失败: {}", e.getMessage(), e);
            info.put("error", "获取系统信息失败: " + e.getMessage());
        }

        return info;
    }

    /**
     * 获取CPU使用率
     * @return CPU使用率（百分比）
     */
    @Tool(description = "获取当前服务器的CPU使用率，返回百分比数值，例如35.5表示35.5%")
    public Double getCpuUsage() {
        log.info("MCP调用-获取CPU使用率");
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                // 系统CPU使用率，0.0-1.0之间
                double cpuUsage = sunOsBean.getSystemCpuLoad() * 100;
                return Double.parseDouble(DECIMAL_FORMAT.format(cpuUsage));
            }
        } catch (Exception e) {
            log.error("获取CPU使用率失败: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取内存使用详情
     * @return 内存使用信息Map
     */
    @Tool(description = "获取当前服务器的内存使用详情，包括总内存、已用内存、可用内存、使用率等信息")
    public Map<String, Object> getMemoryUsage() {
        log.info("MCP调用-获取内存使用情况");
        Map<String, Object> memoryInfo = new HashMap<>();

        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                long total = sunOsBean.getTotalPhysicalMemorySize();
                long free = sunOsBean.getFreePhysicalMemorySize();
                long used = total - free;
                double usage = (double) used / total * 100;

                memoryInfo.put("totalMemory", formatSize(total));
                memoryInfo.put("usedMemory", formatSize(used));
                memoryInfo.put("freeMemory", formatSize(free));
                memoryInfo.put("usagePercent", Double.parseDouble(DECIMAL_FORMAT.format(usage)));
                memoryInfo.put("totalSwap", formatSize(sunOsBean.getTotalSwapSpaceSize()));
                memoryInfo.put("freeSwap", formatSize(sunOsBean.getFreeSwapSpaceSize()));
            }

            // JVM内存
            Runtime runtime = Runtime.getRuntime();
            long jvmTotal = runtime.totalMemory();
            long jvmFree = runtime.freeMemory();
            long jvmUsed = jvmTotal - jvmFree;
            double jvmUsage = (double) jvmUsed / runtime.maxMemory() * 100;

            memoryInfo.put("jvmTotalMemory", formatSize(jvmTotal));
            memoryInfo.put("jvmUsedMemory", formatSize(jvmUsed));
            memoryInfo.put("jvmFreeMemory", formatSize(jvmFree));
            memoryInfo.put("jvmMaxMemory", formatSize(runtime.maxMemory()));
            memoryInfo.put("jvmUsagePercent", Double.parseDouble(DECIMAL_FORMAT.format(jvmUsage)));

        } catch (Exception e) {
            log.error("获取内存信息失败: {}", e.getMessage(), e);
            memoryInfo.put("error", "获取内存信息失败: " + e.getMessage());
        }

        return memoryInfo;
    }

    /**
     * 获取磁盘使用情况
     * @param path 磁盘路径，默认获取所有磁盘
     * @return 磁盘使用信息
     */
    @Tool(description = "获取指定磁盘路径的使用情况，不传路径则返回所有磁盘的信息，包含总容量、已用、可用、使用率")
    public Map<String, Object> getDiskUsage(
            @ToolParam(description = "要查询的磁盘路径，例如: / (Linux)、C:\\ (Windows)，可选，默认返回所有磁盘") String path) {
        log.info("MCP调用-获取磁盘使用情况, 路径: {}", path);
        Map<String, Object> result = new HashMap<>();

        try {
            File[] roots = File.listRoots();
            if (roots == null || roots.length == 0) {
                result.put("error", "未找到磁盘信息");
                return result;
            }

            if (path != null && !path.trim().isEmpty()) {
                // 查询指定路径
                File targetDisk = null;
                for (File root : roots) {
                    if (path.startsWith(root.getAbsolutePath())) {
                        targetDisk = root;
                        break;
                    }
                }
                if (targetDisk == null) {
                    result.put("error", "未找到指定路径对应的磁盘: " + path);
                    return result;
                }
                result.put(path, getDiskInfo(targetDisk));
            } else {
                // 查询所有磁盘
                for (File root : roots) {
                    result.put(root.getAbsolutePath(), getDiskInfo(root));
                }
            }

        } catch (Exception e) {
            log.error("获取磁盘信息失败: {}", e.getMessage(), e);
            result.put("error", "获取磁盘信息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取系统运行时间
     * @return 系统运行时间信息
     */
    @Tool(description = "获取当前系统和JVM的运行时间，返回格式化后的时长信息")
    public Map<String, Object> getUptime() {
        log.info("MCP调用-获取系统运行时间");
        Map<String, Object> uptimeInfo = new HashMap<>();

        try {
            // JVM运行时间
            long jvmUptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
            uptimeInfo.put("jvmUptime", formatDuration(jvmUptimeMs));
            uptimeInfo.put("jvmUptimeMs", jvmUptimeMs);
            uptimeInfo.put("jvmStartTime", ManagementFactory.getRuntimeMXBean().getStartTime());

            // 系统启动时间（通过OS Bean获取）
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
                // 注意：这个方法在不同JDK版本可能有差异，部分版本返回的是进程启动时间
                uptimeInfo.put("systemProcessCpuTime", sunOsBean.getProcessCpuTime() / 1000000 + "ms");
            }

        } catch (Exception e) {
            log.error("获取运行时间失败: {}", e.getMessage(), e);
            uptimeInfo.put("error", "获取运行时间失败: " + e.getMessage());
        }

        return uptimeInfo;
    }

    /**
     * 获取单个磁盘的信息
     */
    private Map<String, Object> getDiskInfo(File disk) {
        Map<String, Object> info = new HashMap<>();
        long total = disk.getTotalSpace();
        long free = disk.getFreeSpace();
        long usable = disk.getUsableSpace();
        long used = total - free;
        double usage = (double) used / total * 100;

        info.put("totalSpace", formatSize(total));
        info.put("usedSpace", formatSize(used));
        info.put("freeSpace", formatSize(free));
        info.put("usableSpace", formatSize(usable));
        info.put("usagePercent", Double.parseDouble(DECIMAL_FORMAT.format(usage)));
        return info;
    }

    /**
     * 格式化文件大小
     */
    private String formatSize(long size) {
        if (size < MB) {
            return size / 1024 + " KB";
        } else if (size < GB) {
            return DECIMAL_FORMAT.format((double) size / MB) + " MB";
        } else {
            return DECIMAL_FORMAT.format((double) size / GB) + " GB";
        }
    }

    /**
     * 格式化时长
     */
    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟 %d秒", days, hours % 24, minutes % 60, seconds % 60);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟 %d秒", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
