import { useEffect } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { syncUserWithBackend } from './services/UserService';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline, Box, Typography, Button } from '@mui/material';

// Importación de componentes existentes
import MainPage from './components/MainPage.jsx';
import PrivateRoute from './components/PrivateRoute';
import UserList from './components/UserList.jsx';
import Profile from './components/Profile';
import Navbar from './components/Navbar';
import PublishPackage from './components/PublishPackage';
import BookingPage from './components/BookingPage';
import MyBookings from './components/MyBookings';
import ManageBookings from './components/ManageBookings';

// IMPORTACIÓN DE LA NUEVA PÁGINA DE PAGO (Épica 5)
import PaymentPage from './components/PaymentPage';

const theme = createTheme({
    palette: {
        primary: {
            main: '#ff8c00',
        },
        secondary: {
            main: '#37474f',
        },
    },
});

function App() {
    const { keycloak, initialized } = useKeycloak();

    useEffect(() => {
        if (initialized && keycloak.authenticated) {
            const performSync = async () => {
                try {
                    await syncUserWithBackend(keycloak);
                    console.log("Sincronización con PostgreSQL completada con éxito.");
                } catch (error) {
                    console.error("Error al intentar sincronizar el usuario:", error);
                }
            };
            performSync();
        }
    }, [initialized, keycloak.authenticated, keycloak]);

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Router>
                <Navbar />
                <Box sx={{ width: '100%', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
                    <Routes>
                        {/* Rutas Públicas */}
                        <Route path="/" element={<MainPage />} />
                        <Route path="/perfil" element={<Profile />} />

                        {/* RUTA DE RESERVA: Protegida */}
                        <Route
                            path="/booking/:id"
                            element={
                                <PrivateRoute>
                                    <BookingPage />
                                </PrivateRoute>
                            }
                        />

                        {/* NUEVA RUTA DE PAGO: Protegida (Épica 5) */}
                        <Route
                            path="/pago/:bookingId"
                            element={
                                <PrivateRoute>
                                    <PaymentPage />
                                </PrivateRoute>
                            }
                        />

                        {/* Rutas de Administrador */}
                        <Route
                            path="/publish-package"
                            element={
                                <PrivateRoute role="ADMIN">
                                    <PublishPackage/>
                                </PrivateRoute>
                            }
                        />

                        <Route
                            path="/my-bookings"
                            element={
                                <PrivateRoute>
                                    <MyBookings />
                                </PrivateRoute>
                            }
                        />

                        <Route
                            path="/manage-bookings"
                            element = {
                        <PrivateRoute role="ADMIN">
                            <ManageBookings />
                        </PrivateRoute>
                    }
                        />

                        <Route
                            path="/usuarios"
                            element={
                                <PrivateRoute role="ADMIN">
                                    <Box sx={{ p: 2, textAlign: 'center', width: '100%' }}>
                                        <Typography variant="h4" gutterBottom>
                                            Lista de Usuarios Registrados
                                        </Typography>
                                        <UserList />
                                        <Button
                                            variant="contained"
                                            color="secondary"
                                            onClick={() => window.history.back()}
                                            sx={{ mt: 3 }}
                                        >
                                            Volver
                                        </Button>
                                    </Box>
                                </PrivateRoute>
                            }
                        />
                    </Routes>
                </Box>
            </Router>
        </ThemeProvider>
    );
}

export default App;