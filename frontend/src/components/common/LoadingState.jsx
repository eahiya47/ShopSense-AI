import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

/**
 * Reusable loading state component.
 * Displays a centered visual progress indicator and optional text message.
 *
 * @param {string} message - Custom loading text (default: 'Loading...')
 * @param {string|number} minHeight - Container minimum height (default: '200px')
 */
const LoadingState = ({ message = 'Loading...', minHeight = '200px' }) => {
    return (
        <Box
            role="status"
            aria-live="polite"
            sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight,
                py: 4,
                px: 2,
                textAlign: 'center',
                width: '100%',
            }}
        >
            <CircularProgress size={40} sx={{ color: 'primary.main', mb: 2 }} />
            <Typography variant="body1" sx={{ color: 'text.secondary', fontWeight: 500 }}>
                {message}
            </Typography>
        </Box>
    );
};

export default LoadingState;
