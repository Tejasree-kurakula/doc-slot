package com.careconnect.repository;

import com.careconnect.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorNameAndTimeSlot(String doctorName, String timeSlot);
    List<Appointment> findByEmail(String email);
}