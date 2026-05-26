import { useState } from 'react';
import { Container, Typography, TextField, Button, Grid, Paper, InputAdornment, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import CalendarMonth from '@mui/icons-material/CalendarMonth';
import httpClient from '../http-common';

function PublishPackage() {
    const navigate = useNavigate();
    const { keycloak } = useKeycloak();
    const [error, setError] = useState(null);
    const [formData, setFormData] = useState({
        name: '',
        destination: '',
        description: '',
        price: '',
        totalSlots: '',
        startDate: '',
        finishDate: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        if ((name === 'price' || name === 'totalSlots') && value !== '' && Number(value) < 0) {
            return;
        }
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        const payload = {
            name: formData.name,
            destination: formData.destination,
            description: formData.description,
            price: String(formData.price),
            totalSlots: parseInt(formData.totalSlots),
            availPlaces: parseInt(formData.totalSlots),
            startDate: `${formData.startDate}T00:00:00`,
            finishDate: `${formData.finishDate}T00:00:00`,
            status: "DISPONIBLE"
        };

        try {
            const response = await httpClient.post('/tourPack/', payload, {
                headers: {
                    Authorization: `Bearer ${keycloak?.token}`
                }
            });
            if (response.status === 200) {
                alert("Paquete publicado con éxito");
                navigate("/");
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || "Error al publicar el paquete";
            setError(errorMessage);
            console.error("Error en el servidor:", err);
        }
    };

    return (
        <Container maxWidth={false} sx={{ width: '95%', mt: 5, mb: 5 }}>
            <Paper elevation={3} sx={{ p: 4, borderRadius: '15px', width: '100%' }}>
                <Typography variant="h4" gutterBottom sx={{ color: '#FB8C00', fontWeight: 'bold', textAlign: 'center', mb: 4 }}>
                    Publicar Nuevo Paquete Turístico
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

                <form onSubmit={handleSubmit}>
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField fullWidth label="Nombre del Paquete" name="name" value={formData.name} onChange={handleChange} required />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField fullWidth label="Destino" name="destination" value={formData.destination} onChange={handleChange} required />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Precio"
                                name="price"
                                type="number"
                                value={formData.price}
                                onChange={handleChange}
                                required
                                inputProps={{ min: "0" }}
                                InputProps={{ startAdornment: <InputAdornment position="start">$</InputAdornment> }}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Descripción"
                                name="description"
                                value={formData.description}
                                onChange={handleChange}
                                required
                                multiline
                                rows={4}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Fecha de Inicio"
                                name="startDate"
                                type="date"
                                value={formData.startDate}
                                onChange={handleChange}
                                required
                                InputLabelProps={{ shrink: true }}
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <CalendarMonth sx={{ color: '#FB8C00', mr: 1 }} />
                                        </InputAdornment>
                                    ),
                                }}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <TextField
                                fullWidth
                                label="Fecha de Término"
                                name="finishDate"
                                type="date"
                                value={formData.finishDate}
                                onChange={handleChange}
                                required
                                InputLabelProps={{ shrink: true }}
                                InputProps={{
                                    startAdornment: (
                                        <InputAdornment position="start">
                                            <CalendarMonth sx={{ color: '#FB8C00', mr: 1 }} />
                                        </InputAdornment>
                                    ),
                                }}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Cupos Totales"
                                name="totalSlots"
                                type="number"
                                value={formData.totalSlots}
                                onChange={handleChange}
                                required
                                inputProps={{ min: "1" }}
                            />
                        </Grid>
                        <Grid item xs={12} sx={{ display: 'flex', gap: 2, mt: 2 }}>
                            <Button type="submit" variant="contained" size="large" sx={{ backgroundColor: '#FB8C00', '&:hover': { backgroundColor: '#e67e00' }, flexGrow: 1, fontWeight: 'bold' }}>
                                Publicar Paquete
                            </Button>
                            <Button variant="outlined" size="large" onClick={() => navigate("/")} sx={{ flexGrow: 1 }}>
                                Cancelar
                            </Button>
                        </Grid>
                    </Grid>
                </form>
            </Paper>
        </Container>
    );
}

export default PublishPackage;