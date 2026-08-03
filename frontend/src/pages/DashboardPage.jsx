import React from 'react';
import {
    Box,
    Container,
    Typography,
    Grid,
    Card,
    CardContent,
    Avatar,
    Chip,
    Button,
} from '@mui/material';
import {
    Dashboard as DashboardIcon,
    AutoAwesome,
    VerifiedUser,
    Compare,
    Analytics,
    NotificationsActive,
    TrendingUp,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const DashboardPage = () => {
    const { user } = useAuth();

    return (
        <Container maxWidth="xl" sx={{ py: 6 }}>
            {/* Banner Card */}
            <Box
                sx={{
                    p: { xs: 4, md: 5 },
                    borderRadius: 4,
                    background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(168, 85, 247, 0.2) 50%, rgba(236, 72, 153, 0.15) 100%)',
                    border: '1px solid rgba(99, 102, 241, 0.3)',
                    backdropFilter: 'blur(16px)',
                    mb: 6,
                }}
            >
                <Grid container spacing={3} alignItems="center">
                    <Grid item xs={12} md={8}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                            <Chip
                                icon={<VerifiedUser sx={{ fontSize: '0.9rem !important', color: '#10b981 !important' }} />}
                                label="Authentication Active"
                                size="small"
                                sx={{ bgcolor: 'rgba(16, 185, 129, 0.15)', color: '#34d399', border: '1px solid rgba(16, 185, 129, 0.3)' }}
                            />
                            <Chip
                                label="Phase 1 Foundation"
                                size="small"
                                sx={{ bgcolor: 'rgba(99, 102, 241, 0.15)', color: '#818cf8', border: '1px solid rgba(99, 102, 241, 0.3)' }}
                            />
                        </Box>
                        <Typography variant="h2" sx={{ fontWeight: 800, color: '#fff', mb: 1, fontSize: { xs: '2rem', md: '2.8rem' } }}>
                            Welcome to ShopSense AI
                        </Typography>
                        <Typography variant="body1" sx={{ color: '#cbd5e1', fontSize: '1.1rem', maxW: 650 }}>
                            Hello, <strong>{user?.name || 'User'}</strong>! Your secure full-stack JWT authentication session is established.
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={4} sx={{ textAlign: { xs: 'left', md: 'right' } }}>
                        <Avatar
                            sx={{
                                width: 72,
                                height: 72,
                                bgcolor: '#6366f1',
                                fontSize: '2rem',
                                fontWeight: 700,
                                ml: { md: 'auto' },
                                boxShadow: '0 8px 24px rgba(99, 102, 241, 0.4)',
                            }}
                        >
                            {user?.name?.charAt(0).toUpperCase() || 'S'}
                        </Avatar>
                    </Grid>
                </Grid>
            </Box>

            {/* Module Overview Placeholders */}
            <Typography variant="h5" sx={{ fontWeight: 700, mb: 3, color: '#fff' }}>
                Upcoming System Modules (Phase 2 Roadmap)
            </Typography>

            <Grid container spacing={3}>
                {[
                    {
                        title: 'Product Search & Aggregation',
                        desc: 'Multi-platform crawler integrating Amazon, Flipkart, Croma, & Reliance Digital live prices.',
                        icon: <Compare sx={{ fontSize: 32, color: '#6366f1' }} />,
                        status: 'Phase 2 Ready',
                    },
                    {
                        title: 'AI Recommendation Engine',
                        desc: 'Gemini AI synthesis for spec matrix comparison and customer review summaries.',
                        icon: <AutoAwesome sx={{ fontSize: 32, color: '#a855f7' }} />,
                        status: 'Phase 2 Ready',
                    },
                    {
                        title: 'Price History & Alerts',
                        desc: 'Historical price trend graphs and automated threshold notification alerts.',
                        icon: <TrendingUp sx={{ fontSize: 32, color: '#ec4899' }} />,
                        status: 'Phase 3 Roadmap',
                    },
                    {
                        title: 'User Analytics & Wishlist',
                        desc: 'Personalized product tracking dashboard and saved comparison collections.',
                        icon: <Analytics sx={{ fontSize: 32, color: '#06b6d4' }} />,
                        status: 'Phase 3 Roadmap',
                    },
                ].map((module, idx) => (
                    <Grid item xs={12} sm={6} md={3} key={idx}>
                        <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                            <CardContent sx={{ p: 3, display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                                    <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: 'rgba(255, 255, 255, 0.05)' }}>
                                        {module.icon}
                                    </Box>
                                    <Chip label={module.status} size="small" variant="outlined" sx={{ borderColor: 'rgba(255, 255, 255, 0.2)', color: '#94a3b8', fontSize: '0.75rem' }} />
                                </Box>
                                <Typography variant="h6" sx={{ fontWeight: 700, color: '#fff', mb: 1 }}>
                                    {module.title}
                                </Typography>
                                <Typography variant="body2" sx={{ color: '#94a3b8', lineHeight: 1.5 }}>
                                    {module.desc}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Container>
    );
};

export default DashboardPage;
