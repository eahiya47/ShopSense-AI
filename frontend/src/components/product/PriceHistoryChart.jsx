import React, { useState, useMemo } from 'react';
import {
    Box,
    Paper,
    Typography,
    Grid,
    Chip,
    Button,
    Divider,
    Alert,
} from '@mui/material';
import {
    TrendingUp,
    TrendingDown,
    Timeline,
    ArrowDownward,
    ArrowUpward,
    Storefront,
    InfoOutlined,
    CheckCircle,
} from '@mui/icons-material';
import {
    ResponsiveContainer,
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip as RechartsTooltip,
    Legend,
    CartesianGrid,
} from 'recharts';
import LoadingState from '../common/LoadingState';
import ErrorState from '../common/ErrorState';

// Platform Color Palette Mapping
const PLATFORM_COLORS = {
    Amazon: '#f59e0b', // Amber / Orange
    Flipkart: '#3b82f6', // Electric Blue
    Croma: '#06b6d4', // Cyan / Teal
    'Reliance Digital': '#ef4444', // Red / Crimson
};

const FALLBACK_COLORS = ['#8b5cf6', '#ec4899', '#10b981', '#6366f1', '#14b8a6', '#f43f5e'];

const getPlatformColor = (platformName, index) => {
    if (!platformName) return FALLBACK_COLORS[index % FALLBACK_COLORS.length];
    const key = Object.keys(PLATFORM_COLORS).find(
        (k) => k.toLowerCase() === platformName.trim().toLowerCase()
    );
    return key ? PLATFORM_COLORS[key] : FALLBACK_COLORS[index % FALLBACK_COLORS.length];
};

// Currency price formatter helper
const formatCurrency = (val, currency = 'INR') => {
    if (val === null || val === undefined || isNaN(val)) return 'N/A';
    const symbol = currency === 'INR' || currency === '₹' ? '₹' : currency + ' ';
    return `${symbol}${Number(val).toLocaleString('en-IN')}`;
};

// Date formatter helper
const formatDate = (dateStr) => {
    if (!dateStr) return '';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
        });
    } catch {
        return dateStr;
    }
};

// Custom Glassmorphic Recharts Tooltip
const CustomTooltip = ({ active, payload, label, currency = 'INR' }) => {
    if (active && payload && payload.length) {
        // Find minimum price in hover payload to highlight best offer
        const validPayloads = payload.filter((p) => p.value !== null && p.value !== undefined);
        const minPriceInHover = validPayloads.length > 0
            ? Math.min(...validPayloads.map((p) => p.value))
            : null;

        return (
            <Paper
                elevation={6}
                sx={{
                    p: 2,
                    bgcolor: 'rgba(15, 23, 42, 0.95)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.15)',
                    borderRadius: 3,
                    boxShadow: '0 8px 32px rgba(0, 0, 0, 0.5)',
                }}
            >
                <Typography variant="caption" sx={{ color: '#94a3b8', fontWeight: 600, display: 'block', mb: 1 }}>
                    {label}
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.8 }}>
                    {payload.map((entry, idx) => {
                        const isBestPrice = minPriceInHover !== null && entry.value === minPriceInHover && validPayloads.length > 1;
                        return (
                            <Box
                                key={idx}
                                sx={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'space-between',
                                    gap: 2,
                                }}
                            >
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                    <Box
                                        sx={{
                                            width: 10,
                                            height: 10,
                                            borderRadius: '50%',
                                            bgcolor: entry.color,
                                        }}
                                    />
                                    <Typography variant="body2" sx={{ color: '#f8fafc', fontWeight: 600 }}>
                                        {entry.name}:
                                    </Typography>
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                    <Typography
                                        variant="body2"
                                        sx={{
                                            fontWeight: 700,
                                            color: isBestPrice ? '#34d399' : '#e2e8f0',
                                        }}
                                    >
                                        {formatCurrency(entry.value, currency)}
                                    </Typography>
                                    {isBestPrice && (
                                        <Chip
                                            label="Best"
                                            size="small"
                                            sx={{
                                                height: 16,
                                                fontSize: '0.65rem',
                                                bgcolor: 'rgba(16, 185, 129, 0.2)',
                                                color: '#34d399',
                                                fontWeight: 700,
                                                px: 0.5,
                                            }}
                                        />
                                    )}
                                </Box>
                            </Box>
                        );
                    })}
                </Box>
            </Paper>
        );
    }
    return null;
};

const PriceHistoryChart = ({ priceHistoryData, loading, error, onRetry }) => {
    const [hiddenPlatforms, setHiddenPlatforms] = useState(new Set());

    const history = priceHistoryData?.history || [];
    const variantName = priceHistoryData?.variantName || '';

    // Calculate Summary Insights
    const stats = useMemo(() => {
        if (!history || history.length === 0) return null;

        const validEntries = history.filter((h) => h.price !== null && h.price !== undefined);
        if (validEntries.length === 0) return null;

        // Sort chronologically
        const sorted = [...validEntries].sort((a, b) => new Date(a.recordedAt) - new Date(b.recordedAt));

        let lowest = sorted[0];
        let highest = sorted[0];
        let maxDrop = null; // { platform, dropAmount, oldPrice, newPrice }

        // Track per-platform previous prices to find largest observed price drop
        const lastSeenPriceByPlatform = {};

        sorted.forEach((item) => {
            const numPrice = Number(item.price);
            if (numPrice < Number(lowest.price)) lowest = item;
            if (numPrice > Number(highest.price)) highest = item;

            const plat = item.platformName || 'Marketplace';
            if (lastSeenPriceByPlatform[plat] !== undefined) {
                const oldP = lastSeenPriceByPlatform[plat];
                if (oldP > numPrice) {
                    const drop = oldP - numPrice;
                    if (!maxDrop || drop > maxDrop.dropAmount) {
                        maxDrop = {
                            platform: plat,
                            dropAmount: drop,
                            oldPrice: oldP,
                            newPrice: numPrice,
                        };
                    }
                }
            }
            lastSeenPriceByPlatform[plat] = numPrice;
        });

        const latestEntry = sorted[sorted.length - 1];

        return {
            lowestPrice: lowest.price,
            lowestPlatform: lowest.platformName,
            lowestDate: lowest.recordedAt,
            highestPrice: highest.price,
            highestPlatform: highest.platformName,
            latestPrice: latestEntry.price,
            currency: latestEntry.currency || 'INR',
            maxDrop,
            totalSnapshots: sorted.length,
        };
    }, [history]);

    // Transform raw API history into pivot data for Recharts
    const { chartData, platforms } = useMemo(() => {
        if (!history || history.length === 0) return { chartData: [], platforms: [] };

        // Identify unique platform names
        const platformSet = new Set();
        history.forEach((h) => {
            if (h.platformName) platformSet.add(h.platformName);
        });
        const platformList = Array.from(platformSet);

        // Group history records by recordedAt timestamp (formatted)
        const sortedHistory = [...history].sort((a, b) => new Date(a.recordedAt) - new Date(b.recordedAt));

        // Create pivot records for chart
        const timeMap = new Map();

        sortedHistory.forEach((h) => {
            const rawTime = h.recordedAt;
            const formattedLabel = formatDate(rawTime);

            if (!timeMap.has(formattedLabel)) {
                timeMap.set(formattedLabel, {
                    dateLabel: formattedLabel,
                    rawTimestamp: new Date(rawTime).getTime(),
                });
            }

            const row = timeMap.get(formattedLabel);
            row[h.platformName || 'Marketplace'] = Number(h.price);
        });

        const dataArray = Array.from(timeMap.values());

        return {
            chartData: dataArray,
            platforms: platformList,
        };
    }, [history]);

    // Handle platform legend toggle
    const togglePlatform = (platform) => {
        setHiddenPlatforms((prev) => {
            const next = new Set(prev);
            if (next.has(platform)) {
                next.delete(platform);
            } else {
                next.add(platform);
            }
            return next;
        });
    };

    // Calculate dynamic Y-axis bounds
    const yAxisDomain = useMemo(() => {
        if (!history || history.length === 0) return ['auto', 'auto'];
        const prices = history.map((h) => Number(h.price)).filter((p) => !isNaN(p));
        if (prices.length === 0) return ['auto', 'auto'];

        const min = Math.min(...prices);
        const max = Math.max(...prices);
        const buffer = Math.max(Math.round((max - min) * 0.15), 500);

        return [Math.max(0, Math.floor((min - buffer) / 100) * 100), Math.ceil((max + buffer) / 100) * 100];
    }, [history]);

    if (loading) {
        return (
            <Paper
                elevation={0}
                sx={{
                    p: 4,
                    mt: 6,
                    bgcolor: 'rgba(19, 27, 46, 0.8)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.08)',
                    borderRadius: 3,
                }}
            >
                <LoadingState message="Fetching product price history..." minHeight="250px" />
            </Paper>
        );
    }

    if (error) {
        return (
            <Paper
                elevation={0}
                sx={{
                    p: 4,
                    mt: 6,
                    bgcolor: 'rgba(19, 27, 46, 0.8)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.08)',
                    borderRadius: 3,
                }}
            >
                <ErrorState message={error} onRetry={onRetry} />
            </Paper>
        );
    }

    const isSingleSnapshot = history && history.length === 1;

    return (
        <Box sx={{ mt: 6 }}>
            {/* Section Title Header */}
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
                    <Timeline sx={{ color: '#06b6d4', fontSize: '1.6rem' }} />
                    <Typography variant="h5" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                        Product Price History
                    </Typography>
                </Box>

                {variantName && (
                    <Chip
                        label={`Variant: ${variantName}`}
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

            <Paper
                elevation={0}
                sx={{
                    p: { xs: 2.5, md: 4 },
                    bgcolor: 'rgba(19, 27, 46, 0.8)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.08)',
                    borderRadius: 3,
                }}
            >
                {/* Empty State Handling */}
                {(!history || history.length === 0) ? (
                    <Box
                        sx={{
                            py: 6,
                            px: 2,
                            textAlign: 'center',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center',
                            justifyContent: 'center',
                            gap: 2,
                        }}
                    >
                        <TrendingDown sx={{ fontSize: 56, color: '#64748b' }} />
                        <Typography variant="h6" sx={{ color: '#cbd5e1', fontWeight: 700 }}>
                            No Price History Recorded Yet
                        </Typography>
                        <Typography variant="body2" sx={{ color: '#94a3b8', maxWidth: 480, lineHeight: 1.6 }}>
                            Price history tracking for this product variant is active. Snapshot trends across marketplaces will populate automatically as prices update over time.
                        </Typography>
                    </Box>
                ) : (
                    <>
                        {/* Summary Metrics Cards */}
                        {stats && (
                            <Grid container spacing={2} sx={{ mb: 4 }}>
                                {/* Lowest Recorded Price */}
                                <Grid item xs={12} sm={6} md={stats.maxDrop ? 3 : 4}>
                                    <Box
                                        sx={{
                                            p: 2.2,
                                            borderRadius: 2.5,
                                            bgcolor: 'rgba(16, 185, 129, 0.08)',
                                            border: '1px solid rgba(16, 185, 129, 0.25)',
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                            <ArrowDownward sx={{ color: '#34d399', fontSize: '1.2rem' }} />
                                            <Typography variant="caption" sx={{ color: '#a7f3d0', fontWeight: 700, uppercase: true }}>
                                                Lowest Price
                                            </Typography>
                                        </Box>
                                        <Typography variant="h5" sx={{ fontWeight: 800, color: '#34d399' }}>
                                            {formatCurrency(stats.lowestPrice, stats.currency)}
                                        </Typography>
                                        {stats.lowestPlatform && (
                                            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block', mt: 0.5 }}>
                                                on {stats.lowestPlatform} ({formatDate(stats.lowestDate)})
                                            </Typography>
                                        )}
                                    </Box>
                                </Grid>

                                {/* Highest Recorded Price */}
                                <Grid item xs={12} sm={6} md={stats.maxDrop ? 3 : 4}>
                                    <Box
                                        sx={{
                                            p: 2.2,
                                            borderRadius: 2.5,
                                            bgcolor: 'rgba(239, 68, 68, 0.08)',
                                            border: '1px solid rgba(239, 68, 68, 0.25)',
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                            <ArrowUpward sx={{ color: '#f87171', fontSize: '1.2rem' }} />
                                            <Typography variant="caption" sx={{ color: '#fca5a5', fontWeight: 700, uppercase: true }}>
                                                Highest Price
                                            </Typography>
                                        </Box>
                                        <Typography variant="h5" sx={{ fontWeight: 800, color: '#f87171' }}>
                                            {formatCurrency(stats.highestPrice, stats.currency)}
                                        </Typography>
                                        {stats.highestPlatform && (
                                            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block', mt: 0.5 }}>
                                                on {stats.highestPlatform}
                                            </Typography>
                                        )}
                                    </Box>
                                </Grid>

                                {/* Latest Recorded Price */}
                                <Grid item xs={12} sm={6} md={stats.maxDrop ? 3 : 4}>
                                    <Box
                                        sx={{
                                            p: 2.2,
                                            borderRadius: 2.5,
                                            bgcolor: 'rgba(99, 102, 241, 0.08)',
                                            border: '1px solid rgba(99, 102, 241, 0.25)',
                                        }}
                                    >
                                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                            <Storefront sx={{ color: '#818cf8', fontSize: '1.2rem' }} />
                                            <Typography variant="caption" sx={{ color: '#c7d2fe', fontWeight: 700, uppercase: true }}>
                                                Latest Offer Price
                                            </Typography>
                                        </Box>
                                        <Typography variant="h5" sx={{ fontWeight: 800, color: '#818cf8' }}>
                                            {formatCurrency(stats.latestPrice, stats.currency)}
                                        </Typography>
                                        <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block', mt: 0.5 }}>
                                            Across active platforms
                                        </Typography>
                                    </Box>
                                </Grid>

                                {/* Largest Observed Price Drop (If applicable) */}
                                {stats.maxDrop && (
                                    <Grid item xs={12} sm={6} md={3}>
                                        <Box
                                            sx={{
                                                p: 2.2,
                                                borderRadius: 2.5,
                                                bgcolor: 'rgba(6, 182, 212, 0.08)',
                                                border: '1px solid rgba(6, 182, 212, 0.25)',
                                            }}
                                        >
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                                <TrendingDown sx={{ color: '#22d3ee', fontSize: '1.2rem' }} />
                                                <Typography variant="caption" sx={{ color: '#a5f3fc', fontWeight: 700, uppercase: true }}>
                                                    Largest Price Drop
                                                </Typography>
                                            </Box>
                                            <Typography variant="h5" sx={{ fontWeight: 800, color: '#22d3ee' }}>
                                                -{formatCurrency(stats.maxDrop.dropAmount, stats.currency)}
                                            </Typography>
                                            <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block', mt: 0.5 }}>
                                                on {stats.maxDrop.platform}
                                            </Typography>
                                        </Box>
                                    </Grid>
                                )}
                            </Grid>
                        )}

                        {/* Single Snapshot Informational Banner */}
                        {isSingleSnapshot && (
                            <Alert
                                icon={<InfoOutlined sx={{ color: '#06b6d4' }} />}
                                variant="outlined"
                                sx={{
                                    mb: 3,
                                    bgcolor: 'rgba(6, 182, 212, 0.08)',
                                    borderColor: 'rgba(6, 182, 212, 0.25)',
                                    color: '#f8fafc',
                                    borderRadius: 2.5,
                                }}
                            >
                                <Typography variant="body2" sx={{ color: '#cbd5e1', fontWeight: 500 }}>
                                    Initial baseline price snapshot recorded. Additional data points will be plotted as price shifts occur over time across marketplace stores.
                                </Typography>
                            </Alert>
                        )}

                        {/* Platform Legend & Filter Pills */}
                        {platforms.length > 0 && (
                            <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3, flexWrap: 'wrap', gap: 1.5 }}>
                                <Typography variant="subtitle2" sx={{ color: '#94a3b8', fontWeight: 700, uppercase: true, fontSize: '0.75rem' }}>
                                    Marketplace Platforms ({platforms.length})
                                </Typography>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                                    {platforms.map((plat, idx) => {
                                        const color = getPlatformColor(plat, idx);
                                        const isHidden = hiddenPlatforms.has(plat);

                                        return (
                                            <Chip
                                                key={plat}
                                                label={plat}
                                                size="small"
                                                onClick={() => togglePlatform(plat)}
                                                icon={
                                                    <Box
                                                        sx={{
                                                            width: 8,
                                                            height: 8,
                                                            borderRadius: '50%',
                                                            bgcolor: isHidden ? '#64748b' : color,
                                                            ml: 0.5,
                                                        }}
                                                    />
                                                }
                                                sx={{
                                                    cursor: 'pointer',
                                                    fontWeight: 600,
                                                    fontSize: '0.8rem',
                                                    bgcolor: isHidden ? 'rgba(255, 255, 255, 0.04)' : `${color}20`,
                                                    color: isHidden ? '#64748b' : color,
                                                    border: `1px solid ${isHidden ? 'rgba(255, 255, 255, 0.1)' : `${color}50`}`,
                                                    opacity: isHidden ? 0.6 : 1,
                                                    '&:hover': {
                                                        bgcolor: isHidden ? 'rgba(255, 255, 255, 0.08)' : `${color}35`,
                                                    },
                                                }}
                                            />
                                        );
                                    })}
                                </Box>
                            </Box>
                        )}

                        {/* Recharts Multi-Line Responsive Chart */}
                        <Box sx={{ width: '100%', height: 350, pt: 1 }}>
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart
                                    data={chartData}
                                    margin={{ top: 10, right: 30, left: 10, bottom: 10 }}
                                >
                                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255, 255, 255, 0.06)" vertical={false} />
                                    <XAxis
                                        dataKey="dateLabel"
                                        stroke="#64748b"
                                        tick={{ fill: '#94a3b8', fontSize: 12 }}
                                        tickLine={{ stroke: 'rgba(255, 255, 255, 0.1)' }}
                                    />
                                    <YAxis
                                        stroke="#64748b"
                                        domain={yAxisDomain}
                                        tick={{ fill: '#94a3b8', fontSize: 12 }}
                                        tickFormatter={(val) => `₹${Number(val).toLocaleString('en-IN')}`}
                                        tickLine={{ stroke: 'rgba(255, 255, 255, 0.1)' }}
                                    />
                                    <RechartsTooltip content={<CustomTooltip currency={stats?.currency || 'INR'} />} />
                                    <Legend
                                        wrapperStyle={{ paddingTop: 15 }}
                                        formatter={(value) => (
                                            <span style={{ color: '#cbd5e1', fontWeight: 600, fontSize: '0.85rem' }}>
                                                {value}
                                            </span>
                                        )}
                                    />

                                    {platforms.map((plat, idx) => {
                                        if (hiddenPlatforms.has(plat)) return null;
                                        const color = getPlatformColor(plat, idx);

                                        return (
                                            <Line
                                                key={plat}
                                                type="monotone"
                                                dataKey={plat}
                                                name={plat}
                                                stroke={color}
                                                strokeWidth={3}
                                                dot={{ fill: color, r: isSingleSnapshot ? 6 : 4, strokeWidth: 2, stroke: '#0f172a' }}
                                                activeDot={{ r: 7, stroke: '#ffffff', strokeWidth: 2 }}
                                                connectNulls={true}
                                            />
                                        );
                                    })}
                                </LineChart>
                            </ResponsiveContainer>
                        </Box>
                    </>
                )}
            </Paper>
        </Box>
    );
};

export default PriceHistoryChart;
