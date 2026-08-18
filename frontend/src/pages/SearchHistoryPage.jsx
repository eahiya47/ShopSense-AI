import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Container,
    Box,
    Typography,
    Paper,
    Button,
    IconButton,
    Chip,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogContentText,
    DialogActions,
    Snackbar,
    Alert,
    Tooltip,
} from '@mui/material';
import {
    History,
    Search,
    Delete,
    DeleteSweep,
    AccessTime,
} from '@mui/icons-material';
import {
    getSearchHistory,
    deleteSearchHistoryItem,
    clearSearchHistory,
} from '../api/userFeatures';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';

const SearchHistoryPage = () => {
    const navigate = useNavigate();
    const [historyItems, setHistoryItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [confirmClearOpen, setConfirmClearOpen] = useState(false);
    const [clearing, setClearing] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'info' });

    const fetchHistory = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const data = await getSearchHistory();
            setHistoryItems(data?.history || []);
        } catch (err) {
            console.error('Failed to fetch search history:', err);
            setError(err?.response?.data?.message || 'Failed to load search history.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchHistory();
    }, [fetchHistory]);

    const handleSearchQueryClick = (query, category) => {
        if (query) {
            const params = new URLSearchParams();
            params.set('q', query);
            if (category && category.trim()) {
                params.set('category', category.trim());
            }
            navigate(`/search?${params.toString()}`);
        }
    };

    const handleDeleteItem = async (id) => {
        try {
            await deleteSearchHistoryItem(id);
            setHistoryItems((prev) => prev.filter((item) => item.id !== id));
            setSnackbar({
                open: true,
                message: 'Search history entry deleted.',
                severity: 'success',
            });
        } catch (err) {
            console.error('Failed to delete search history item:', err);
            setSnackbar({
                open: true,
                message: err?.response?.data?.message || 'Failed to delete entry.',
                severity: 'error',
            });
        }
    };

    const handleConfirmClearAll = async () => {
        setClearing(true);
        try {
            await clearSearchHistory();
            setHistoryItems([]);
            setConfirmClearOpen(false);
            setSnackbar({
                open: true,
                message: 'All search history cleared.',
                severity: 'success',
            });
        } catch (err) {
            console.error('Failed to clear search history:', err);
            setSnackbar({
                open: true,
                message: err?.response?.data?.message || 'Failed to clear search history.',
                severity: 'error',
            });
        } finally {
            setClearing(false);
        }
    };

    const handleCloseSnackbar = () => {
        setSnackbar((prev) => ({ ...prev, open: false }));
    };

    const formatSearchedDate = (dateStr) => {
        if (!dateStr) return null;
        try {
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) return String(dateStr);

            const now = new Date();
            const isToday = date.toDateString() === now.toDateString();
            const yesterday = new Date(now);
            yesterday.setDate(now.getDate() - 1);
            const isYesterday = date.toDateString() === yesterday.toDateString();

            const timeStr = date.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });

            if (isToday) {
                return `Today · ${timeStr}`;
            } else if (isYesterday) {
                return `Yesterday · ${timeStr}`;
            } else {
                return `${date.toLocaleDateString([], { month: 'short', day: 'numeric' })} · ${timeStr}`;
            }
        } catch (e) {
            return String(dateStr);
        }
    };

    if (loading) {
        return (
            <Container maxWidth="md" sx={{ py: 6 }}>
                <LoadingState message="Loading search history..." minHeight="300px" />
            </Container>
        );
    }

    if (error) {
        return (
            <Container maxWidth="md" sx={{ py: 6 }}>
                <ErrorState message={error} onRetry={fetchHistory} />
            </Container>
        );
    }

    return (
        <Container maxWidth="md" sx={{ py: { xs: 4, md: 6 } }}>
            {/* Header and Actions */}
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    mb: 4,
                    flexWrap: 'wrap',
                    gap: 2,
                }}
            >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Box
                        sx={{
                            width: 48,
                            height: 48,
                            borderRadius: 3,
                            background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            boxShadow: '0 4px 14px rgba(6, 182, 212, 0.4)',
                        }}
                    >
                        <History sx={{ color: '#ffffff', fontSize: 26 }} />
                    </Box>
                    <Box>
                        <Typography variant="h4" component="h1" sx={{ fontWeight: 800, color: '#f8fafc' }}>
                            Search History
                        </Typography>
                        <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                            Your recent searches and discoveries
                        </Typography>
                    </Box>
                </Box>

                {historyItems.length > 0 && (
                    <Button
                        variant="outlined"
                        color="error"
                        startIcon={<DeleteSweep />}
                        onClick={() => setConfirmClearOpen(true)}
                        sx={{
                            borderColor: 'rgba(239, 68, 68, 0.3)',
                            color: '#f87171',
                            fontWeight: 600,
                            textTransform: 'none',
                            borderRadius: 2,
                            '&:hover': {
                                borderColor: '#ef4444',
                                bgcolor: 'rgba(239, 68, 68, 0.1)',
                            },
                        }}
                    >
                        Clear All
                    </Button>
                )}
            </Box>

            {/* Empty State */}
            {historyItems.length === 0 ? (
                <Paper
                    elevation={0}
                    sx={{
                        p: { xs: 5, sm: 7 },
                        textAlign: 'center',
                        bgcolor: 'rgba(19, 27, 46, 0.6)',
                        backdropFilter: 'blur(12px)',
                        border: '1px dashed rgba(255, 255, 255, 0.15)',
                        borderRadius: 4,
                    }}
                >
                    <Box
                        sx={{
                            width: 64,
                            height: 64,
                            borderRadius: '50%',
                            bgcolor: 'rgba(6, 182, 212, 0.1)',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            mx: 'auto',
                            mb: 2,
                        }}
                    >
                        <History sx={{ fontSize: 32, color: '#06b6d4' }} />
                    </Box>
                    <Typography variant="h5" sx={{ color: '#f8fafc', fontWeight: 700, mb: 1 }}>
                        Search history is empty
                    </Typography>
                    <Typography variant="body1" sx={{ color: '#94a3b8', mb: 3.5, maxWidth: 460, mx: 'auto' }}>
                        Your searches will appear here when you start exploring products.
                    </Typography>
                    <Button
                        variant="contained"
                        startIcon={<Search />}
                        onClick={() => navigate('/search')}
                        sx={{
                            py: 1.2,
                            px: 3,
                            background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
                            fontWeight: 600,
                            textTransform: 'none',
                            borderRadius: 2,
                            boxShadow: '0 4px 14px rgba(6, 182, 212, 0.3)',
                        }}
                    >
                        Start Searching
                    </Button>
                </Paper>
            ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
                    {historyItems.map((item) => {
                        const formattedDate = formatSearchedDate(item.searchedAt);

                        return (
                            <Paper
                                key={item.id}
                                elevation={0}
                                sx={{
                                    p: { xs: 2, sm: 2.5 },
                                    bgcolor: 'rgba(19, 27, 46, 0.75)',
                                    backdropFilter: 'blur(12px)',
                                    border: '1px solid rgba(255, 255, 255, 0.08)',
                                    borderRadius: 3,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'space-between',
                                    flexWrap: { xs: 'wrap', sm: 'nowrap' },
                                    gap: 2,
                                    transition: 'all 0.2s ease-in-out',
                                    '&:hover': {
                                        bgcolor: 'rgba(255, 255, 255, 0.03)',
                                        borderColor: 'rgba(6, 182, 212, 0.3)',
                                        transform: 'translateY(-1px)',
                                    },
                                }}
                            >
                                {/* Left: Query & Category group */}
                                <Box
                                    onClick={() => handleSearchQueryClick(item.query, item.category)}
                                    sx={{
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: 1.5,
                                        flexGrow: 1,
                                        minWidth: 0,
                                        cursor: 'pointer',
                                    }}
                                >
                                    <Box
                                        sx={{
                                            width: 38,
                                            height: 38,
                                            borderRadius: 2,
                                            bgcolor: 'rgba(6, 182, 212, 0.12)',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            flexShrink: 0,
                                        }}
                                    >
                                        <Search sx={{ color: '#06b6d4', fontSize: 20 }} />
                                    </Box>

                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, flexWrap: 'wrap', minWidth: 0 }}>
                                        <Typography
                                            variant="subtitle1"
                                            sx={{
                                                fontWeight: 700,
                                                color: '#f8fafc',
                                                fontSize: { xs: '0.95rem', sm: '1rem' },
                                                '&:hover': { color: '#38bdf8' },
                                            }}
                                        >
                                            {item.query}
                                        </Typography>

                                        <Chip
                                            label={item.category ? item.category : 'All Categories'}
                                            size="small"
                                            variant="outlined"
                                            sx={{
                                                height: 24,
                                                fontSize: '0.75rem',
                                                fontWeight: 600,
                                                color: item.category ? '#22d3ee' : '#94a3b8',
                                                borderColor: item.category ? 'rgba(6, 182, 212, 0.35)' : 'rgba(255, 255, 255, 0.12)',
                                                bgcolor: item.category ? 'rgba(6, 182, 212, 0.08)' : 'rgba(255, 255, 255, 0.03)',
                                            }}
                                        />
                                    </Box>
                                </Box>

                                {/* Right: Timestamp & Delete action */}
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexShrink: 0, ml: { xs: 'auto', sm: 0 } }}>
                                    {formattedDate && (
                                        <Typography
                                            variant="caption"
                                            sx={{
                                                color: '#64748b',
                                                fontWeight: 500,
                                                display: 'flex',
                                                alignItems: 'center',
                                                gap: 0.6,
                                                fontSize: '0.8rem',
                                            }}
                                        >
                                            <AccessTime sx={{ fontSize: '0.85rem', color: '#64748b' }} />
                                            {formattedDate}
                                        </Typography>
                                    )}

                                    <Tooltip title="Delete entry">
                                        <IconButton
                                            onClick={() => handleDeleteItem(item.id)}
                                            aria-label={`Delete search entry for ${item.query}`}
                                            size="small"
                                            sx={{
                                                color: '#64748b',
                                                '&:hover': { color: '#ef4444', bgcolor: 'rgba(239, 68, 68, 0.1)' },
                                            }}
                                        >
                                            <Delete fontSize="small" />
                                        </IconButton>
                                    </Tooltip>
                                </Box>
                            </Paper>
                        );
                    })}
                </Box>
            )}

            {/* Clear All Confirmation Dialog */}
            <Dialog
                open={confirmClearOpen}
                onClose={() => !clearing && setConfirmClearOpen(false)}
                PaperProps={{
                    sx: {
                        bgcolor: '#131b2e',
                        border: '1px solid rgba(255, 255, 255, 0.1)',
                        borderRadius: 3,
                        color: '#f8fafc',
                    },
                }}
            >
                <DialogTitle sx={{ fontWeight: 700 }}>Clear Search History?</DialogTitle>
                <DialogContent>
                    <DialogContentText sx={{ color: '#cbd5e1' }}>
                        Are you sure you want to delete all search history entries? This action cannot be undone.
                    </DialogContentText>
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 2.5 }}>
                    <Button
                        onClick={() => setConfirmClearOpen(false)}
                        disabled={clearing}
                        sx={{ color: '#94a3b8', fontWeight: 600, textTransform: 'none' }}
                    >
                        Cancel
                    </Button>
                    <Button
                        onClick={handleConfirmClearAll}
                        disabled={clearing}
                        variant="contained"
                        color="error"
                        sx={{ fontWeight: 600, textTransform: 'none', bgcolor: '#ef4444', '&:hover': { bgcolor: '#dc2626' } }}
                    >
                        {clearing ? 'Clearing...' : 'Clear All'}
                    </Button>
                </DialogActions>
            </Dialog>

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

export default SearchHistoryPage;
