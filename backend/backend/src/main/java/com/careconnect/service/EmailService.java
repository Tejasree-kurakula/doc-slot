package com.careconnect.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public boolean sendAppointmentConfirmation(String to, String patientName, String doctorName, 
                                                String timeSlot, String service, Long appointmentId,
                                                LocalDate appointmentDate) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("✅ Appointment Confirmed - CareConnect");
            message.setText(String.format(
                "Dear %s,\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "     APPOINTMENT CONFIRMATION\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Your appointment has been successfully confirmed!\n\n" +
                "📋 APPOINTMENT DETAILS:\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "🆔 Booking ID: %d\n" +
                "👨‍⚕️ Doctor: %s\n" +
                "🏥 Service: %s\n" +
                "📅 Date: %s\n" +
                "⏰ Time: %s IST\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                "📍 Location: CareConnect Healthcare\n" +
                "📞 Helpline: 7416688652 / 720390153\n\n" +
                "❗ Please arrive 10 minutes before your scheduled time.\n\n" +
                "Thank you for choosing CareConnect!\n" +
                "✉️ sarah.mumeena@gmail.com\n\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                patientName, appointmentId, doctorName, service, appointmentDate, timeSlot
            ));
            message.setFrom("sarah.mumeena@gmail.com");
            
            mailSender.send(message);
            System.out.println("✅ Email sent to: " + to + " at " + java.time.LocalDateTime.now());
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Email failed to " + to + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}