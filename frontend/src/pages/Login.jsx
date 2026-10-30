import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';
import { mensajeError } from '../api';
import { RE } from '../validators';
import { Alerta, Campo } from '../components/ui';

export default function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [f, setF] = useState({ email: '', password: '' });
  const [errores, setErrores] = useState({});
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    const errs = {};
    if (!f.email.trim()) errs.email = 'El correo es obligatorio';
    else if (!RE.email.test(f.email.trim())) errs.email = 'El correo no tiene un formato válido';
    if (!f.password) errs.password = 'La contraseña es obligatoria';
    setErrores(errs);
    if (Object.keys(errs).length) return;
    setEnviando(true);
    try {
      await login(f.email.trim(), f.password);
      navigate('/');
    } catch (err) {
      setError(mensajeError(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-fondo">
      <div className="tarjeta login-caja">
        <div className="marca" style={{ color: 'var(--oscuro)' }}><span>N</span> Nido</div>
        <h1>Bienvenido de vuelta</h1>
        <p className="sub">Sistema de administración y reserva de habitaciones</p>
        <Alerta mensaje={error} />
        <form onSubmit={enviar} noValidate>
          <Campo label="Correo electrónico" error={errores.email}>
            <input type="email" value={f.email} placeholder="nombre@correo.com" onChange={(e) => setF({ ...f, email: e.target.value })} />
          </Campo>
          <Campo label="Contraseña" error={errores.password}>
            <input type="password" value={f.password} onChange={(e) => setF({ ...f, password: e.target.value })} />
          </Campo>
          <button className="btn btn-oscuro" disabled={enviando}>{enviando ? 'Ingresando…' : 'Iniciar sesión'}</button>
        </form>
        <p style={{ fontSize: 13 }}>¿No tienes cuenta? <Link to="/registro">Regístrate como huésped</Link></p>
        <div className="demo">
          <strong>Usuarios de prueba</strong><br />
          admin@hotel.com / Admin123*<br />
          recepcion@hotel.com / Recepcion123*<br />
          cliente@hotel.com / Cliente123*
        </div>
      </div>
    </div>
  );
}
