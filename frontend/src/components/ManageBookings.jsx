import { useEffect, useState, useCallback } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { Container, Paper, Typography, Box, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Select, MenuItem, Button, CircularProgress, Alert } from '@mui/material';
import BookingService from '../services/BookingService';

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

                    const safeData = rawList.map((booking) => {
                        const castedBooking = booking;
                        return {
                            id: castedBooking?.id ? String(castedBooking.id) : '',
                            packageName: castedBooking?.touristPackage?.name || "Destino Turístico",
                            userEmail: castedBooking?.userEmail || "Cliente",
                            totalAmount: Number(castedBooking?.totalAmount || 0),
                            passengerCount: Number(castedBooking?.passengerCount || 1),
                            dateText: castedBooking?.date || "Reciente",
                            status: castedBooking?.status ? String(castedBooking.status).toUpperCase() : "PENDIENTE"
                        };
                    });

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
                alert("Estado actualizado correctamente a: " + newStatus);
                fetchAllBookings();
            })
            .catch((err) => {
                console.error("Error updating status:", err);
                const serverMessage = err.response?.data || "No se pudo actualizar el estado de la reserva.";
                alert("Error de Regla de Negocio: " + serverMessage);
                fetchAllBookings();
            });
    };

    const handlePrintReceipt = (booking) => {
        // --- REGLA DE NEGOCIO ENFORZADA ---
        // 1. Validamos que la reserva exista de verdad y tenga un ID válido asignado por la BD
        if (!booking || !booking.id || booking.id.trim() === '') {
            alert("Error: No se puede emitir un comprobante para una reserva que no está válidamente registrada.");
            return;
        }

        // 2. Control de emisión según el estado operativo (Evitamos emitir vouchers para pendientes o cancelados)
        if (booking.status === 'PENDIENTE') {
            alert("Aviso: No se puede generar una constancia oficial. La reserva aún se encuentra PENDIENTE de pago.");
            return;
        }
        if (booking.status === 'CANCELADA') {
            alert("Error: No se permite emitir comprobantes para reservas CANCELADAS.");
            return;
        }

        const printWindow = window.open('', '_blank');
        if (!printWindow) return;

        printWindow.document.title = `Comprobante de Reserva #${booking.id}`;

        printWindow.document.body.innerHTML = `
            <div style="font-family: Arial, sans-serif; padding: 40px; color: #333;">
                <div style="text-align: center; border-bottom: 2px solid #ff8c00; padding-bottom: 20px;">
                    <h2>COMPROBANTE OFICIAL DE RESERVA</h2>
                    <p>Agencia de Turismo S.A.</p>
                </div>
                <div style="margin-top: 30px; line-height: 1.8;">
                    <p><strong>ID de Operación:</strong> #${booking.id}</p>
                    <p><strong>Cliente:</strong> ${booking.userEmail}</p>
                    <p><strong>Paquete Turístico:</strong> ${booking.packageName}</p>
                    <p><strong>Fecha de Registro:</strong> ${booking.dateText}</p>
                    <p><strong>Cantidad de Pasajeros:</strong> ${booking.passengerCount}</p>
                    <p><strong>Monto Total Financiado:</strong> $${booking.totalAmount} CLP</p>
                    <p><strong>Estado Actual:</strong> ${booking.status}</p>
                </div>
                <div style="margin-top: 50px; text-align: center; font-size: 12px; color: #777;">
                    <p>* Este documento sirve como respaldo legal de la operación y reserva de cupos.</p>
                </div>
            </div>
        `;

        printWindow.document.close();
        printWindow.print();
    };

    if (!initialized || loading) {
        return <Box sx={{ p: 5, textAlign: 'center' }}><CircularProgress color="primary" /></Box>;
    }

    return (
        <Container maxWidth="lg" sx={{ py: 5 }}>
            <Typography variant="h4" gutterBottom sx={{ color: '#37474f', fontWeight: 'bold', mb: 4 }}>
                Panel de Supervisión de Reservas (Agencia)
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
                                <TableCell>{item.userEmail}</TableCell>
                                <TableCell>{item.packageName}</TableCell>
                                <TableCell>{item.passengerCount}</TableCell>
                                <TableCell sx={{ fontWeight: 'bold', color: '#2e7d32' }}>${item.totalAmount}</TableCell>
                                <TableCell>
                                    <Select
                                        value={item.status}
                                        size="small"
                                        variant="outlined"
                                        onChange={(e) => handleStatusChange(item.id, e.target.value)}
                                        sx={{ minWidth: '130px' }}
                                    >
                                        <MenuItem value="PENDIENTE">PENDIENTE</MenuItem>
                                        <MenuItem value="CONFIRMADA">CONFIRMADA</MenuItem>
                                        <MenuItem value="COMPLETADA">COMPLETADA</MenuItem>
                                        <MenuItem value="CANCELADA">CANCELADA</MenuItem>
                                    </Select>
                                </TableCell>
                                <TableCell sx={{ textAlign: 'center' }}>
                                    <Button
                                        variant="outlined"
                                        color="primary"
                                        size="small"
                                        onClick={() => handlePrintReceipt(item)}
                                    >
                                        Documento
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                        {bookings.length === 0 && (
                            <TableRow>
                                <TableCell colSpan={7} sx={{ textAlign: 'center', py: 3, color: 'text.secondary' }}>
                                    No se registran reservas activas en el sistema.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
    );
}