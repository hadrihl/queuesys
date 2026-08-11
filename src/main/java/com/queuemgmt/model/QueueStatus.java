package com.queuemgmt.model;

public enum QueueStatus {
    WAITING,      // User is waiting in queue
    CALLED,       // User has been called (it's their turn)
    IN_SERVICE,   // User is currently being served
    COMPLETED,    // Service completed
    CANCELLED     // User cancelled or no-show
}
