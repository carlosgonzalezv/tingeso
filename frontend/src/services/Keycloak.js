import Keycloak from 'keycloak-js';

const keycloakConfig = {
    url: 'http://159.203.186.121:8070/auth',
    realm: 'TravelAgency',
    clientId: 'sisgr-frontend'
};

const keycloak = new Keycloak(keycloakConfig);
export default keycloak;