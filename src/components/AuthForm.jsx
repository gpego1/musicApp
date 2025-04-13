import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const AuthForm = ({ isLogin, onSubmit }) => {
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        senha: ''
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(isLogin ? { email: formData.email, senha: formData.senha } : formData);
    };

    return (
        <form onSubmit={handleSubmit} className="max-w-md mx-auto mt-8">
            {!isLogin && (
                <div className="mb-4">
                    <label htmlFor="name" className="block text-gray-700 mb-2">Nome</label>
                    <input
                        type="text"
                        id="name"
                        name="name"
                        value={formData.name}
                        onChange={handleChange}
                        className="w-full px-3 py-2 border rounded-md"
                        required
                    />
                </div>
            )}

            <div className="mb-4">
                <label htmlFor="email" className="block text-gray-700 mb-2">Email</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded-md"
                    required
                />
            </div>

            <div className="mb-6">
                <label htmlFor="senha" className="block text-gray-700 mb-2">Senha</label>
                <input
                    type="password"
                    id="senha"
                    name="senha"
                    value={formData.senha}
                    onChange={handleChange}
                    className="w-full px-3 py-2 border rounded-md"
                    required
                />
            </div>

            <button
                type="submit"
                className="w-full bg-blue-500 text-white py-2 px-4 rounded-md hover:bg-blue-600"
            >
                {isLogin ? 'Login' : 'Registrar'}
            </button>
        </form>
    );
};

export default AuthForm;