import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Container,
    Box,
    Typography,
    Grid,
    Paper,
    Button,
    IconButton,
    Snackbar,
    Alert,
    Tooltip,
} from '@mui/material';
import {
    Favorite,
    Delete,
    Visibility,
    Search,
    AccessTime,
} from '@mui/icons-material';
import { getWishlist, removeFromWishlist } from '../api/userFeatures';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';

const WishlistPage = () => {
    const navigate = useNavigate();
    const [wishlistItems, setWishlistItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });

    const fetchWishlist = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await getWishlist();
            setWishlistItems(data?.items || []);
        } catch (err) {
            console.error('Failed to fetch wishlist:', err);
            setError(err?.response?.data?.message || 'Failed to load your wishlist.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchWishlist();
    }, [fetchWishlist]);

    const handleRemove = async (productVariantId) => {
        try {
            await removeFromWishlist(productVariantId);
            setWishlistItems((prev) => prev.filter((item) => item.productVariantId !== productVariantId));
            setSnackbar({
                open: true,
                message: 'Item removed from wishlist.',
                severity: 'success',
            });
        } catch (err) {
            console.error('Failed to remove item from wishlist:', err);
            setSnackbar({
                open: true,
                message: err?.response?.data?.message || 'Failed to remove item from wishlist.',
                severity: 'error',
            });
        }
    };

    const handleNavigateProduct = (item) => {
        if (item && item.productId) {
            navigate(`/products/${item.productId}`);
        } else {
            console.error('Product ID missing on wishlist item:', item);
            setSnackbar({
                open: true,
                message: 'Unable to open product details: Product ID is missing.',
                severity: 'error',
            });
        }
    };

    const handleCloseSnackbar = () => {
        setSnackbar((prev) => ({ ...prev, open: false }));
    };

    const formatAddedDate = (dateStr) => {
        if (!dateStr) return null;
        try {
            const date = new Date(dateStr);
            return isNaN(date.getTime()) ? String(dateStr) : date.toLocaleDateString();
        } catch (e) {
            return String(dateStr);
        }
    };

    if (loading) {
        return (
            <Container maxWidth="xl" sx={{ py: 6 }}>
                <LoadingState message="Loading your wishlist..." minHeight="300px" />
            </Container>
        );
    }

    if (error) {
        return (
            <Container maxWidth="xl" sx={{ py: 6 }}>
                <ErrorState message={error} onRetry={fetchWishlist} />
            </Container>
        );
    }

    return (
        <Container maxWidth="xl" sx={{ py: { xs: 4, md: 6 } }}>
            {/* Header */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 4 }}>
                <Box
                    sx={{
                        width: 48,
                        height: 48,
                        borderRadius: 3,
                        background: 'linear-gradient(135deg, #ec4899 0%, #a855f7 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: '0 4px 14px rgba(236, 72, 153, 0.4)',
                    }}
                >
                    <Favorite sx={{ color: '#ffffff', fontSize: 26 }} />
                </Box>
                <Box>
                    <Typography variant="h4" component="h1" sx={{ fontWeight: 800, color: '#f8fafc' }}>
                        My Wishlist
                    </Typography>
                    <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                        Saved product variants for future comparison and purchase
                    </Typography>
                </Box>
            </Box>

            {/* Empty State */}
            {wishlistItems.length === 0 ? (
                <Paper
                    elevation={0}
                    sx={{
                        p: 6,
                        textAlign: 'center',
                        bgcolor: 'rgba(19, 27, 46, 0.6)',
                        backdropFilter: 'blur(12px)',
                        border: '1px dashed rgba(255, 255, 255, 0.15)',
                        borderRadius: 4,
                    }}
                >
                    <Favorite sx={{ fontSize: 56, color: 'rgba(236, 72, 153, 0.3)', mb: 2 }} />
                    <Typography variant="h6" sx={{ color: '#f8fafc', fontWeight: 700, mb: 1 }}>
                        No items in your wishlist yet.
                    </Typography>
                    <Typography variant="body2" sx={{ color: '#94a3b8', mb: 3, maxWidth: 450, mx: 'auto' }}>
                        Browse products and save your favorite variants here for quick access.
                    </Typography>
                    <Button
                        variant="contained"
                        startIcon={<Search />}
                        onClick={() => navigate('/search')}
                        sx={{
                            background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                            fontWeight: 600,
                            textTransform: 'none',
                        }}
                    >
                        Explore Products
                    </Button>
                </Paper>
            ) : (
                <Grid container spacing={3}>
                    {wishlistItems.map((item) => {
                        const formattedDate = formatAddedDate(item.addedAt);

                        return (
                            <Grid item xs={12} sm={6} md={4} key={item.id || item.productVariantId}>
                                <Paper
                                    elevation={0}
                                    sx={{
                                        p: 3,
                                        height: '100%',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        justifyContent: 'space-between',
                                        bgcolor: 'rgba(19, 27, 46, 0.75)',
                                        backdropFilter: 'blur(12px)',
                                        border: '1px solid rgba(255, 255, 255, 0.08)',
                                        borderRadius: 3,
                                        transition: 'all 0.2s ease',
                                        '&:hover': {
                                            borderColor: 'rgba(236, 72, 153, 0.4)',
                                            boxShadow: '0 8px 24px rgba(236, 72, 153, 0.1)',
                                        },
                                    }}
                                >
                                    <Box>
                                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                                            <Typography
                                                variant="h6"
                                                sx={{
                                                    fontWeight: 700,
                                                    color: '#f8fafc',
                                                    lineHeight: 1.3,
                                                    fontSize: '1.1rem',
                                                }}
                                            >
                                                {item.productName || 'Product Variant'}
                                            </Typography>

                                            <Tooltip title="Remove from wishlist">
                                                <IconButton
                                                    onClick={() => handleRemove(item.productVariantId)}
                                                    size="small"
                                                    aria-label={`Remove ${item.productName || 'item'} from wishlist`}
                                                    sx={{
                                                        color: '#94a3b8',
                                                        '&:hover': { color: '#ef4444', bgcolor: 'rgba(239, 68, 68, 0.1)' },
                                                    }}
                                                >
                                                    <Delete fontSize="small" />
                                                </IconButton>
                                            </Tooltip>
                                        </Box>

                                        {item.variantName && (
                                            <Typography
                                                variant="body2"
                                                sx={{
                                                    color: '#22d3ee',
                                                    fontWeight: 600,
                                                    mb: 2,
                                                }}
                                            >
                                                {item.variantName}
                                            </Typography>
                                        )}

                                        {formattedDate && (
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75, color: '#64748b', mb: 2 }}>
                                                <AccessTime sx={{ fontSize: '0.85rem' }} />
                                                <Typography variant="caption">
                                                    Added on {formattedDate}
                                                </Typography>
                                            </Box>
                                        )}
                                    </Box>

                                    <Box sx={{ pt: 2, borderTop: '1px dashed rgba(255, 255, 255, 0.08)' }}>
                                        <Button
                                            variant="outlined"
                                            fullWidth
                                            startIcon={<Visibility fontSize="small" />}
                                            onClick={() => handleNavigateProduct(item)}
                                            aria-label={`View product details for ${item.productName || 'item'}`}
                                            sx={{
                                                borderColor: 'rgba(99, 102, 241, 0.4)',
                                                color: '#818cf8',
                                                textTransform: 'none',
                                                fontWeight: 600,
                                                '&:hover': {
                                                    borderColor: '#6366f1',
                                                    bgcolor: 'rgba(99, 102, 241, 0.1)',
                                                },
                                            }}
                                        >
                                            View Product
                                        </Button>
                                    </Box>
                                </Paper>
                            </Grid>
                        );
                    })}
                </Grid>
            )}

            {/* Notification Snackbar */}
            <Snackbar
                open={snackbar.open}
                autoHideDuration={4000}
                onClose={handleCloseSnackbar}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            >
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.message}
                </Alert>
            </Snackbar>
        </Container>
    );
};

export default WishlistPage;
