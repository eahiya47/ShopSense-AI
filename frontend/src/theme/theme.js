import { createTheme } from '@mui/material/styles';

const theme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            main: '#6366f1',
            light: '#818cf8',
            dark: '#4f46e5',
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#06b6d4',
            light: '#22d3ee',
            dark: '#0891b2',
        },
        background: {
            default: '#090d16',
            paper: '#131b2e',
        },
        text: {
            primary: '#f8fafc',
            secondary: '#94a3b8',
        },
    },
    typography: {
        fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
        h1: {
            fontFamily: '"Outfit", sans-serif',
            fontWeight: 700,
        },
        h2: {
            fontFamily: '"Outfit", sans-serif',
            fontWeight: 700,
        },
        h3: {
            fontFamily: '"Outfit", sans-serif',
            fontWeight: 600,
        },
        h4: {
            fontFamily: '"Outfit", sans-serif',
            fontWeight: 600,
        },
        h5: {
            fontFamily: '"Outfit", sans-serif',
            fontWeight: 600,
        },
        h6: {
            fontFamily: '"Outfit", sans-serif',
            fontWeight: 600,
        },
        button: {
            textTransform: 'none',
            fontWeight: 600,
        },
    },
    shape: {
        borderRadius: 12,
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    padding: '10px 24px',
                    boxShadow: 'none',
                    '&:hover': {
                        boxShadow: '0 4px 12px rgba(99, 102, 241, 0.3)',
                    },
                },
                containedPrimary: {
                    background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    backgroundImage: 'none',
                    backgroundColor: 'rgba(19, 27, 46, 0.8)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.08)',
                    borderRadius: 16,
                },
            },
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    backgroundImage: 'none',
                },
            },
        },
        MuiTextField: {
            styleOverrides: {
                root: {
                    '& .MuiOutlinedInput-root': {
                        borderRadius: 8,
                        '& fieldset': {
                            borderColor: 'rgba(255, 255, 255, 0.15)',
                        },
                        '&:hover fieldset': {
                            borderColor: '#6366f1',
                        },
                    },
                },
            },
        },
    },
});

export default theme;
