/* eslint-disable react-hooks/set-state-in-effect */
import { useState, useEffect, useCallback } from 'react';
import { useKeycloak } from "@react-keycloak/web";
import { Container, Paper, Typography, TextField, Button, Box, Grid, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { updateUserInfo, getUserInfo } from '../services/UserService';


const Profile = () => {
    const { keycloak, initialized } = useKeycloak();
    const navigate = useNavigate();
    const [edit, setEdit] = useState(false);
    const [success, setSuccess] = useState(false);
    const [formData, setFormData] = useState({
        name: '', idDocument: '', cellphone: '', nationality: ''
    });

    const fetchUserData = useCallback(async () => {
        const userEmail = keycloak?.tokenParsed?.email;
        if (initialized && keycloak?.authenticated && userEmail) {
            try {
                const response = await getUserInfo(keycloak, userEmail);
                if (response && response.data) {
                    const data = response.data;
                    setFormData({
                        name: data.name || '',
                        idDocument: data.idDocument || '',
                        cellphone: data.cellphone || '',
                        nationality: data.nationality || ''
                    });
                }
            } catch (error) {
                console.error("Error cargando perfil:", error);
            }
        }
    }, [initialized, keycloak]);

    useEffect(() => {
        if (initialized) {
            void fetchUserData();
        }
    }, [initialized, fetchUserData]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = async () => {
        try {
            const response = await updateUserInfo(keycloak, formData);
            if (response && response.data) {
                setFormData(response.data);
            }
            setEdit(false);
            setSuccess(true);
            setTimeout(() => setSuccess(false), 3000);
        } catch (error) {
            alert("Error al guardar: " + error);
        }
    };

    if (!initialized) return null;

    return (
        <Container maxWidth={false} sx={{ mt: 5 }}>
            {success && <Alert severity="success" sx={{ mb: 2 }}>Perfil actualizado correctamente</Alert>}
            <Paper elevation={3} sx={{ p: 4, borderRadius: 3 }}>
                <Typography variant="h4" gutterBottom fontWeight="bold" color="primary">Mi Perfil</Typography>
                <Grid container spacing={3}>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            fullWidth label="Nombre Completo" name="name"
                            value={formData.name || ''} onChange={handleChange}
                            disabled={!edit} variant="outlined"
                        />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            fullWidth label="RUT / Documento" name="idDocument"
                            value={formData.idDocument || ''} onChange={handleChange}
                            disabled={!edit} variant="outlined"
                        />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            fullWidth label="Teléfono" name="cellphone"
                            value={formData.cellphone || ''} onChange={handleChange}
                            disabled={!edit} variant="outlined"
                        />
                    </Grid>
                    <Grid item xs={12} sm={6}>
                        <TextField
                            fullWidth label="Nacionalidad" name="nationality"
                            value={formData.nationality || ''} onChange={handleChange}
                            disabled={!edit} variant="outlined"
                        />
                    </Grid>
                    <Grid item xs={12}>
                        <TextField
                            fullWidth label="Correo Electrónico"
                            value={keycloak?.tokenParsed?.email || ''}
                            disabled variant="filled"
                        />
                    </Grid>
                </Grid>
                <Box sx={{ mt: 4, display: 'flex', gap: 2 }}>
                    {edit ? (
                        <>
                            <Button variant="contained" color="success" onClick={handleSave}>Guardar</Button>
                            <Button variant="outlined" color="error" onClick={() => setEdit(false)}>Cancelar</Button>
                        </>
                    ) : (
                        <>
                            <Button variant="contained" onClick={() => setEdit(true)}>Editar Perfil</Button>
                            <Button variant="outlined" onClick={() => navigate(-1)}>Volver</Button>
                        </>
                    )}
                </Box>
            </Paper>
        </Container>
    );
};

export default Profile;