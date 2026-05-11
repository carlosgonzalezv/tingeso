import { useState } from 'react';
import { Card, CardMedia, CardContent, Typography, Box, Collapse, Button, Chip } from '@mui/material';
import { useKeycloak } from '@react-keycloak/web'; // O de donde importes tu hook de Keycloak
import BookingService from '../services/BookingService'; // Tu carpeta de services

function PackCard({ pack, onManage, isAdmin }) {
    const [expanded, setExpanded] = useState(false);
    const { keycloak } = useKeycloak();

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const [year, month, day] = dateStr.split('-');
        return `${day}/${month}/${year}`;
    };

    // Función de reserva que usa tu Service
    const handleBooking = async () => {
        if (!keycloak.authenticated) {
            keycloak.login();
            return;
        }

        try {
            // Pasamos el token o el objeto keycloak completo según pida tu service
            await BookingService.createBooking(keycloak, pack.id);
            alert("¡Intención de reserva registrada con éxito!");
        } catch (error) {
            console.error("Error en la reserva:", error);
            alert("Error al procesar la reserva. Intenta nuevamente.");
        }
    };

    const isDisabled = pack.status === 'DESHABILITADO' || pack.availableSlots <= 0;

    return (
        <Card
            onMouseEnter={() => setExpanded(true)}
            onMouseLeave={() => setExpanded(false)}
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                borderRadius: '16px',
                transition: 'all 0.3s ease',
                boxShadow: expanded ? '0px 8px 25px rgba(0,0,0,0.2)' : '0px 2px 10px rgba(0,0,0,0.1)',
                opacity: isDisabled ? 0.7 : 1
            }}
        >
            <CardMedia
                component="img"
                height="200"
                image={pack.imageUrl || "https://images.unsplash.com/photo-1501785888041-af3ef285b470?q=80&w=800&auto=format&fit=crop"}
                alt={pack.name}
            />
            <CardContent sx={{ flexGrow: 1 }}>
                <Typography variant="h5" sx={{ fontWeight: 'bold', color: '#FB8C00' }}>
                    {pack.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                    {formatDate(pack.startDate)} al {formatDate(pack.finishDate)}
                </Typography>

                <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h6" sx={{ fontWeight: 800, color: '#2e7d32' }}>
                        ${Number(pack.price).toLocaleString('es-CL')}
                    </Typography>
                    <Chip
                        label={pack.availableSlots > 0 ? `${pack.availableSlots} cupos` : "Agotado"}
                        color={pack.availableSlots < 5 ? "warning" : "default"}
                        size="small"
                    />
                </Box>

                <Collapse in={expanded} timeout="auto">
                    <Box sx={{ mt: 2, pt: 2, borderTop: '1px solid #eee' }}>
                        <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>
                            {pack.description}
                        </Typography>

                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button
                                variant="contained"
                                fullWidth
                                size="small"
                                onClick={handleBooking} // <--- AQUÍ VINCULAMOS LA FUNCIÓN
                                disabled={isDisabled}
                                sx={{ backgroundColor: '#FB8C00', '&:hover': { backgroundColor: '#e67e00' } }}
                            >
                                {keycloak.authenticated ? 'Reservar Ahora' : 'Loguear para Reservar'}
                            </Button>

                            {isAdmin && (
                                <Button
                                    variant="outlined"
                                    fullWidth
                                    size="small"
                                    onClick={() => onManage(pack)}
                                    sx={{ color: '#37474f', borderColor: '#37474f' }}
                                >
                                    Gestionar
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