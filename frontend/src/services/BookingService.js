import axios from 'axios';

const API_URL = "http://localhost:8080/api/v1/booking";

const createBooking = async (token, bookingData) => {
    try {
        // Ahora enviamos bookingData directamente como el body
        const response = await axios.post(`${API_URL}/create`, bookingData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error("Error en BookingService:", error);
        throw error;
    }
};

export default { createBooking };