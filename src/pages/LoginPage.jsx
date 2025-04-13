// src/pages/LoginPage.jsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { loginUser } from '../services/api';
import AuthForm from '../components/AuthForm';

const LoginPage = () => {
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleLogin = async (authData) => {
        try {
            const token = await loginUser(authData);
            localStorage.setItem('token', token);
            navigate('/');
        } catch (err) {
            setError('Credenciais inválidas');
        }
    };

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col">
            <div className="container mx-auto px-4 py-8 flex-grow">
                <h1 className="text-3xl font-bold text-center mb-8">Login</h1>
                {error && <div className="mb-4 p-2 bg-red-100 text-red-700 rounded">{error}</div>}
                <AuthForm isLogin={true} onSubmit={handleLogin} />
                <p className="mt-4 text-center">
                    Não tem uma conta?{' '}
                    <a href="/register" className="text-blue-500 hover:underline">Registre-se</a>
                </p>
            </div>
        </div>
    );
};

export default LoginPage;