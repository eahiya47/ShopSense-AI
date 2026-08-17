import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Container,
    Box,
    Typography,
    Paper,
    Button,
    IconButton,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
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

    const handleSearchQueryClick = (query) => {
        if (query) {
            navigate(`/search?q=${encodeURIComponent(query)}`);
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
            return isNaN(date.getTime()) ? String(dateStr) : date.toLocaleString();
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
                            Your recent product searches across ShopSense AI
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
                            borderColor: 'rgba(239, 68, 68, 0.4)',
                            color: '#f87171',
                            fontWeight: 600,
                            textTransform: 'none',
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
                        p: 6,
                        textAlign: 'center',
                        bgcolor: 'rgba(19, 27, 46, 0.6)',
                        backdropFilter: 'blur(12px)',
                        border: '1px dashed rgba(255, 255, 255, 0.15)',
                        borderRadius: 4,
                    }}
                >
                    <History sx={{ fontSize: 56, color: 'rgba(6, 182, 212, 0.3)', mb: 2 }} />
                    <Typography variant="h6" sx={{ color: '#f8fafc', fontWeight: 700, mb: 1 }}>
                        No search history yet.
                    </Typography>
                    <Typography variant="body2" sx={{ color: '#94a3b8', mb: 3, maxWidth: 450, mx: 'auto' }}>
                        Searches you perform will be saved here for easy re-discovery.
                    </Typography>
                    <Button
                        variant="contained"
                        startIcon={<Search />}
                        onClick={() => navigate('/search')}
                        sx={{
                            background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
                            fontWeight: 600,
                            textTransform: 'none',
                        }}
                    >
                        Start Searching
                    </Button>
                </Paper>
            ) : (
                <Paper
                    elevation={0}
                    sx={{
                        bgcolor: 'rgba(19, 27, 46, 0.75)',
                        backdropFilter: 'blur(12px)',
                        border: '1px solid rgba(255, 255, 255, 0.08)',
                        borderRadius: 3,
                        overflow: 'hidden',
                    }}
                >
                    <List disablePadding>
                        {historyItems.map((item, index) => {
                            const formattedDate = formatSearchedDate(item.searchedAt);
                            const isLast = index === historyItems.length - 1;

                            return (
                                <ListItem
                                    key={item.id}
                                    divider={!isLast}
                                    sx={{
                                        py: 2,
                                        px: 3,
                                        borderColor: 'rgba(255, 255, 255, 0.05)',
                                        transition: 'background-color 0.2s ease',
                                        '&:hover': {
                                            bgcolor: 'rgba(255, 255, 255, 0.03)',
                                        },
                                    }}
                                    secondaryAction={
                                        <Tooltip title="Delete entry">
                                            <IconButton
                                                edge="end"
                                                onClick={() => handleDeleteItem(item.id)}
                                                aria-label={`Delete search entry for ${item.query}`}
                                                sx={{
                                                    color: '#94a3b8',
                                                    '&:hover': { color: '#ef4444', bgcolor: 'rgba(239, 68, 68, 0.1)' },
                                                }}
                                            >
                                                <Delete fontSize="small" />
                                            </IconButton>
                                        </Tooltip>
                                    }
                                >
                                    <ListItemIcon
                                        onClick={() => handleSearchQueryClick(item.query)}
                                        sx={{ minWidth: 42, cursor: 'pointer' }}
                                    >
                                        <Search sx={{ color: '#06b6d4' }} />
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={
                                            <Typography
                                                variant="subtitle1"
                                                onClick={() => handleSearchQueryClick(item.query)}
                                                sx={{
                                                    fontWeight: 600,
                                                    color: '#f8fafc',
                                                    cursor: 'pointer',
                                                    '&:hover': { color: '#38bdf8', textDecoration: 'underline' },
                                                }}
                                            >
                                                {item.query}
                                            </Typography>
                                        }
                                        secondary={
                                            formattedDate && (
                                                <Typography
                                                    component="span"
                                                    variant="caption"
                                                    sx={{ color: '#64748b', display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5 }}
                                                >
                                                    <AccessTime sx={{ fontSize: '0.8rem' }} />
                                                    {formattedDate}
                                                </Typography>
                                            )
                                        }
                                    />
                                </ListItem>
                            );
                        })}
                    </List>
                </Paper>
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
