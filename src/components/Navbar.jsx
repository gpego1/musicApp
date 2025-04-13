import { Link } from 'react-router-dom';

const Navbar = () => {
    const isAuthenticated = !!localStorage.getItem('token');

    const handleLogout = () => {
        localStorage.removeItem('token');
        window.location.href = '/login';
    };

    return (
        <nav className="bg-blue-600 text-white p-4">
            <div className="container mx-auto flex justify-between items-center">
                <Link to="/" className="text-xl font-bold">Music App</Link>

                <div className="space-x-4">
                    {isAuthenticated ? (
                        <button onClick={handleLogout} className="hover:underline">Logout</button>
                    ) : (
                        <>
                            <Link to="/login" className="hover:underline">Login</Link>
                            <Link to="/register" className="hover:underline">Registrar</Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;