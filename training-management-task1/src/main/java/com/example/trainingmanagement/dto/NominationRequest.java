package com.example.trainingmanagement.dto;

import jakarta.validation.constraints.NotNull;

public class NominationRequest {
    @NotNull
    private Long officerId;

    @NotNull
    private Long trainingId;

    @NotNull
    private Long departmentId;

    public Long getOfficerId() { return officerId; }
    public Long getTrainingId() { return trainingId; }
    public Long getDepartmentId() { return departmentId; }

    public void setOfficerId(Long officerId) { this.officerId = officerId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
