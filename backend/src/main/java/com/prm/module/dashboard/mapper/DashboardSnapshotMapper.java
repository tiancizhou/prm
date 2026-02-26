package com.prm.module.dashboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prm.module.dashboard.entity.DashboardSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Optional;

@Mapper
public interface DashboardSnapshotMapper extends BaseMapper<DashboardSnapshot> {

    @Select("SELECT * FROM pm_dashboard_snapshot WHERE project_id = #{projectId} AND snap_date = #{snapDate} LIMIT 1")
    DashboardSnapshot findByProjectAndDate(Long projectId, LocalDate snapDate);
}
