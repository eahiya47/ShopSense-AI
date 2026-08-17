import React from 'react';
import { Box, Alert, AlertTitle, Button } from '@mui/material';
import { Refresh } from '@mui/icons-material';

/**
 * Reusable error state component.
 * Displays a styled error alert message with an optional retry button.
 *
 * @param {string} message - Custom error message (default: 'Something went wrong. Please try again.')
 * @param {function} onRetry - Optional callback triggered when user clicks Retry
 */
const ErrorState = ({ message = 'Something went wrong. Please try again.', onRetry }) => {
    return (
        <Box sx={{ my: 3, width: '100%' }}>
            <Alert
                severity="error"
                variant="outlined"
                sx={{
                    bgcolor: 'rgba(239, 68, 68, 0.08)',
                    borderColor: 'rgba(239, 68, 68, 0.3)',
                    color: '#f8fafc',
                    borderRadius: 3,
                    p: 2.5,
                    alignItems: 'center',
                }}
                action={
                    onRetry && (
                        <Button
                            color="error"
                            variant="contained"
                            size="small"
                            onClick={onRetry}
                            startIcon={<Refresh />}
                            sx={{
                                textTransform: 'none',
                                fontWeight: 600,
                                bgcolor: '#ef4444',
                                '&:hover': { bgcolor: '#dc2626' },
                                ml: 2,
                                whiteSpace: 'nowrap',
                            }}
                        >
                            Retry
                        </Button>
                    )
                }
            >
                <AlertTitle sx={{ fontWeight: 700, mb: 0.5 }}>Error</AlertTitle>
                {message}
            </Alert>
        </Box>
    );
};

export default ErrorState;
