import React, { useState, useEffect, useCallback } from 'react';
import {
    Box,
    Paper,
    Typography,
    Grid,
    Chip,
    Rating,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    Button,
    Link,
    Divider,
} from '@mui/material';
import {
    RateReview,
    VerifiedUser,
    OpenInNew,
    FilterList,
} from '@mui/icons-material';
import { getVariantReviews } from '../../api/catalog';
import LoadingState from '../common/LoadingState';
import ErrorState from '../common/ErrorState';

/**
 * Component for displaying customer reviews for a selected product variant with optional platform filtering.
 *
 * @param {number|string} variantId - Selected variant ID
 * @param {Array} platforms - List of available platform responses [{ id, name }]
 */
const ReviewList = ({ variantId, platforms = [] }) => {
    const [reviews, setReviews] = useState([]);
    const [totalReviews, setTotalReviews] = useState(0);
    const [selectedPlatformId, setSelectedPlatformId] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    // Fetch reviews for the active variant & selected platform
    const fetchReviews = useCallback(async () => {
        if (!variantId) return;

        setLoading(true);
        setError(null);

        try {
            const data = await getVariantReviews(variantId, {
                platformId: selectedPlatformId || undefined,
                limit: 15,
            });
            setReviews(data?.reviews || []);
            setTotalReviews(data?.totalReviews || (data?.reviews ? data.reviews.length : 0));
        } catch (err) {
            console.error('Failed to fetch variant reviews:', err);
            setError(err?.response?.data?.message || 'Failed to load customer reviews.');
        } finally {
            setLoading(false);
        }
    }, [variantId, selectedPlatformId]);

    useEffect(() => {
        fetchReviews();
    }, [fetchReviews]);

    // Handle platform filter change
    const handlePlatformChange = (event) => {
        setSelectedPlatformId(event.target.value);
    };

    if (!variantId) {
        return null;
    }

    // Deduplicate platforms list from props if provided
    const validPlatforms = Array.isArray(platforms)
        ? platforms.filter((p, index, self) => p?.id && self.findIndex((t) => t.id === p.id) === index)
        : [];

    return (
        <Box sx={{ mt: 6 }}>
            {/* Header and Filter Row */}
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    mb: 3,
                    flexWrap: 'wrap',
                    gap: 2,
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <RateReview sx={{ color: '#818cf8', fontSize: '1.6rem' }} />
                    <Typography variant="h5" component="h2" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                        Customer Reviews
                    </Typography>
                    {totalReviews > 0 && (
                        <Chip
                            label={`${totalReviews} ${totalReviews === 1 ? 'Review' : 'Reviews'}`}
                            size="small"
                            sx={{
                                bgcolor: 'rgba(99, 102, 241, 0.15)',
                                color: '#818cf8',
                                border: '1px solid rgba(99, 102, 241, 0.3)',
                                fontWeight: 600,
                            }}
                        />
                    )}
                </Box>

                {/* Platform Filter Dropdown */}
                {validPlatforms.length > 0 && (
                    <FormControl size="small" sx={{ minWidth: 180 }}>
                        <InputLabel id="platform-filter-label" sx={{ color: '#94a3b8' }}>
                            Filter by Platform
                        </InputLabel>
                        <Select
                            labelId="platform-filter-label"
                            id="platform-filter-select"
                            value={selectedPlatformId}
                            label="Filter by Platform"
                            onChange={handlePlatformChange}
                            startAdornment={<FilterList sx={{ color: '#818cf8', mr: 1, fontSize: '1.1rem' }} />}
                            sx={{
                                color: '#f8fafc',
                                bgcolor: 'rgba(19, 27, 46, 0.6)',
                                '.MuiOutlinedInput-notchedOutline': { borderColor: 'rgba(255, 255, 255, 0.15)' },
                                '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: '#818cf8' },
                                '.MuiSvgIcon-root': { color: '#94a3b8' },
                            }}
                        >
                            <MenuItem value="">
                                <em>All Platforms</em>
                            </MenuItem>
                            {validPlatforms.map((p) => (
                                <MenuItem key={p.id} value={p.id}>
                                    {p.name}
                                </MenuItem>
                            ))}
                        </Select>
                    </FormControl>
                )}
            </Box>

            {/* Loading State */}
            {loading && <LoadingState message="Loading customer reviews..." minHeight="200px" />}

            {/* Error State */}
            {error && !loading && (
                <ErrorState message={error} onRetry={fetchReviews} />
            )}

            {/* Empty State */}
            {!loading && !error && reviews.length === 0 && (
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
                        No customer reviews available for this variant.
                    </Typography>
                </Paper>
            )}

            {/* Reviews List */}
            {!loading && !error && reviews.length > 0 && (
                <Grid container spacing={2.5}>
                    {reviews.map((review) => {
                        const numericRating = review.rating ? Number(review.rating) : 0;

                        return (
                            <Grid item xs={12} key={review.id}>
                                <Paper
                                    elevation={0}
                                    sx={{
                                        p: 3,
                                        bgcolor: 'rgba(19, 27, 46, 0.7)',
                                        backdropFilter: 'blur(8px)',
                                        border: '1px solid rgba(255, 255, 255, 0.08)',
                                        borderRadius: 3,
                                        transition: 'border-color 0.2s ease',
                                        '&:hover': {
                                            borderColor: 'rgba(99, 102, 241, 0.3)',
                                        },
                                    }}
                                >
                                    {/* Review Top Header */}
                                    <Box
                                        sx={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            alignItems: 'flex-start',
                                            mb: 1.5,
                                            flexWrap: 'wrap',
                                            gap: 1,
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
                                            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                                {review.reviewerName || 'Anonymous Reviewer'}
                                            </Typography>

                                            {review.verifiedPurchase && (
                                                <Chip
                                                    icon={<VerifiedUser sx={{ fontSize: '0.85rem !important', color: '#34d399 !important' }} />}
                                                    label="Verified Purchase"
                                                    size="small"
                                                    sx={{
                                                        bgcolor: 'rgba(16, 185, 129, 0.12)',
                                                        color: '#34d399',
                                                        border: '1px solid rgba(16, 185, 129, 0.25)',
                                                        fontSize: '0.75rem',
                                                        fontWeight: 600,
                                                    }}
                                                />
                                            )}
                                        </Box>

                                        {review.reviewDate && (
                                            <Typography variant="caption" sx={{ color: '#64748b' }}>
                                                {String(review.reviewDate)}
                                            </Typography>
                                        )}
                                    </Box>

                                    {/* Rating Display */}
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                                        <Rating
                                            value={numericRating}
                                            precision={0.5}
                                            readOnly
                                            size="small"
                                            aria-label={`${numericRating} out of 5 stars`}
                                            sx={{
                                                '& .MuiRating-iconFilled': { color: '#fbbf24' },
                                                '& .MuiRating-iconEmpty': { color: 'rgba(255, 255, 255, 0.2)' },
                                            }}
                                        />
                                        <Typography variant="caption" sx={{ fontWeight: 700, color: '#fbbf24' }}>
                                            {numericRating.toFixed(1)} / 5
                                        </Typography>
                                    </Box>

                                    {/* Review Title */}
                                    {review.reviewTitle && (
                                        <Typography
                                            variant="subtitle2"
                                            sx={{ fontWeight: 700, color: '#f8fafc', mb: 1, fontSize: '0.95rem' }}
                                        >
                                            {review.reviewTitle}
                                        </Typography>
                                    )}

                                    {/* Review Text */}
                                    {review.reviewText && (
                                        <Typography
                                            variant="body2"
                                            sx={{
                                                color: '#cbd5e1',
                                                lineHeight: 1.6,
                                                whiteSpace: 'pre-line',
                                                maxHeight: 300,
                                                overflowY: 'auto',
                                                pr: 1,
                                            }}
                                        >
                                            {review.reviewText}
                                        </Typography>
                                    )}

                                    {/* Source URL External Link */}
                                    {review.sourceUrl && (
                                        <Box sx={{ mt: 2, pt: 1.5, borderTop: '1px dashed rgba(255, 255, 255, 0.08)' }}>
                                            <Button
                                                component={Link}
                                                href={review.sourceUrl}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                size="small"
                                                endIcon={<OpenInNew sx={{ fontSize: '0.85rem !important' }} />}
                                                sx={{
                                                    color: '#818cf8',
                                                    textTransform: 'none',
                                                    fontWeight: 600,
                                                    fontSize: '0.8rem',
                                                    p: 0,
                                                    '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' },
                                                }}
                                            >
                                                View Original Review
                                            </Button>
                                        </Box>
                                    )}
                                </Paper>
                            </Grid>
                        );
                    })}
                </Grid>
            )}
        </Box>
    );
};

export default ReviewList;
