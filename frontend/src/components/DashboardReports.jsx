import { useState } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Container, Paper, Typography, TextField, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Grid, Tabs, Tab, Box, Alert } from '@mui/material';
import BookingService from '../services/BookingService';

export default function DashboardReports() {
    const { keycloak } = useKeycloak();
    const [tabIndex, setTabIndex] = useState(0);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');

    const [salesData, setSalesData] = useState([]);
    const [rankingData, setRankingData] = useState([]);
    const [error, setError] = useState(null);

    const handleFetchReport = () => {
        if (!startDate || !endDate) {
            alert("Por favor selecciona ambas fechas.");
            return;
        }

        // Validación de reglas de negocio
        if (new Date(startDate) > new Date(endDate)) {
            setError("La fecha de inicio no puede ser posterior a la fecha de término.");
            return;
        }

        setError(null);
        // Conversión a formato ISO para el LocalDateTime del backend
        const startISO = `${startDate}T00:00:00`;
        const endISO = `${endDate}T23:59:59`;

        if (tabIndex === 0) {
            BookingService.getSalesReport(keycloak.token, startISO, endISO)
                .then(setSalesData)
                .catch(err => {
                    console.error("Error fetching sales report:", err);
                    setError("No se pudo obtener el listado de ventas para ese rango.");
                });
        } else {
            BookingService.getRankingReport(keycloak.token, startISO, endISO)
                .then(setRankingData)
                .catch(err => {
                    console.error("Error fetching ranking report:", err);
                    setError("No se pudo obtener el ranking de paquetes.");
                });
        }
    };

    return (
        <Container maxWidth="lg" sx={{ py: 5 }}>
            <Paper elevation={3} sx={{ p: 4, borderRadius: '12px' }}>
                <Typography variant="h4" fontWeight="bold" gutterBottom color="#37474f">
                    Centro de Informes y Gestión Comercial
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                    Filtre y procese datos operacionales para apoyar la toma de decisiones estratégicas de la agencia.
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

                {/* Filtros por rango de fechas */}
                <Grid container spacing={3} sx={{ my: 1 }} alignItems="center">
                    <Grid item xs={12} sm={4}>
                        <TextField
                            label="Fecha de Inicio"
                            type="date"
                            fullWidth
                            InputLabelProps={{ shrink: true }}
                            value={startDate}
                            onChange={(e) => setStartDate(e.target.value)}
                        />
                    </Grid>
                    <Grid item xs={12} sm={4}>
                        <TextField
                            label="Fecha de Término"
                            type="date"
                            fullWidth
                            InputLabelProps={{ shrink: true }}
                            value={endDate}
                            onChange={(e) => setEndDate(e.target.value)}
                        />
                    </Grid>
                    <Grid item xs={12} sm={4}>
                        <Button variant="contained" color="primary" fullWidth size="large" onClick={handleFetchReport}>
                            Procesar Reporte
                        </Button>
                    </Grid>
                </Grid>

                <Box sx={{ borderBottom: 1, borderColor: 'divider', mt: 3, mb: 2 }}>
                    <Tabs value={tabIndex} onChange={(e, nv) => setTabIndex(nv)}>
                        <Tab label="1. Listado Detallado de Ventas" />
                        <Tab label="2. Ranking de Demanda de Paquetes" />
                    </Tabs>
                </Box>

                {/* REPORTE 1: LISTADO CRONOLÓGICO DE VENTAS */}
                {tabIndex === 0 && (
                    <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: '8px' }}>
                        <Table>
                            <TableHead sx={{ bgcolor: '#37474f' }}>
                                <TableRow>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Fecha Operación</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Cliente (Email)</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Pasajeros</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Monto Total</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Estado</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {salesData.map((row) => (
                                    <TableRow key={row.id} hover>
                                        <TableCell>{row.reservationDate ? row.reservationDate.replace('T', ' ') : 'N/A'}</TableCell>
                                        <TableCell>{row.userID?.email || row.userEmail || "Cliente"}</TableCell>
                                        <TableCell align="center">{row.passengerCount || 1} pas.</TableCell>
                                        <TableCell>${row.totalAmount?.toLocaleString('es-CL')} CLP</TableCell>
                                        <TableCell style={{
                                            fontWeight: 'bold',
                                            // Se evalúan las cadenas de estado que vienen en español de la BD
                                            color: row.status === 'CONFIRMADA' || row.status === 'COMPLETADA' ? '#2e7d32' : '#f57c00'
                                        }}>
                                            {row.status}
                                        </TableCell>
                                    </TableRow>
                                ))}
                                {salesData.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={5} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                                            No se registran ventas válidas en el período ingresado.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}

                {/* REPORTE 2: RANKING DE DEMANDA (Variables en inglés según el DTO corregido) */}
                {tabIndex === 1 && (
                    <TableContainer component={Paper} variant="outlined" sx={{ borderRadius: '8px' }}>
                        <Table>
                            <TableHead sx={{ bgcolor: '#37474f' }}>
                                <TableRow>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Posición</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Paquete Turístico</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Cantidad de Ventas</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Total Pasajeros</TableCell>
                                    <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Monto Total Generado</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {rankingData.map((row, index) => (
                                    <TableRow key={index} hover sx={{ bgcolor: index === 0 ? '#e8f5e9' : 'inherit' }}>
                                        <TableCell><strong>#{index + 1}</strong></TableCell>
                                        <TableCell style={{ fontWeight: 'bold' }}>{row.packageName}</TableCell>
                                        {/* TODO EN INGLÉS SÓLIDO: */}
                                        <TableCell>{row.totalBookings || 0} bookings</TableCell>
                                        <TableCell>{row.totalPassengers || 0} passengers</TableCell>
                                        <TableCell sx={{ color: '#2e7d32', fontWeight: 'bold' }}>
                                            ${row.generatedAmount?.toLocaleString('es-CL')} CLP
                                        </TableCell>
                                    </TableRow>
                                ))}
                                {rankingData.length === 0 && (
                                    <TableRow>
                                        <TableCell colSpan={5} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                                            No se registra demanda en el período ingresado.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </Paper>
        </Container>
    );
}