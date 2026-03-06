package com.example.rqm.models;

public class SyncRun {
    public long id;
    public String syncUuid;
    public String startedAt;
    public String finishedAt;
    public String status;
    public int pendingTotal;
    public int pendingSent;
    public int materialsUpdated;
    public int conflictsFound;
    public int errorsCount;
    public String message;
    public String deviceId;
    public String userId;
    public String tenantId;
    public String source;
    public boolean uploadedToServer;
    public String createdAt;
}