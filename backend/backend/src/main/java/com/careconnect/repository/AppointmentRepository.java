package com.careconnect.repository;

import com.careconnect.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // Check if a doctor is already booked at a specific time on a specific date
    List<Appointment> findByDoctorNameAndTimeSlotAndAppointmentDate(
        String doctorName, String timeSlot, LocalDate appointmentDate);
    
    // Get all appointments for a specific doctor on a specific date
    List<Appointment> findByDoctorNameAndAppointmentDate(String doctorName, LocalDate appointmentDate);
    
    // Get all appointments for a specific doctor
    List<Appointment> findByDoctorName(String doctorName);
    
    // Get appointments by patient email
    List<Appointment> findByEmail(String email);
    
    // Get appointments by status
    List<Appointment> findByStatus(String status);
    
    // Custom query to check availability quickly
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctorName = :doctorName AND a.timeSlot = :timeSlot AND a.appointmentDate = :date AND a.status != 'CANCELLED'")
    boolean isSlotBooked(@Param("doctorName") String doctorName, 
                         @Param("timeSlot") String timeSlot, 
                         @Param("date") LocalDate date);
}