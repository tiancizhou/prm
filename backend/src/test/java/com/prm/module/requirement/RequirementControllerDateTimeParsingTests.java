package com.prm.module.requirement;

import com.prm.common.exception.GlobalExceptionHandler;
import com.prm.module.requirement.application.RequirementService;
import com.prm.module.requirement.controller.RequirementController;
import com.prm.module.requirement.dto.RequirementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RequirementControllerDateTimeParsingTests {

    @Mock
    private RequirementService requirementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RequirementController controller = new RequirementController(requirementService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateStatusShouldParseIsoMinutePrecision() throws Exception {
        Long requirementId = 9001L;
        LocalDateTime start = LocalDateTime.of(2026, 3, 4, 9, 15);
        LocalDateTime end = LocalDateTime.of(2026, 3, 4, 11, 45);

        RequirementDTO dto = new RequirementDTO();
        dto.setId(requirementId);
        dto.setStatus("DONE");
        when(requirementService.updateStatus(eq(requirementId), eq("DONE"), eq(start), eq(end))).thenReturn(dto);

        mockMvc.perform(put("/api/requirements/{id}/status", requirementId)
                        .param("status", "DONE")
                        .param("actualStartAt", "2026-03-04T09:15")
                        .param("actualEndAt", "2026-03-04T11:45")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9001));

        verify(requirementService).updateStatus(eq(requirementId), eq("DONE"), eq(start), eq(end));
    }

    @Test
    void updateStatusShouldParseSpaceSeparatedDateTime() throws Exception {
        Long requirementId = 9002L;
        LocalDateTime start = LocalDateTime.of(2026, 3, 4, 9, 15);
        LocalDateTime end = LocalDateTime.of(2026, 3, 4, 11, 45, 30);

        RequirementDTO dto = new RequirementDTO();
        dto.setId(requirementId);
        dto.setStatus("DONE");
        when(requirementService.updateStatus(eq(requirementId), eq("DONE"), eq(start), eq(end))).thenReturn(dto);

        mockMvc.perform(put("/api/requirements/{id}/status", requirementId)
                        .param("status", "DONE")
                        .param("actualStartAt", "2026-03-04 09:15")
                        .param("actualEndAt", "2026-03-04 11:45:30")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9002));

        verify(requirementService).updateStatus(eq(requirementId), eq("DONE"), eq(start), eq(end));
    }

    @Test
    void updateStatusShouldParseOffsetDateTime() throws Exception {
        Long requirementId = 9004L;
        LocalDateTime start = LocalDateTime.of(2026, 3, 4, 9, 15);
        LocalDateTime end = LocalDateTime.of(2026, 3, 4, 11, 45);

        RequirementDTO dto = new RequirementDTO();
        dto.setId(requirementId);
        dto.setStatus("DONE");
        when(requirementService.updateStatus(eq(requirementId), eq("DONE"), eq(start), eq(end))).thenReturn(dto);

        mockMvc.perform(put("/api/requirements/{id}/status", requirementId)
                        .param("status", "DONE")
                        .param("actualStartAt", "2026-03-04T09:15:00+08:00")
                        .param("actualEndAt", "2026-03-04T11:45:00+08:00")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9004));

        verify(requirementService).updateStatus(eq(requirementId), eq("DONE"), eq(start), eq(end));
    }

    @Test
    void updateStatusShouldRejectInvalidDateTimeFormat() throws Exception {
        mockMvc.perform(put("/api/requirements/{id}/status", 9003L)
                        .param("status", "DONE")
                        .param("actualStartAt", "2026/03/04 09:15")
                        .param("actualEndAt", "2026-03-04T11:45")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg", containsString("actualStartAt")));

        verify(requirementService, never()).updateStatus(anyLong(), anyString(), any(), any());
    }
}
