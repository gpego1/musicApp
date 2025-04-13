// src/pages/RegisterPage.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { registerUser } from '../services/api';
import AuthForm from '../components/AuthForm';

const RegisterPage = () => {
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleRegister = async (userData) => {
        try {
            const response = await registerUser(userData);
            console.log('Usuário registrado:', response);
            navigate('/login');
        } catch (err) {
            setError(err.message || 'Erro ao registrar usuário');
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col">
            <div className="container mx-auto px-4 py-8 flex-grow">
                <h1 className="text-3xl font-bold text-center mb-8">Registrar Nova Conta</h1>
                {error && <div className="mb-4 p-2 bg-red-100 text-red-700 rounded">{error}</div>}
                <AuthForm isLogin={false} onSubmit={handleRegister} />
            </div>
        </div>
    );
};

export default RegisterPage;