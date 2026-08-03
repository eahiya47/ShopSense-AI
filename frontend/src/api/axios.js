import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to attach Authorization Bearer Token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor to handle 401 Unauthorized globally
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Clear token if expired or invalid
            localStorage.removeItem('token');
            localStorage.removeItem('user');
        }
        return Promise.reject(error);
    }
);

export default api;
