package com.example.batch.Domain;

import java.sql.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class JobStatus {
	@JsonProperty
    private long batchId;
	@JsonProperty
    private String status;
	@JsonProperty
    private Timestamp startTime;
	@JsonProperty
    private Timestamp endTime;
	public long getBatchId() {
		return batchId;
	}
	public void setBatchId(long batchId) {
		this.batchId = batchId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getStartTime() {
		return startTime;
	}
	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
	public Timestamp getEndTime() {
		return endTime;
	}
	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}
}