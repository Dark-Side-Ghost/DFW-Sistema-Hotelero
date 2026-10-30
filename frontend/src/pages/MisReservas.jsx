import { useEffect, useState } from 'react';
import api, { mensajeError, erroresCampos } from '../api';
import { dinero, hoyISO, noches as calcNoches, sumarDias } from '../validators';
import { Alerta, Badge, Campo, Modal } from '../components/ui';
import PagoForm, { METODOS_CLIENTE } from '../components/PagoForm';

const TIPOS_RECLAMO = [
  { value: 'LIMPIEZA', label: 'Limpieza' }, { value: 'FACTURACION', label: 'Facturación' },
  { value: 'INFRAESTRUCTURA', label: 'Infraestructura' }, { value: 'ATENCION', label: 'Atención al cliente' },
];

/** Reservas del usuario: pagar, modificar fechas, cancelar y reportar un problema. */
export default function MisReservas() {
  const [lista, setLista] = useState([]);
  const [modal, setModal] = useState(null); // { tipo, r }
  const [ok, setOk] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(true);

  const cargar = async () => {
    try {
      const { data } = await api.get('/reservas/mias');
      setLista(data);
    } catch (e) {
      setError(mensajeError(e));
    } finally {
      setCargando(false);
    }
  };
  useEffect(() => { cargar(); }, []);

  const cerrar = (mensaje) => {
    setModal(null);
    if (mensaje) { setOk(mensaje); setError(''); }
    cargar();
  };

  return (
    <div>
      <h1>Mis reservas</h1>
      <p className="sub">Consulta, paga, modifica o cancela tus reservas.</p>
      <Alerta tipo="ok" mensaje={ok} />
      <Alerta mensaje={error} />
      {cargando && <p>Cargando…</p>}
      {!cargando && lista.length === 0 && <div className="tarjeta">Aún no tienes reservas.</div>}

      {lista.map((r) => {
        const activa = ['PENDIENTE', 'CONFIRMADA'].includes(r.estado);
        const saldo = Number(r.precioTotal) - Number(r.montoPagado);
        return (
          <div key={r.id} className="tarjeta hab-card">
            <div>
              <small>{r.codigo}</small>
              <h3>{r.hotel} · {r.tipoHabitacion} {r.numeroHabitacion}</h3>
              <small>{r.fechaEntrada} → {r.fechaSalida} · {r.noches} noche(s) · {r.numeroHuespedes} huésped(es)</small>
              <div style={{ marginTop: 6 }}><Badge valor={r.estado} /> <Badge valor={r.estadoPago} /></div>
              {r.estado === 'CANCELADA' && (
                <small style={{ display: 'block', marginTop: 6 }}>
                  Motivo: {r.motivoCancelacion} · Reembolso: {dinero(r.montoReembolso)}
                </small>
              )}
            </div>
            <div className="precio">
              <strong>{dinero(r.precioTotal)}</strong>
              <div className="fila" style={{ justifyContent: 'flex-end' }}>
                {activa && saldo > 0 && <button className="btn btn-sm" onClick={() => setModal({ tipo: 'pago', r })}>Pagar</button>}
                {activa && <button className="btn btn-sec btn-sm" onClick={() => setModal({ tipo: 'modificar', r })}>Modificar</button>}
                {activa && <button className="btn btn-peligro btn-sm" onClick={() => setModal({ tipo: 'cancelar', r })}>Cancelar</button>}
                <button className="btn btn-sec btn-sm" onClick={() => setModal({ tipo: 'reclamo', r })}>Reportar problema</button>
              </div>
            </div>
          </div>
        );
      })}

      {modal?.tipo === 'pago' && (
        <Modal titulo={`Pagar ${modal.r.codigo}`} onCerrar={() => setModal(null)}>
          <PagoForm reserva={modal.r} metodos={METODOS_CLIENTE} onPagado={() => cerrar('Pago registrado correctamente.')} />
        </Modal>
      )}
      {modal?.tipo === 'modificar' && <ModificarForm r={modal.r} onCerrar={cerrar} />}
      {modal?.tipo === 'cancelar' && <CancelarForm r={modal.r} onCerrar={cerrar} />}
      {modal?.tipo === 'reclamo' && <ReclamoForm r={modal.r} onCerrar={cerrar} />}
    </div>
  );
}

function ModificarForm({ r, onCerrar }) {
  const [f, setF] = useState({ fechaEntrada: r.fechaEntrada, fechaSalida: r.fechaSalida, numeroHuespedes: r.numeroHuespedes });
  const [errores, setErrores] = useState({});
  const [apiErr, setApiErr] = useState({});
  const [error, setError] = useState('');

  const enviar = async (e) => {
    e.preventDefault();
    setError(''); setApiErr({});
    const errs = {};
    if (!f.fechaEntrada) errs.fechaEntrada = 'La fecha de entrada es obligatoria';
    else if (f.fechaEntrada < hoyISO()) errs.fechaEntrada = 'La fecha de entrada no puede ser anterior a hoy';
    if (!f.fechaSalida) errs.fechaSalida = 'La fecha de salida es obligatoria';
    else if (f.fechaEntrada && f.fechaSalida <= f.fechaEntrada) errs.fechaSalida = 'La salida debe ser posterior a la entrada';
    else if (f.fechaEntrada && calcNoches(f.fechaEntrada, f.fechaSalida) > 30) errs.fechaSalida = 'La estadía máxima es de 30 noches';
    const n = Number(f.numeroHuespedes);
    if (!Number.isInteger(n) || n < 1 || n > 10) errs.numeroHuespedes = 'Debe ser un entero entre 1 y 10';
    setErrores(errs);
    if (Object.keys(errs).length) return;
    try {
      await api.put(`/reservas/${r.id}`, { ...f, numeroHuespedes: n });
      onCerrar('La reserva se modificó correctamente.');
    } catch (err) {
      setError(mensajeError(err)); setApiErr(erroresCampos(err));
    }
  };

  return (
    <Modal titulo={`Modificar ${r.codigo}`} onCerrar={() => onCerrar()}>
      <form onSubmit={enviar} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <Alerta mensaje={error} errores={apiErr} />
        <Campo label="Fecha de entrada" error={errores.fechaEntrada}><input type="date" min={hoyISO()} value={f.fechaEntrada} onChange={(e) => setF({ ...f, fechaEntrada: e.target.value })} /></Campo>
        <Campo label="Fecha de salida" error={errores.fechaSalida}><input type="date" min={f.fechaEntrada ? sumarDias(f.fechaEntrada, 1) : hoyISO()} value={f.fechaSalida} onChange={(e) => setF({ ...f, fechaSalida: e.target.value })} /></Campo>
        <Campo label="Número de huéspedes" error={errores.numeroHuespedes}><input type="number" min="1" max="10" value={f.numeroHuespedes} onChange={(e) => setF({ ...f, numeroHuespedes: e.target.value })} /></Campo>
        <button className="btn">Guardar cambios</button>
      </form>
    </Modal>
  );
}

function CancelarForm({ r, onCerrar }) {
  const [motivo, setMotivo] = useState('');
  const [errorCampo, setErrorCampo] = useState('');
  const [error, setError] = useState('');

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    if (motivo.trim().length < 5) { setErrorCampo('El motivo debe tener al menos 5 caracteres'); return; }
    setErrorCampo('');
    try {
      const { data } = await api.post(`/reservas/${r.id}/cancelar`, { motivo: motivo.trim() });
      onCerrar(`Reserva cancelada. Reembolso: ${dinero(data.montoReembolso)}.`);
    } catch (err) {
      setError(mensajeError(err));
    }
  };

  return (
    <Modal titulo={`Cancelar ${r.codigo}`} onCerrar={() => onCerrar()}>
      <p className="sub">
        Política: cancelando con {r.politicaDias} día(s) o más de anticipación no hay cargo; después se cobra el {r.politicaPorcentaje}% del total.
      </p>
      <form onSubmit={enviar} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <Alerta mensaje={error} />
        <Campo label="Motivo de la cancelación" error={errorCampo}>
          <textarea rows={3} maxLength={500} value={motivo} onChange={(e) => setMotivo(e.target.value)} />
        </Campo>
        <button className="btn btn-peligro">Cancelar reserva</button>
      </form>
    </Modal>
  );
}

function ReclamoForm({ r, onCerrar }) {
  const [f, setF] = useState({ tipo: 'ATENCION', descripcion: '' });
  const [errorCampo, setErrorCampo] = useState('');
  const [error, setError] = useState('');

  const enviar = async (e) => {
    e.preventDefault();
    setError('');
    if (f.descripcion.trim().length < 10) { setErrorCampo('Describe el problema con al menos 10 caracteres'); return; }
    setErrorCampo('');
    try {
      await api.post('/reclamos', { idReserva: r.id, tipo: f.tipo, descripcion: f.descripcion.trim() });
      onCerrar('Tu reclamo fue enviado. El personal del hotel lo atenderá pronto.');
    } catch (err) {
      setError(mensajeError(err));
    }
  };

  return (
    <Modal titulo={`Reportar problema · ${r.codigo}`} onCerrar={() => onCerrar()}>
      <form onSubmit={enviar} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <Alerta mensaje={error} />
        <Campo label="Tipo">
          <select value={f.tipo} onChange={(e) => setF({ ...f, tipo: e.target.value })}>
            {TIPOS_RECLAMO.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
          </select>
        </Campo>
        <Campo label="Descripción" error={errorCampo}>
          <textarea rows={4} maxLength={1000} value={f.descripcion} onChange={(e) => setF({ ...f, descripcion: e.target.value })} />
        </Campo>
        <button className="btn">Enviar reclamo</button>
      </form>
    </Modal>
  );
}
