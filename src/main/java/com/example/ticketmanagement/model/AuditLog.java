package com.example.ticketmanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ticketId; // ID of the ticket being modified

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy; // IT support user who made the changes

    private LocalDateTime creationDate; // When the log was created

    @Lob // For long text
    private String observation; // Description of what happened

}
