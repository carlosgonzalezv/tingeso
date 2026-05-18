import axios from 'axios';

const API_URL = "http://localhost:8080/api/v1/booking";

// 1. Create a new booking
const createBooking = async (token, bookingData) => {
    try {
        return await axios.post(`${API_URL}/create`, bookingData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    } catch (error) {
        console.error("Error en BookingService (createBooking):", error);
        throw error;
    }
};

// 2. CLIENT VISIBILITY: Get bookings of authenticated user by email
const getBookingsByEmail = async (token, email) => {
    try {
        const response = await axios.get(`${API_URL}/my-bookings/${email}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getBookingsByEmail):", error);
        throw error;
    }
};

// 3. AGENCY VISIBILITY: Get all bookings in the system (ADMIN Role)
const getAllBookings = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getAllBookings):", error);
        throw error;
    }
};

// 4. STATUS MANAGEMENT: Update booking status (ADMIN Role)
const updateBookingStatus = async (token, bookingId, newStatus) => {
    try {
        const response = await axios.put(`${API_URL}/${bookingId}/status`, {}, {
            params: {
                newStatus: newStatus
            },
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (updateBookingStatus):", error);
        throw error;
    }
};

// 5. REPORTS: Get general dashboard metrics (ADMIN Role)
const getDashboardStats = async (token) => {
    try {
        const response = await axios.get(`${API_URL}/dashboard/stats`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getDashboardStats):", error);
        throw error;
    }
};

// 6. REPORTS: Get chronological sales list by date range (ADMIN Role)
const getSalesReport = async (token, start, end) => {
    try {
        const response = await axios.get(`${API_URL}/reports/sales`, {
            params: { start, end },
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getSalesReport):", error);
        throw error;
    }
};

// 7. REPORTS: Get package demand ranking by date range (ADMIN Role)
const getRankingReport = async (token, start, end) => {
    try {
        const response = await axios.get(`${API_URL}/reports/ranking`, {
            params: { start, end },
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getRankingReport):", error);
        throw error;
    }
};

export default {
    createBooking,
    getBookingsByEmail,
    getAllBookings,
    updateBookingStatus,
    getDashboardStats,
    getSalesReport,
    getRankingReport
};