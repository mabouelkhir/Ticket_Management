package com.example.ticketmanagement.service;

import com.example.ticketmanagement.model.*;
import com.example.ticketmanagement.repository.CommentRepository;
import com.example.ticketmanagement.repository.TicketRepository;
import com.example.ticketmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Comment addComment(Long ticketId, String content, Long userId) {
        // Check if the user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User not found"));

        // Check if the user's role is "ITSupport"
        if (user.getRole() == null || !user.getRole().equals(Role.ITSupport)) {
            throw new AccessDeniedException("Only IT Support can add comments");
        }

        // Retrieve the ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Create and save the comment
        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setCreatedBy(user);
        comment.setContent(content);

        Comment savedComment = commentRepository.save(comment);

        // Log the comment addition
        auditLogService.logCommentAddition(ticketId, user, content);

        return savedComment;
    }


}