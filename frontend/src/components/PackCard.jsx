import { useState } from 'react';
import { Card, CardMedia, CardContent, Typography, Box, Collapse, Button, Chip } from '@mui/material';
import { useKeycloak } from '@react-keycloak/web';
import BookingService from '../services/BookingService';

function PackCard({ pack, onManage, isAdmin }) {
    const [expanded, setExpanded] = useState(false);
    const { keycloak } = useKeycloak();

    // 1. Limpieza de fechas (quita el 00:00:00 y maneja nulos)
    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const dateOnly = dateStr.split('T')[0];
        const [year, month, day] = dateOnly.split('-');
        return `${day}/${month}/${year}`;
    };

    const handleBooking = async () => {
        if (!keycloak.authenticated) {
            keycloak.login();
            return;
        }
        try {
            await BookingService.createBooking(keycloak.token, pack.id);
            alert("¡Intención de reserva registrada con éxito!");
        } catch (error) {
            console.error("Error en la reserva:", error);
            alert("Error al procesar la reserva.");
        }
    };

    // 2. Definición de variables para que no salgan errores de "Unused"
    const availableSlots = pack.availableSlots || 0;
    const isDisabled = pack.status === 'DESHABILITADO' || availableSlots <= 0;

    return (
        <Card
            onMouseEnter={() => setExpanded(true)}
            onMouseLeave={() => setExpanded(false)}
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                borderRadius: '12px',
                transition: 'all 0.3s ease',
                boxShadow: expanded ? '0px 4px 15px rgba(0,0,0,0.2)' : '0px 1px 5px rgba(0,0,0,0.1)',
                opacity: isDisabled ? 0.8 : 1
            }}
        >
            <CardMedia
                component="img"
                height="160"
                image={pack.imageUrl || "https://images.unsplash.com/photo-1501785888041-af3ef285b470?q=80&w=800&auto=format&fit=crop"}
                alt={pack.name}
            />
            <CardContent sx={{ flexGrow: 1, p: 2, textAlign: 'center' }}>
                <Typography variant="h6" sx={{ fontWeight: 'bold', color: '#FB8C00', mb: 0.5 }}>
                    {pack.name}
                </Typography>

                {/* Mostramos ambas fechas: startDate y endDate */}
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>
                    {formatDate(pack.startDate)} al {formatDate(pack.finishDate)}
                </Typography>

                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: 2 }}>
                    <Typography variant="subtitle1" sx={{ fontWeight: 800, color: '#2e7d32' }}>
                        ${Number(pack.price).toLocaleString('es-CL')}
                    </Typography>
                    <Chip
                        label={availableSlots > 0 ? `${availableSlots} cupos` : "Agotado"}
                        color={availableSlots < 5 ? "warning" : "default"}
                        size="small"
                        sx={{ fontSize: '0.7rem', height: '20px' }}
                    />
                </Box>

                <Collapse in={expanded} timeout="auto">
                    <Box sx={{ mt: 1.5, pt: 1.5, borderTop: '1px solid #eee' }}>
                        <Typography variant="caption" sx={{ display: 'block', mb: 1.5, color: 'text.secondary' }}>
                            {pack.description || "viaje de pruebas"}
                        </Typography>

                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                            <Button
                                variant="contained"
                                fullWidth
                                size="small"
                                onClick={handleBooking} // Aquí se usa la función que daba error
                                disabled={isDisabled}   // Aquí se usa la constante que daba error
                                sx={{ backgroundColor: '#FB8C00', fontWeight: 'bold' }}
                            >
                                {keycloak.authenticated ? 'RESERVAR' : 'LOGIN'}
                            </Button>

                            {isAdmin && (
                                <Button
                                    variant="outlined"
                                    fullWidth
                                    size="small"
                                    onClick={() => onManage(pack)}
                                    sx={{ color: '#37474f', borderColor: '#37474f', fontWeight: 'bold' }}
                                >
                                    GESTIONAR
                                </Button>
                            )}
                        </Box>
                    </Box>
                </Collapse>
            </CardContent>
        </Card>
    );
}

export default PackCard;