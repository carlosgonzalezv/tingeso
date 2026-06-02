import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { Container, Paper, Typography, Box, Chip, Button, Stack, Divider, CircularProgress, Alert } from '@mui/material';
import BookingService from '../services/BookingService';

//page where you can see all your bookings that you have made.
export default function MyBookings() {
    const { keycloak, initialized } = useKeycloak();
    const navigate = useNavigate();
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadBookings = useCallback(() => {
        if (!initialized || !keycloak.authenticated) return;

        BookingService.getBookingsByEmail(keycloak.token, keycloak.tokenParsed?.email || "")
            .then((data) => {
                const rawList = Array.isArray(data) ? data : [];
                const safeData = rawList.map((booking) => {
                    return {
                        id: booking?.id ? String(booking.id) : '',
                        packageName: booking?.packTour?.name || booking?.touristPackage?.name || "Destino Turístico",
                        totalAmount: Number(booking?.totalAmount || 0),
                        passengerCount: Number(booking?.passengersCount || booking?.passengerCount || 1),
                        dateText: booking?.date || "Reciente",
                        companions: Array.isArray(booking?.companionNames) ? booking.companionNames : [],
                        requests: booking?.specialRequests || "",
                        status: booking?.status ? String(booking.status).toUpperCase() : "PENDIENTE"
                    };
                });
                setBookings(safeData);
                setLoading(false);
            })
            .catch((err) => {
                console.error("Error loading bookings:", err);
                setError("No se pudieron cargar tus reservas. Inténtalo más tarde.");
                setLoading(false);
            });
    }, [initialized, keycloak.authenticated, keycloak.token, keycloak.tokenParsed?.email]);

    useEffect(() => {
        if (!initialized) return;

        if (keycloak.authenticated) {
            loadBookings();
        } else {
            const timer = setTimeout(() => {
                setLoading(false);
            }, 0);
            return () => clearTimeout(timer);
        }
    }, [initialized, keycloak.authenticated, loadBookings]);

    const handleCancelBooking = (bookingId) => {
        const confirmCancel = window.confirm("¿Estás seguro de que deseas cancelar esta reserva?");
        if (confirmCancel) {
            BookingService.updateBookingStatus(keycloak.token, bookingId, "CANCELADA")
                .then(() => {
                    alert("Reserva cancelada correctamente.");
                    loadBookings();
                })
                .catch((err) => {
                    console.error("Error cancelling booking:", err);
                    alert("No se pudo cancelar la reserva. Inténtalo de nuevo.");
                });
        }
    };

    const getStatusChip = (status) => {
        switch (status) {
            case 'CONFIRMADA':
            case 'CONFIRMED':
                return <Chip label="CONFIRMADA (PAGADA)" color="success" variant="filled" />;
            case 'COMPLETADA':
            case 'COMPLETED':
                return <Chip label="COMPLETADA" color="info" variant="outlined" />;
            case 'CANCELADA':
            case 'CANCELED':
                return <Chip label="CANCELADA" color="error" variant="filled" />;
            default:
                return <Chip label="PENDIENTE DE PAGO" color="warning" variant="filled" />;
        }
    };

    if (!initialized || loading) {
        return <Box sx={{ p: 5, textAlign: 'center' }}><CircularProgress color="primary" /></Box>;
    }

    return (
        <Container maxWidth="md" sx={{ py: 5 }}>
            <Typography variant="h4" gutterBottom sx={{ color: '#37474f', fontWeight: 'bold', mb: 4 }}>
                Mis Reservas e Historial de Viajes
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

            {bookings.length === 0 ? (
                <Paper elevation={1} sx={{ p: 4, textAlign: 'center', borderRadius: '10px' }}>
                    <Typography variant="body1" color="text.secondary">
                        Aún no tienes ninguna reserva registrada en la plataforma.
                    </Typography>
                    <Button variant="contained" color="primary" sx={{ mt: 2 }} onClick={() => navigate('/')}>
                        Explorar Paquetes
                    </Button>
                </Paper>
            ) : (
                <Stack spacing={3}>
                    {bookings.map((item) => (
                        <Paper key={item.id} elevation={3} sx={{ p: 3, borderRadius: '12px', borderLeft: '6px solid #ff8c00' }}>
                            {/* Card Header */}
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2, flexWrap: 'wrap', gap: 1 }}>
                                <Box>
                                    <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                                        {item.packageName}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        ID de Reserva: #{item.id} | Fecha de Solicitud: {item.dateText}
                                    </Typography>
                                </Box>
                                {getStatusChip(item.status)}
                            </Box>
                            <Divider sx={{ my: 1.5 }} />
                            {/* Booking Details */}
                            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2, my: 2 }}>
                                <Typography variant="body2">
                                    <strong>Cantidad de Pasajeros:</strong> {item.passengerCount} {item.passengerCount === 1 ? 'persona' : 'personas'}
                                </Typography>
                                <Typography variant="body2" sx={{ color: '#2e7d32', fontWeight: 'bold' }}>
                                    <strong>Monto Total:</strong> ${item.totalAmount?.toLocaleString('es-CL')}
                                </Typography>
                                {item.companions.length > 0 && (
                                    <Typography variant="body2" sx={{ gridColumn: { sm: '1 / span 2' } }}>
                                        <strong>Acompañantes:</strong> {item.companions.join(', ')}
                                    </Typography>
                                )}
                                {item.requests && (
                                    <Typography variant="body2" sx={{ gridColumn: { sm: '1 / span 2' }, fontStyle: 'italic', color: 'text.secondary' }}>
                                        <strong>Solicitudes Especiales:</strong> "{item.requests}"
                                    </Typography>
                                )}
                            </Box>
                            <Divider sx={{ my: 1.5 }} />
                            {/* Terms & Actions */}
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pt: 1, flexWrap: 'wrap', gap: 1 }}>
                                <Typography variant="caption" color="text.secondary" sx={{ maxWidth: '60%' }}>
                                    * Condiciones: Para cambios o cancelaciones, póngase en contacto con la administración antes de la fecha programada.
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 1 }}>
                                    {(item.status === 'PENDIENTE' || item.status === 'CONFIRMADA') && (
                                        <Button
                                            variant="outlined"
                                            color="error"
                                            size="small"
                                            onClick={() => handleCancelBooking(item.id)}
                                            sx={{ fontWeight: 'bold' }}
                                        >
                                            Cancelar Reserva
                                        </Button>
                                    )}
                                    {item.status === 'PENDIENTE' && (
                                        <Button
                                            variant="contained"
                                            color="primary"
                                            size="small"
                                            onClick={() => navigate(`/pago/${item.id}`)}
                                            sx={{ fontWeight: 'bold' }}
                                        >
                                            Pagar Ahora
                                        </Button>
                                    )}
                                </Box>
                            </Box>
                        </Paper>
                    ))}
                </Stack>
            )}
        </Container>
    );
}