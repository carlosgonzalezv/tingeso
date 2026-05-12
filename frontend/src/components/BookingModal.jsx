import { Modal, Box, Typography, TextField, Button, Stack } from '@mui/material';
import { useState } from 'react';

const style = {
    position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)',
    width: 400, bgcolor: 'background.paper', borderRadius: '8px', boxShadow: 24, p: 4,
};

export default function BookingModal({ open, onClose, onConfirm, packName, maxSlots }) {
    const [count, setCount] = useState(1);
    const [requests, setRequests] = useState("");

    const handleConfirm = () => {
        onConfirm({ passengerCount: count, specialRequests: requests });
        onClose();
    };

    return (
        <Modal open={open} onClose={onClose}>
            <Box sx={style}>
                <Typography variant="h6" sx={{ mb: 2 }}>Reservar: {packName}</Typography>
                <Stack spacing={3}>
                    <TextField
                        label="Cantidad de Pasajeros"
                        type="number"
                        fullWidth
                        inputProps={{ min: 1, max: maxSlots }}
                        value={count}
                        onChange={(e) => setCount(parseInt(e.target.value))}
                        helperText={`Máximo disponible: ${maxSlots}`}
                    />
                    <TextField
                        label="Solicitudes Especiales (Opcional)"
                        multiline rows={3}
                        fullWidth
                        value={requests}
                        onChange={(e) => setRequests(e.target.value)}
                        placeholder="Ej: Alergias alimentarias, habitación cerca del ascensor..."
                    />
                    <Button onClick={handleConfirm} variant="contained" sx={{ bgcolor: '#FB8C00' }}>
                        Confirmar Reserva
                    </Button>
                </Stack>
            </Box>
        </Modal>
    );
}