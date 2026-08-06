package cc.shiyi.dashboard.config;

import cc.shiyi.dashboard.advisor.SolverProvider;
import cc.shiyi.dashboard.dto.a2a.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * A2ARequestHandler 单元测试
 * 验证 JSON-RPC 方法分发与求解器路由逻辑。
 */
class A2ARequestHandlerTest {

    private A2ARequestHandler handler;
    private SolverProvider mockSolverProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        handler = new A2ARequestHandler();
        mockSolverProvider = Mockito.mock(SolverProvider.class);
        objectMapper = new ObjectMapper();

        handler.setSolverProviders(List.of(mockSolverProvider));
        handler.setObjectMapper(objectMapper);

        when(mockSolverProvider.getSolverType()).thenReturn("mpi");
        when(mockSolverProvider.getSkills()).thenReturn(List.of());
    }

    @Test
    void handleMessageSend_returnsTaskFromSolver() {
        // 准备 mock 求解器返回
        Task expectedTask = buildSampleTask(TaskState.COMPLETED);
        when(mockSolverProvider.processMessage(any(), anyString())).thenReturn(expectedTask);

        // 构造 message/send 请求
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("1");
        request.setMethod("message/send");
        request.setParams(Map.of(
                "message", Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("kind", "text", "text", "创建MPI作业"))
                )
        ));

        JsonRpcResponse response = handler.handle(request);

        assertNotNull(response);
        assertEquals("1", response.getId());
        assertNotNull(response.getResult());
        assertNull(response.getError());
        // A2A 1.0：SendMessage 响应 result 为 SendMessageResponse，Task 包装在 task 字段下
        assertTrue(response.getResult() instanceof SendMessageResponse);
        assertEquals(expectedTask, ((SendMessageResponse) response.getResult()).getTask());
    }

    @Test
    void handleMessageSend_acceptsA2aV1Format() {
        // A2A 1.0：方法名 SendMessage（PascalCase），role=ROLE_USER，Part 无 kind（成员存在判别）
        Task expectedTask = buildSampleTask(TaskState.COMPLETED);
        when(mockSolverProvider.processMessage(any(), anyString())).thenReturn(expectedTask);

        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("7");
        request.setMethod("SendMessage");
        request.setParams(Map.of(
                "message", Map.of(
                        "role", "ROLE_USER",
                        "messageId", "msg-1",
                        "parts", List.of(Map.of("text", "创建一个简单的MPI Hello World作业", "mediaType", "text/plain"))
                ),
                "configuration", Map.of("acceptedOutputModes", List.of("text/plain"))
        ));

        JsonRpcResponse response = handler.handle(request);

        assertNotNull(response);
        assertNull(response.getError());
        assertTrue(response.getResult() instanceof SendMessageResponse);
        assertEquals(expectedTask, ((SendMessageResponse) response.getResult()).getTask());
    }

    @Test
    void handleTasksGet_returnsTaskStatus() {
        Task expectedTask = buildSampleTask(TaskState.WORKING);
        when(mockSolverProvider.getTask(anyString())).thenReturn(expectedTask);

        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("2");
        request.setMethod("tasks/get");
        request.setParams(Map.of("id", "123"));

        JsonRpcResponse response = handler.handle(request);

        assertNotNull(response);
        assertEquals("2", response.getId());
        // A2A 1.0：GetTask 响应 result 为 GetTaskResponse
        assertTrue(response.getResult() instanceof GetTaskResponse);
        assertEquals(expectedTask, ((GetTaskResponse) response.getResult()).getTask());
    }

    @Test
    void handleTasksCancel_returnsCanceledTask() {
        Task expectedTask = buildSampleTask(TaskState.CANCELED);
        when(mockSolverProvider.cancelTask(anyString())).thenReturn(expectedTask);

        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("3");
        request.setMethod("tasks/cancel");
        request.setParams(Map.of("id", "123"));

        JsonRpcResponse response = handler.handle(request);

        assertNotNull(response);
        assertNotNull(response.getResult());
        Task task = (Task) response.getResult();
        assertEquals(TaskState.CANCELED, task.getStatus().getState());
    }

    @Test
    void handleUnknownMethod_returnsMethodNotFoundError() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("4");
        request.setMethod("unknown/method");

        JsonRpcResponse response = handler.handle(request);

        assertNotNull(response);
        assertNull(response.getResult());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
    }

    @Test
    void handleNullMethod_returnsInvalidRequestError() {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("5");
        request.setMethod(null);

        JsonRpcResponse response = handler.handle(request);

        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals(-32600, response.getError().getCode());
    }

    @Test
    void routeSolverProvider_bySolverTypeMetadata() {
        SolverProvider secondProvider = Mockito.mock(SolverProvider.class);
        when(secondProvider.getSolverType()).thenReturn("cfd");
        when(secondProvider.getSkills()).thenReturn(List.of());
        handler.setSolverProviders(List.of(mockSolverProvider, secondProvider));

        Task cfdTask = buildSampleTask(TaskState.COMPLETED);
        cfdTask.setId("cfd-task");
        when(secondProvider.processMessage(any(), anyString())).thenReturn(cfdTask);
        when(mockSolverProvider.processMessage(any(), anyString())).thenReturn(buildSampleTask(TaskState.COMPLETED));

        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc("2.0");
        request.setId("6");
        request.setMethod("message/send");
        request.setParams(Map.of(
                "message", Map.of("role", "user", "parts", List.of(Map.of("kind", "text", "text", "test"))),
                "metadata", Map.of("solverType", "cfd")
        ));

        JsonRpcResponse response = handler.handle(request);
        Task task = ((SendMessageResponse) response.getResult()).getTask();
        assertEquals("cfd-task", task.getId());
    }

    @Test
    void jsonRpcResponse_successFactory() {
        JsonRpcResponse response = JsonRpcResponse.success("id1", "result");
        assertEquals("2.0", response.getJsonrpc());
        assertEquals("id1", response.getId());
        assertEquals("result", response.getResult());
        assertNull(response.getError());
    }

    @Test
    void jsonRpcResponse_errorFactory() {
        JsonRpcResponse response = JsonRpcResponse.error("id2", -32601, "not found");
        assertEquals("id2", response.getId());
        assertNotNull(response.getError());
        assertEquals(-32601, response.getError().getCode());
        assertEquals("not found", response.getError().getMessage());
    }

    @Test
    void taskState_enumValuesPresent() {
        assertNotNull(TaskState.valueOf("SUBMITTED"));
        assertNotNull(TaskState.valueOf("WORKING"));
        assertNotNull(TaskState.valueOf("COMPLETED"));
        assertNotNull(TaskState.valueOf("FAILED"));
        assertNotNull(TaskState.valueOf("CANCELED"));
    }

    @Test
    void taskState_serializesToA2aV1WireName() throws Exception {
        // A2A 1.0：TaskState 线名带 TASK_STATE_ 前缀（@JsonProperty）
        TaskStatus status = new TaskStatus();
        status.setState(TaskState.COMPLETED);
        String json = objectMapper.writeValueAsString(status);
        assertTrue(json.contains("TASK_STATE_COMPLETED"));
        assertFalse(json.contains("\"COMPLETED\""));
    }

    /**
     * 构造测试用 Task 对象
     */
    private Task buildSampleTask(TaskState state) {
        Task task = new Task();
        task.setId("test-task");
        task.setContextId("test-ctx");

        TaskStatus status = new TaskStatus();
        status.setState(state);
        status.setTimestamp("2026-07-06T12:00:00");
        task.setStatus(status);

        Part part = new Part();
        part.setText("test response");

        Artifact artifact = new Artifact();
        artifact.setName("response");
        artifact.setIndex(0);
        artifact.setParts(List.of(part));
        task.setArtifacts(List.of(artifact));

        return task;
    }
}
