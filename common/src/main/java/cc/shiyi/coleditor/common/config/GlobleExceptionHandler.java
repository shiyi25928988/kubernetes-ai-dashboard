package cc.shiyi.coleditor.common.config;

import cc.shiyi.coleditor.common.http.ResponseWrapper;
import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.exception.SaTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobleExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleExposableException(Exception e) {
        ResponseWrapper<Exception> responseWrapper = new ResponseWrapper<>();
        log.error("", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @ExceptionHandler({SaTokenException.class, SaTokenContextException.class})
    public ResponseEntity<String> handleSaTokenException(SaTokenException e) {
        log.error("", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
}
