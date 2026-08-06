package cc.shiyi.coleditor.common.http;


import lombok.Data;

import java.util.List;

@Data
public class ListWrapper<T> {
    List<T> data;

    Integer count;

    Integer pageNum;

    Integer pageSize;

    String message;

    int code;

    public static <T> ListWrapper<T> wrap(List<T> data, Integer count, Integer pageNum, Integer pageSize){
        ListWrapper<T> listWrapper = new ListWrapper<>();
        listWrapper.setData(data);
        listWrapper.setCount(count);
        listWrapper.setPageNum(pageNum);
        listWrapper.setPageSize(pageSize);
        listWrapper.setCode(ResponseCode.SUCCESS);
        return listWrapper;
    }
}
