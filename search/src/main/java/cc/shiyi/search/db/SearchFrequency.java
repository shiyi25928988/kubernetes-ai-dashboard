package cc.shiyi.search.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "search_frequency")
public class SearchFrequency {

    @TableId(type = IdType.INPUT)
    @TableField(value = "term")
    String term;

    @TableField(value = "frequency")
    Long frequency;

    @TableField(value = "last_access_time")
    Date lastAccessTime;
}
