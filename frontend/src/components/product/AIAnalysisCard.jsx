import React from 'react';
import {
    Box,
    Paper,
    Typography,
    Grid,
    Chip,
    Divider,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
} from '@mui/material';
import {
    AutoAwesome,
    ThumbUp,
    ThumbDown,
    MonetizationOn,
    Analytics,
    LocalOffer,
    TipsAndUpdates,
    CheckCircle,
    Cancel,
    AccessTime,
} from '@mui/icons-material';

/**
 * Presentation component displaying AI-synthesized product variant insights.
 * Strictly renders data matching AIAnalysisResponse DTO fields:
 * - summary
 * - strengths
 * - drawbacks
 * - valueAssessment
 * - reviewInsights
 * - bestOfferRecommendation
 * - buyingGuidance
 * - generatedAt
 *
 * @param {object} aiAnalysis - AIAnalysisResponse object
 */
const AIAnalysisCard = ({ aiAnalysis }) => {
    if (!aiAnalysis) {
        return null;
    }

    const {
        variantName,
        productName,
        summary,
        strengths = [],
        drawbacks = [],
        valueAssessment,
        reviewInsights,
        bestOfferRecommendation,
        buyingGuidance,
        generatedAt,
    } = aiAnalysis;

    // Helper for formatting timestamp
    const formatTimestamp = (ts) => {
        if (!ts) return null;
        try {
            const date = new Date(ts);
            return isNaN(date.getTime()) ? String(ts) : date.toLocaleString();
        } catch (e) {
            return String(ts);
        }
    };

    const formattedTime = formatTimestamp(generatedAt);

    return (
        <Paper
            elevation={0}
            sx={{
                mt: 6,
                p: { xs: 3, md: 4 },
                bgcolor: 'rgba(19, 27, 46, 0.85)',
                backdropFilter: 'blur(16px)',
                border: '1px solid rgba(129, 140, 248, 0.3)',
                borderRadius: 4,
                boxShadow: '0 8px 32px rgba(99, 102, 241, 0.15)',
            }}
        >
            {/* AI Banner Header */}
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    flexWrap: 'wrap',
                    gap: 1.5,
                    mb: 3,
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <Box
                        sx={{
                            width: 42,
                            height: 42,
                            borderRadius: '12px',
                            background: 'linear-gradient(135deg, #6366f1 0%, #06b6d4 100%)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            boxShadow: '0 4px 12px rgba(99, 102, 241, 0.4)',
                        }}
                    >
                        <AutoAwesome sx={{ color: '#ffffff', fontSize: '1.4rem' }} />
                    </Box>
                    <Box>
                        <Typography variant="h5" component="h2" sx={{ fontWeight: 800, color: '#f8fafc' }}>
                            AI Product Analysis
                        </Typography>
                        <Typography variant="caption" sx={{ color: '#818cf8', fontWeight: 600 }}>
                            Synthesized Intelligence & Comparison Insights
                        </Typography>
                    </Box>
                </Box>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                    {variantName && (
                        <Chip
                            label={`Variant: ${variantName}`}
                            size="small"
                            sx={{
                                bgcolor: 'rgba(6, 182, 212, 0.12)',
                                color: '#22d3ee',
                                border: '1px solid rgba(6, 182, 212, 0.3)',
                                fontWeight: 600,
                            }}
                        />
                    )}
                    {formattedTime && (
                        <Chip
                            icon={<AccessTime sx={{ fontSize: '0.85rem !important', color: '#94a3b8 !important' }} />}
                            label={`Generated: ${formattedTime}`}
                            size="small"
                            sx={{
                                bgcolor: 'rgba(255, 255, 255, 0.05)',
                                color: '#94a3b8',
                                border: '1px solid rgba(255, 255, 255, 0.1)',
                                fontSize: '0.75rem',
                            }}
                        />
                    )}
                </Box>
            </Box>

            <Divider sx={{ borderColor: 'rgba(255, 255, 255, 0.08)', mb: 4 }} />

            {/* Executive Summary */}
            {summary && (
                <Box sx={{ mb: 4 }}>
                    <Typography
                        variant="subtitle2"
                        sx={{ color: '#818cf8', fontWeight: 700, uppercase: true, letterSpacing: 1, mb: 1 }}
                    >
                        Executive Summary
                    </Typography>
                    <Typography variant="body1" sx={{ color: '#f8fafc', lineHeight: 1.7, fontSize: '1.05rem' }}>
                        {summary}
                    </Typography>
                </Box>
            )}

            {/* Strengths & Drawbacks Grid */}
            <Grid container spacing={3} sx={{ mb: 4 }}>
                {/* Key Strengths */}
                <Grid item xs={12} md={6}>
                    <Paper
                        elevation={0}
                        sx={{
                            p: 3,
                            height: '100%',
                            bgcolor: 'rgba(16, 185, 129, 0.05)',
                            border: '1px solid rgba(16, 185, 129, 0.2)',
                            borderRadius: 3,
                        }}
                    >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                            <ThumbUp sx={{ color: '#34d399', fontSize: '1.3rem' }} />
                            <Typography variant="h6" component="h3" sx={{ fontWeight: 700, color: '#34d399' }}>
                                Key Strengths
                            </Typography>
                        </Box>
                        {strengths && strengths.length > 0 ? (
                            <List disablePadding>
                                {strengths.map((strength, index) => (
                                    <ListItem key={index} disableGutters sx={{ py: 0.75 }}>
                                        <ListItemIcon sx={{ minWidth: 32 }}>
                                            <CheckCircle sx={{ color: '#34d399', fontSize: '1.1rem' }} />
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={strength}
                                            primaryTypographyProps={{ variant: 'body2', sx: { color: '#f8fafc', lineHeight: 1.5 } }}
                                        />
                                    </ListItem>
                                ))}
                            </List>
                        ) : (
                            <Typography variant="body2" sx={{ color: '#94a3b8', fontStyle: 'italic' }}>
                                No AI strengths available.
                            </Typography>
                        )}
                    </Paper>
                </Grid>

                {/* Potential Drawbacks */}
                <Grid item xs={12} md={6}>
                    <Paper
                        elevation={0}
                        sx={{
                            p: 3,
                            height: '100%',
                            bgcolor: 'rgba(239, 68, 68, 0.05)',
                            border: '1px solid rgba(239, 68, 68, 0.2)',
                            borderRadius: 3,
                        }}
                    >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                            <ThumbDown sx={{ color: '#f87171', fontSize: '1.3rem' }} />
                            <Typography variant="h6" component="h3" sx={{ fontWeight: 700, color: '#f87171' }}>
                                Potential Drawbacks
                            </Typography>
                        </Box>
                        {drawbacks && drawbacks.length > 0 ? (
                            <List disablePadding>
                                {drawbacks.map((drawback, index) => (
                                    <ListItem key={index} disableGutters sx={{ py: 0.75 }}>
                                        <ListItemIcon sx={{ minWidth: 32 }}>
                                            <Cancel sx={{ color: '#f87171', fontSize: '1.1rem' }} />
                                        </ListItemIcon>
                                        <ListItemText
                                            primary={drawback}
                                            primaryTypographyProps={{ variant: 'body2', sx: { color: '#f8fafc', lineHeight: 1.5 } }}
                                        />
                                    </ListItem>
                                ))}
                            </List>
                        ) : (
                            <Typography variant="body2" sx={{ color: '#94a3b8', fontStyle: 'italic' }}>
                                No AI drawbacks available.
                            </Typography>
                        )}
                    </Paper>
                </Grid>
            </Grid>

            {/* Additional AI Insight Cards */}
            <Grid container spacing={3}>
                {/* Value Assessment */}
                {valueAssessment && (
                    <Grid item xs={12} md={6}>
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                height: '100%',
                                bgcolor: 'rgba(9, 13, 22, 0.5)',
                                border: '1px solid rgba(255, 255, 255, 0.08)',
                                borderRadius: 3,
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                                <MonetizationOn sx={{ color: '#fbbf24', fontSize: '1.3rem' }} />
                                <Typography variant="h6" component="h3" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                    Value Assessment
                                </Typography>
                            </Box>
                            <Typography variant="body2" sx={{ color: '#cbd5e1', lineHeight: 1.6 }}>
                                {valueAssessment}
                            </Typography>
                        </Paper>
                    </Grid>
                )}

                {/* Review Insights */}
                {reviewInsights && (
                    <Grid item xs={12} md={6}>
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                height: '100%',
                                bgcolor: 'rgba(9, 13, 22, 0.5)',
                                border: '1px solid rgba(255, 255, 255, 0.08)',
                                borderRadius: 3,
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyBetween: 'space-between', mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Analytics sx={{ color: '#06b6d4', fontSize: '1.3rem' }} />
                                    <Typography variant="h6" component="h3" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                        Review Insights
                                    </Typography>
                                </Box>
                                <Chip
                                    label="AI Review Interpretation"
                                    size="small"
                                    sx={{ bgcolor: 'rgba(6, 182, 212, 0.1)', color: '#22d3ee', fontSize: '0.7rem' }}
                                />
                            </Box>
                            <Typography variant="body2" sx={{ color: '#cbd5e1', lineHeight: 1.6 }}>
                                {reviewInsights}
                            </Typography>
                        </Paper>
                    </Grid>
                )}

                {/* Best Offer Recommendation */}
                {bestOfferRecommendation && (
                    <Grid item xs={12} md={6}>
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                height: '100%',
                                bgcolor: 'rgba(9, 13, 22, 0.5)',
                                border: '1px solid rgba(255, 255, 255, 0.08)',
                                borderRadius: 3,
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                                <LocalOffer sx={{ color: '#a855f7', fontSize: '1.3rem' }} />
                                <Typography variant="h6" component="h3" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                    Best Offer Recommendation
                                </Typography>
                            </Box>
                            <Typography variant="body2" sx={{ color: '#cbd5e1', lineHeight: 1.6 }}>
                                {bestOfferRecommendation}
                            </Typography>
                        </Paper>
                    </Grid>
                )}

                {/* Buying Guidance */}
                {buyingGuidance && (
                    <Grid item xs={12} md={6}>
                        <Paper
                            elevation={0}
                            sx={{
                                p: 3,
                                height: '100%',
                                bgcolor: 'rgba(9, 13, 22, 0.5)',
                                border: '1px solid rgba(255, 255, 255, 0.08)',
                                borderRadius: 3,
                            }}
                        >
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1.5 }}>
                                <TipsAndUpdates sx={{ color: '#38bdf8', fontSize: '1.3rem' }} />
                                <Typography variant="h6" component="h3" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                    Buying Guidance
                                </Typography>
                            </Box>
                            <Typography variant="body2" sx={{ color: '#cbd5e1', lineHeight: 1.6 }}>
                                {buyingGuidance}
                            </Typography>
                        </Paper>
                    </Grid>
                )}
            </Grid>
        </Paper>
    );
};

export default AIAnalysisCard;
