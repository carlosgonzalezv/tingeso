import { useKeycloak } from '@react-keycloak/web';
import { Typography, Container, Button } from '@mui/material';

const PrivateRoute = ({ children, role }) => {
    const { keycloak, initialized } = useKeycloak();

    if (!initialized) {
        return <Typography>Cargando seguridad...</Typography>;
    }

    if (!keycloak.authenticated) {
        keycloak.login().catch(console.error);
        return null;
    }

    if (role && !keycloak.hasRealmRole(role)) {
        return (
            <Container sx={{ mt: 5, textAlign: 'center' }}>
                <Typography variant="h4" color="error">Acceso Denegado</Typography>
                <Typography sx={{ mt: 2 }}>No tienes los permisos de administrador necesarios.</Typography>
                <Button
                    variant="contained"
                    sx={{ mt: 3 }}
                    onClick={() => window.location.href = '/'}
                >
                    Volver al Inicio
                </Button>
            </Container>
        );
    }

    return children;
};

export default PrivateRoute;