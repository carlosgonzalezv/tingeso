import { useEffect, useState } from 'react';
import axios from 'axios';
import { useKeycloak } from '@react-keycloak/web';
import {
    Table, TableBody, TableCell, TableHead,
    TableRow, Paper, CircularProgress, Typography
} from '@mui/material';

const UserList = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const { keycloak } = useKeycloak();

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                const response = await axios.get('/api/v1/user/users', {
                    headers: {
                        Authorization: `Bearer ${keycloak.token}`
                    }
                });
                setUsers(response.data);
            } catch (error) {
                console.error("Error al obtener la lista de usuarios:", error);
            } finally {
                setLoading(false);
            }
        };
        if (keycloak.token) {
            fetchUsers();
        }
    }, [keycloak.token]);

    if (loading) return <CircularProgress sx={{ mt: 2 }} />;

    if (users.length === 0) {
        return <Typography sx={{ mt: 2 }}>No hay usuarios registrados aún.</Typography>;
    }

    return (
        <Paper elevation={3} sx={{ mt: 3, width: '100%', overflowX: 'auto' }}>
            <Table>
                <TableHead sx={{ backgroundColor: '#eeeeee' }}>
                    <TableRow>
                        <TableCell><strong>Nombre</strong></TableCell>
                        <TableCell><strong>Email</strong></TableCell>
                        <TableCell><strong>RUT / ID</strong></TableCell>
                        <TableCell><strong>Rol</strong></TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    {users.map((user) => (
                        <TableRow key={user.keycloackid || user.id}>
                            <TableCell>{user.name}</TableCell>
                            <TableCell>{user.email}</TableCell>
                            <TableCell>{user.idDocument}</TableCell>
                            <TableCell>{user.rol}</TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </Paper>
    );
};

export default UserList;