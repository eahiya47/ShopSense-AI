import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import api from '../api/axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token') || null);
    const [loading, setLoading] = useState(true);

    const logout = useCallback(() => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    }, []);

    useEffect(() => {
        const initializeAuth = async () => {
            const storedToken = localStorage.getItem('token');
            if (storedToken) {
                try {
                    const response = await api.get('/users/profile');
                    setUser(response.data);
                    setToken(storedToken);
                } catch (error) {
                    console.error('Failed to load user profile from stored token:', error);
                    logout();
                }
            } else {
                setToken(null);
                setUser(null);
            }
            setLoading(false);
        };

        initializeAuth();
    }, [logout]);

    useEffect(() => {
        const handleUnauthorized = () => {
            logout();
        };
        window.addEventListener('auth:unauthorized', handleUnauthorized);
        return () => {
            window.removeEventListener('auth:unauthorized', handleUnauthorized);
        };
    }, [logout]);

    const login = async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        const { token: jwtToken, ...userData } = response.data;

        localStorage.setItem('token', jwtToken);
        localStorage.setItem('user', JSON.stringify(userData));

        setToken(jwtToken);

        // Fetch full profile to keep state up to date
        try {
            const profileRes = await api.get('/users/profile', {
                headers: { Authorization: `Bearer ${jwtToken}` }
            });
            setUser(profileRes.data);
        } catch {
            setUser(userData);
        }

        return response.data;
    };

    const register = async (name, email, password) => {
        const response = await api.post('/auth/register', { name, email, password });
        const { token: jwtToken, ...userData } = response.data;

        localStorage.setItem('token', jwtToken);
        localStorage.setItem('user', JSON.stringify(userData));

        setToken(jwtToken);

        try {
            const profileRes = await api.get('/users/profile', {
                headers: { Authorization: `Bearer ${jwtToken}` }
            });
            setUser(profileRes.data);
        } catch {
            setUser(userData);
        }

        return response.data;
    };

    const value = {
        user,
        token,
        loading,
        isAuthenticated: !!token && !!user,
        login,
        register,
        logout,
        setUser
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
