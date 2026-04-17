import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API_URL = 'http://localhost:8080/api';

function App() {
  const [formData, setFormData] = useState({
    patientName: '',
    phone: '',
    email: '',
    service: 'Cardiologist',
    doctorName: '',
    timeSlot: '',
    query: '',
    appointmentDate: ''
  });
  
  const [timeSlots, setTimeSlots] = useState([]);
  const [doctors, setDoctors] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [selectedSlot, setSelectedSlot] = useState('');
  const [bookedSlots, setBookedSlots] = useState([]);
  const [selectedDate, setSelectedDate] = useState('');
  const [activeFaq, setActiveFaq] = useState(null);

  useEffect(() => {
    fetchTimeSlots();
    fetchDoctors();
  }, []);

  useEffect(() => {
    if (formData.doctorName && selectedDate) {
      fetchBookedSlots(formData.doctorName, selectedDate);
    }
  }, [formData.doctorName, selectedDate]);

  const fetchTimeSlots = async () => {
    try {
      const res = await axios.get(`${API_URL}/time-slots`);
      setTimeSlots(res.data);
    } catch (error) {
      console.error('Error fetching slots:', error);
      setTimeSlots(["09:30 AM", "10:30 AM", "11:30 AM", "12:30 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:30 PM"]);
    }
  };

  const fetchDoctors = async () => {
    try {
      const res = await axios.get(`${API_URL}/doctors`);
      setDoctors(res.data);
    } catch (error) {
      console.error('Error fetching doctors:', error);
    }
  };

  const checkSlotAvailability = async (doctorName, timeSlot, date) => {
    if (!doctorName || !timeSlot) return true;
    
    try {
      const response = await axios.get(`${API_URL}/check-slot`, {
        params: { doctorName, timeSlot, date }
      });
      return response.data.isAvailable;
    } catch (error) {
      console.error('Error checking slot:', error);
      return true;
    }
  };

  const fetchBookedSlots = async (doctorName, date) => {
    if (!doctorName) return;
    
    try {
      const response = await axios.get(`${API_URL}/available-slots`, {
        params: { doctorName, date }
      });
      setBookedSlots(response.data.bookedSlots || []);
    } catch (error) {
      console.error('Error fetching booked slots:', error);
      setBookedSlots([]);
    }
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    
    if (e.target.name === 'doctorName') {
      setSelectedSlot('');
      setFormData(prev => ({ ...prev, timeSlot: '' }));
      setMessage({ type: '', text: '' });
    }
  };

  const handleDateChange = (e) => {
    const date = e.target.value;
    setSelectedDate(date);
    setFormData(prev => ({ ...prev, appointmentDate: date }));
    setSelectedSlot('');
    setFormData(prev => ({ ...prev, timeSlot: '' }));
    setMessage({ type: '', text: '' });
    
    if (formData.doctorName && date) {
      fetchBookedSlots(formData.doctorName, date);
    }
  };

  const handleSlotSelect = async (slot) => {
    setMessage({ type: '', text: '' });
    
    // Check if slot is booked
    const isAvailable = await checkSlotAvailability(formData.doctorName, slot, selectedDate);
    
    if (!isAvailable) {
      setMessage({ 
        type: 'error', 
        text: `❌ SLOT UNAVAILABLE: Dr. ${formData.doctorName} is already booked at ${slot} on ${selectedDate}. Please select a different time slot.` 
      });
      return;
    }
    
    setSelectedSlot(slot);
    setFormData({ ...formData, timeSlot: slot });
    setMessage({ type: 'success', text: `✅ ${slot} is available! Click "Confirm & Book" to complete.` });
    
    setTimeout(() => {
      if (message.type === 'success') {
        setMessage({ type: '', text: '' });
      }
    }, 3000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.patientName || !formData.phone || !formData.email || !formData.timeSlot) {
      setMessage({ type: 'error', text: 'Please fill all required fields and select a time slot!' });
      return;
    }
    
    if (!formData.doctorName) {
      setMessage({ type: 'error', text: 'Please select a doctor!' });
      return;
    }
    
    if (!selectedDate) {
      setMessage({ type: 'error', text: 'Please select an appointment date!' });
      return;
    }
    
    setLoading(true);
    setMessage({ type: '', text: '' });
    
    try {
      const isAvailable = await checkSlotAvailability(formData.doctorName, formData.timeSlot, selectedDate);
      
      if (!isAvailable) {
        await fetchBookedSlots(formData.doctorName, selectedDate);
        setMessage({ 
          type: 'error', 
          text: `❌ SLOT UNAVAILABLE: Dr. ${formData.doctorName} is already booked at ${formData.timeSlot} on ${selectedDate}.` 
        });
        setSelectedSlot('');
        setFormData(prev => ({ ...prev, timeSlot: '' }));
        setLoading(false);
        return;
      }
      
      const response = await axios.post(`${API_URL}/book-appointment`, formData);
      
      if (response.data.success) {
        setMessage({ 
          type: 'success', 
          text: `✅ APPOINTMENT CONFIRMED! Booking ID: ${response.data.appointmentId}` 
        });
        
        setFormData({
          patientName: '',
          phone: '',
          email: '',
          service: 'Cardiologist',
          doctorName: '',
          timeSlot: '',
          query: '',
          appointmentDate: ''
        });
        setSelectedSlot('');
        setSelectedDate('');
        setBookedSlots([]);
      }
    } catch (error) {
      setMessage({ 
        type: 'error', 
        text: error.response?.data?.message || 'Booking failed. Please try again.' 
      });
      
      if (formData.doctorName && selectedDate) {
        await fetchBookedSlots(formData.doctorName, selectedDate);
      }
      
      if (error.response?.data?.message?.includes('already booked')) {
        setSelectedSlot('');
        setFormData(prev => ({ ...prev, timeSlot: '' }));
      }
    } finally {
      setLoading(false);
    }
  };

  const isSlotBooked = (slot) => {
    return bookedSlots.includes(slot);
  };

  const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const toggleFaq = (index) => {
    setActiveFaq(activeFaq === index ? null : index);
  };

  const faqData = [
    {
      question: "What types of heart conditions do you treat?",
      answer: "We treat a wide range of heart conditions, including coronary artery disease, heart failure, arrhythmias, valvular heart diseases, and congenital heart defects. Our comprehensive services ensure that all aspects of cardiovascular health are addressed."
    },
    {
      question: "What diagnostic tests are available for cardiac conditions?",
      answer: "We offer ECG, Echocardiogram, Stress Test, Holter Monitoring, Cardiac CT Scan, Cardiac MRI, and Angiography for accurate diagnosis of heart conditions."
    },
    {
      question: "What should I expect during my first cardiology consultation?",
      answer: "During your first visit, the cardiologist will review your medical history, perform a physical examination, discuss your symptoms, and may recommend diagnostic tests based on your condition."
    },
    {
      question: "Are there non-surgical treatment options available?",
      answer: "Yes, we offer medications, lifestyle modifications, cardiac rehabilitation, and minimally invasive procedures like angioplasty and stenting as non-surgical treatment options."
    },
    {
      question: "How can I schedule an appointment with a cardiologist?",
      answer: "You can schedule an appointment by filling out the booking form above, calling our helpline at 7416688652 / 720390153, or visiting our hospital directly."
    },
    {
      question: "What is the recovery time after heart surgery?",
      answer: "Recovery time varies depending on the type of surgery. Generally, it takes 6-8 weeks for initial recovery and 3-6 months for full recovery with cardiac rehabilitation."
    },
    {
      question: "Do you accept health insurance?",
      answer: "Yes, we accept all major health insurance plans. Please bring your insurance card and ID proof during your first visit for verification."
    }
  ];

  return (
    <div className="container">
      <nav className="navbar">
        <div className="logo">🏥 CareConnect</div>
        <div className="nav-links">
          <a href="#home">Home</a>
          <a href="#booking">Book Now</a>
          <a href="#faq">FAQs</a>
        </div>
      </nav>

      <section className="hero" id="home">
        <div className="hero-content">
          <h1>Book Appointments with <span>Top Doctors</span></h1>
          <p>Instant online booking • Real-time availability • No waiting time</p>
          <button className="cta-btn" onClick={() => document.getElementById('booking').scrollIntoView({ behavior: 'smooth' })}>
            Book Appointment →
          </button>
        </div>
        <div className="hero-image">
          <img src="https://images.pexels.com/photos/5215024/pexels-photo-5215024.jpeg?auto=compress&cs=tinysrgb&w=600" alt="Doctor" />
        </div>
      </section>

      <section className="booking-section" id="booking">
        <h2>📅 Book Your Appointment</h2>
        <div className="booking-glass">
          <form onSubmit={handleSubmit}>
            <div className="form-row">
              <div className="form-group">
                <label>Full Name *</label>
                <input type="text" name="patientName" value={formData.patientName} onChange={handleChange} required placeholder="Enter your full name" />
              </div>
              <div className="form-group">
                <label>Phone Number *</label>
                <input type="tel" name="phone" value={formData.phone} onChange={handleChange} required placeholder="+91 98765 43210" />
              </div>
            </div>
            
            <div className="form-row">
              <div className="form-group">
                <label>Email Address *</label>
                <input type="email" name="email" value={formData.email} onChange={handleChange} required placeholder="you@example.com" />
              </div>
              <div className="form-group">
                <label>Select Service *</label>
                <select name="service" value={formData.service} onChange={handleChange}>
                  <option value="Cardiologist">❤️ Cardiologist</option>
                  <option value="Gynecologist">🌸 Gynecologist</option>
                  <option value="Orthopedic">🦴 Orthopedic</option>
                  <option value="Dermatologist">🧴 Dermatologist</option>
                  <option value="Neurologist">🧠 Neurologist</option>
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Appointment Date *</label>
                <input 
                  type="date" 
                  name="appointmentDate"
                  value={selectedDate}
                  onChange={handleDateChange}
                  required
                  min={getTodayDate()}
                />
              </div>
              <div className="form-group">
                <label>Select Doctor *</label>
                <select name="doctorName" value={formData.doctorName} onChange={handleChange} required>
                  <option value="">-- Select a doctor --</option>
                  {doctors[formData.service]?.map((doc, idx) => (
                    <option key={idx} value={doc.name}>
                      👨‍⚕️ {doc.name} - {doc.exp} (⭐ {doc.rating})
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-group">
              <label>Available Time Slots (IST) *</label>
              <div className="time-slots">
                {timeSlots.map(slot => {
                  const isBooked = isSlotBooked(slot);
                  const isSelected = selectedSlot === slot;
                  return (
                    <button
                      key={slot}
                      type="button"
                      className={`time-slot-btn ${isSelected ? 'selected' : ''}`}
                      onClick={() => handleSlotSelect(slot)}
                      style={{
                        padding: '10px',
                        margin: '5px',
                        border: isSelected ? '2px solid #4CAF50' : '1px solid #ddd',
                        backgroundColor: isSelected ? '#e8f5e9' : 'white',
                        cursor: 'pointer',
                        borderRadius: '5px'
                      }}
                    >
                      {slot}
                      {isSelected && <span style={{marginLeft: '5px', color: '#4CAF50'}}>✓</span>}
                    </button>
                  );
                })}
              </div>
              <small>
                {formData.doctorName && selectedDate ? (
                  bookedSlots.length > 0 ? 
                    `⚠️ ${bookedSlots.length} slot(s) are already booked. Click on any slot to check availability.` : 
                    '✅ All slots are available! Click on a slot to select.'
                ) : '👆 Select a doctor and date first'}
              </small>
            </div>

            <div className="form-group">
              <label>Symptoms / Query</label>
              <textarea name="query" rows="3" value={formData.query} onChange={handleChange} placeholder="Describe your health concern..."></textarea>
            </div>

            {message.text && (
              <div className={`message ${message.type}`}>
                {message.text}
              </div>
            )}

            <button type="submit" className="submit-btn" disabled={loading || !selectedSlot}>
              {loading ? '⏳ Processing...' : '✅ Confirm & Book Appointment'}
            </button>
          </form>
        </div>
      </section>

      {/* FAQ Section */}
      <section className="faq-section" id="faq">
        <h2>❓ Frequently Asked Questions</h2>
        <div className="faq-container">
          {faqData.map((faq, index) => (
            <div key={index} className="faq-item">
              <div 
                className="faq-question" 
                onClick={() => toggleFaq(index)}
              >
                <span>{faq.question}</span>
                <span className="faq-icon">{activeFaq === index ? '−' : '+'}</span>
              </div>
              {activeFaq === index && (
                <div className="faq-answer">
                  {faq.answer}
                </div>
              )}
            </div>
          ))}
        </div>
      </section>

      <footer>
        <p>📞 Helpline: 7416688652 / 720390153 | ✉️ careconnect@healthcare.com</p>
        <p>© 2026 CareConnect — Smart Healthcare Appointment System</p>
      </footer>
    </div>
  );
}

export default App;