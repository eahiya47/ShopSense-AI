import api from './axios';

/**
 * Add a product variant to the authenticated user's wishlist.
 * POST /api/v1/wishlist
 */
export const addToWishlist = async (productVariantId) => {
    const response = await api.post('/wishlist', { productVariantId });
    return response.data;
};

/**
 * Get all wishlist items for the authenticated user.
 * GET /api/v1/wishlist
 */
export const getWishlist = async () => {
    const response = await api.get('/wishlist');
    return response.data;
};

/**
 * Remove a product variant from the authenticated user's wishlist.
 * DELETE /api/v1/wishlist/{productVariantId}
 */
export const removeFromWishlist = async (productVariantId) => {
    const response = await api.delete(`/wishlist/${productVariantId}`);
    return response.data;
};

/**
 * Save a search query to the authenticated user's search history.
 * POST /api/v1/search-history
 */
export const saveSearchHistory = async (query) => {
    const response = await api.post('/search-history', { query });
    return response.data;
};

/**
 * Get search history entries for the authenticated user.
 * GET /api/v1/search-history
 */
export const getSearchHistory = async () => {
    const response = await api.get('/search-history');
    return response.data;
};

/**
 * Delete a specific search history entry owned by the authenticated user.
 * DELETE /api/v1/search-history/{id}
 */
export const deleteSearchHistoryItem = async (id) => {
    const response = await api.delete(`/search-history/${id}`);
    return response.data;
};

/**
 * Clear all search history entries owned by the authenticated user.
 * DELETE /api/v1/search-history
 */
export const clearSearchHistory = async () => {
    const response = await api.delete('/search-history');
    return response.data;
};
