package com.careconnect.controller;

import com.careconnect.entity.Appointment;
import com.careconnect.repository.AppointmentRepository;
import com.careconnect.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        // Cardiologists
        DOCTORS.put("Cardiologist", Arrays.asList(
            Map.of("name", "Dr. Arjun Reddy", "exp", "15 years", "rating", "4.9"),
            Map.of("name", "Dr. Neha Verma", "exp", "12 years", "rating", "4.8"),
            Map.of("name", "Dr. K. S. Rao", "exp", "20 years", "rating", "4.9"),
            Map.of("name", "Dr. Priya Sharma", "exp", "10 years", "rating", "4.7")
        ));
        
        // Gynecologists
        DOCTORS.put("Gynecologist", Arrays.asList(
            Map.of("name", "Dr. Meera Nair", "exp", "14 years", "rating", "4.8"),
            Map.of("name", "Dr. Swati Phadke", "exp", "12 years", "rating", "4.7"),
            Map.of("name", "Dr. Anjali Deshmukh", "exp", "10 years", "rating", "4.8"),
            Map.of("name", "Dr. Ritu Agarwal", "exp", "16 years", "rating", "4.9")
        ));
        
        // Orthopedics
        DOCTORS.put("Orthopedic", Arrays.asList(
            Map.of("name", "Dr. S. K. Sharma", "exp", "18 years", "rating", "4.9"),
            Map.of("name", "Dr. Vikram Shetty", "exp", "11 years", "rating", "4.8"),
            Map.of("name", "Dr. Ritu Malhotra", "exp", "14 years", "rating", "4.7"),
            Map.of("name", "Dr. Anil Kumar", "exp", "22 years", "rating", "4.9")
        ));
        
        // Dermatologists
        DOCTORS.put("Dermatologist", Arrays.asList(
            Map.of("name", "Dr. Rajesh Khanna", "exp", "13 years", "rating", "4.7"),
            Map.of("name", "Dr. Preeti Arora", "exp", "12 years", "rating", "4.8"),
            Map.of("name", "Dr. Anil Goel", "exp", "16 years", "rating", "4.8")
        ));
        
        // Neurologists
        DOCTORS.put("Neurologist", Arrays.asList(
            Map.of("name", "Dr. Vivek Gupta", "exp", "14 years", "rating", "4.8"),
            Map.of("name", "Dr. Nandini Reddy", "exp", "12 years", "rating", "4.7"),
            Map.of("name", "Dr. Sameer Sinha", "exp", "10 years", "rating", "4.8")
        ));
    }
    
    // Get all time slots
    @GetMapping("/time-slots")
    public ResponseEntity<List<String>> getTimeSlots() {
        return ResponseEntity.ok(TIME_SLOTS);
    }
    
    // Get all doctors by specialty
    @GetMapping("/doctors")
    public ResponseEntity<Map<String, List<Map<String, String>>>> getDoctors() {
        return ResponseEntity.ok(DOCTORS);
    }
    
    // Get available time slots for a specific doctor on a specific date
    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(
            @RequestParam String doctorName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // If no date provided, use today
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Get all booked slots for this doctor on this date
        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorNameAndAppointmentDate(doctorName, date);
        Set<String> bookedSlots = bookedAppointments.stream()
            .map(Appointment::getTimeSlot)
            .collect(Collectors.toSet());
        
        // Filter available slots (not in booked slots)
        List<String> availableSlots = TIME_SLOTS.stream()
            .filter(slot -> !bookedSlots.contains(slot))
            .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("doctorName", doctorName);
        response.put("date", date);
        response.put("availableSlots", availableSlots);
        response.put("bookedSlots", bookedSlots);
        response.put("allSlots", TIME_SLOTS);
        
        return ResponseEntity.ok(response);
    }
    
    // Check if a specific slot is available for a doctor on a specific date
    @GetMapping("/check-slot")
    public ResponseEntity<Map<String, Object>> checkSlotAvailability(
            @RequestParam String doctorName,
            @RequestParam String timeSlot,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        boolean isBooked = appointmentRepository.isSlotBooked(doctorName, timeSlot, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("doctorName", doctorName);
        response.put("timeSlot", timeSlot);
        response.put("date", date);
        response.put("isAvailable", !isBooked);
        
        if (isBooked) {
            response.put("message", "❌ This slot is already booked for Dr. " + doctorName + " on " + date);
        } else {
            response.put("message", "✅ Slot is available!");
        }
        
        return ResponseEntity.ok(response);
    }
    
    // Book an appointment with availability check
    @PostMapping("/book-appointment")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Appointment appointment) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Set default date if not provided
            if (appointment.getAppointmentDate() == null) {
                appointment.setAppointmentDate(LocalDate.now());
            }
            
            // CRITICAL: Check if this doctor is already booked at this time slot on this date
            boolean isBooked = appointmentRepository.isSlotBooked(
                appointment.getDoctorName(), 
                appointment.getTimeSlot(),
                appointment.getAppointmentDate()
            );
            
            // If slot is already booked, reject the booking
            if (isBooked) {
                response.put("success", false);
                response.put("message", "❌ Dr. " + appointment.getDoctorName() + 
                       " is already booked at " + appointment.getTimeSlot() + 
                       " on " + appointment.getAppointmentDate() + 
                       ". Please select a different time slot or date.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Save appointment to database
            appointment.setStatus("CONFIRMED");
            appointment.setBookedAt(LocalDateTime.now());
            Appointment saved = appointmentRepository.save(appointment);
            
            // Send email confirmation IMMEDIATELY
            boolean emailSent = emailService.sendAppointmentConfirmation(
                appointment.getEmail(),
                appointment.getPatientName(),
                appointment.getDoctorName(),
                appointment.getTimeSlot(),
                appointment.getService(),
                saved.getId(),
                appointment.getAppointmentDate()
            );
            
            response.put("success", true);
            response.put("message", "✅ Appointment confirmed! " + (emailSent ? "Email sent to " + appointment.getEmail() : "Booking saved but email failed"));
            response.put("appointmentId", saved.getId());
            response.put("bookingDetails", saved);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "❌ Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Get all appointments
    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentRepository.findAll());
    }
    
    // Get appointments by email
    @GetMapping("/appointments/{email}")
    public ResponseEntity<List<Appointment>> getAppointmentsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(appointmentRepository.findByEmail(email));
    }
    
    // Get appointments by doctor
    @GetMapping("/appointments/doctor/{doctorName}")
    public ResponseEntity<List<Appointment>> getAppointmentsByDoctor(@PathVariable String doctorName) {
        return ResponseEntity.ok(appointmentRepository.findByDoctorName(doctorName));
    }
    
    // Cancel an appointment
    @PutMapping("/cancel-appointment/{id}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
            
            appointment.setStatus("CANCELLED");
            appointmentRepository.save(appointment);
            
            response.put("success", true);
            response.put("message", "Appointment cancelled successfully. The time slot is now available.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        return ResponseEntity.ok(Map.of("status", "Backend is running!"));
    }
}