import axios from 'axios';

const API_URL = "http://localhost:8080/api/v1/booking";

// 1. Crear una nueva reserva
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

// 2. VISIBILIDAD CLIENTE: Obtener las reservas del usuario autenticado por su correo
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

// 3. VISIBILIDAD AGENCIA: Obtener todas las reservas del sistema (Rol ADMIN)
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

// 4. GESTIÓN DE ESTADOS: Actualizar el estado de una reserva (Rol ADMIN)
const updateBookingStatus = async (token, bookingId, newStatus) => {
    try {
        // SOLUCIÓN LIMPIA: Pasamos un objeto vacío como body ({}) y los params estructurados
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

export default {
    createBooking,
    getBookingsByEmail,
    getAllBookings,
    updateBookingStatus
};