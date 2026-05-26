import httpClient from '../http-common';
const API_URL = "/user";
const PACKS_URL = "/tourPack";

const getPackages = async (token) => {
    try {
        const config = {};
        if (token) {
            config.headers = {
                Authorization: `Bearer ${token}`
            };
        }
        const response = await httpClient.get(`${PACKS_URL}/`, config);
        return response.data;
    } catch (error) {
        console.error("Error al obtener paquetes:", error);
        throw error;
    }
};

const savePackage = async (packData, token) => {
    try {
        const response = await httpClient.post(`${PACKS_URL}/save`, packData, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        throw error.response?.data || "Error al guardar el paquete";
    }
};

const filterPackages = (packages, filters, isAdmin) => {
    if (!packages) return [];
    return packages.filter(pack => {
        const matchesSearch = (pack.name || "").toLowerCase().includes(filters.searchTerm.toLowerCase());
        const matchesMinPrice = !filters.minPrice || pack.price >= parseFloat(filters.minPrice);
        const matchesMaxPrice = !filters.maxPrice || pack.price <= parseFloat(filters.maxPrice);
        const matchesStatus = isAdmin || pack.status === 'DISPONIBLE';
        return matchesSearch && matchesMinPrice && matchesMaxPrice && matchesStatus;
    });
};

const syncUserWithBackend = async (keycloak) => {
    try {
        const userData = {
            email: keycloak.tokenParsed.email,
            firstName: keycloak.tokenParsed.given_name,
            lastName: keycloak.tokenParsed.family_name,
            username: keycloak.tokenParsed.preferred_username
        };

        const response = await httpClient.post(`${API_URL}/sync`, userData, {
            headers: { Authorization: `Bearer ${keycloak.token}` }
        });
        return response.data;
    } catch (error) {
        console.error("Error al sincronizar usuario:", error);
    }
};

export const PackService = {
    getPackages,
    savePackage,
    filterPackages,
    syncUserWithBackend
};