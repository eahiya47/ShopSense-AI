import api from './axios';

/**
 * Search products with optional query, category, and pagination.
 * GET /api/v1/products/search
 */
export const searchProducts = async ({ q = '', category = '', page = 0, size = 20 } = {}) => {
    const params = {};
    if (q) params.q = q;
    if (category) params.category = category;
    if (page !== undefined) params.page = page;
    if (size !== undefined) params.size = size;

    const response = await api.get('/products/search', { params });
    return response.data;
};

/**
 * Get catalog details and specifications for a product.
 * GET /api/v1/products/{productId}
 */
export const getProductById = async (productId) => {
    const response = await api.get(`/products/${productId}`);
    return response.data;
};

/**
 * Get available variants and configuration attributes for a product.
 * GET /api/v1/products/{productId}/variants
 */
export const getProductVariants = async (productId) => {
    const response = await api.get(`/products/${productId}/variants`);
    return response.data;
};

/**
 * Get live normalized marketplace comparison offers for a variant.
 * GET /api/v1/variants/{variantId}/comparison
 */
export const getVariantComparison = async (variantId) => {
    const response = await api.get(`/variants/${variantId}/comparison`);
    return response.data;
};

/**
 * Get customer reviews for a variant with optional platform filter and limit.
 * GET /api/v1/variants/{variantId}/reviews
 */
export const getVariantReviews = async (variantId, { platformId, limit = 15 } = {}) => {
    const params = {};
    if (platformId) params.platformId = platformId;
    if (limit) params.limit = limit;

    const response = await api.get(`/variants/${variantId}/reviews`, { params });
    return response.data;
};

/**
 * Get AI analysis and comparison summary for a variant.
 * GET /api/v1/variants/{variantId}/ai-analysis
 */
export const getVariantAIAnalysis = async (variantId) => {
    const response = await api.get(`/variants/${variantId}/ai-analysis`);
    return response.data;
};
