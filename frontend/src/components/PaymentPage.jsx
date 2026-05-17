import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Box, Typography, TextField, Button, Paper, Divider, Alert, CircularProgress } from '@mui/material';
import bookingService from "../services/BookingService";
import paymentService from "../services/PaymentService";

const PaymentPage = () => {
    const { bookingId } = useParams();
    const navigate = useNavigate();

    // Estados
    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(true);
    const [processing, setProcessing] = useState(false);
    const [success, setSuccess] = useState(false);
    const [error, setError] = useState(null);

    // Estado del Formulario de Tarjeta Simulada (Regla: Permitir ingresar datos simulados)
    const [formData, setFormData] = useState({
        cardNumber: '',
        expirationDate: '',
        cvv: '',
        cardHolder: ''
    });

    useEffect(() => {
        // Carga el resumen inicial
        bookingService.get(bookingId)
            .then(response => {
                setBooking(response.data);
                setLoading(false);
            })
            .catch(err => {
                console.error(err);
                setError("No se pudo cargar la información de la reserva.");
                setLoading(false);
            });
    }, [bookingId]);

    const handleInputChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setProcessing(true);
        setError(null);

        // Construimos el objeto para el Backend
        const paymentPayload = {
            bookingID: { id: booking.id },
            amount: booking.totalAmount, // Regla: Pago Total
            paymentMethod: "Tarjeta de Crédito", // Regla: Medio definido
            cardNumber: formData.cardNumber, // Dato simulado
            // Nota: Expiración y CVV se envían pero el backend solo asume éxito
        };

        try {
            await paymentService.processPayment(paymentPayload);
            setSuccess(true); // Regla: Mostrar confirmación clara
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

                {/* 3.2.5 Resumen del pago antes de confirmar */}
                <Box sx={{ bgcolor: '#f5f5f5', p: 2, mb: 3, borderRadius: 1 }}>
                    <Typography variant="h6">Resumen de Reserva</Typography>
                    <Divider sx={{ my: 1 }} />
                    <Typography><strong>Paquete:</strong> {booking.touristPackage?.name}</Typography>
                    <Typography variant="h5" color="secondary" sx={{ mt: 1 }}>
                        Total a pagar: ${booking.totalAmount}
                    </Typography>
                </Box>

                {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                {/* Formulario de Pago Simulado */}
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
                            {processing ? "Procesando..." : `Pagar $${booking.totalAmount}`}
                        </Button>
                    </Box>
                </form>
            </Paper>
        </Box>
    );
};

export default PaymentPage;