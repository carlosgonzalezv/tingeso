import axios from 'axios';

// Usábamos rutas relativas para que el Proxy (Vite o Nginx) redirigiera al 8080
const API_URL = "/api/v1/user";
const PACKS_URL = "/api/v1/tourPack/";

// --- FUNCIONES DE PAQUETES ---

const getPackages = async (token) => {
    try {
        const config = {};

        // CORREGIDO: Solo inyectamos el header si el token realmente existe
        if (token) {
            config.headers = {
                Authorization: `Bearer ${token}`
            };
        }
        // Pasamos el objeto 'config' que estará vacío para invitados
        const response = await axios.get(PACKS_URL, config);
        return response.data;
    } catch (error) {
        console.error("Error al obtener paquetes:", error);
        throw error;
    }
};

const savePackage = async (packData, token) => {
    try {
        const response = await axios.post(`${PACKS_URL}/save`, packData, {
            headers: { Authorization: `Bearer ${token}` }
        });
        return response.data;
    } catch (error) {
        throw error.response?.data || "Error al guardar el paquete";
    }
};

const filterPackages = (packages, filters, isAdmin) => {
    // Si no hay paquetes (porque la API falló), devolvemos array vacío
    if (!packages) return [];

    return packages.filter(pack => {
        const matchesSearch = (pack.name || "").toLowerCase().includes(filters.searchTerm.toLowerCase());
        const matchesMinPrice = !filters.minPrice || pack.price >= parseFloat(filters.minPrice);
        const matchesMaxPrice = !filters.maxPrice || pack.price <= parseFloat(filters.maxPrice);

        // Si no es admin, solo ve los DISPONIBLES
        const matchesStatus = isAdmin || pack.status === 'DISPONIBLE';

        return matchesSearch && matchesMinPrice && matchesMaxPrice && matchesStatus;
    });
};

// --- FUNCIONES DE USUARIO ---

const syncUserWithBackend = async (keycloak) => {
    try {
        const userData = {
            email: keycloak.tokenParsed.email,
            firstName: keycloak.tokenParsed.given_name,
            lastName: keycloak.tokenParsed.family_name,
            username: keycloak.tokenParsed.preferred_username
        };

        const response = await axios.post(`${API_URL}/sync`, userData, {
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