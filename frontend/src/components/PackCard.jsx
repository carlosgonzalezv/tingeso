import { useState } from 'react';
import { Card, CardMedia, CardContent, Typography, Box, Collapse, Button, Chip } from '@mui/material';
import { useKeycloak } from '@react-keycloak/web';
import { useNavigate } from "react-router-dom";

function PackCard({ pack, onManage, isAdmin }) {
    const [expanded, setExpanded] = useState(false);
    const { keycloak } = useKeycloak();
    const navigate = useNavigate();

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        const dateOnly = dateStr.split('T')[0];
        const [year, month, day] = dateOnly.split('-');
        return `${day}/${month}/${year}`;
    };

    const handleButtonClick = () => {
        if (!keycloak.authenticated) {
            keycloak.login();
            return;
        }
        // Redirigimos a la página completa de reservas pasando el ID del paquete
        navigate(`/booking/${pack.id}`);
    };

    const availableSlots = pack.totalSlots || pack.availPlaces || pack.availableSlots || 0;
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
                            {pack.description || "Sin descripción"}
                        </Typography>

                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                            <Button
                                variant="contained"
                                fullWidth
                                onClick={handleButtonClick}
                                disabled={isDisabled}
                                sx={{ backgroundColor: '#FB8C00', fontWeight: 'bold' }}
                            >
                                {keycloak.authenticated ? 'RESERVAR' : 'LOGIN PARA RESERVAR'}
                            </Button>

                            {isAdmin && (
                                <Button
                                    variant="outlined"
                                    fullWidth
                                    onClick={() => onManage(pack)}
                                    sx={{ color: '#37474f', borderColor: '#37474f' }}
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