/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Paper, Typography, TextField, Button, Stack, Box, Divider, Alert } from '@mui/material';
import { useKeycloak } from '@react-keycloak/web';
import BookingService from '../services/BookingService';

export default function BookingPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { keycloak } = useKeycloak();

    const [passengerCount, setPassengerCount] = useState(1);
    const [specialRequests, setSpecialRequests] = useState("");
    const [companionNames, setCompanionNames] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        const companionsNeeded = Math.max(0, passengerCount - 1);
        setCompanionNames(prev => {
            const newArr = [...prev];
            if (newArr.length < companionsNeeded) {
                return [...newArr, ...Array(companionsNeeded - newArr.length).fill("")];
            }
            return newArr.slice(0, companionsNeeded);
        });
    }, [passengerCount]);

    const handleCompanionChange = (index, value) => {
        const updated = [...companionNames];
        updated[index] = value;
        setCompanionNames(updated);
    };

    // --- CAMBIO AQUÍ: Manejo de la redirección al pago ---
    const handleConfirm = async () => {
        if (passengerCount < 1) {
            setError("La cantidad de pasajeros debe ser al menos 1.");
            return;
        }

        const payload = {
            packId: id,
            userEmail: keycloak.tokenParsed?.email,
            passengerCount: passengerCount,
            specialRequests: specialRequests,
            companionNames: companionNames
        };

        try {
            // Guardamos la respuesta para obtener el ID de la reserva creada
            const response = await BookingService.createBooking(keycloak.token, payload);
            const newBooking = response.data;

            // Redirigimos directamente a la página de pago usando el ID de la nueva reserva
            // Esto cumple con el flujo de "visualizar resumen antes de confirmar" que pide la épica
            navigate(`/pago/${newBooking.id}`);

        } catch (err) {
            setError(err.response?.data || "Ocurrió un error al procesar la reserva.");
        }
    };
    // ----------------------------------------------------

    return (
        <Container maxWidth="md" sx={{ py: 5 }}>
            <Paper elevation={3} sx={{ p: 4, borderRadius: '15px' }}>
                <Typography variant="h4" gutterBottom sx={{ color: '#FB8C00', fontWeight: 'bold' }}>
                    Finalizar Reserva
                </Typography>
                <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                    Por favor, completa los detalles de tu viaje para asegurar tus cupos.
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

                <Stack spacing={4}>
                    <Box>
                        <Typography variant="h6" gutterBottom>1. Cantidad de Personas</Typography>
                        <TextField
                            label="Pasajeros totales"
                            type="number"
                            fullWidth
                            inputProps={{ min: 1 }}
                            value={passengerCount}
                            onChange={(e) => setPassengerCount(Math.max(1, parseInt(e.target.value) || 1))}
                        />
                    </Box>

                    {companionNames.length > 0 && (
                        <Box>
                            <Typography variant="h6" gutterBottom>2. Datos de Acompañantes</Typography>
                            <Stack spacing={2}>
                                {companionNames.map((name, index) => (
                                    <TextField
                                        key={index}
                                        label={`Nombre Acompañante ${index + 1}`}
                                        variant="outlined"
                                        fullWidth
                                        value={name}
                                        onChange={(e) => handleCompanionChange(index, e.target.value)}
                                    />
                                ))}
                            </Stack>
                        </Box>
                    )}

                    <Box>
                        <Typography variant="h6" gutterBottom>3. Información Adicional</Typography>
                        <TextField
                            label="Solicitudes especiales o preferencias"
                            multiline
                            rows={3}
                            fullWidth
                            value={specialRequests}
                            onChange={(e) => setSpecialRequests(e.target.value)}
                            placeholder="Ej: Restricciones alimentarias, tipo de cama, etc."
                        />
                    </Box>

                    <Divider />

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Button variant="text" onClick={() => navigate(-1)} color="inherit">
                            Volver
                        </Button>
                        <Button
                            variant="contained"
                            size="large"
                            onClick={handleConfirm}
                            sx={{ bgcolor: '#FB8C00', px: 5, '&:hover': { bgcolor: '#e67e00' } }}
                        >
                            Confirmar y Proceder al Pago
                        </Button>
                    </Box>
                </Stack>
            </Paper>
        </Container>
    );
}