package cc.shiyi.search.db;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SearchFrequencyMapper extends BaseMapper<SearchFrequency> {

    @Insert("insert into search_frequency(term, frequency, last_access_time) values(#{term}, 1, now()) on duplicate key update frequency = frequency + 1, last_access_time = now()")
    void updateFrequency(@Param("term") String term);

    @Select("select * from search_frequency")
    List<SearchFrequency> getAll();
}
