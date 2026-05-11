import * as React from 'react';
import { AppBar, Box, Toolbar, Typography, Menu, Container, Button, MenuItem } from '@mui/material';
import TravelExploreIcon from '@mui/icons-material/TravelExplore';
import { useKeycloak } from "@react-keycloak/web";
import { useNavigate } from 'react-router-dom';
const pages = ['Paquetes', 'Destinos'];

function Navbar() {
    const {keycloak, initialized} = useKeycloak();
    const [anchorElUser, setAnchorElUser] = React.useState(null);
    //const [userRole, setUserRole] = React.useState(null);
    const roles = keycloak.tokenParsed?.resource_access?.["sisgr-frontend"]?.roles || [];
    const isAdmin = roles.includes("ADMIN");
    const navigate = useNavigate();
    const handleOpenUserMenu = (event) => setAnchorElUser(event.currentTarget);
    const handleCloseUserMenu = () => setAnchorElUser(null);
    const userName = keycloak.tokenParsed?.name || "Perfil";
    const handleLogin = () => keycloak.login();
    const handleLogout = async () => {
        handleCloseUserMenu();
        await keycloak.logout({redirectUri: window.location.origin});
    };

    if (!initialized) {
        return <AppBar position="static" sx={{ backgroundColor: '#FB8C00' }}><Toolbar /></AppBar>;
    }

    return (
        <AppBar position="static" sx={{ backgroundColor: '#FB8C00' }}>
            <Container maxWidth="{false}">
                <Toolbar disableGutters>
                    <TravelExploreIcon sx={{ display: { xs: 'none', md: 'flex' }, mr: 1 }} />
                    <Typography variant="h6" noWrap component="a" href="/" sx={{ mr: 2, display: { xs: 'none', md: 'flex' }, fontWeight: 700, color: 'inherit', textDecoration: 'none' }}>
                        TravelAgency
                    </Typography>

                    <Box sx={{ flexGrow: 1, display: { xs: 'none', md: 'flex' } }}>
                        {pages.map((page) => (
                            <Button key={page} sx={{ my: 2, color: 'white', display: 'block', textTransform: 'none' }}>
                                {page}
                            </Button>
                        ))}
                    </Box>

                    <Box sx={{ flexGrow: 0 }}>
                        {!keycloak.authenticated ? (
                            <Button
                                onClick={handleLogin}
                                variant="outlined"
                                color="inherit"
                                sx={{ borderRadius: '20px', textTransform: 'none' }}
                            >
                                Iniciar Sesión
                            </Button>
                        ) : (
                            <>
                                <Button
                                    onClick={handleOpenUserMenu}
                                    variant="outlined"
                                    color="inherit"
                                    sx={{
                                        borderRadius: '20px',
                                        textTransform: 'none',
                                        borderColor: 'rgba(255, 255, 255, 0.5)',
                                        '&:hover': { borderColor: 'white' }
                                    }}
                                >
                                    {userName}
                                </Button>
                                <Menu
                                    sx={{ mt: '45px' }}
                                    anchorEl={anchorElUser}
                                    anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
                                    keepMounted
                                    transformOrigin={{ vertical: 'top', horizontal: 'right' }}
                                    open={Boolean(anchorElUser)}
                                    onClose={handleCloseUserMenu}
                                >
                                    <MenuItem disabled>
                                        <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                                            {keycloak.tokenParsed?.email}
                                        </Typography>
                                    </MenuItem>
                                    {isAdmin && (
                                        <MenuItem onClick={() => { handleCloseUserMenu(); navigate("/publish-package"); }}>
                                            <Typography sx={{ color: '#FB8C00', fontWeight: 'bold', textAlign: 'center' }}>
                                                Publicar Paquete
                                            </Typography>
                                        </MenuItem>
                                    )}
                                    <MenuItem onClick={() => { handleCloseUserMenu(); navigate("/perfil"); }}>
                                        <Typography textAlign="center">Mi Cuenta</Typography>
                                    </MenuItem>

                                    <MenuItem onClick={handleLogout}>
                                        <Typography textAlign="center" color="error">Cerrar Sesión</Typography>
                                    </MenuItem>
                                    <MenuItem disabled sx={{ opacity: "1 !important", py: 1.5 }}>
                                        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                                            <Typography variant="body2" sx={{ fontWeight: 'bold', color: 'text.primary' }}>
                                                {keycloak.tokenParsed?.email}
                                            </Typography>
                                            <Typography
                                                variant="caption"
                                                sx={{
                                                    color: isAdmin ? '#FB8C00' : 'text.secondary',
                                                    fontWeight: 700,
                                                    textTransform: 'uppercase',
                                                    fontSize: '0.7rem',
                                                    mt: 0.5
                                                }}
                                            >
                                                Rol: {isAdmin ? 'Administrador' : 'Usuario'}
                                            </Typography>
                                        </Box>
                                    </MenuItem>
                                </Menu>
                            </>
                        )}
                    </Box>
                </Toolbar>
            </Container>
        </AppBar>
    );
}

export default Navbar;