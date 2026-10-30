import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';
import { mensajeError, erroresCampos } from '../api';
import { RE } from '../validators';
import { Alerta, Campo } from '../components/ui';

export default function Registro() {
  const { user, registro } = useAuth();
  const navigate = useNavigate();
  const [f, setF] = useState({ nombres: '', apellidos: '', email: '', password: '', confirmar: '' });
  const [errores, setErrores] = useState({});
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (user) return <Navigate to="/" replace />;
  const cambiar = (k, v) => setF((x) => ({ ...x, [k]: v }));

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    const errs = {};
    if (!f.nombres.trim()) errs.nombres = 'Los nombres son obligatorios';
    else if (!RE.nombre.test(f.nombres.trim())) errs.nombres = 'Solo se permiten letras y espacios';
    if (!f.apellidos.trim()) errs.apellidos = 'Los apellidos son obligatorios';
    else if (!RE.nombre.test(f.apellidos.trim())) errs.apellidos = 'Solo se permiten letras y espacios';
    if (!f.email.trim()) errs.email = 'El correo es obligatorio';
    else if (!RE.email.test(f.email.trim())) errs.email = 'El correo no tiene un formato válido';
    if (!RE.clave.test(f.password)) errs.password = 'Mínimo 8 caracteres, con al menos una letra y un número';
    if (f.confirmar !== f.password) errs.confirmar = 'Las contraseñas no coinciden';
    setErrores(errs);
    if (Object.keys(errs).length) return;
    setEnviando(true);
    try {
      await registro({ nombres: f.nombres.trim(), apellidos: f.apellidos.trim(), email: f.email.trim(), password: f.password });
      navigate('/');
    } catch (err) {
      setError(mensajeError(err));
      setErrores(erroresCampos(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-fondo">
      <div className="tarjeta login-caja">
        <h1>Crear cuenta</h1>
        <p className="sub">Regístrate para reservar habitaciones</p>
        <Alerta mensaje={error} />
        <form onSubmit={enviar} noValidate>
          <Campo label="Nombres" error={errores.nombres}><input value={f.nombres} onChange={(e) => cambiar('nombres', e.target.value)} /></Campo>
          <Campo label="Apellidos" error={errores.apellidos}><input value={f.apellidos} onChange={(e) => cambiar('apellidos', e.target.value)} /></Campo>
          <Campo label="Correo electrónico" error={errores.email}><input type="email" value={f.email} onChange={(e) => cambiar('email', e.target.value)} /></Campo>
          <Campo label="Contraseña" error={errores.password}><input type="password" value={f.password} onChange={(e) => cambiar('password', e.target.value)} /></Campo>
          <Campo label="Confirmar contraseña" error={errores.confirmar}><input type="password" value={f.confirmar} onChange={(e) => cambiar('confirmar', e.target.value)} /></Campo>
          <button className="btn btn-oscuro" disabled={enviando}>{enviando ? 'Creando…' : 'Crear cuenta'}</button>
        </form>
        <p style={{ fontSize: 13 }}>¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </div>
    </div>
  );
}
