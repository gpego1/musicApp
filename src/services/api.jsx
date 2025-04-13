
import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
});

export const registerUser = async (userData) => {
    try {
        const response = await api.post('/auth/register', userData);
        return response.data;
    } catch (error) {
        throw error.response.data;
    }
};

export const loginUser = async (authData) => {
    try {
        const response = await api.post('/auth/login', authData);
        return response.data;
    } catch (error) {
        throw error.response.data;
    }
};