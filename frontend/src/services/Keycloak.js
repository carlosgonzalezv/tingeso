import Keycloak from 'keycloak-js';

const keycloakConfig = {
    url: 'https://turismo-auth.duckdns.org',
    realm: 'TravelAgency',
    clientId: 'sisgr-frontend'
};

const keycloak = new Keycloak(keycloakConfig);
export default keycloak;