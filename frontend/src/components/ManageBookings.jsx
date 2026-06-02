import { useEffect, useState, useCallback } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Container, Paper, Typography, Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Select, MenuItem, Button, CircularProgress, Alert } from '@mui/material';
import BookingService from '../services/BookingService';

//interface that connects the administrator to the reservation's database.
export default function ManageBookings() {
    const { keycloak, initialized } = useKeycloak();
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchAllBookings = useCallback(() => {
        if (initialized && keycloak.authenticated) {
            BookingService.getAllBookings(keycloak.token)
                .then((data) => {
                    const rawList = Array.isArray(data) ? data : [];
                    const safeData = rawList.map((booking) => ({
                        id: booking?.id ? String(booking.id) : '',
                        packageName: booking?.packTour?.name || booking?.packageName || "Sin nombre",
                        userName: booking?.users?.name || booking?.userEmail || "Usuario Desconocido",
                        totalAmount: Number(booking?.totalAmount || booking?.totalamount || 0),
                        passengerCount: Number(booking?.passengersCount || booking?.passengerCount || 1),
                        status: booking?.status ? String(booking.status).toUpperCase() : "PENDIENTE"
                    }));
                    setBookings(safeData);
                    setLoading(false);
                })
                .catch((err) => {
                    console.error("Error fetching all bookings:", err);
                    setError("No se pudieron cargar las reservas del sistema.");
                    setLoading(false);
                });
        }
    }, [initialized, keycloak.authenticated, keycloak.token]);

    useEffect(() => {
        fetchAllBookings();
    }, [fetchAllBookings]);

    const handleStatusChange = (bookingId, newStatus) => {
        BookingService.updateBookingStatus(keycloak.token, bookingId, newStatus)
            .then(() => {
                alert("Estado actualizado correctamente");
                fetchAllBookings();
            })
            .catch((err) => {
                console.error(err);
                alert("Error al actualizar el estado");
            });
    };

    const handlePrintReceipt = (booking) => {
        if (!booking || !booking.id) return;
        const printWindow = window.open('', '_blank');
        printWindow.document.write(`
            <html>
                <head><title>Comprobante de Reserva</title></head>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Reserva #${booking.id}</h2>
                    <p><strong>Cliente:</strong> ${booking.userName}</p>
                    <p><strong>Paquete:</strong> ${booking.packageName}</p>
                    <p><strong>Pasajeros:</strong> ${booking.passengerCount}</p>
                    <p><strong>Total:</strong> $${booking.totalAmount}</p>
                </body>
            </html>
        `);
        printWindow.document.close();
        printWindow.print();
    };

    if (!initialized || loading) {
        return <Box sx={{ p: 5, textAlign: 'center' }}><CircularProgress color="primary" /></Box>;
    }

    return (
        <Container maxWidth="lg" sx={{ py: 5 }}>
            <Typography variant="h4" gutterBottom sx={{ color: '#37474f', fontWeight: 'bold', mb: 4 }}>
                Panel de Supervisión de Reservas
            </Typography>

            {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

            <TableContainer component={Paper} elevation={3} sx={{ borderRadius: '12px' }}>
                <Table>
                    <TableHead sx={{ bgcolor: '#37474f' }}>
                        <TableRow>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>ID</TableCell>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Cliente</TableCell>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Paquete</TableCell>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Pasajeros</TableCell>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Total</TableCell>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold' }}>Estado</TableCell>
                            <TableCell sx={{ color: '#fff', fontWeight: 'bold', textAlign: 'center' }}>Acciones</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {bookings.map((item) => (
                            <TableRow key={item.id} hover>
                                <TableCell>#{item.id}</TableCell>
                                <TableCell>{item.userName}</TableCell>
                                <TableCell>{item.packageName}</TableCell>
                                <TableCell>{item.passengerCount}</TableCell>
                                <TableCell sx={{ fontWeight: 'bold', color: '#2e7d32' }}>${item.totalAmount?.toLocaleString('es-CL')}</TableCell>
                                <TableCell>
                                    <Select
                                        value={item.status}
                                        size="small"
                                        onChange={(e) => handleStatusChange(item.id, e.target.value)}
                                        sx={{ minWidth: 120 }}
                                    >
                                        <MenuItem value="PENDIENTE">PENDIENTE</MenuItem>
                                        <MenuItem value="CONFIRMADA">CONFIRMADA</MenuItem>
                                        <MenuItem value="COMPLETADA">COMPLETADA</MenuItem>
                                        <MenuItem value="CANCELADA">CANCELADA</MenuItem>
                                        <MenuItem value="EXPIRADA">EXPIRADA</MenuItem>
                                    </Select>
                                </TableCell>
                                <TableCell sx={{ textAlign: 'center' }}>
                                    <Button variant="outlined" size="small" onClick={() => handlePrintReceipt(item)}>
                                        Documento
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );
}