package com.example.ticketmanagement.controller;

import com.example.ticketmanagement.model.*;
import com.example.ticketmanagement.request.TicketRequest;
import com.example.ticketmanagement.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    // Create a new ticket
    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody TicketRequest ticket) {
        try {
            logger.info("Received request to create ticket: {}", ticket);

            // Get the current authenticated user from the security context
            Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (!(principalObj instanceof MyUserDetails)) {
                logger.error("Principal is not an instance of MyUserDetails: {}", principalObj);
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            MyUserDetails myUserDetails = (MyUserDetails) principalObj;
            User principal = myUserDetails.getUser(); // Assuming you have a method to get the User object

            logger.info("Authenticated User: {}", principal);
            logger.info("User role from SecurityContext: '{}'", principal.getRole());


            if (principal.getRole() != Role.Employee) {
                logger.warn("User does not have permission to create a ticket. Found role: '{}'", principal.getRole());
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
            }


            // If the user is an Employee, proceed with ticket creation
            logger.info("User has permission, proceeding with ticket creation...");
            Ticket createdTicket = ticketService.createTicket(ticket, principal.getId());

            logger.info("Ticket created successfully: {}", createdTicket);
            return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error occurred while creating ticket", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // Get tickets created by a specific employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Ticket>> getTicketsByEmployee(@PathVariable Long employeeId) {
        try {
            // Call the service method to get the tickets for the employee
            List<Ticket> tickets = ticketService.getTicketsByEmployee(employeeId);
            return ResponseEntity.ok(tickets); // Return the list of tickets
        } catch (AccessDeniedException e) {
            // Return a FORBIDDEN status if the employee does not have access
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (Exception e) {
            // Handle any other exceptions and return a BAD_REQUEST status
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Ticket>> getAllTickets(Authentication authentication) {
        try {
            // Get the authenticated user
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            User principal = myUserDetails.getUser();

            // Check if the user has the 'ITSupport' role
            if (principal.getRole() != Role.ITSupport) {
                return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);  // Access denied
            }

            // Proceed to fetch all tickets if the user is IT Support
            List<Ticket> tickets = ticketService.getAllTickets(principal.getId());
            return new ResponseEntity<>(tickets, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        }
    }



    // Update the status of a ticket
    @PutMapping("/update-status/{ticketId}")
    public ResponseEntity<Ticket> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam Status newStatus) {
        try {
            // Get the authenticated user
            MyUserDetails myUserDetails = (MyUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Long userId = myUserDetails.getUser().getId();

            // Update the ticket status
            Ticket updatedTicket = ticketService.updateTicketStatus(ticketId, newStatus, userId);
            return ResponseEntity.ok(updatedTicket);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Ticket>> getTickets(
            @RequestParam(required = false) Long ticketId,
            @RequestParam(required = false) Status status) {

        try {
            // Call the service method to get the filtered tickets
            List<Ticket> tickets = ticketService.getTickets(ticketId, status);
            return ResponseEntity.ok(tickets); // Return the filtered list of tickets
        } catch (IllegalArgumentException e) {
            // Handle invalid enum values (i.e., when an invalid status is provided)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            // Handle any other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
