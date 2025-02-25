package com.example.ticketmanagement.service;

import com.example.ticketmanagement.model.AuditLog;
import com.example.ticketmanagement.model.Status;
import com.example.ticketmanagement.model.User;
import com.example.ticketmanagement.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Log a status change
    public void logStatusChange(Long ticketId, User changedBy, Status oldStatus, Status newStatus) {
        String observation = "IT Support " + changedBy.getName() +
                " changed the status of Ticket " + ticketId +
                " from " + oldStatus + " to " + newStatus + ".";
        saveAuditLog(ticketId, changedBy, observation);
    }

    // Log a comment addition
    public void logCommentAddition(Long ticketId, User changedBy, String comment) {
        String observation = "IT Support " + changedBy.getName() +
                " added a comment on Ticket " + ticketId +
                ": " + comment + ".";
        saveAuditLog(ticketId, changedBy, observation);
    }

    // Save the audit log
    private void saveAuditLog(Long ticketId, User changedBy, String observation) {
        AuditLog auditLog = new AuditLog();
        auditLog.setTicketId(ticketId);
        auditLog.setChangedBy(changedBy);
        auditLog.setCreationDate(LocalDateTime.now());
        auditLog.setObservation(observation);
        auditLogRepository.save(auditLog);
    }

    // Get all audit logs for a specific ticket
    public List<AuditLog> getAuditLogsForTicket(Long ticketId) {
        return auditLogRepository.findByTicketId(ticketId);
    }
}