import React, { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
    AppBar,
    Toolbar,
    Typography,
    Button,
    Box,
    Container,
    Avatar,
    Menu,
    MenuItem,
    IconButton,
    Divider,
    ListItemIcon,
} from '@mui/material';
import {
    Dashboard as DashboardIcon,
    Person as PersonIcon,
    Logout as LogoutIcon,
    AutoAwesome,
    Favorite as FavoriteIcon,
    History as HistoryIcon,
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';

const Navbar = () => {
    const { user, isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();
    const [anchorEl, setAnchorEl] = useState(null);

    const handleMenuOpen = (event) => {
        setAnchorEl(event.currentTarget);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
    };

    const handleLogout = () => {
        handleMenuClose();
        logout();
        navigate('/login');
    };

    return (
        <AppBar position="sticky" elevation={0} sx={{ bgcolor: 'rgba(9, 13, 22, 0.85)', backdropFilter: 'blur(16px)', borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
            <Container maxWidth="xl">
                <Toolbar disableGutters sx={{ justifyContent: 'space-between', height: 70 }}>
                    {/* Logo */}
                    <Box component={RouterLink} to="/" sx={{ display: 'flex', alignItems: 'center', gap: 1.5, textDecoration: 'none' }}>
                        <Box
                            sx={{
                                width: 40,
                                height: 40,
                                borderRadius: 2,
                                background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 50%, #ec4899 100%)',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                boxShadow: '0 4px 14px rgba(99, 102, 241, 0.4)',
                            }}
                        >
                            <AutoAwesome sx={{ color: '#fff', fontSize: 24 }} />
                        </Box>
                        <Typography variant="h5" sx={{ fontWeight: 800, color: '#fff', letterSpacing: '-0.5px' }}>
                            ShopSense <span style={{ background: 'linear-gradient(135deg, #818cf8 0%, #ec4899 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>AI</span>
                        </Typography>
                    </Box>

                    {/* Right Menu Links */}
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: { xs: 1, md: 2 } }}>
                        {isAuthenticated ? (
                            <>
                                <Button
                                    component={RouterLink}
                                    to="/dashboard"
                                    startIcon={<DashboardIcon />}
                                    sx={{
                                        display: { xs: 'none', sm: 'inline-flex' },
                                        color: '#94a3b8',
                                        '&:hover': { color: '#fff', bgcolor: 'rgba(255, 255, 255, 0.05)' },
                                    }}
                                >
                                    Dashboard
                                </Button>
                                <Button
                                    component={RouterLink}
                                    to="/wishlist"
                                    startIcon={<FavoriteIcon />}
                                    sx={{
                                        display: { xs: 'none', md: 'inline-flex' },
                                        color: '#94a3b8',
                                        '&:hover': { color: '#ec4899', bgcolor: 'rgba(236, 72, 153, 0.05)' },
                                    }}
                                >
                                    Wishlist
                                </Button>
                                <Button
                                    component={RouterLink}
                                    to="/search-history"
                                    startIcon={<HistoryIcon />}
                                    sx={{
                                        display: { xs: 'none', md: 'inline-flex' },
                                        color: '#94a3b8',
                                        '&:hover': { color: '#22d3ee', bgcolor: 'rgba(6, 182, 212, 0.05)' },
                                    }}
                                >
                                    History
                                </Button>
                                <IconButton onClick={handleMenuOpen} sx={{ p: 0.5, border: '2px solid rgba(99, 102, 241, 0.5)' }}>
                                    <Avatar sx={{ bgcolor: '#6366f1', width: 36, height: 36, fontWeight: 700 }}>
                                        {user?.name?.charAt(0).toUpperCase() || 'U'}
                                    </Avatar>
                                </IconButton>
                                <Menu
                                    anchorEl={anchorEl}
                                    open={Boolean(anchorEl)}
                                    onClose={handleMenuClose}
                                    PaperProps={{
                                        sx: {
                                            mt: 1.5,
                                            minWidth: 200,
                                            bgcolor: '#131b2e',
                                            border: '1px solid rgba(255, 255, 255, 0.1)',
                                            boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
                                        },
                                    }}
                                    transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                                    anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                                >
                                    <Box sx={{ px: 2, py: 1.5 }}>
                                        <Typography variant="subtitle2" sx={{ fontWeight: 700, color: '#fff' }}>
                                            {user?.name}
                                        </Typography>
                                        <Typography variant="body2" sx={{ color: '#94a3b8', fontSize: '0.8rem' }}>
                                            {user?.email}
                                        </Typography>
                                    </Box>
                                    <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />
                                    <MenuItem onClick={() => { handleMenuClose(); navigate('/dashboard'); }}>
                                        <ListItemIcon><DashboardIcon fontSize="small" sx={{ color: '#818cf8' }} /></ListItemIcon>
                                        Dashboard
                                    </MenuItem>
                                    <MenuItem onClick={() => { handleMenuClose(); navigate('/wishlist'); }}>
                                        <ListItemIcon><FavoriteIcon fontSize="small" sx={{ color: '#ec4899' }} /></ListItemIcon>
                                        Wishlist
                                    </MenuItem>
                                    <MenuItem onClick={() => { handleMenuClose(); navigate('/search-history'); }}>
                                        <ListItemIcon><HistoryIcon fontSize="small" sx={{ color: '#22d3ee' }} /></ListItemIcon>
                                        Search History
                                    </MenuItem>
                                    <MenuItem onClick={() => { handleMenuClose(); navigate('/profile'); }}>
                                        <ListItemIcon><PersonIcon fontSize="small" sx={{ color: '#818cf8' }} /></ListItemIcon>
                                        Profile
                                    </MenuItem>
                                    <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />
                                    <MenuItem onClick={handleLogout} sx={{ color: '#ef4444' }}>
                                        <ListItemIcon><LogoutIcon fontSize="small" sx={{ color: '#ef4444' }} /></ListItemIcon>
                                        Logout
                                    </MenuItem>
                                </Menu>
                            </>
                        ) : (
                            <>
                                <Button
                                    component={RouterLink}
                                    to="/login"
                                    sx={{ color: '#cbd5e1', '&:hover': { color: '#fff', bgcolor: 'rgba(255, 255, 255, 0.05)' } }}
                                >
                                    Sign In
                                </Button>
                                <Button
                                    component={RouterLink}
                                    to="/register"
                                    variant="contained"
                                    sx={{
                                        background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                                        '&:hover': { background: 'linear-gradient(135deg, #4f46e5 0%, #9333ea 100%)' },
                                    }}
                                >
                                    Get Started
                                </Button>
                            </>
                        )}
                    </Box>
                </Toolbar>
            </Container>
        </AppBar>
    );
};

export default Navbar;
