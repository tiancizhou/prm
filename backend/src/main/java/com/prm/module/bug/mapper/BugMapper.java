package com.prm.module.bug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prm.module.bug.entity.Bug;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BugMapper extends BaseMapper<Bug> {

    @Select("SELECT COUNT(*) FROM pm_bug WHERE sprint_id = #{sprintId} " +
            "AND severity IN ('CRITICAL','BLOCKER') AND status NOT IN ('CLOSED','VERIFIED') AND deleted = 0")
    int countOpenCriticalInSprint(Long sprintId);
}
