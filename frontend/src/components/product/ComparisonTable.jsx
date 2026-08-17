import React from 'react';
import {
    Box,
    Paper,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Chip,
    Button,
    Grid,
    Link,
} from '@mui/material';
import {
    OpenInNew,
    CheckCircle,
    Info,
    Warning,
    Storefront,
    LocalShipping,
    Star,
} from '@mui/icons-material';

/**
 * Presentation component for rendering live marketplace comparison offers and connector statuses.
 *
 * @param {object} comparisonData - ProductComparisonResponse object
 */
const ComparisonTable = ({ comparisonData }) => {
    if (!comparisonData) {
        return null;
    }

    const { variant, offers = [], platformStatus = [] } = comparisonData;

    // Helper to format currency price
    const formatPrice = (price, currency = 'INR') => {
        if (price === null || price === undefined) return 'N/A';
        const symbol = currency === 'INR' || currency === '₹' ? '₹' : currency + ' ';
        return `${symbol}${Number(price).toLocaleString('en-IN')}`;
    };

    // Helper for connector status icon & color scheme
    const getStatusBadge = (status, message) => {
        switch (status) {
            case 'AVAILABLE':
                return (
                    <Chip
                        icon={<CheckCircle sx={{ fontSize: '0.9rem !important', color: '#34d399 !important' }} />}
                        label={`Available${message ? ` - ${message}` : ''}`}
                        size="small"
                        sx={{
                            bgcolor: 'rgba(16, 185, 129, 0.15)',
                            color: '#34d399',
                            border: '1px solid rgba(16, 185, 129, 0.3)',
                            fontWeight: 600,
                        }}
                    />
                );
            case 'NO_OFFER':
                return (
                    <Chip
                        icon={<Info sx={{ fontSize: '0.9rem !important', color: '#818cf8 !important' }} />}
                        label={`No Offer${message ? ` - ${message}` : ''}`}
                        size="small"
                        sx={{
                            bgcolor: 'rgba(99, 102, 241, 0.15)',
                            color: '#818cf8',
                            border: '1px solid rgba(99, 102, 241, 0.3)',
                            fontWeight: 500,
                        }}
                    />
                );
            case 'UNAVAILABLE':
            default:
                return (
                    <Chip
                        icon={<Warning sx={{ fontSize: '0.9rem !important', color: '#fbbf24 !important' }} />}
                        label={`Unavailable${message ? ` - ${message}` : ''}`}
                        size="small"
                        sx={{
                            bgcolor: 'rgba(245, 158, 11, 0.15)',
                            color: '#fbbf24',
                            border: '1px solid rgba(245, 158, 11, 0.3)',
                            fontWeight: 500,
                        }}
                    />
                );
        }
    };

    return (
        <Box sx={{ mt: 5 }}>
            {/* Section Heading */}
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    mb: 3,
                    flexWrap: 'wrap',
                    gap: 1.5,
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Storefront sx={{ color: '#06b6d4', fontSize: '1.6rem' }} />
                    <Typography variant="h5" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                        Marketplace Comparison
                    </Typography>
                </Box>
                {variant?.name && (
                    <Chip
                        label={`Variant: ${variant.name}`}
                        size="small"
                        sx={{
                            bgcolor: 'rgba(6, 182, 212, 0.12)',
                            color: '#22d3ee',
                            border: '1px solid rgba(6, 182, 212, 0.25)',
                            fontWeight: 600,
                        }}
                    />
                )}
            </Box>

            {/* Marketplace Offers Section */}
            {offers.length > 0 ? (
                <TableContainer
                    component={Paper}
                    elevation={0}
                    sx={{
                        bgcolor: 'rgba(19, 27, 46, 0.8)',
                        backdropFilter: 'blur(12px)',
                        border: '1px solid rgba(255, 255, 255, 0.08)',
                        borderRadius: 3,
                        mb: 4,
                        overflowX: 'auto',
                    }}
                >
                    <Table aria-label="marketplace comparison table" sx={{ minWidth: 650 }}>
                        <TableHead>
                            <TableRow sx={{ bgcolor: 'rgba(99, 102, 241, 0.1)' }}>
                                <TableCell sx={{ color: '#818cf8', fontWeight: 700, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                    Platform
                                </TableCell>
                                <TableCell sx={{ color: '#818cf8', fontWeight: 700, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                    Price
                                </TableCell>
                                <TableCell sx={{ color: '#818cf8', fontWeight: 700, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                    Seller & Rating
                                </TableCell>
                                <TableCell sx={{ color: '#818cf8', fontWeight: 700, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                    Availability & Delivery
                                </TableCell>
                                <TableCell align="right" sx={{ color: '#818cf8', fontWeight: 700, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                    Marketplace Link
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {offers.map((offer, index) => {
                                const platformName = offer.platform?.name || 'Platform';
                                const hasOriginalPrice = offer.originalPrice && offer.originalPrice > offer.currentPrice;

                                return (
                                    <TableRow
                                        key={offer.platform?.id || index}
                                        sx={{
                                            '&:last-child td, &:last-child th': { border: 0 },
                                            '&:nth-of-type(odd)': { bgcolor: 'rgba(255, 255, 255, 0.02)' },
                                        }}
                                    >
                                        {/* Platform Name */}
                                        <TableCell component="th" scope="row" sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                                {platformName}
                                            </Typography>
                                        </TableCell>

                                        {/* Current & Original Price */}
                                        <TableCell sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
                                                <Typography variant="h6" sx={{ fontWeight: 800, color: '#34d399' }}>
                                                    {formatPrice(offer.currentPrice, offer.currency)}
                                                </Typography>
                                                {hasOriginalPrice && (
                                                    <Typography
                                                        variant="body2"
                                                        sx={{ textDecoration: 'line-through', color: '#64748b', fontSize: '0.85rem' }}
                                                    >
                                                        {formatPrice(offer.originalPrice, offer.currency)}
                                                    </Typography>
                                                )}
                                            </Box>
                                        </TableCell>

                                        {/* Seller & Rating */}
                                        <TableCell sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            {offer.sellerName ? (
                                                <Box>
                                                    <Typography variant="body2" sx={{ color: '#f8fafc', fontWeight: 600 }}>
                                                        {offer.sellerName}
                                                    </Typography>
                                                    {offer.sellerRating && (
                                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.3 }}>
                                                            <Star sx={{ fontSize: '0.9rem', color: '#fbbf24' }} />
                                                            <Typography variant="caption" sx={{ color: '#cbd5e1', fontWeight: 600 }}>
                                                                {offer.sellerRating}
                                                            </Typography>
                                                        </Box>
                                                    )}
                                                </Box>
                                            ) : (
                                                <Typography variant="body2" sx={{ color: '#64748b' }}>
                                                    Standard Seller
                                                </Typography>
                                            )}
                                        </TableCell>

                                        {/* Availability & Delivery Info */}
                                        <TableCell sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                                                {offer.availabilityStatus && (
                                                    <Chip
                                                        label={offer.availabilityStatus.replace('_', ' ')}
                                                        size="small"
                                                        sx={{
                                                            width: 'fit-content',
                                                            height: '20px',
                                                            fontSize: '0.7rem',
                                                            fontWeight: 700,
                                                            bgcolor:
                                                                offer.availabilityStatus === 'IN_STOCK'
                                                                    ? 'rgba(16, 185, 129, 0.15)'
                                                                    : 'rgba(239, 68, 68, 0.15)',
                                                            color:
                                                                offer.availabilityStatus === 'IN_STOCK' ? '#34d399' : '#f87171',
                                                        }}
                                                    />
                                                )}
                                                {offer.deliveryInfo && (
                                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                                        <LocalShipping sx={{ fontSize: '0.85rem', color: '#94a3b8' }} />
                                                        <Typography variant="caption" sx={{ color: '#cbd5e1' }}>
                                                            {offer.deliveryInfo}
                                                        </Typography>
                                                    </Box>
                                                )}
                                            </Box>
                                        </TableCell>

                                        {/* Marketplace Product URL Action Button */}
                                        <TableCell align="right" sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            {offer.productUrl ? (
                                                <Button
                                                    component={Link}
                                                    href={offer.productUrl}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    variant="contained"
                                                    size="small"
                                                    endIcon={<OpenInNew sx={{ fontSize: '0.9rem !important' }} />}
                                                    sx={{
                                                        bgcolor: '#6366f1',
                                                        color: '#ffffff',
                                                        fontWeight: 600,
                                                        textTransform: 'none',
                                                        borderRadius: 2,
                                                        px: 2,
                                                        '&:hover': { bgcolor: '#4f46e5' },
                                                    }}
                                                >
                                                    View on Store
                                                </Button>
                                            ) : (
                                                <Typography variant="caption" sx={{ color: '#64748b' }}>
                                                    No Link
                                                </Typography>
                                            )}
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </TableContainer>
            ) : (
                /* No Offers State */
                <Paper
                    elevation={0}
                    sx={{
                        p: 4,
                        textAlign: 'center',
                        bgcolor: 'rgba(19, 27, 46, 0.4)',
                        border: '1px dashed rgba(255, 255, 255, 0.15)',
                        borderRadius: 3,
                        mb: 4,
                    }}
                >
                    <Typography variant="body1" sx={{ color: '#cbd5e1', fontWeight: 600 }}>
                        No marketplace offers are currently available for this variant.
                    </Typography>
                </Paper>
            )}

            {/* Platform Connector Status Section */}
            {platformStatus.length > 0 && (
                <Paper
                    elevation={0}
                    sx={{
                        p: 3,
                        bgcolor: 'rgba(19, 27, 46, 0.5)',
                        border: '1px solid rgba(255, 255, 255, 0.08)',
                        borderRadius: 3,
                    }}
                >
                    <Typography variant="subtitle2" sx={{ color: '#94a3b8', fontWeight: 700, mb: 2, uppercase: true }}>
                        Platform Connector Status
                    </Typography>
                    <Grid container spacing={2}>
                        {platformStatus.map((item, idx) => (
                            <Grid item xs={12} sm={6} md={3} key={idx}>
                                <Box
                                    sx={{
                                        p: 2,
                                        borderRadius: 2,
                                        bgcolor: 'rgba(9, 13, 22, 0.5)',
                                        border: '1px solid rgba(255, 255, 255, 0.05)',
                                        display: 'flex',
                                        flexDirection: 'column',
                                        gap: 1,
                                    }}
                                >
                                    <Typography variant="body2" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                        {item.platform}
                                    </Typography>
                                    {getStatusBadge(item.status, item.message)}
                                </Box>
                            </Grid>
                        ))}
                    </Grid>
                </Paper>
            )}
        </Box>
    );
};

export default ComparisonTable;
