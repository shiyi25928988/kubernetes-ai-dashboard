package cc.shiyi.coleditor.common.http;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseWrapper<T> {
    T data;
    String message;
    int code;

    public ResponseWrapper success(T data, String message) {
        this.code = ResponseCode.SUCCESS;
        this.data = data;
        this.message = message;
        return this;
    }

    public ResponseWrapper success(T data) {
        this.code = ResponseCode.SUCCESS;
        this.data = data;
        return this;
    }

    public ResponseWrapper success(String message) {
        this.code = ResponseCode.SUCCESS;
        this.message = message;
        return this;
    }

    public ResponseWrapper success() {
        this.code = ResponseCode.SUCCESS;
        return this;
    }

    public ResponseWrapper fail(String message) {
        this.code = ResponseCode.FAIL;
        this.message = message;
        return this;
    }

    public ResponseWrapper fail(T data, String message) {
        this.code = ResponseCode.FAIL;
        this.data = data;
        this.message = message;
        return this;
    }

    public ResponseWrapper fail() {
        this.code = ResponseCode.FAIL;
        return this;
    }

    public boolean isOK() {
        return this.code == ResponseCode.SUCCESS;
    }

    public ResponseWrapper(T data, int code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public ResponseWrapper(){

    }
}
