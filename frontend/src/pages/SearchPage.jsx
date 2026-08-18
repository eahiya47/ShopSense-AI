import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
    Container,
    Box,
    Typography,
    TextField,
    Button,
    Grid,
    Pagination,
    InputAdornment,
    IconButton,
    Paper,
    Chip,
} from '@mui/material';
import { Search as SearchIcon, Clear as ClearIcon, FilterList as FilterIcon } from '@mui/icons-material';
import { searchProducts } from '../api/catalog';
import { saveSearchHistory, getSearchHistory } from '../api/userFeatures';
import { useAuth } from '../context/AuthContext';
import ProductCard from '../components/product/ProductCard';
import LoadingState from '../components/common/LoadingState';
import ErrorState from '../components/common/ErrorState';

const PAGE_SIZE = 12;

const makeSearchKey = (query, category) => {
    const q = (query || '').trim().toLowerCase();
    const c = (category || '').trim().toLowerCase();
    return `${q}|${c}`;
};

const SearchPage = () => {

    const { user, isAuthenticated } = useAuth();

    const [searchParams, setSearchParams] = useSearchParams();

    // Cache key for current session
    const getCacheKey = useCallback(() => `shopsense_history_keys_${user?.id || 'anon'}`, [user?.id]);

    const getHistoryKeysCache = useCallback(() => {
        try {
            const raw = sessionStorage.getItem(getCacheKey());
            return raw ? new Set(JSON.parse(raw)) : null;
        } catch (e) {
            return null;
        }
    }, [getCacheKey]);

    const addHistoryKeyToCache = useCallback((searchKey) => {
        try {
            const cache = getHistoryKeysCache() || new Set();
            cache.add(searchKey);
            sessionStorage.setItem(getCacheKey(), JSON.stringify(Array.from(cache)));
        } catch (e) {
            console.error('Failed to update search history cache', e);
        }
    }, [getHistoryKeysCache, getCacheKey]);

    // Save search history entry if authenticated, new, and on page 1
    const maybeSaveSearchHistory = useCallback(async (query, category, pageIndex) => {
        if (!isAuthenticated) return;
        if (!query || !query.trim()) return;
        if (pageIndex > 0) return;

        const searchKey = makeSearchKey(query, category);

        let cache = getHistoryKeysCache();
        if (!cache) {
            try {
                const data = await getSearchHistory();
                const existingKeys = (data?.history || []).map((item) =>
                    makeSearchKey(item.query, item.category)
                );
                cache = new Set(existingKeys);
                sessionStorage.setItem(getCacheKey(), JSON.stringify(Array.from(cache)));
            } catch (err) {
                console.error('Failed to fetch existing search history keys:', err);
                cache = new Set();
            }
        }

        if (cache.has(searchKey)) {
            return;
        }

        addHistoryKeyToCache(searchKey);
        try {
            await saveSearchHistory(query, category);
            console.log('Search history saved:', query, category);
        } catch (err) {
            console.error('Failed to save search history:', err?.response?.data || err);
        }
    }, [isAuthenticated, getHistoryKeysCache, getCacheKey, addHistoryKeyToCache]);

    // Read initial URL params
    const initialQuery = searchParams.get('q') || '';
    const initialCategory = searchParams.get('category') || '';
    const initialPageParam = parseInt(searchParams.get('page') || '1', 10);
    const initialPage = isNaN(initialPageParam) || initialPageParam < 1 ? 0 : initialPageParam - 1;

    // Form inputs state
    const [searchInput, setSearchInput] = useState(initialQuery);
    const [categoryInput, setCategoryInput] = useState(initialCategory);

    // API & Response state
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [searchResponse, setSearchResponse] = useState(null);
    const [hasSearched, setHasSearched] = useState(false);

    // Execute search request
    const executeSearch = useCallback(async (query, category, pageIndex) => {
        setLoading(true);
        setError(null);
        try {
            const data = await searchProducts({
                q: query,
                category: category,
                page: pageIndex,
                size: PAGE_SIZE,
            });
            setSearchResponse(data);
            setHasSearched(true);
        } catch (err) {
            console.error('Search request failed:', err);
            setError(err?.response?.data?.message || 'Failed to search products. Please check your connection and try again.');
        } finally {
            setLoading(false);
        }
    }, []);

    // Effect to trigger search when URL query parameters change
    useEffect(() => {
        const query = searchParams.get('q') || '';
        const category = searchParams.get('category') || '';
        const pageParam = parseInt(searchParams.get('page') || '1', 10);
        const pageIndex = isNaN(pageParam) || pageParam < 1 ? 0 : pageParam - 1;

        setSearchInput(query);
        setCategoryInput(category);

        // Execute search if query or category parameter is present in URL
        if (query.trim() !== '' || category.trim() !== '') {
            executeSearch(query.trim(), category.trim(), pageIndex);
            maybeSaveSearchHistory(query.trim(), category.trim(), pageIndex);
        } else {
            // Initial landing on /search with no params
            setSearchResponse(null);
            setHasSearched(false);
        }
    }, [searchParams, executeSearch, maybeSaveSearchHistory]);

    // Handle form submit (button click or Enter key)
    const handleSearchSubmit = (e) => {
        if (e) e.preventDefault();

        const query = searchInput.trim();
        const category = categoryInput.trim();

        const nextParams = {};

        if (query) nextParams.q = query;
        if (category) nextParams.category = category;

        nextParams.page = '1';

        setSearchParams(nextParams);
    };

    // Clear search inputs
    const handleClear = () => {
        setSearchInput('');
        setCategoryInput('');
        setSearchParams({});
    };

    // Active search parameters derived from URL (represents executed search state)
    const activeQuery = searchParams.get('q') || '';
    const activeCategory = searchParams.get('category') || '';

    // Handle pagination page change
    const handlePageChange = (event, newUiPage) => {
        const nextParams = {};
        if (activeQuery) nextParams.q = activeQuery;
        if (activeCategory) nextParams.category = activeCategory;
        nextParams.page = newUiPage.toString();

        setSearchParams(nextParams);
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // Extract fields from ProductSearchResponse DTO
    const products = searchResponse?.products || [];
    const totalResults = searchResponse?.totalResults || 0;
    const totalPages = searchResponse?.totalPages || 0;
    const currentPageIndex = searchResponse?.page ?? 0;

    return (
        <Container maxWidth="xl" sx={{ py: { xs: 4, md: 6 } }}>
            {/* Header / Hero Section */}
            <Box sx={{ mb: 5, textAlign: 'center' }}>
                <Typography
                    variant="h2"
                    component="h1"
                    sx={{
                        fontWeight: 800,
                        fontSize: { xs: '2rem', sm: '2.8rem', md: '3.4rem' },
                        mb: 2,
                        color: '#f8fafc',
                        letterSpacing: '-1px',
                    }}
                >
                    Find the Right Product
                </Typography>
                <Typography
                    variant="h6"
                    sx={{
                        color: '#94a3b8',
                        fontWeight: 400,
                        maxWidth: 700,
                        mx: 'auto',
                        fontSize: { xs: '0.95rem', md: '1.15rem' },
                    }}
                >
                    Search across top e-commerce stores to compare prices, specifications, and AI insights.
                </Typography>
            </Box>

            {/* Search Input Bar Paper */}
            <Paper
                component="form"
                onSubmit={handleSearchSubmit}
                elevation={0}
                sx={{
                    p: { xs: 2, sm: 3 },
                    mb: 5,
                    bgcolor: 'rgba(19, 27, 46, 0.8)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.08)',
                    borderRadius: 4,
                    boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)',
                }}
            >
                <Grid container spacing={2} alignItems="center">
                    {/* Keyword Input */}
                    <Grid item xs={12} sm={6} md={7}>
                        <TextField
                            fullWidth
                            id="search-query-input"
                            label="Product Keyword"
                            placeholder="e.g. iPhone, Samsung, Laptop..."
                            value={searchInput}
                            onChange={(e) => setSearchInput(e.target.value)}
                            variant="outlined"
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchIcon sx={{ color: '#818cf8' }} />
                                    </InputAdornment>
                                ),
                                endAdornment: searchInput && (
                                    <InputAdornment position="end">
                                        <IconButton
                                            aria-label="clear search query"
                                            onClick={() => setSearchInput('')}
                                            edge="end"
                                            size="small"
                                            sx={{ color: '#94a3b8' }}
                                        >
                                            <ClearIcon fontSize="small" />
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Grid>

                    {/* Category Input */}
                    <Grid item xs={12} sm={3} md={3}>
                        <TextField
                            fullWidth
                            id="category-filter-input"
                            label="Category (Optional)"
                            placeholder="e.g. Smartphone, Audio"
                            value={categoryInput}
                            onChange={(e) => setCategoryInput(e.target.value)}
                            variant="outlined"
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <FilterIcon sx={{ color: '#06b6d4' }} />
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Grid>

                    {/* Action Buttons */}
                    <Grid item xs={12} sm={3} md={2} sx={{ display: 'flex', gap: 1 }}>
                        <Button
                            type="submit"
                            variant="contained"
                            fullWidth
                            disabled={loading}
                            sx={{
                                py: 1.8,
                                background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                                fontWeight: 700,
                                fontSize: '1rem',
                                '&:hover': {
                                    background: 'linear-gradient(135deg, #4f46e5 0%, #4338ca 100%)',
                                },
                            }}
                        >
                            Search
                        </Button>
                        {(searchInput || categoryInput || hasSearched) && (
                            <Button
                                variant="outlined"
                                onClick={handleClear}
                                sx={{
                                    py: 1.8,
                                    borderColor: 'rgba(255, 255, 255, 0.15)',
                                    color: '#cbd5e1',
                                    '&:hover': {
                                        borderColor: '#ef4444',
                                        color: '#ef4444',
                                    },
                                }}
                            >
                                Clear
                            </Button>
                        )}
                    </Grid>
                </Grid>
            </Paper>

            {/* Loading State */}
            {loading && <LoadingState message="Searching catalog..." minHeight="300px" />}

            {/* Error State */}
            {error && !loading && (
                <ErrorState message={error} onRetry={handleSearchSubmit} />
            )}

            {/* Results Display */}
            {!loading && !error && hasSearched && (
                <Box sx={{ mb: 6 }}>
                    {/* Result Metadata Header */}
                    <Box
                        sx={{
                            display: 'flex',
                            justify: 'space-between',
                            alignItems: 'center',
                            mb: 3,
                            pb: 2,
                            borderBottom: '1px solid rgba(255, 255, 255, 0.08)',
                            flexWrap: 'wrap',
                            gap: 1.5,
                        }}
                    >
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
                            <Typography variant="h6" sx={{ fontWeight: 700, color: '#f8fafc' }}>
                                Search Results
                            </Typography>
                            {activeQuery && (
                                <Chip
                                    label={`Query: "${activeQuery}"`}
                                    size="small"
                                    sx={{ bgcolor: 'rgba(99, 102, 241, 0.15)', color: '#818cf8', fontWeight: 600 }}
                                />
                            )}
                            {activeCategory && (
                                <Chip
                                    label={`Category: "${activeCategory}"`}
                                    size="small"
                                    sx={{ bgcolor: 'rgba(6, 182, 212, 0.15)', color: '#22d3ee', fontWeight: 600 }}
                                />
                            )}
                        </Box>
                        <Typography variant="body2" sx={{ color: '#94a3b8', fontWeight: 500 }}>
                            Showing {products.length} of {totalResults} products
                        </Typography>
                    </Box>

                    {/* Product Cards Grid or Empty State */}
                    {products.length > 0 ? (
                        <>
                            <Grid container spacing={3}>
                                {products.map((product) => (
                                    <Grid item xs={12} sm={6} md={4} lg={3} key={product.id}>
                                        <ProductCard product={product} />
                                    </Grid>
                                ))}
                            </Grid>

                            {/* Pagination Controls */}
                            {totalPages > 1 && (
                                <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}>
                                    <Pagination
                                        count={totalPages}
                                        page={currentPageIndex + 1}
                                        onChange={handlePageChange}
                                        color="primary"
                                        size="large"
                                        shape="rounded"
                                        sx={{
                                            '& .MuiPaginationItem-root': {
                                                color: '#cbd5e1',
                                                borderColor: 'rgba(255, 255, 255, 0.15)',
                                            },
                                            '& .Mui-selected': {
                                                bgcolor: '#6366f1 !important',
                                                color: '#ffffff',
                                                fontWeight: 700,
                                            },
                                        }}
                                    />
                                </Box>
                            )}
                        </>
                    ) : (
                        /* Empty Results State */
                        <Paper
                            elevation={0}
                            sx={{
                                p: 6,
                                textAlign: 'center',
                                bgcolor: 'rgba(19, 27, 46, 0.4)',
                                border: '1px dashed rgba(255, 255, 255, 0.15)',
                                borderRadius: 4,
                            }}
                        >
                            <Typography variant="h5" sx={{ fontWeight: 700, color: '#f8fafc', mb: 1 }}>
                                No products found
                            </Typography>
                            <Typography variant="body1" sx={{ color: '#94a3b8', maxWidth: 500, mx: 'auto' }}>
                                We couldn't find any products matching your search criteria. Try adjusting your keyword or clearing category filters.
                            </Typography>
                        </Paper>
                    )}
                </Box>
            )}

            {/* Discovery State (No search performed yet) */}
            {!loading && !error && !hasSearched && (
                <Paper
                    elevation={0}
                    sx={{
                        p: { xs: 4, md: 8 },
                        textAlign: 'center',
                        bgcolor: 'rgba(19, 27, 46, 0.4)',
                        border: '1px solid rgba(255, 255, 255, 0.08)',
                        borderRadius: 4,
                    }}
                >
                    <SearchIcon sx={{ fontSize: 56, color: '#6366f1', mb: 2, opacity: 0.8 }} />
                    <Typography variant="h5" sx={{ fontWeight: 700, color: '#f8fafc', mb: 1 }}>
                        Ready to Compare?
                    </Typography>
                    <Typography variant="body1" sx={{ color: '#94a3b8', maxWidth: 550, mx: 'auto' }}>
                        Enter a product brand or model (e.g. "iPhone", "Apple", "Samsung") in the search box above to explore multi-store price comparisons and AI recommendations.
                    </Typography>
                </Paper>
            )}
        </Container>
    );
};

export default SearchPage;
