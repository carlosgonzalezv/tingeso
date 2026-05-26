import Keycloak from 'keycloak-js';

const keycloakConfig = {
    url: 'http://localhost:9090',
    realm: 'TravelAgency',
    clientId: 'sisgr-frontend'
};

const keycloak = new Keycloak(keycloakConfig);
export default keycloak;