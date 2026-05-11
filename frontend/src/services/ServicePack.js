import axios from 'axios';
const API_URL = "/api/v1/user";

export const syncUserWithBackend = async (keycloak) => {
    const userData = {
        keycloackID: keycloak.tokenParsed.sub,
        username: keycloak.tokenParsed.preferred_username,
        email: keycloak.tokenParsed.email,
        name: keycloak.tokenParsed.name,
        idDocument: keycloak.tokenParsed.idDocument,
        cellphone: keycloak.tokenParsed.cellphone,
        nationality: keycloak.tokenParsed.nationality,
        rol: "USER",
        statement: "Activo"
    };
    try {
        const response = await axios.post(`${API_URL}/sync`, userData, {
            headers: { 'Authorization': `Bearer ${keycloak.token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error syncing user:", error);
        throw error;
    }
};

export const updateUserInfo = async (keycloak, formData) => {
    const keycloakId = keycloak.tokenParsed.sub;
    const updateData = {
        name: formData.name,
        idDocument: formData.idDocument,
        cellphone: formData.cellphone,
        nationality: formData.nationality
    };
    try {
        const response = await axios.put(`${API_URL}/update/${keycloakId}`, updateData, {
            headers: { 'Authorization': `Bearer ${keycloak.token}` }
        });
        return response.data; // Retornamos el dato limpio
    } catch (error) {
        throw error.response?.data || "Error al actualizar la información";
    }
};

export const getUserInfo = async (keycloak, email) => {
    if (!email) return null;
    try {
        const response = await axios.get(`${API_URL}/${email}`, {
            headers: { 'Authorization': `Bearer ${keycloak.token}` }
        });
        return response.data; // Retornamos el dato limpio
    } catch (error) {
        console.error("Error fetching user info:", error);
        throw error;
    }
};