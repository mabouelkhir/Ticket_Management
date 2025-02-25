package com.example.ticketmanagement.request;

import com.example.ticketmanagement.model.Category;
import com.example.ticketmanagement.model.Priority;
import com.example.ticketmanagement.model.Status;
import com.example.ticketmanagement.model.User;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketRequest {
    private String title;
    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime creationDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;

    private User createdBy;
}
