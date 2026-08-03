import React from 'react';
import { Box, Container, Typography, Grid, Link, Divider } from '@mui/material';
import { AutoAwesome } from '@mui/icons-material';

const Footer = () => {
    return (
        <Box component="footer" sx={{ bgcolor: '#070a12', borderTop: '1px solid rgba(255, 255, 255, 0.08)', pt: 6, pb: 4, mt: 'auto' }}>
            <Container maxWidth="xl">
                <Grid container spacing={4} justifyContent="space-between">
                    <Grid item xs={12} md={4}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
                            <Box
                                sx={{
                                    width: 32,
                                    height: 32,
                                    borderRadius: 1.5,
                                    background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 50%, #ec4899 100%)',
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                }}
                            >
                                <AutoAwesome sx={{ color: '#fff', fontSize: 18 }} />
                            </Box>
                            <Typography variant="h6" sx={{ fontWeight: 800, color: '#fff' }}>
                                ShopSense AI
                            </Typography>
                        </Box>
                        <Typography variant="body2" sx={{ color: '#94a3b8', maxW: 360, lineHeight: 1.6 }}>
                            AI-powered e-commerce product comparison platform helping consumers make smarter, faster purchasing decisions.
                        </Typography>
                    </Grid>
                    <Grid item xs={6} sm={3} md={2}>
                        <Typography variant="subtitle2" sx={{ color: '#fff', fontWeight: 700, mb: 2 }}>
                            Platform
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                            <Link href="/" underline="hover" sx={{ color: '#94a3b8', fontSize: '0.875rem' }}>Landing</Link>
                            <Link href="/dashboard" underline="hover" sx={{ color: '#94a3b8', fontSize: '0.875rem' }}>Dashboard</Link>
                            <Link href="/profile" underline="hover" sx={{ color: '#94a3b8', fontSize: '0.875rem' }}>Profile</Link>
                        </Box>
                    </Grid>
                    <Grid item xs={6} sm={3} md={2}>
                        <Typography variant="subtitle2" sx={{ color: '#fff', fontWeight: 700, mb: 2 }}>
                            Supported Platforms
                        </Typography>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                            <Typography variant="body2" sx={{ color: '#94a3b8' }}>Amazon</Typography>
                            <Typography variant="body2" sx={{ color: '#94a3b8' }}>Flipkart</Typography>
                            <Typography variant="body2" sx={{ color: '#94a3b8' }}>Croma</Typography>
                            <Typography variant="body2" sx={{ color: '#94a3b8' }}>Reliance Digital</Typography>
                        </Box>
                    </Grid>
                </Grid>
                <Divider sx={{ my: 4, borderColor: 'rgba(255, 255, 255, 0.08)' }} />
                <Typography variant="body2" align="center" sx={{ color: '#64748b' }}>
                    &copy; {new Date().getFullYear()} ShopSense AI. All rights reserved. Built with Spring Boot & React.
                </Typography>
            </Container>
        </Box>
    );
};

export default Footer;
