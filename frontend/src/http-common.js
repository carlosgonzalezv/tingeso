import axios from "axios";
import keycloak from './services/Keycloak.js';

const httpClient = axios.create({
    baseURL: "http://localhost:8090/api/v1",
    headers: {
        "Content-type": "application/json",
    },
});

httpClient.interceptors.request.use(
    async (config) => {
        if (keycloak.authenticated) {
            await keycloak.updateToken(30).catch(() => keycloak.login());
            config.headers.Authorization = `Bearer ${keycloak.token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default httpClient;