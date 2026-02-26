package com.prm.module.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prm.module.project.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    @Select("SELECT project_id FROM pm_project_member WHERE user_id = #{userId}")
    List<Long> selectProjectIdsByUserId(Long userId);
}
