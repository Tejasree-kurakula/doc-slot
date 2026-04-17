package com.careconnect.controller;

import com.careconnect.entity.Appointment;
import com.careconnect.repository.AppointmentRepository;
import com.careconnect.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AppointmentController {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private EmailService emailService;
    
    private final List<String> TIME_SLOTS = Arrays.asList(
        "09:30 AM", "10:30 AM", "11:30 AM", "12:30 PM",
        "02:00 PM", "03:00 PM", "04:00 PM", "05:30 PM"
    );
    
    private final Map<String, List<Map<String, String>>> DOCTORS = new HashMap<>();
    
    public AppointmentController() {
        DOCTORS.put("Cardiologist", Arrays.asList(
            Map.of("name", "Dr. Arjun Reddy", "exp", "15 years", "rating", "4.9"),
            Map.of("name", "Dr. Neha Verma", "exp", "12 years", "rating", "4.8"),
            Map.of("name", "Dr. K. S. Rao", "exp", "20 years", "rating", "4.9"),
            Map.of("name", "Dr. Priya Sharma", "exp", "10 years", "rating", "4.7")
        ));
        DOCTORS.put("Gynecologist", Arrays.asList(
            Map.of("name", "Dr. Meera Nair", "exp", "14 years", "rating", "4.8"),
            Map.of("name", "Dr. Swati Phadke", "exp", "12 years", "rating", "4.7"),
            Map.of("name", "Dr. Anjali Deshmukh", "exp", "10 years", "rating", "4.8"),
            Map.of("name", "Dr. Ritu Agarwal", "exp", "16 years", "rating", "4.9")
        ));
        DOCTORS.put("Orthopedic", Arrays.asList(
            Map.of("name", "Dr. S. K. Sharma", "exp", "18 years", "rating", "4.9"),
            Map.of("name", "Dr. Vikram Shetty", "exp", "11 years", "rating", "4.8"),
            Map.of("name", "Dr. Ritu Malhotra", "exp", "14 years", "rating", "4.7"),
            Map.of("name", "Dr. Anil Kumar", "exp", "22 years", "rating", "4.9")
        ));
        DOCTORS.put("Dermatologist", Arrays.asList(
            Map.of("name", "Dr. Rajesh Khanna", "exp", "13 years", "rating", "4.7"),
            Map.of("name", "Dr. Preeti Arora", "exp", "12 years", "rating", "4.8"),
            Map.of("name", "Dr. Anil Goel", "exp", "16 years", "rating", "4.8")
        ));
        DOCTORS.put("Neurologist", Arrays.asList(
            Map.of("name", "Dr. Vivek Gupta", "exp", "14 years", "rating", "4.8"),
            Map.of("name", "Dr. Nandini Reddy", "exp", "12 years", "rating", "4.7"),
            Map.of("name", "Dr. Sameer Sinha", "exp", "10 years", "rating", "4.8")
        ));
    }
    
    @GetMapping("/time-slots")
    public ResponseEntity<List<String>> getTimeSlots() {
        return ResponseEntity.ok(TIME_SLOTS);
    }
    
    @GetMapping("/doctors")
    public ResponseEntity<Map<String, List<Map<String, String>>>> getDoctors() {
        return ResponseEntity.ok(DOCTORS);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Backend is running!");
        response.put("message", "CareConnect API is working");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/book-appointment")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Appointment appointment) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Appointment> existing = appointmentRepository.findByDoctorNameAndTimeSlot(
                appointment.getDoctorName(), appointment.getTimeSlot());
            
            if (!existing.isEmpty()) {
                response.put("success", false);
                response.put("message", "❌ This time slot is already booked. Please select another slot.");
                return ResponseEntity.badRequest().body(response);
            }
            
            appointment.setStatus("CONFIRMED");
            appointment.setBookedAt(LocalDateTime.now());
            Appointment saved = appointmentRepository.save(appointment);
            
            emailService.sendAppointmentConfirmation(
                appointment.getEmail(),
                appointment.getPatientName(),
                appointment.getDoctorName(),
                appointment.getTimeSlot(),
                appointment.getService(),
                saved.getId()
            );
            
            response.put("success", true);
            response.put("message", "✅ Appointment booked successfully!");
            response.put("appointmentId", saved.getId());
            response.put("bookingDetails", saved);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Booking failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }
}