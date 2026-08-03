import React, { useState, useEffect } from 'react';
import {
    Box,
    Container,
    Typography,
    Card,
    CardContent,
    Avatar,
    Grid,
    Divider,
    Chip,
    CircularProgress,
    Alert,
    Paper,
} from '@mui/material';
import {
    Person,
    Email,
    Badge,
    CalendarToday,
    Security,
    VerifiedUser,
} from '@mui/icons-material';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';

const ProfilePage = () => {
    const { user: initialUser } = useAuth();
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get('/users/profile');
                setProfile(response.data);
            } catch (err) {
                console.error('Failed to fetch user profile:', err);
                setError(err.response?.data?.message || 'Failed to load user profile from backend API.');
                setProfile(initialUser);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, [initialUser]);

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
                <CircularProgress size={48} sx={{ color: '#6366f1' }} />
            </Box>
        );
    }

    const currentUser = profile || initialUser;

    return (
        <Container maxWidth="md" sx={{ py: 8 }}>
            <Box sx={{ mb: 4 }}>
                <Typography variant="h3" sx={{ fontWeight: 800, color: '#fff', mb: 1 }}>
                    User Profile
                </Typography>
                <Typography variant="body1" sx={{ color: '#94a3b8' }}>
                    Account details retrieved securely via JWT Bearer authentication
                </Typography>
            </Box>

            {error && (
                <Alert severity="warning" sx={{ mb: 4, borderRadius: 2 }}>
                    {error}
                </Alert>
            )}

            <Card>
                <CardContent sx={{ p: { xs: 3, md: 5 } }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, mb: 4 }}>
                        <Avatar
                            sx={{
                                width: 80,
                                height: 80,
                                bgcolor: '#6366f1',
                                fontSize: '2.5rem',
                                fontWeight: 700,
                                boxShadow: '0 8px 24px rgba(99, 102, 241, 0.4)',
                            }}
                        >
                            {currentUser?.name?.charAt(0).toUpperCase() || 'U'}
                        </Avatar>
                        <Box>
                            <Typography variant="h4" sx={{ fontWeight: 700, color: '#fff' }}>
                                {currentUser?.name}
                            </Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                                <Chip
                                    icon={<VerifiedUser sx={{ fontSize: '0.85rem !important', color: '#818cf8 !important' }} />}
                                    label={currentUser?.role || 'ROLE_USER'}
                                    size="small"
                                    sx={{ bgcolor: 'rgba(99, 102, 241, 0.15)', color: '#818cf8', fontWeight: 600 }}
                                />
                                <Chip
                                    label="JWT Authenticated"
                                    size="small"
                                    sx={{ bgcolor: 'rgba(16, 185, 129, 0.15)', color: '#34d399', fontWeight: 600 }}
                                />
                            </Box>
                        </Box>
                    </Box>

                    <Divider sx={{ my: 3, borderColor: 'rgba(255, 255, 255, 0.08)' }} />

                    <Grid container spacing={3}>
                        <Grid item xs={12} sm={6}>
                            <Paper sx={{ p: 2.5, bgcolor: 'rgba(255, 255, 255, 0.03)', borderRadius: 2, border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
                                    <Badge sx={{ color: '#818cf8' }} />
                                    <Typography variant="caption" sx={{ color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1, fontWeight: 700 }}>
                                        User ID
                                    </Typography>
                                </Box>
                                <Typography variant="h6" sx={{ color: '#fff', fontWeight: 600 }}>
                                    #{currentUser?.id}
                                </Typography>
                            </Paper>
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <Paper sx={{ p: 2.5, bgcolor: 'rgba(255, 255, 255, 0.03)', borderRadius: 2, border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
                                    <Email sx={{ color: '#818cf8' }} />
                                    <Typography variant="caption" sx={{ color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1, fontWeight: 700 }}>
                                        Email Address
                                    </Typography>
                                </Box>
                                <Typography variant="h6" sx={{ color: '#fff', fontWeight: 600 }}>
                                    {currentUser?.email}
                                </Typography>
                            </Paper>
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <Paper sx={{ p: 2.5, bgcolor: 'rgba(255, 255, 255, 0.03)', borderRadius: 2, border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
                                    <Person sx={{ color: '#818cf8' }} />
                                    <Typography variant="caption" sx={{ color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1, fontWeight: 700 }}>
                                        Full Name
                                    </Typography>
                                </Box>
                                <Typography variant="h6" sx={{ color: '#fff', fontWeight: 600 }}>
                                    {currentUser?.name}
                                </Typography>
                            </Paper>
                        </Grid>

                        <Grid item xs={12} sm={6}>
                            <Paper sx={{ p: 2.5, bgcolor: 'rgba(255, 255, 255, 0.03)', borderRadius: 2, border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 1 }}>
                                    <CalendarToday sx={{ color: '#818cf8' }} />
                                    <Typography variant="caption" sx={{ color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1, fontWeight: 700 }}>
                                        Account Created
                                    </Typography>
                                </Box>
                                <Typography variant="h6" sx={{ color: '#fff', fontWeight: 600 }}>
                                    {currentUser?.createdAt ? new Date(currentUser.createdAt).toLocaleDateString(undefined, { year: 'numeric', month: 'long', day: 'numeric' }) : 'Active Session'}
                                </Typography>
                            </Paper>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>
        </Container>
    );
};

export default ProfilePage;
