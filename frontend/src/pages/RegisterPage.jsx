import React, { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
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
    Person,
    Email,
    Lock,
    Visibility,
    VisibilityOff,
    AutoAwesome,
    PersonAddOutlined,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';

const RegisterPage = () => {
    const { register } = useAuth();
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
    });
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        if (error) setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const { name, email, password, confirmPassword } = formData;

        if (!name || !email || !password || !confirmPassword) {
            setError('Please fill in all required fields.');
            return;
        }

        if (password.length < 6) {
            setError('Password must be at least 6 characters long.');
            return;
        }

        if (password !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        setLoading(true);
        setError('');

        try {
            await register(name, email, password);
            navigate('/dashboard');
        } catch (err) {
            console.error('Registration error:', err);
            const errMsg = err.response?.data?.message || 'Registration failed. Email may already be in use.';
            setError(errMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="xs" sx={{ py: 8 }}>
            <Box sx={{ textCenter: 'center', mb: 3, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <Box
                    sx={{
                        width: 48,
                        height: 48,
                        borderRadius: 3,
                        background: 'linear-gradient(135deg, #a855f7 0%, #ec4899 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mb: 2,
                    }}
                >
                    <AutoAwesome sx={{ color: '#fff', fontSize: 28 }} />
                </Box>
                <Typography variant="h4" sx={{ fontWeight: 800, color: '#fff', mb: 1 }}>
                    Create Account
                </Typography>
                <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                    Join ShopSense AI to unlock smart product comparison
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
                            id="name"
                            label="Full Name"
                            name="name"
                            autoComplete="name"
                            autoFocus
                            value={formData.name}
                            onChange={handleChange}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Person sx={{ color: '#94a3b8' }} />
                                    </InputAdornment>
                                ),
                            }}
                        />

                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            id="email"
                            label="Email Address"
                            name="email"
                            autoComplete="email"
                            value={formData.email}
                            onChange={handleChange}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Email sx={{ color: '#94a3b8' }} />
                                    </InputAdornment>
                                ),
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
                            autoComplete="new-password"
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
                        />

                        <TextField
                            margin="normal"
                            required
                            fullWidth
                            name="confirmPassword"
                            label="Confirm Password"
                            type={showPassword ? 'text' : 'password'}
                            id="confirmPassword"
                            autoComplete="new-password"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <Lock sx={{ color: '#94a3b8' }} />
                                    </InputAdornment>
                                ),
                            }}
                        />

                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            size="large"
                            disabled={loading}
                            startIcon={loading ? <CircularProgress size={20} color="inherit" /> : <PersonAddOutlined />}
                            sx={{
                                mt: 3,
                                mb: 2,
                                py: 1.5,
                                fontSize: '1rem',
                                background: 'linear-gradient(135deg, #a855f7 0%, #ec4899 100%)',
                            }}
                        >
                            {loading ? 'Creating Account...' : 'Register'}
                        </Button>

                        <Box sx={{ textAlign: 'center', mt: 2 }}>
                            <Typography variant="body2" sx={{ color: '#94a3b8' }}>
                                Already have an account?{' '}
                                <Link component={RouterLink} to="/login" underline="hover" sx={{ color: '#818cf8', fontWeight: 600 }}>
                                    Sign in
                                </Link>
                            </Typography>
                        </Box>
                    </Box>
                </CardContent>
            </Card>
        </Container>
    );
};

export default RegisterPage;
