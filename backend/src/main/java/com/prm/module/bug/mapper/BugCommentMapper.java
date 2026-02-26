package com.prm.module.bug.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prm.module.bug.entity.BugComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BugCommentMapper extends BaseMapper<BugComment> {
}
