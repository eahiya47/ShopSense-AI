import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Paper, InputBase, IconButton, Button } from '@mui/material';
import { Search as SearchIcon, Clear as ClearIcon } from '@mui/icons-material';

const QuickSearchBar = ({
    placeholder = 'What are you looking for? (e.g. iPhone, Laptop, Wireless Earbuds)',
    sx = {},
    buttonText = 'Search',
}) => {
    const navigate = useNavigate();
    const [query, setQuery] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        const trimmed = query.trim();
        if (trimmed) {
            navigate(`/search?q=${encodeURIComponent(trimmed)}`);
        }
    };

    const handleClear = () => {
        setQuery('');
    };

    return (
        <Paper
            component="form"
            onSubmit={handleSubmit}
            elevation={0}
            sx={{
                display: 'flex',
                alignItems: 'center',
                p: '4px 6px 4px 16px',
                width: '100%',
                bgcolor: 'rgba(19, 27, 46, 0.85)',
                backdropFilter: 'blur(12px)',
                border: '1px solid rgba(255, 255, 255, 0.12)',
                borderRadius: 3,
                boxShadow: '0 8px 32px rgba(0, 0, 0, 0.25)',
                transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
                '&:hover': {
                    borderColor: 'rgba(99, 102, 241, 0.4)',
                },
                '&:focus-within': {
                    borderColor: '#6366f1',
                    boxShadow: '0 0 0 3px rgba(99, 102, 241, 0.25)',
                },
                ...sx,
            }}
        >
            <SearchIcon sx={{ color: '#06b6d4', mr: 1.5, fontSize: 24, flexShrink: 0 }} />
            <InputBase
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder={placeholder}
                inputProps={{
                    'aria-label': 'Search products across stores',
                }}
                sx={{
                    ml: 0.5,
                    flex: 1,
                    color: '#f8fafc',
                    fontSize: { xs: '0.95rem', sm: '1.05rem' },
                    '& ::placeholder': {
                        color: '#94a3b8',
                        opacity: 1,
                    },
                }}
            />
            {query && (
                <IconButton
                    size="small"
                    onClick={handleClear}
                    aria-label="Clear search input"
                    sx={{ color: '#64748b', mr: 1, '&:hover': { color: '#94a3b8' } }}
                >
                    <ClearIcon fontSize="small" />
                </IconButton>
            )}
            <Button
                type="submit"
                variant="contained"
                aria-label="Execute search"
                disabled={!query.trim()}
                sx={{
                    py: 1.2,
                    px: { xs: 2.5, sm: 3.5 },
                    borderRadius: 2.5,
                    fontWeight: 700,
                    fontSize: { xs: '0.9rem', sm: '0.95rem' },
                    textTransform: 'none',
                    background: 'linear-gradient(135deg, #06b6d4 0%, #3b82f6 100%)',
                    boxShadow: '0 4px 14px rgba(6, 182, 212, 0.3)',
                    flexShrink: 0,
                    '&:hover': {
                        background: 'linear-gradient(135deg, #0891b2 0%, #2563eb 100%)',
                        boxShadow: '0 6px 20px rgba(6, 182, 212, 0.4)',
                    },
                    '&.Mui-disabled': {
                        background: 'rgba(255, 255, 255, 0.08)',
                        color: 'rgba(255, 255, 255, 0.3)',
                    },
                }}
            >
                {buttonText}
            </Button>
        </Paper>
    );
};

export default QuickSearchBar;
