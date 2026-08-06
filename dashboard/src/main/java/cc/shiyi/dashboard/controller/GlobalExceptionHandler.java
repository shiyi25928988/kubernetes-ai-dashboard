package cc.shiyi.dashboard.controller;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cc.shiyi.dashboard.service.ResourceNotManagedException;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotManagedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseWrapper<Void> handleResourceNotManaged(ResourceNotManagedException ex) {
        return new ResponseWrapper<Void>().fail(ex.getMessage());
    }

    @ExceptionHandler(KubernetesClientException.class)
    public ResponseWrapper<Void> handleKubernetesClientException(KubernetesClientException ex) {
        String message;
        if (ex.getStatus() != null && ex.getStatus().getMessage() != null) {
            message = ex.getStatus().getMessage();
        } else {
            message = ex.getMessage();
        }
        return new ResponseWrapper<Void>().fail(message);
    }
}
