import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
    Container,
    Box,
    Grid,
    Typography,
    Paper,
    Chip,
    Button,
    CardMedia,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableRow,
    TableHead,
    Divider,
    Alert,
    CircularProgress,
} from '@mui/material';
import {
    ArrowBack,
    ImageNotSupported,
    CheckCircle,
    Layers,
    ListAlt,
} from '@mui/icons-material';
import { getProductById, getProductVariants } from '../api/catalog';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';

const ProductDetailPage = () => {
    const { productId } = useParams();
    const navigate = useNavigate();

    // Product state
    const [product, setProduct] = useState(null);
    const [productLoading, setProductLoading] = useState(true);
    const [productError, setProductError] = useState(null);

    // Variants state
    const [variants, setVariants] = useState([]);
    const [selectedVariant, setSelectedVariant] = useState(null);
    const [variantLoading, setVariantLoading] = useState(true);
    const [variantError, setVariantError] = useState(null);

    const [imageError, setImageError] = useState(false);

    // Fetch product details and variants
    const loadProductData = useCallback(async () => {
        if (!productId) return;

        setProductLoading(true);
        setProductError(null);
        setVariantLoading(true);
        setVariantError(null);

        // Fetch Product Details
        try {
            const productData = await getProductById(productId);
            setProduct(productData);
        } catch (err) {
            console.error('Failed to fetch product detail:', err);
            setProductError(err?.response?.data?.message || 'Failed to load product details.');
        } finally {
            setProductLoading(false);
        }

        // Fetch Product Variants
        try {
            const variantListData = await getProductVariants(productId);
            const variantList = variantListData?.variants || [];
            setVariants(variantList);

            // Select default variant (isDefault === true) or first variant
            const defaultVar = variantList.find((v) => v.isDefault) || variantList[0] || null;
            setSelectedVariant(defaultVar);
        } catch (err) {
            console.error('Failed to fetch product variants:', err);
            setVariantError('Product variants could not be loaded.');
        } finally {
            setVariantLoading(false);
        }
    }, [productId]);

    useEffect(() => {
        loadProductData();
    }, [loadProductData]);

    // Handle variant selection
    const handleVariantSelect = (variant) => {
        setSelectedVariant(variant);
    };

    if (productLoading) {
        return (
            <Container maxWidth="xl" sx={{ py: 6 }}>
                <LoadingState message="Loading product details..." minHeight="450px" />
            </Container>
        );
    }

    if (productError) {
        return (
            <Container maxWidth="xl" sx={{ py: 6 }}>
                <Button
                    startIcon={<ArrowBack />}
                    onClick={() => navigate(-1)}
                    sx={{ mb: 3, color: '#94a3b8' }}
                >
                    Back
                </Button>
                <ErrorState message={productError} onRetry={loadProductData} />
            </Container>
        );
    }

    if (!product) {
        return null;
    }

    const { brand, series, model, category, description, imageUrl, specifications } = product;
    const titleText = [brand, series, model].filter(Boolean).join(' ');

    return (
        <Container maxWidth="xl" sx={{ py: { xs: 4, md: 6 } }}>
            {/* Back Button */}
            <Button
                startIcon={<ArrowBack />}
                onClick={() => navigate(-1)}
                sx={{
                    mb: 4,
                    color: '#94a3b8',
                    textTransform: 'none',
                    fontWeight: 600,
                    '&:hover': { color: '#f8fafc', bgcolor: 'rgba(255, 255, 255, 0.05)' },
                }}
            >
                Back to Search
            </Button>

            {/* Main Product Overview Grid */}
            <Grid container spacing={4} sx={{ mb: 6 }}>
                {/* Left Column: Product Image */}
                <Grid item xs={12} md={5}>
                    <Paper
                        elevation={0}
                        sx={{
                            p: 2,
                            bgcolor: 'rgba(19, 27, 46, 0.8)',
                            backdropFilter: 'blur(12px)',
                            border: '1px solid rgba(255, 255, 255, 0.08)',
                            borderRadius: 4,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            minHeight: { xs: 300, md: 420 },
                        }}
                    >
                        {imageUrl && !imageError ? (
                            <CardMedia
                                component="img"
                                image={imageUrl}
                                alt={titleText || 'Product image'}
                                onError={() => setImageError(true)}
                                sx={{
                                    maxHeight: 400,
                                    maxWidth: '100%',
                                    objectFit: 'contain',
                                    borderRadius: 2,
                                }}
                            />
                        ) : (
                            <Box
                                sx={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    color: '#64748b',
                                    gap: 1.5,
                                    py: 8,
                                }}
                            >
                                <ImageNotSupported sx={{ fontSize: 64 }} />
                                <Typography variant="body2" sx={{ color: '#64748b' }}>
                                    No Product Image Available
                                </Typography>
                            </Box>
                        )}
                    </Paper>
                </Grid>

                {/* Right Column: Product Metadata & Variant Selector */}
                <Grid item xs={12} md={7}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                        {/* Category Chip */}
                        {category?.name && (
                            <Box sx={{ mb: 1.5 }}>
                                <Chip
                                    label={category.name}
                                    size="small"
                                    sx={{
                                        bgcolor: 'rgba(99, 102, 241, 0.15)',
                                        color: '#818cf8',
                                        border: '1px solid rgba(99, 102, 241, 0.3)',
                                        fontWeight: 600,
                                        fontSize: '0.8rem',
                                    }}
                                />
                            </Box>
                        )}

                        {/* Brand */}
                        {brand && (
                            <Typography
                                variant="subtitle2"
                                sx={{
                                    color: '#94a3b8',
                                    textTransform: 'uppercase',
                                    letterSpacing: 1.5,
                                    fontWeight: 700,
                                    mb: 0.5,
                                }}
                            >
                                {brand}
                            </Typography>
                        )}

                        {/* Title */}
                        <Typography
                            variant="h3"
                            component="h1"
                            sx={{
                                fontWeight: 800,
                                color: '#f8fafc',
                                mb: 2,
                                fontSize: { xs: '1.8rem', sm: '2.4rem', md: '2.8rem' },
                                lineHeight: 1.2,
                            }}
                        >
                            {titleText || 'Product Details'}
                        </Typography>

                        {/* Description */}
                        {description && (
                            <Typography
                                variant="body1"
                                sx={{
                                    color: '#cbd5e1',
                                    mb: 4,
                                    lineHeight: 1.6,
                                    fontSize: '1.05rem',
                                }}
                            >
                                {description}
                            </Typography>
                        )}

                        <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.08)', mb: 4 }} />

                        {/* Variant Selection Section */}
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                bgcolor: 'rgba(19, 27, 46, 0.5)',
                                border: '1px solid rgba(255, 255, 255, 0.08)',
                                borderRadius: 3,
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                                <Layers sx={{ color: '#06b6d4', fontSize: '1.4rem' }} />
                                <Typography variant="h6" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                    Select Variant
                                </Typography>
                            </Box>

                            {/* Variant Loading */}
                            {variantLoading && (
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 2 }}>
                                    <CircularProgress size={20} sx={{ color: '#06b6d4' }} />
                                    <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                                        Loading product variants...
                                    </Typography>
                                </Box>
                            )}

                            {/* Variant Error */}
                            {variantError && !variantLoading && (
                                <Alert severity="warning" variant="outlined" sx={{ color: '#f8fafc', borderColor: 'rgba(234, 179, 8, 0.3)' }}>
                                    {variantError}
                                </Alert>
                            )}

                            {/* Variant Options */}
                            {!variantLoading && !variantError && (
                                <>
                                    {variants.length > 0 ? (
                                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.5, mb: 2 }}>
                                            {variants.map((v) => {
                                                const isSelected = selectedVariant?.id === v.id;
                                                return (
                                                    <Button
                                                        key={v.id}
                                                        variant={isSelected ? 'contained' : 'outlined'}
                                                        onClick={() => handleVariantSelect(v)}
                                                        startIcon={isSelected ? <CheckCircle sx={{ fontSize: '1rem !important' }} /> : null}
                                                        sx={{
                                                            py: 1.2,
                                                            px: 2.5,
                                                            borderRadius: 2,
                                                            fontWeight: 600,
                                                            textTransform: 'none',
                                                            bgcolor: isSelected ? '#6366f1' : 'transparent',
                                                            color: isSelected ? '#ffffff' : '#cbd5e1',
                                                            borderColor: isSelected ? '#6366f1' : 'rgba(255, 255, 255, 0.15)',
                                                            '&:hover': {
                                                                bgcolor: isSelected ? '#4f46e5' : 'rgba(255, 255, 255, 0.05)',
                                                                borderColor: isSelected ? '#4f46e5' : 'rgba(99, 102, 241, 0.4)',
                                                            },
                                                        }}
                                                    >
                                                        {v.name || 'Standard Variant'}
                                                    </Button>
                                                );
                                            })}
                                        </Box>
                                    ) : (
                                        <Chip
                                            label="Standard Variant"
                                            sx={{ bgcolor: 'rgba(255, 255, 255, 0.08)', color: '#cbd5e1', fontWeight: 600 }}
                                        />
                                    )}

                                    {/* Display Selected Variant Attributes */}
                                    {selectedVariant && selectedVariant.attributes && selectedVariant.attributes.length > 0 && (
                                        <Box sx={{ mt: 2.5, pt: 2, borderTop: '1px dashed rgba(255, 255, 255, 0.1)' }}>
                                            <Typography variant="caption" sx={{ color: '#94a3b8', fontWeight: 700, uppercase: true, display: 'block', mb: 1 }}>
                                                Selected Configuration Attributes
                                            </Typography>
                                            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                                {selectedVariant.attributes.map((attr, idx) => (
                                                    <Chip
                                                        key={idx}
                                                        label={`${attr.name}: ${attr.value}`}
                                                        size="small"
                                                        variant="outlined"
                                                        sx={{
                                                            borderColor: 'rgba(6, 182, 212, 0.3)',
                                                            color: '#22d3ee',
                                                            bgcolor: 'rgba(6, 182, 212, 0.08)',
                                                            fontWeight: 500,
                                                        }}
                                                    />
                                                ))}
                                            </Box>
                                        </Box>
                                    )}
                                </>
                            )}
                        </Paper>
                    </Box>
                </Grid>
            </Grid>

            {/* Product Specifications Section */}
            <Box sx={{ mt: 6 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 3 }}>
                    <ListAlt sx={{ color: '#6366f1', fontSize: '1.6rem' }} />
                    <Typography variant="h5" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                        Product Specifications
                    </Typography>
                </Box>

                {specifications && specifications.length > 0 ? (
                    <TableContainer
                        component={Paper}
                        elevation={0}
                        sx={{
                            bgcolor: 'rgba(19, 27, 46, 0.8)',
                            backdropFilter: 'blur(12px)',
                            border: '1px solid rgba(255, 255, 255, 0.08)',
                            borderRadius: 3,
                            overflow: 'hidden',
                        }}
                    >
                        <Table aria-label="product specifications table">
                            <TableHead>
                                <TableRow sx={{ bgcolor: 'rgba(99, 102, 241, 0.1)' }}>
                                    <TableCell sx={{ color: '#818cf8', fontWeight: 700, width: '35%', borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                        Specification
                                    </TableCell>
                                    <TableCell sx={{ color: '#818cf8', fontWeight: 700, borderBottom: '1px solid rgba(255, 255, 255, 0.08)' }}>
                                        Value
                                    </TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {specifications.map((spec, index) => (
                                    <TableRow
                                        key={index}
                                        sx={{
                                            '&:last-child td, &:last-child th': { border: 0 },
                                            '&:nth-of-type(odd)': { bgcolor: 'rgba(255, 255, 255, 0.02)' },
                                        }}
                                    >
                                        <TableCell component="th" scope="row" sx={{ color: '#f8fafc', fontWeight: 600, borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            {spec.name}
                                        </TableCell>
                                        <TableCell sx={{ color: '#cbd5e1', borderBottom: '1px solid rgba(255, 255, 255, 0.05)' }}>
                                            {spec.value}
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                ) : (
                    <Paper
                        elevation={0}
                        sx={{
                            p: 4,
                            textAlign: 'center',
                            bgcolor: 'rgba(19, 27, 46, 0.4)',
                            border: '1px dashed rgba(255, 255, 255, 0.15)',
                            borderRadius: 3,
                        }}
                    >
                        <Typography variant="body1" sx={{ color: '#94a3b8' }}>
                            No specifications available for this product.
                        </Typography>
                    </Paper>
                )}
            </Box>
        </Container>
    );
};

export default ProductDetailPage;
