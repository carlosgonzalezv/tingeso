import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, TextField, Button, Paper, Divider, Alert, CircularProgress } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack'; // Asegúrate de tener instalado @mui/icons-material
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

        // Enviamos el precio final calculado
        const paymentPayload = {
            bookingId: parseInt(bookingId),
            amount: booking.finalPrice || booking.totalAmount,
            paymentMethod: "Tarjeta de Crédito",
            cardNumber: formData.cardNumber,
            cardHolder: formData.cardHolder,
            expirationDate: formData.expirationDate,
            cvv: formData.cvv
        };

        try {
            await paymentService.processPayment(paymentPayload);
            setSuccess(true);
        } catch (err) {
            setError(err.response?.data?.message || "Error al procesar el pago.");
        } finally {
            setProcessing(false);
        }
    };

    if (loading) return <Box sx={{ p: 4, textAlign: 'center' }}><CircularProgress /></Box>;
    if (success) return (
        <Box sx={{ p: 4, textAlign: 'center' }}>
            <Alert severity="success" sx={{ mb: 2 }}>¡Pago realizado con éxito!</Alert>
            <Typography variant="h5">Su reserva #{bookingId} ha sido confirmada.</Typography>
            <Button variant="contained" sx={{ mt: 3 }} onClick={() => navigate('/')}>Volver al Inicio</Button>
        </Box>
    );

    return (
        <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }}>
            <Paper elevation={3} sx={{ p: 4, maxWidth: 600, width: '100%' }}>

                {/* Botón de Volver */}
                <Button startIcon={<ArrowBackIcon />} onClick={() => navigate(-1)} sx={{ mb: 2 }}>
                    Volver
                </Button>

                <Typography variant="h4" gutterBottom color="primary">Procesar Pago</Typography>

                {/* Resumen de Reserva detallado */}
                <Box sx={{ bgcolor: '#f9f9f9', p: 2, mb: 3, borderRadius: 1, border: '1px solid #ddd' }}>
                    <Typography variant="h6" gutterBottom>Resumen de Reserva</Typography>

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography>Precio Original:</Typography>
                        <Typography sx={{ textDecoration: 'line-through', color: 'gray' }}>
                            ${booking.originalPrice || booking.totalAmount}
                        </Typography>
                    </Box>

                    {/* Mostrar Descuentos si existen */}
                    {booking.appliedDiscounts && booking.appliedDiscounts.length > 0 && (
                        <Box sx={{ my: 1, p: 1, bgcolor: '#e8f5e9', borderRadius: 1 }}>
                            <Typography variant="body2" color="success.main" sx={{ fontWeight: 'bold' }}>
                                Descuentos aplicados:
                            </Typography>
                            {booking.appliedDiscounts.map((desc, i) => (
                                <Typography key={i} variant="body2" color="success.main">• {desc}</Typography>
                            ))}
                            <Typography variant="body2" color="success.main" sx={{ mt: 1 }}>
                                <strong>Ahorro total:</strong> -${booking.totalSavings || 0}
                            </Typography>
                        </Box>
                    )}

                    <Divider sx={{ my: 2 }} />

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Typography variant="h6">Total a Pagar:</Typography>
                        <Typography variant="h4" color="primary" sx={{ fontWeight: 'bold' }}>
                            ${booking.finalPrice || booking.totalAmount}
                        </Typography>
                    </Box>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                <form onSubmit={handleSubmit}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                        <TextField label="Nombre del Titular" name="cardHolder" required value={formData.cardHolder} onChange={handleInputChange} />
                        <TextField label="Número de Tarjeta" name="cardNumber" required placeholder="XXXX XXXX XXXX XXXX" value={formData.cardNumber} onChange={handleInputChange} />
                        <Box sx={{ display: 'flex', gap: 2 }}>
                            <TextField label="Fecha Expiración" name="expirationDate" required placeholder="MM/YY" value={formData.expirationDate} onChange={handleInputChange} />
                            <TextField label="CVV" name="cvv" type="password" required placeholder="123" value={formData.cvv} onChange={handleInputChange} />
                        </Box>
                        <Button type="submit" variant="contained" color="primary" size="large" disabled={processing} sx={{ mt: 2 }}>
                            {processing ? "Procesando..." : `Pagar $${booking.finalPrice || booking.totalAmount}`}
                        </Button>
                    </Box>
                </form>
            </Paper>
        </Box>
    );
};

export default PaymentPage;