package com.queuemgmt.dto;

import com.queuemgmt.model.QueueStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private String trackingNumber;
    private String userName;
    private Integer queueNumber;
    private Integer peopleAhead;
    private QueueStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
    private String message;
}
