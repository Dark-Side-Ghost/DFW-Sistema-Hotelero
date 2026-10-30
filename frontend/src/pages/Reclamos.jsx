import { useEffect, useState } from 'react';
import api, { mensajeError } from '../api';
import { useAuth } from '../auth';
import { Alerta, Badge } from '../components/ui';

const TIPOS = { LIMPIEZA: 'Limpieza', FACTURACION: 'Facturación', INFRAESTRUCTURA: 'Infraestructura', ATENCION: 'Atención' };
const ESTADOS = ['PENDIENTE', 'EN_PROCESO', 'RESUELTO', 'RECHAZADO'];

export default function Reclamos() {
  const { user } = useAuth();
  const [lista, setLista] = useState([]);
  const [sel, setSel] = useState(null);
  const [ok, setOk] = useState('');
  const [error, setError] = useState('');

  const cargar = async () => {
    try {
      const { data } = await api.get('/reclamos');
      setLista(data);
      setSel((s) => (s ? data.find((x) => x.id === s.id) || null : data[0] || null));
    } catch (e) {
      setError(mensajeError(e));
    }
  };
  useEffect(() => { cargar(); }, []);

  const cambiar = async (estado) => {
    setOk(''); setError('');
    try {
      await api.put(`/reclamos/${sel.id}/estado`, { estado });
      setOk('El estado del reclamo se actualizó.');
      cargar();
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  const eliminar = async () => {
    if (!window.confirm('¿Eliminar este reclamo?')) return;
    try {
      await api.delete(`/reclamos/${sel.id}`);
      setOk('Reclamo eliminado.');
      setSel(null);
      cargar();
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  return (
    <div>
      <h1>Reclamos y solicitudes</h1>
      <p className="sub">Da seguimiento a los reportes de los huéspedes.</p>
      <Alerta tipo="ok" mensaje={ok} />
      <Alerta mensaje={error} />
      <div className="grid-2" style={{ alignItems: 'start' }}>
        <div>
          {lista.length === 0 && <div className="tarjeta">No hay reclamos registrados.</div>}
          {lista.map((r) => (
            <div key={r.id} className="tarjeta" style={{ cursor: 'pointer', borderColor: sel?.id === r.id ? 'var(--dorado)' : undefined }} onClick={() => setSel(r)}>
              <div className="fila espacio"><strong>{TIPOS[r.tipo]} · {r.codigoReserva}</strong><Badge valor={r.estado} /></div>
              <small>{r.huesped} · {r.hotel}</small>
            </div>
          ))}
        </div>
        {sel && (
          <div className="tarjeta">
            <small>RECLAMO #{sel.id} · {String(sel.fechaCreacion).slice(0, 10)}</small>
            <h3>{TIPOS[sel.tipo]} — {sel.codigoReserva}</h3>
            <p>{sel.descripcion}</p>
            <p className="sub">Huésped: {sel.huesped}<br />Atendido por: {sel.atendidoPor || 'sin asignar'}</p>
            <strong style={{ fontSize: 13 }}>Actualizar estado</strong>
            <div className="fila" style={{ marginTop: 6 }}>
              {ESTADOS.map((e) => (
                <button key={e} className={`btn btn-sm ${sel.estado === e ? 'btn-oscuro' : 'btn-sec'}`} onClick={() => cambiar(e)}>
                  {e === 'PENDIENTE' ? 'Nuevo' : e === 'EN_PROCESO' ? 'En proceso' : e === 'RESUELTO' ? 'Resuelto' : 'Rechazado'}
                </button>
              ))}
            </div>
            {user.rol === 'ADMIN' && <button className="btn btn-peligro btn-sm" style={{ marginTop: 14 }} onClick={eliminar}>Eliminar reclamo</button>}
          </div>
        )}
      </div>
    </div>
  );
}
