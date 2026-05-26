import httpClient from '../http-common';
const API_URL = "/user";

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
    return await httpClient.post(`${API_URL}/sync`, userData, {
        headers: {
            'Authorization': `Bearer ${keycloak.token}`
        }
    });
};

export const updateUserInfo = async (keycloak, formData) => {
    const keycloakId = keycloak.tokenParsed.sub;
    const updateData = {
        name: formData.name,
        idDocument: formData.idDocument,
        cellphone: formData.cellphone,
        nationality: formData.nationality
    };
    return await httpClient.put(`${API_URL}/update/${keycloakId}`, updateData, {
        headers: {
            'Authorization': `Bearer ${keycloak.token}`
        }
    });
};

export const getUserInfo = async (keycloak, email) => {
    if (!email) return null;
    return await httpClient.get(`${API_URL}/${email}`, {
        headers: {
            'Authorization': `Bearer ${keycloak.token}`
        }
    });
};