import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth';
import Layout from './components/Layout';
import Login from './pages/Login';
import Registro from './pages/Registro';
import Estadisticas from './pages/Estadisticas';
import Hoteles from './pages/Hoteles';
import Habitaciones from './pages/Habitaciones';
import Tipos from './pages/Tipos';
import Servicios from './pages/Servicios';
import Politicas from './pages/Politicas';
import Usuarios from './pages/Usuarios';
import ReservasStaff from './pages/ReservasStaff';
import Reclamos from './pages/Reclamos';
import Reservar from './pages/Reservar';
import MisReservas from './pages/MisReservas';

// Si no hay sesión, manda al login
function Protegida() {
  const { user } = useAuth();
  return user ? <Layout /> : <Navigate to="/login" replace />;
}

// Si el rol no tiene permiso, vuelve al inicio
function Rol({ roles, children }) {
  const { user } = useAuth();
  return roles.includes(user.rol) ? children : <Navigate to="/" replace />;
}

// Cada rol aterriza en su pantalla principal
function Inicio() {
  const { user } = useAuth();
  const destino = { ADMIN: '/estadisticas', RECEPCIONISTA: '/recepcion', CLIENTE: '/reservar' }[user.rol] || '/login';
  return <Navigate to={destino} replace />;
}

const ADMIN = ['ADMIN'];
const STAFF = ['ADMIN', 'RECEPCIONISTA'];

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />
      <Route element={<Protegida />}>
        <Route index element={<Inicio />} />
        <Route path="estadisticas" element={<Rol roles={ADMIN}><Estadisticas /></Rol>} />
        <Route path="hoteles" element={<Rol roles={ADMIN}><Hoteles /></Rol>} />
        <Route path="habitaciones" element={<Rol roles={ADMIN}><Habitaciones /></Rol>} />
        <Route path="tipos" element={<Rol roles={ADMIN}><Tipos /></Rol>} />
        <Route path="servicios" element={<Rol roles={ADMIN}><Servicios /></Rol>} />
        <Route path="politicas" element={<Rol roles={ADMIN}><Politicas /></Rol>} />
        <Route path="usuarios" element={<Rol roles={ADMIN}><Usuarios /></Rol>} />
        <Route path="recepcion" element={<Rol roles={STAFF}><ReservasStaff /></Rol>} />
        <Route path="reclamos" element={<Rol roles={STAFF}><Reclamos /></Rol>} />
        <Route path="reservar" element={<Reservar />} />
        <Route path="mis-reservas" element={<MisReservas />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
