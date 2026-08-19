import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
    Box,
    Container,
    Typography,
    Button,
    Grid,
    Card,
    CardContent,
    Chip,
    Stack,
} from '@mui/material';
import {
    AutoAwesome,
    CompareArrows,
    TrendingDown,
    Psychology,
    Shield,
    ArrowForward,
    Dashboard as DashboardIcon,
    Favorite as FavoriteIcon,
} from '@mui/icons-material';

import { useAuth } from '../context/AuthContext';
import QuickSearchBar from '../components/product/QuickSearchBar';

const LandingPage = () => {
    const { isAuthenticated } = useAuth();

    return (
        <Box sx={{ minHeight: '80vh' }}>
            {/* Hero Section */}
            <Container maxWidth="xl" sx={{ pt: { xs: 8, md: 14 }, pb: { xs: 8, md: 10 } }}>
                <Box sx={{ textAlign: 'center', maxW: 900, mx: 'auto' }}>
                    <Chip
                        icon={<AutoAwesome sx={{ fontSize: '1rem !important', color: '#a855f7 !important' }} />}
                        label="ShopSense AI • Foundation Phase 1 Ready"
                        sx={{
                            bgcolor: 'rgba(99, 102, 241, 0.1)',
                            color: '#818cf8',
                            border: '1px solid rgba(99, 102, 241, 0.3)',
                            mb: 3,
                            fontWeight: 600,
                            py: 0.5,
                            px: 1,
                        }}
                    />
                    <Typography
                        variant="h1"
                        sx={{
                            fontSize: { xs: '2.5rem', sm: '3.8rem', md: '4.5rem' },
                            fontWeight: 800,
                            lineHeight: 1.1,
                            letterSpacing: '-1.5px',
                            mb: 3,
                        }}
                    >
                        Compare Products Across Stores with{' '}
                        <span className="gradient-text">Intelligent AI</span>
                    </Typography>
                    <Typography
                        variant="h6"
                        sx={{
                            color: '#94a3b8',
                            fontWeight: 400,
                            maxWidth: 750,
                            mx: 'auto',
                            mb: 4,
                            fontSize: { xs: '1rem', md: '1.25rem' },
                            lineHeight: 1.6,
                        }}
                    >
                        ShopSense AI aggregates real-time prices, specs, and review summaries from Amazon, Flipkart, Croma, and Reliance Digital to find your perfect match.
                    </Typography>

                    <Box sx={{ maxWidth: 750, mx: 'auto', mb: 4 }}>
                        <QuickSearchBar />
                    </Box>

                    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="center">
                        {isAuthenticated ? (
                            <>
                                <Button
                                    component={RouterLink}
                                    to="/dashboard"
                                    variant="contained"
                                    size="large"
                                    startIcon={<DashboardIcon />}
                                    endIcon={<ArrowForward />}
                                    sx={{
                                        py: 1.8,
                                        px: 4,
                                        fontSize: '1.05rem',
                                        background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                                    }}
                                >
                                    Go to Dashboard
                                </Button>
                                <Button
                                    component={RouterLink}
                                    to="/wishlist"
                                    variant="outlined"
                                    size="large"
                                    startIcon={<FavoriteIcon />}
                                    sx={{
                                        py: 1.8,
                                        px: 4,
                                        fontSize: '1.05rem',
                                        borderColor: 'rgba(255, 255, 255, 0.2)',
                                        color: '#f8fafc',
                                        '&:hover': { borderColor: '#ec4899', bgcolor: 'rgba(236, 72, 153, 0.1)' },
                                    }}
                                >
                                    View Wishlist
                                </Button>
                            </>
                        ) : (
                            <>
                                <Button
                                    component={RouterLink}
                                    to="/register"
                                    variant="contained"
                                    size="large"
                                    endIcon={<ArrowForward />}
                                    sx={{
                                        py: 1.8,
                                        px: 4,
                                        fontSize: '1.05rem',
                                        background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                                    }}
                                >
                                    Get Started Free
                                </Button>
                                <Button
                                    component={RouterLink}
                                    to="/login"
                                    variant="outlined"
                                    size="large"
                                    sx={{
                                        py: 1.8,
                                        px: 4,
                                        fontSize: '1.05rem',
                                        borderColor: 'rgba(255, 255, 255, 0.2)',
                                        color: '#f8fafc',
                                        '&:hover': { borderColor: '#6366f1', bgcolor: 'rgba(99, 102, 241, 0.1)' },
                                    }}
                                >
                                    Sign In to Account
                                </Button>
                            </>
                        )}
                    </Stack>
                </Box>

                {/* Feature Cards Grid */}
                <Grid container spacing={4} sx={{ mt: { xs: 6, md: 10 } }}>
                    {[
                        {
                            icon: <CompareArrows sx={{ fontSize: 40, color: '#6366f1' }} />,
                            title: 'Multi-Store Comparison',
                            desc: 'Seamlessly compare product prices, features, and availability across top Indian e-commerce platforms in one unified view.',
                        },
                        {
                            icon: <Psychology sx={{ fontSize: 40, color: '#a855f7' }} />,
                            title: 'AI Product Insights',
                            desc: 'Generative AI synthesizes thousands of customer reviews into concise pros, cons, and tailored buying recommendations.',
                        },
                        {
                            icon: <TrendingDown sx={{ fontSize: 40, color: '#ec4899' }} />,
                            title: 'Price Tracking & Alerts',
                            desc: 'Monitor price history trends and receive instant notifications when your wishlisted items drop to your target price.',
                        },
                    ].map((feature, idx) => (
                        <Grid item xs={12} md={4} key={idx}>
                            <Card
                                sx={{
                                    height: '100%',
                                    transition: 'transform 0.3s ease, border-color 0.3s ease',
                                    '&:hover': {
                                        transform: 'translateY(-6px)',
                                        borderColor: 'rgba(99, 102, 241, 0.5)',
                                    },
                                }}
                            >
                                <CardContent sx={{ p: 4 }}>
                                    <Box
                                        sx={{
                                            width: 60,
                                            height: 60,
                                            borderRadius: 3,
                                            bgcolor: 'rgba(99, 102, 241, 0.1)',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            mb: 3,
                                        }}
                                    >
                                        {feature.icon}
                                    </Box>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1.5, color: '#fff' }}>
                                        {feature.title}
                                    </Typography>
                                    <Typography variant="body1" sx={{ color: '#94a3b8', lineHeight: 1.6 }}>
                                        {feature.desc}
                                    </Typography>
                                </CardContent>
                            </Card>
                        </Grid>
                    ))}
                </Grid>

                {/* Tech Stack Banner */}
                <Box
                    sx={{
                        mt: 10,
                        p: 4,
                        borderRadius: 4,
                        bgcolor: 'rgba(19, 27, 46, 0.6)',
                        border: '1px solid rgba(255, 255, 255, 0.08)',
                        textAlign: 'center',
                    }}
                >
                    <Typography variant="subtitle2" sx={{ textTransform: 'uppercase', letterSpacing: 2, color: '#818cf8', fontWeight: 700, mb: 2 }}>
                        Engineered with Production-Grade Tech Stack
                    </Typography>
                    <Stack direction="row" spacing={3} justifyContent="center" flexWrap="wrap" useFlexGap sx={{ gap: 2 }}>
                        {['Spring Boot 3', 'Java 17', 'Spring Security', 'JWT Auth', 'Spring Data JPA', 'MySQL', 'React 18', 'Material-UI', 'OpenAPI / Swagger'].map((tech, i) => (
                            <Chip key={i} label={tech} variant="outlined" sx={{ borderColor: 'rgba(255, 255, 255, 0.15)', color: '#cbd5e1' }} />
                        ))}
                    </Stack>
                </Box>
            </Container>
        </Box>
    );
};

export default LandingPage;
