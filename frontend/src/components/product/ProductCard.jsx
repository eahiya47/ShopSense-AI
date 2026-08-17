import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Card,
    CardContent,
    CardMedia,
    Typography,
    Chip,
    Box,
    CardActionArea,
} from '@mui/material';
import { ImageNotSupported, Layers } from '@mui/icons-material';

/**
 * Reusable product summary card component.
 * Presentation-only component rendering fields strictly matching ProductSummaryResponse DTO:
 * - id
 * - brand
 * - series
 * - model
 * - category
 * - imageUrl
 * - hasVariants
 *
 * @param {object} product - ProductSummaryResponse object
 * @param {function} onClick - Optional click handler for parent-controlled action/navigation
 */
const ProductCard = ({ product, onClick }) => {
    const navigate = useNavigate();
    const [imageError, setImageError] = useState(false);

    if (!product) {
        return null;
    }

    const handleCardClick = onClick || (() => {
        if (product?.id) {
            navigate(`/products/${product.id}`);
        }
    });

    const { brand, series, model, category, imageUrl, hasVariants } = product;

    // Display title combining available brand, series, and model
    const titleText = [brand, series, model].filter(Boolean).join(' ');

    const cardContent = (
        <CardContent sx={{ p: 3, flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
            {/* Category & Variant Availability Badges */}
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5, flexWrap: 'wrap' }}>
                {category && (
                    <Chip
                        label={category}
                        size="small"
                        sx={{
                            bgcolor: 'rgba(99, 102, 241, 0.15)',
                            color: '#818cf8',
                            border: '1px solid rgba(99, 102, 241, 0.3)',
                            fontWeight: 600,
                            fontSize: '0.75rem',
                        }}
                    />
                )}
                {hasVariants && (
                    <Chip
                        icon={<Layers sx={{ fontSize: '0.85rem !important', color: '#06b6d4 !important' }} />}
                        label="Variants Available"
                        size="small"
                        sx={{
                            bgcolor: 'rgba(6, 182, 212, 0.12)',
                            color: '#22d3ee',
                            border: '1px solid rgba(6, 182, 212, 0.25)',
                            fontWeight: 500,
                            fontSize: '0.75rem',
                        }}
                    />
                )}
            </Box>

            {/* Brand Label */}
            {brand && (
                <Typography
                    variant="caption"
                    sx={{
                        color: '#94a3b8',
                        textTransform: 'uppercase',
                        letterSpacing: 1,
                        fontWeight: 700,
                        mb: 0.5,
                    }}
                >
                    {brand}
                </Typography>
            )}

            {/* Product Model / Name */}
            <Typography
                variant="h6"
                component="h3"
                sx={{
                    fontWeight: 700,
                    color: '#f8fafc',
                    lineHeight: 1.3,
                    mb: 1,
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden',
                }}
            >
                {titleText || 'Unnamed Product'}
            </Typography>
        </CardContent>
    );

    return (
        <Card
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'transform 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease',
                '&:hover': {
                    transform: 'translateY(-4px)',
                    borderColor: 'rgba(99, 102, 241, 0.5)',
                    boxShadow: '0 8px 24px rgba(99, 102, 241, 0.2)',
                },
            }}
        >
            <CardActionArea onClick={handleCardClick} sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column', alignItems: 'stretch' }}>
                {/* Product Image Section */}
                {imageUrl && !imageError ? (
                    <CardMedia
                        component="img"
                        height="200"
                        image={imageUrl}
                        alt={titleText || 'Product image'}
                        onError={() => setImageError(true)}
                        sx={{
                            objectFit: 'cover',
                            bgcolor: 'rgba(9, 13, 22, 0.5)',
                        }}
                    />
                ) : (
                    <Box
                        sx={{
                            height: 200,
                            bgcolor: 'rgba(9, 13, 22, 0.6)',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: '#64748b',
                            gap: 1,
                        }}
                    >
                        <ImageNotSupported sx={{ fontSize: 48 }} />
                        <Typography variant="caption" sx={{ color: '#64748b' }}>
                            No Image Available
                        </Typography>
                    </Box>
                )}

                {cardContent}
            </CardActionArea>
        </Card>
    );
};

export default ProductCard;
