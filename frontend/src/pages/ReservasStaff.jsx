import { useEffect, useState } from 'react';
import api, { mensajeError } from '../api';
import { dinero, hoyISO } from '../validators';
import { Alerta, Badge, Modal } from '../components/ui';
import PagoForm, { METODOS_STAFF } from '../components/PagoForm';

/** Recepción/Admin: reservas del día o todas, con pago, check-in, check-out y cancelación. */
export default function ReservasStaff() {
  const [vista, setVista] = useState('hoy');
  const [reservas, setReservas] = useState([]);
  const [habitaciones, setHabitaciones] = useState([]);
  const [ok, setOk] = useState('');
  const [error, setError] = useState('');
  const [pagando, setPagando] = useState(null);
  const [cargando, setCargando] = useState(true);

  const cargar = async (v = vista) => {
    setCargando(true);
    try {
      const [r, h] = await Promise.all([api.get(v === 'hoy' ? '/reservas/hoy' : '/reservas'), api.get('/habitaciones')]);
      setReservas(r.data);
      setHabitaciones(h.data);
    } catch (e) {
      setError(mensajeError(e));
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => { cargar(vista); }, [vista]);

  const hoy = hoyISO();
  const llegadas = reservas.filter((r) => r.fechaEntrada === hoy && r.estado !== 'CANCELADA').length;
  const salidas = reservas.filter((r) => r.fechaSalida === hoy && r.estado !== 'CANCELADA').length;
  const ocupadas = habitaciones.filter((h) => h.estado === 'OCUPADA').length;
  const pagosPend = reservas.filter((r) => ['PENDIENTE', 'CONFIRMADA'].includes(r.estado) && r.estadoPago !== 'PAGADO').length;

  const accion = async (fn, mensaje) => {
    setOk(''); setError('');
    try {
      await fn();
      setOk(mensaje);
      await cargar();
    } catch (e) {
      setError(mensajeError(e));
    }
  };

  const cancelar = (r) => {
    const motivo = window.prompt('Motivo de la cancelación (mínimo 5 caracteres):');
    if (motivo === null) return;
    if (motivo.trim().length < 5) { setError('El motivo debe tener al menos 5 caracteres.'); return; }
    accion(() => api.post(`/reservas/${r.id}/cancelar`, { motivo: motivo.trim() }), `Reserva ${r.codigo} cancelada.`);
  };

  return (
    <div>
      <h1>Reservas {vista === 'hoy' ? 'del día' : ''}</h1>
      <p className="sub">Gestiona llegadas, salidas, pagos y cancelaciones.</p>

      <div className="kpis">
        <div className="kpi"><small>Llegadas hoy</small><strong>{llegadas}</strong></div>
        <div className="kpi"><small>Salidas hoy</small><strong>{salidas}</strong></div>
        <div className="kpi"><small>Habitaciones ocupadas</small><strong>{ocupadas} / {habitaciones.length}</strong></div>
        <div className="kpi"><small>Pagos pendientes</small><strong>{pagosPend}</strong></div>
      </div>

      <div className="fila" style={{ marginBottom: 14 }}>
        <button className={`btn ${vista === 'hoy' ? '' : 'btn-sec'}`} onClick={() => setVista('hoy')}>Hoy</button>
        <button className={`btn ${vista === 'todas' ? '' : 'btn-sec'}`} onClick={() => setVista('todas')}>Todas las reservas</button>
      </div>

      <Alerta tipo="ok" mensaje={ok} />
      <Alerta mensaje={error} />

      {cargando ? <p>Cargando…</p> : (
        <div className="scroll-x">
          <table className="tabla">
            <thead>
              <tr><th>Código</th><th>Huésped</th><th>Habitación</th><th>Entrada</th><th>Salida</th><th>Estado</th><th>Pago</th><th>Total</th><th style={{ textAlign: 'right' }}>Acción</th></tr>
            </thead>
            <tbody>
              {reservas.length === 0 && <tr><td colSpan={9}>No hay reservas para mostrar.</td></tr>}
              {reservas.map((r) => {
                const activa = ['PENDIENTE', 'CONFIRMADA'].includes(r.estado);
                return (
                  <tr key={r.id}>
                    <td><strong>{r.codigo}</strong></td>
                    <td>{r.huespedNombre}</td>
                    <td>{r.hotel}<br /><small>{r.tipoHabitacion} · {r.numeroHabitacion}</small></td>
                    <td>{r.fechaEntrada}</td>
                    <td>{r.fechaSalida}</td>
                    <td><Badge valor={r.estado} /></td>
                    <td><Badge valor={r.estadoPago} /></td>
                    <td>{dinero(r.precioTotal)}</td>
                    <td>
                      <div className="acciones">
                        {activa && r.estadoPago !== 'PAGADO' && <button className="btn btn-sm" onClick={() => setPagando(r)}>Registrar pago</button>}
                        {r.estado === 'CONFIRMADA' && <button className="btn btn-oscuro btn-sm" onClick={() => accion(() => api.post(`/reservas/${r.id}/check-in`), `Check-in registrado para ${r.codigo}.`)}>Check-in</button>}
                        {r.estado === 'EN_CURSO' && <button className="btn btn-oscuro btn-sm" onClick={() => accion(() => api.post(`/reservas/${r.id}/check-out`), `Check-out registrado para ${r.codigo}.`)}>Check-out</button>}
                        {activa && <button className="btn btn-peligro btn-sm" onClick={() => cancelar(r)}>Cancelar</button>}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {pagando && (
        <Modal titulo={`Registrar pago ${pagando.codigo}`} onCerrar={() => setPagando(null)}>
          <PagoForm
            reserva={pagando}
            metodos={METODOS_STAFF}
            onPagado={() => { setOk(`Pago registrado para ${pagando.codigo}.`); setPagando(null); cargar(); }}
          />
        </Modal>
      )}
    </div>
  );
}
