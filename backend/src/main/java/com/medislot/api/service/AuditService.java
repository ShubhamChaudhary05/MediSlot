package com.medislot.api.service;

import com.medislot.api.domain.AppUser;
import com.medislot.api.domain.AuditLog;
import com.medislot.api.repository.AuditLogRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository logs;
    public AuditService(AuditLogRepository logs) { this.logs = logs; }
    public void record(AppUser actor, String action, String type, UUID id, String detail) {
        logs.save(new AuditLog(actor, action, type, id, detail));
    }
}
