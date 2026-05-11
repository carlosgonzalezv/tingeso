import axios from 'axios';
const API_URL = 'http://localhost:8080/api/v1/bookings';

export const BookingService = {
    createBooking: async (keycloak, packId) => {
        const bookingData = {
            userEmail: keycloak.tokenParsed.email,
            packId: packId,
            bookingDate: new Date().toISOString()
        };

        try {
            const response = await axios.post(`${API_URL}/create`, bookingData, {
                headers: { Authorization: `Bearer ${keycloak.token}` }
            });
            return response.data;
        } catch (error) {
            throw error.response?.data || "No se pudo registrar la reserva";
        }
    }
};