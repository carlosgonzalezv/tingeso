import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Box, Typography, TextField, Button, Paper, Divider, Alert, CircularProgress } from '@mui/material';
import { useKeycloak } from "@react-keycloak/web";
import bookingService from "../services/BookingService";
import paymentService from "../services/PaymentService";

const PaymentPage = () => {
    const { bookingId } = useParams();
    const navigate = useNavigate();
    const { keycloak, initialized } = useKeycloak();

    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState(null);

    const [formData, setFormData] = useState({
        cardNumber: '',
        expirationDate: '',
        cvv: '',
        cardHolder: ''
    });

    useEffect(() => {
        if (initialized && keycloak.token) {
            bookingService.getBookingById(keycloak.token, bookingId)
                .then(response => {
                    setBooking(response.data);
                    setLoading(false);
                })
                .catch(err => {
                    console.error(err);
                    setError("No se pudo cargar la información de la reserva.");
                    setLoading(false);
                });
        }
    }, [bookingId, initialized, keycloak.token]);

    const handleInputChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setProcessing(true);
        setError(null);

        // CORREGIDO: Payload alineado con las propiedades de la entidad Java
        const paymentPayload = {
            bookingID: { id: Number(booking.id) },
            amount: Number(booking.totalAmount),
            paymentMethod: "Tarjeta de Crédito",
            cardNumber: formData.cardNumber,
        };

        try {
            await paymentService.processPayment(paymentPayload);
            setSuccess(true);
        } catch (err) {
            setError(err.response?.data || "Error al procesar el pago");
        } finally {
            setProcessing(false);
        }
    };

    if (loading) return <Box sx={{ p: 4, textAlign: 'center' }}><CircularProgress /></Box>;
    if (success) return (
        <Box sx={{ p: 4, textAlign: 'center' }}>
            <Alert severity="success" sx={{ mb: 2 }}>¡Pago realizado con éxito!</Alert>
            <Typography variant="h5">Su reserva #{bookingId} ha sido CONFIRMADA.</Typography>
            <Button variant="contained" sx={{ mt: 3 }} onClick={() => navigate('/')}>Volver al Inicio</Button>
        </Box>
    );

    return (
        <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}>
            <Paper elevation={3} sx={{ p: 4, maxWidth: 600, width: '100%' }}>
                <Typography variant="h4" gutterBottom color="primary">Procesar Pago</Typography>

                <Box sx={{ bgcolor: '#f5f5f5', p: 2, mb: 3, borderRadius: 1 }}>
                    <Typography variant="h6">Resumen de Reserva</Typography>
                    <Divider sx={{ my: 1 }} />
                    {/* CORREGIDO: Mapeo seguro usando packTourID o tourPackID */}
                    <Typography>
                        <strong>Paquete:</strong> {booking.packTourID?.name || booking.tourPackID?.name || "Destino seleccionado"}
                    </Typography>
                    <Typography variant="h5" color="secondary" sx={{ mt: 1 }}>
                        Total a pagar: ${booking.totalAmount?.toLocaleString('es-CL')} CLP
                    </Typography>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                <form onSubmit={handleSubmit}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                        <TextField
                            label="Nombre del Titular" name="cardHolder" required
                            value={formData.cardHolder} onChange={handleInputChange}
                        />
                        <TextField
                            label="Número de Tarjeta" name="cardNumber" required
                            placeholder="XXXX XXXX XXXX XXXX"
                            value={formData.cardNumber} onChange={handleInputChange}
                        />
                        <Box sx={{ display: 'flex', gap: 2 }}>
                            <TextField
                                label="Fecha Expiración" name="expirationDate" required placeholder="MM/YY"
                                value={formData.expirationDate} onChange={handleInputChange}
                            />
                            <TextField
                                label="CVV" name="cvv" type="password" required placeholder="123"
                                value={formData.cvv} onChange={handleInputChange}
                            />
                        </Box>

                        <Button
                            type="submit" variant="contained" color="primary" size="large"
                            disabled={processing}
                            sx={{ mt: 2 }}
                        >
                            {processing ? "Procesando..." : `Pagar $${booking.totalAmount?.toLocaleString('es-CL')} CLP`}
                        </Button>
                    </Box>
                </form>
            </Paper>
        </Box>
    );
};

export default PaymentPage;