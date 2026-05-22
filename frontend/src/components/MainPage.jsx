/* eslint-disable react-hooks/set-state-in-effect */
import { useEffect, useState, useMemo, useCallback } from 'react';
import {
    Container, Typography, Box, Button, Grid,
    Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from "@react-keycloak/web";
import PackCard from './PackCard';
import SearchBar from './SearchBar';
import { PackService } from '../services/ServicePack.js';

const MainPage = () => {
    const navigate = useNavigate();
    const { keycloak, initialized } = useKeycloak();

    // Estados de datos
    const [packages, setPackages] = useState([]);

    // Agrupamos los filtros en un solo objeto para limpiar el código
    const [filters, setFilters] = useState({
        searchTerm: '',
        minPrice: '',
        maxPrice: '',
        startDate: '',
        endDate: ''
    });

    // Estados del Modal de Edición
    const [open, setOpen] = useState(false);
    const [editData, setEditData] = useState({});

    const roles = keycloak.tokenParsed?.resource_access?.["sisgr-frontend"]?.roles || [];
    const isAdmin = roles.includes("ADMIN");

    // Función para cargar datos (usando el Service)
    // Busca esta parte en tu archivo MainPage.jsx
    // FUNCIÓN CORREGIDA: Carga datos con o sin token de sesión
    const loadData = useCallback(async () => {
        try {
            // Pasamos el token si existe; si no, irá como undefined o null
            // Tu PackService debe estar preparado para no enviarlo en los headers si no existe
            const data = await PackService.getPackages(keycloak.token);
            setPackages(data || []);
        } catch (error) {
            console.error("Error al cargar paquetes de forma pública/privada:", error);
        }
    }, [keycloak.token]);

    useEffect(() => {
        if (initialized) {
            loadData();
        }
    }, [initialized, loadData]);

    /**
     * Lógica de Filtrado:
     * Usamos useMemo para que el filtrado solo se ejecute cuando
     * cambien los paquetes, los filtros o el rol de admin.
     */
    const filteredPackages = useMemo(() => {
        return PackService.filterPackages(packages, filters, isAdmin);
    }, [packages, filters, isAdmin]);

    // Manejadores de eventos
    const handleOpenEdit = (pack) => {
        setEditData({ ...pack });
        setOpen(true);
    };

    const handleClose = () => setOpen(false);

    const handleEditChange = (e) => {
        const { name, value } = e.target;
        if ((name === 'price' || name === 'totalSlots') && value < 0) return;
        setEditData(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = async () => {
        try {
            await PackService.savePackage(editData, keycloak.token);
            alert("Paquete actualizado correctamente");
            handleClose();
            await loadData(); // Recarga limpia desde el service
        } catch (error) {
            alert("Error: " + error);
        }
    };

    // Funciones para actualizar filtros individuales desde SearchBar
    const updateFilter = (name, value) => {
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    if (!initialized) return <Typography sx={{ p: 5 }}>Iniciando sesión...</Typography>;

    return (
        <Box sx={{ width: '100%', minHeight: '100vh', backgroundColor: '#f5f5f5', py: 5 }}>
            <Container maxWidth="lg">
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
                    <Typography variant="h4" sx={{ fontWeight: 'bold', color: '#37474f' }}>
                        Catálogo de Paquetes
                    </Typography>
                    {isAdmin && (
                        <Button
                            variant="contained"
                            onClick={() => navigate('/publish-package')}
                            sx={{ backgroundColor: '#FB8C00', fontWeight: 'bold' }}
                        >
                            Nuevo Paquete
                        </Button>
                    )}
                </Box>

                <SearchBar
                    searchTerm={filters.searchTerm}
                    setSearchTerm={(val) => updateFilter('searchTerm', val)}
                    minPrice={filters.minPrice}
                    setMinPrice={(val) => updateFilter('minPrice', val)}
                    maxPrice={filters.maxPrice}
                    setMaxPrice={(val) => updateFilter('maxPrice', val)}
                    startDate={filters.startDate}
                    setStartDate={(val) => updateFilter('startDate', val)}
                    endDate={filters.endDate}
                    setEndDate={(val) => updateFilter('endDate', val)}
                />

                <Grid container spacing={3}>
                    {filteredPackages.length > 0 ? (
                        filteredPackages.map((pack) => (
                            <Grid item xs={12} sm={6} md={4} key={pack.id}>
                                <PackCard
                                    pack={pack}
                                    onManage={handleOpenEdit}
                                    isAdmin={isAdmin}
                                />
                            </Grid>
                        ))
                    ) : (
                        <Box sx={{ width: '100%', textAlign: 'center', py: 10 }}>
                            <Typography variant="h6" color="text.secondary">
                                No hay paquetes disponibles para esta consulta.
                            </Typography>
                        </Box>
                    )}
                </Grid>

                {/* Modal de Gestión */}
                <Dialog open={open} onClose={handleClose} fullWidth maxWidth="xs">
                    <DialogTitle sx={{ fontWeight: 'bold' }}>Gestionar Paquete</DialogTitle>
                    <DialogContent dividers>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                            <TextField fullWidth label="Nombre" name="name" value={editData.name || ''} onChange={handleEditChange} />
                            <TextField fullWidth label="Precio" name="price" type="number" value={editData.price || ''} onChange={handleEditChange} />
                            <TextField fullWidth label="Cupos Totales" name="totalSlots" type="number" value={editData.totalSlots || ''} onChange={handleEditChange} />

                            <Typography variant="caption" color="text.secondary">
                                Cupos disponibles actuales: {editData.availableSlots || 0}
                            </Typography>

                            <TextField select fullWidth label="Estado" name="status" value={editData.status || ''} onChange={handleEditChange}>
                                <MenuItem value="DISPONIBLE">DISPONIBLE</MenuItem>
                                <MenuItem value="DESHABILITADO">DESHABILITADO</MenuItem>
                                <MenuItem value="CANCELADO">CANCELADO</MenuItem>
                                <MenuItem value="AGOTADO">AGOTADO</MenuItem>
                                <MenuItem value="NO VIGENTE">NO VIGENTE</MenuItem>
                            </TextField>
                        </Box>
                    </DialogContent>
                    <DialogActions sx={{ p: 2 }}>
                        <Button onClick={handleClose}>Cancelar</Button>
                        <Button onClick={handleSave} variant="contained" sx={{ backgroundColor: '#FB8C00' }}>
                            Guardar Cambios
                        </Button>
                    </DialogActions>
                </Dialog>
            </Container>
        </Box>
    );
};

export default MainPage;