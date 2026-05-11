import axios from 'axios';
const API_URL = "/api/v1/user";

//Function responsible for registering or synchronizing the user with the local database
//the first time they enter the application or each time they log in.
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
    return await axios.post(`${API_URL}/sync`, userData, {
        headers: {
            'Authorization': `Bearer ${keycloak.token}`
        }
    });
};

// Function to update personal data (Profile Management)
export const updateUserInfo = async (keycloak, formData) => {
    const keycloakId = keycloak.tokenParsed.sub;
    const updateData = {
        name: formData.name,
        idDocument: formData.idDocument,
        cellphone: formData.cellphone,
        nationality: formData.nationality
    };
    return await axios.put(`${API_URL}/update/${keycloakId}`, updateData, {
        headers: {
            'Authorization': `Bearer ${keycloak.token}`
        }
    });
};

// Function to obtain updated information from the database
export const getUserInfo = async (keycloak, email) => {
    if (!email) return null; // Evita el error de 'undefined'
    return await axios.get(`${API_URL}/${email}`, {
        headers: {
            'Authorization': `Bearer ${keycloak.token}`
        }
    });
};