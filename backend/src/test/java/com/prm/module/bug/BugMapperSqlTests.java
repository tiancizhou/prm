package com.prm.module.bug;

import com.prm.module.bug.mapper.BugMapper;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class BugMapperSqlTests {

    @Test
    void countOpenCriticalInSprintShouldOnlyTreatClosedAsClosedBug() throws NoSuchMethodException {
        Method method = BugMapper.class.getMethod("countOpenCriticalInSprint", Long.class);
        Select select = method.getAnnotation(Select.class);

        assertThat(select).isNotNull();
        String sql = String.join(" ", select.value());

        assertThat(sql).contains("CLOSED");
        assertThat(sql).doesNotContain("VERIFIED");
    }
}
