package com.careconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_name", nullable = false)
    private String patientName;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String service;
    
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;
    
    @Column(name = "time_slot", nullable = false)
    private String timeSlot;
    
    @Column(length = 1000)
    private String query;
    
    private String status;
    
    @Column(name = "booked_at")
    private LocalDateTime bookedAt;
    
    public Appointment() {}
    
    // Getters
    public Long getId() { return id; }
    public String getPatientName() { return patientName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getService() { return service; }
    public String getDoctorName() { return doctorName; }
    public String getTimeSlot() { return timeSlot; }
    public String getQuery() { return query; }
    public String getStatus() { return status; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setService(String service) { this.service = service; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public void setQuery(String query) { this.query = query; }
    public void setStatus(String status) { this.status = status; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
}