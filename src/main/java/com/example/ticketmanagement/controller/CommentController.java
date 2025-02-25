package com.example.ticketmanagement.controller;

import com.example.ticketmanagement.model.Comment;
import com.example.ticketmanagement.model.MyUserDetails;
import com.example.ticketmanagement.model.User;
import com.example.ticketmanagement.service.CommentService;
import com.example.ticketmanagement.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private CommentService commentService;

    // Add a comment to a ticket
    @PostMapping("/add/{ticketId}")
    public ResponseEntity<?> addComment(
            @PathVariable Long ticketId,
            @RequestParam String content,
            Authentication authentication) {

        User principal = new User();
        try {
            // Log incoming request
            logger.info("Received request to add comment to ticket {}: {}", ticketId, content);

            // Get authenticated user
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            principal = myUserDetails.getUser();

            logger.info("Authenticated User: {} (Role: {})", principal.getName(), principal.getRole());

            // Add comment
            Comment savedComment = commentService.addComment(ticketId, content, principal.getId());

            logger.info("Comment added successfully to ticket {}", ticketId);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);

        } catch (EntityNotFoundException e) {
            logger.warn("Ticket {} not found", ticketId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found");

        } catch (AccessDeniedException e) {
            logger.warn("User {} does not have permission to add a comment", principal.getId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");

        } catch (Exception e) {
            logger.error("Error adding comment to ticket {}: {}", ticketId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
        }

    }
}
