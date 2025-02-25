package com.example.ticketmanagement.service;

import com.example.ticketmanagement.model.*;
import com.example.ticketmanagement.repository.TicketRepository;
import com.example.ticketmanagement.repository.UserRepository;
import com.example.ticketmanagement.request.TicketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);


    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Ticket createTicket(TicketRequest ticketRequest, Long employeeId) {
        logger.info("Creating ticket for user ID: {}", employeeId);

        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(employeeId);
        if (userOptional.isEmpty()) {
            logger.error("User not found with ID: {}", employeeId);
            throw new AccessDeniedException("User not found");
        }

        User user = userOptional.get();
        logger.info("Found user: {}", user);

        // Check if the user has the role "Employee"
        if (user.getRole() != Role.Employee) {
            logger.warn("User {} does not have the required role to create a ticket", user.getId());
            throw new AccessDeniedException("Only employees can create tickets");
        }

        // Create the Ticket entity and map fields from TicketRequest
        logger.info("User is an Employee, proceeding with ticket creation...");

        Ticket ticket = new Ticket();
        ticket.setTitle(ticketRequest.getTitle());
        ticket.setDescription(ticketRequest.getDescription());
        ticket.setPriority(ticketRequest.getPriority());
        ticket.setCategory(ticketRequest.getCategory());
        ticket.setCreationDate(ticketRequest.getCreationDate());
        ticket.setStatus(ticketRequest.getStatus());

        // Set the Employee (creator) of the ticket
        ticket.setCreatedBy(user);

        // Save the ticket and return it
        Ticket savedTicket = ticketRepository.save(ticket);
        logger.info("Ticket saved successfully with ID: {}", savedTicket.getId());

        return savedTicket;
    }




    public List<Ticket> getAllTickets(Long userId) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new AccessDeniedException("User not found");
        }

        User user = userOptional.get();

        // Check if the user's role is "ITSupport"
        if (user.getRole() != Role.ITSupport) {
            throw new AccessDeniedException("Only IT Support can access all tickets");
        }

        // Return all tickets
        return ticketRepository.findAll();
    }




    public Ticket updateTicketStatus(Long ticketId, Status newStatus, Long userId) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new AccessDeniedException("User not found");
        }

        User user = userOptional.get();

        // Check if the user's role is "ITSupport"
        if (user.getRole() != Role.ITSupport) {
            throw new AccessDeniedException("Only IT Support can update ticket status");
        }

        // Retrieve the ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Save the old status before updating
        Status oldStatus = ticket.getStatus();
        ticket.setStatus(newStatus);

        // Save the updated ticket
        Ticket updatedTicket = ticketRepository.save(ticket);

        // Log the status change
        auditLogService.logStatusChange(ticketId, user, oldStatus, newStatus);

        return updatedTicket;
    }

    public List<Ticket> getTicketsByEmployee(Long employeeId) {
        logger.info("Fetching tickets for employee ID: {}", employeeId);

        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(employeeId);
        if (userOptional.isEmpty()) {
            logger.error("User not found with ID: {}", employeeId);
            throw new AccessDeniedException("User not found");
        }

        User user = userOptional.get();
        logger.info("Found user: {}", user);

        // Check if the user has the role "Employee"
        if (user.getRole() != Role.Employee) {
            logger.warn("User {} does not have the required role to view tickets", user.getId());
            throw new AccessDeniedException("Only employees can view tickets");
        }

        // Fetch tickets associated with the employee
        logger.info("User is an Employee, fetching tickets...");

        return ticketRepository.findByCreatedById(employeeId);
    }

    public List<Ticket> getTickets(Long ticketId, Status status) {
        logger.info("Fetching tickets with ticket ID: {} and status: {}", ticketId, status);

        // Log inputs for better visibility
        if (ticketId == null) {
            logger.debug("Ticket ID is null.");
        } else {
            logger.debug("Ticket ID: {}", ticketId);
        }

        if (status == null) {
            logger.debug("Status is null.");
        } else {
            logger.debug("Status: {}", status);
        }

        // If both ticketId and status are provided
        if (ticketId != null && status != null) {
            logger.debug("Querying by both ticketId and status");
            return ticketRepository.findByIdAndStatus(ticketId, status);
        }
        // If only ticketId is provided
        else if (ticketId != null) {
            logger.debug("Querying by ticketId only");
            // Handle Optional and return a list
            Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
            return ticketOptional.map(Collections::singletonList) // Convert to list if present
                    .orElse(Collections.emptyList()); // Return empty list if not present
        }
        // If only status is provided
        else if (status != null) {
            logger.debug("Querying by status only");
            return ticketRepository.findByStatus(status);
        }
        // If neither is provided, return all tickets
        else {
            logger.debug("Querying all tickets as no filters are provided");
            List<Ticket> tickets = ticketRepository.findAll();
            return tickets.isEmpty() ? Collections.emptyList() : tickets;
        }
    }







}