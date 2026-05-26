import httpClient from '../http-common';
const API_URL = "/booking";

const getBookingById = async (token, bookingId) => {
    try {
        return await httpClient.get(`${API_URL}/summary/${bookingId}`, {
            headers: { Authorization: `Bearer ${token}` }
        });
    } catch (error) {
        console.error("Error en BookingService (getBookingById):", error);
        throw error;
    }
};

const createBooking = async (token, bookingData) => {
    try {
        return await httpClient.post(`${API_URL}/create`, bookingData, {
            headers: { Authorization: `Bearer ${token}` }
        });
    } catch (error) {
        console.error("Error en BookingService (createBooking):", error);
        throw error;
    }
};

const getBookingsByEmail = async (token, email) => {
    try {
        const response = await httpClient.get(`${API_URL}/my-bookings/${email}`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getBookingsByEmail):", error);
        throw error;
    }
};

const getAllBookings = async (token) => {
    try {
        const response = await httpClient.get(`${API_URL}/`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getAllBookings):", error);
        throw error;
    }
};

const updateBookingStatus = async (token, bookingId, newStatus) => {
    try {
        const response = await httpClient.put(`${API_URL}/${bookingId}/status`, {}, {
            params: { newStatus: newStatus },
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (updateBookingStatus):", error);
        throw error;
    }
};

const getDashboardStats = async (token) => {
    try {
        const response = await httpClient.get(`${API_URL}/dashboard/stats`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getDashboardStats):", error);
        throw error;
    }
};

const getSalesReport = async (token, start, end) => {
    try {
        const response = await httpClient.get(`${API_URL}/reports/sales`, {
            params: { start, end },
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getSalesReport):", error);
        throw error;
    }
};

const getRankingReport = async (token, start, end) => {
    try {
        const response = await httpClient.get(`${API_URL}/reports/ranking`, {
            params: { start, end },
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService (getRankingReport):", error);
        throw error;
    }
};

export default {
    getBookingById,
    createBooking,
    getBookingsByEmail,
    getAllBookings,
    updateBookingStatus,
    getDashboardStats,
    getSalesReport,
    getRankingReport
};