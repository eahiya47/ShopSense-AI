import React, { useState } from 'react';
import { Link as RouterLink, useNavigate, useLocation } from 'react-router-dom';
import {
    Box,
    Container,
    Typography,
    TextField,
    Button,
    Card,
    CardContent,
    Alert,
    InputAdornment,
    IconButton,
    Link,
    CircularProgress,
} from '@mui/material';
import {
    Email,
    Lock,
    Visibility,
    VisibilityOff,
    AutoAwesome,
    LoginOutlined,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const [formData, setFormData] = useState({
        email: '',
        password: '',
    });
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const from = location.state?.from?.pathname || '/dashboard';

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (error) setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.email || !formData.password) {
            setError('Please fill in all required fields.');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await login(formData.email, formData.password);
            navigate(from, { replace: true });
        } catch (err) {
            console.error('Login error:', err);
            const errMsg = err.response?.data?.message || 'Invalid email or password. Please try again.';
            setError(errMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="xs" sx={{ py: 10 }}>
            <Box sx={{ textCenter: 'center', mb: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Box
                    sx={{
                        width: 48,
                        height: 48,
                        borderRadius: 3,
                        background: 'linear-gradient(135deg, #6366f1 0%, #a855f7 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mb: 2,
                    }}
                >
                    <AutoAwesome sx={{ color: '#fff', fontSize: 28 }} />
                </Box>
                <Typography variant="h4" sx={{ fontWeight: 800, color: '#fff', mb: 1 }}>
                    Welcome Back
                </Typography>
                <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                    Sign in to your ShopSense AI account
                </Typography>
            </Box>

            <Card sx={{ p: 1 }}>
                <CardContent>
                    {error && (
                        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
                            {error}
                        </Alert>
                    )}

                    <Box component="form" onSubmit={handleSubmit} noValidate>
                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            id="email"
                            label="Email Address"
                            name="email"
                            autoComplete="email"
                            autoFocus
                            value={formData.email}
                            onChange={handleChange}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Email sx={{ color: '#94a3b8' }} />
                                    </InputAdornment>
                                ),
                            }}
                            sx={{
                                '& .MuiInputBase-input:-webkit-autofill': {
                                    WebkitBoxShadow: '0 0 0 1000px #131b2e inset',
                                    WebkitTextFillColor: '#f8fafc',
                                    caretColor: '#f8fafc',
                                    transition: 'background-color 5000s ease-in-out 0s',
                                },
                            }}
                        />

                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="password"
                            label="Password"
                            type={showPassword ? 'text' : 'password'}
                            id="password"
                            autoComplete="current-password"
                            value={formData.password}
                            onChange={handleChange}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Lock sx={{ color: '#94a3b8' }} />
                                    </InputAdornment>
                                ),
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <IconButton
                                            onClick={() => setShowPassword(!showPassword)}
                                            edge="end"
                                            sx={{ color: '#94a3b8' }}
                                        >
                                            {showPassword ? <VisibilityOff /> : <Visibility />}
                                        </IconButton>
                                    </InputAdornment>
                                ),
                            }}
                            sx={{
                                '& .MuiInputBase-input:-webkit-autofill': {
                                    WebkitBoxShadow: '0 0 0 1000px #131b2e inset',
                                    WebkitTextFillColor: '#f8fafc',
                                    caretColor: '#f8fafc',
                                    transition: 'background-color 5000s ease-in-out 0s',
                                },
                            }}
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            size="large"
                            disabled={loading}
                            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <LoginOutlined />}
                            sx={{
                                mt: 3,
                                mb: 2,
                                py: 1.5,
                                fontSize: '1rem',
                                background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                            }}
                        >
                            {loading ? 'Signing in...' : 'Sign In'}
                        </Button>

                        <Box sx={{ textAlign: 'center', mt: 2 }}>
                            <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                                Don't have an account?{' '}
                                <Link component={RouterLink} to="/register" underline="hover" sx={{ color: '#818cf8', fontWeight: 600 }}>
                                    Register now
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </CardContent>
            </Card>
        </Container>
    );
};

export default LoginPage;
