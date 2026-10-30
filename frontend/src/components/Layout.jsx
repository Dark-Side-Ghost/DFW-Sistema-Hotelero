import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

const MENU = {
  ADMIN: [
    ['/estadisticas', 'Estadísticas'], ['/hoteles', 'Hoteles y sucursales'], ['/habitaciones', 'Habitaciones'],
    ['/tipos', 'Tipos de habitación'], ['/servicios', 'Servicios adicionales'], ['/politicas', 'Políticas de cancelación'],
    ['/usuarios', 'Usuarios'], ['/recepcion', 'Reservas'], ['/reclamos', 'Reclamos'], ['/reservar', 'Nueva reserva'],
  ],
  RECEPCIONISTA: [['/recepcion', 'Reservas del día'], ['/reclamos', 'Reclamos y solicitudes'], ['/reservar', 'Nueva reserva']],
  CLIENTE: [['/reservar', 'Buscar y reservar'], ['/mis-reservas', 'Mis reservas']],
};

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const salir = () => { logout(); navigate('/login'); };

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="marca"><span>N</span> Nido</div>
        <nav>
          {(MENU[user.rol] || []).map(([ruta, texto]) => (
            <NavLink key={ruta} to={ruta}>{texto}</NavLink>
          ))}
        </nav>
        <div className="usuario-box">
          <strong>{user.nombres} {user.apellidos}</strong>
          <small>{user.rol}</small>
          <button className="btn btn-sec btn-sm" onClick={salir}>Cerrar sesión</button>
        </div>
      </aside>
      <main className="contenido"><Outlet /></main>
    </div>
  );
}
