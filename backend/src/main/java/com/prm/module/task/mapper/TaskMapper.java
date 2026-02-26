package com.prm.module.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prm.module.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    @Select("SELECT COALESCE(SUM(spent_hours), 0) FROM pm_task_worklog WHERE task_id = #{taskId}")
    BigDecimal sumSpentHoursByTaskId(Long taskId);
}
