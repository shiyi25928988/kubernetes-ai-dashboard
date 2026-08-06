package cc.shiyi.coleditor.common.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ListPageUtil {

    public static <T> List<T> page(List<T> list, Integer pageNum, Integer pageSize){
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex+pageSize, list.size());
        if(startIndex > endIndex){
            return Collections.EMPTY_LIST;
        }
        List<T> page = new ArrayList<>(list.subList(startIndex, endIndex));
        return page;
    }
}
