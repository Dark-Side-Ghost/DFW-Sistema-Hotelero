import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api, { mensajeError, erroresCampos } from '../api';
import { useAuth } from '../auth';
import { DOCS, RE, dinero, edad, hoyISO, noches as calcNoches, sumarDias } from '../validators';
import { Alerta, Badge, Campo } from '../components/ui';
import PagoForm, { METODOS_CLIENTE, METODOS_STAFF } from '../components/PagoForm';

const PASOS = ['Buscar', 'Elegir habitación', 'Datos del huésped', 'Resumen', 'Pago', 'Confirmación'];

/** Flujo de reserva en 6 pasos: buscar -> elegir -> datos -> resumen -> pago -> confirmación */
export default function Reservar() {
  const { user } = useAuth();
  const esStaff = user.rol !== 'CLIENTE';
  const [paso, setPaso] = useState(1);
  const [tipos, setTipos] = useState([]);
  const [servicios, setServicios] = useState([]);
  const [b, setB] = useState({ ciudad: '', entrada: '', salida: '', huespedes: 1, tipoId: '' });
  const [resultados, setResultados] = useState([]);
  const [hab, setHab] = useState(null);
  const [h, setH] = useState({
    nombreCompleto: esStaff ? '' : `${user.nombres} ${user.apellidos}`, fechaNacimiento: '', tipoDocumento: 'DUI',
    numeroDocumento: '', telefono: '', correoElectronico: esStaff ? '' : user.email,
  });
  const [extra, setExtra] = useState({}); // idServicio -> cantidad
  const [reserva, setReserva] = useState(null);
  const [errores, setErrores] = useState({});
  const [erroresApi, setErroresApi] = useState({});
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  useEffect(() => {
    api.get('/tipos-habitacion').then((r) => setTipos(r.data)).catch(() => {});
    api.get('/servicios').then((r) => setServicios(r.data)).catch(() => {});
  }, []);

  const nochesN = b.entrada && b.salida ? calcNoches(b.entrada, b.salida) : 0;
  const lineasExtra = servicios.filter((s) => extra[s.id]).map((s) => ({ ...s, cantidad: Number(extra[s.id]), subtotal: Number(s.precio) * Number(extra[s.id]) }));
  const totalExtras = lineasExtra.reduce((a, s) => a + s.subtotal, 0);
  const subtotalHab = hab ? Number(hab.precioBaseNoche) * nochesN : 0;

  const falla = (e) => { setError(mensajeError(e)); setErroresApi(erroresCampos(e)); };
  const limpiar = () => { setError(''); setErroresApi({}); };

  // ---- Paso 1: buscar
  const buscar = async (ev) => {
    ev.preventDefault();
    limpiar();
    const e = {};
    if (!b.entrada) e.entrada = 'La fecha de entrada es obligatoria';
    else if (b.entrada < hoyISO()) e.entrada = 'La fecha de entrada no puede ser anterior a hoy';
    if (!b.salida) e.salida = 'La fecha de salida es obligatoria';
    else if (b.entrada && b.salida <= b.entrada) e.salida = 'La salida debe ser posterior a la entrada';
    else if (b.entrada && calcNoches(b.entrada, b.salida) > 30) e.salida = 'La estadía máxima es de 30 noches';
    const n = Number(b.huespedes);
    if (!Number.isInteger(n) || n < 1 || n > 10) e.huespedes = 'Indica un número entero de huéspedes entre 1 y 10';
    setErrores(e);
    if (Object.keys(e).length) return;
    setCargando(true);
    try {
      const { data } = await api.get('/habitaciones/disponibles', {
        params: { ciudad: b.ciudad, entrada: b.entrada, salida: b.salida, huespedes: n, tipoId: b.tipoId || undefined },
      });
      setResultados(data);
      setPaso(2);
    } catch (err) {
      falla(err);
    } finally {
      setCargando(false);
    }
  };

  // ---- Paso 3: datos del huésped
  const continuarDatos = (ev) => {
    ev.preventDefault();
    limpiar();
    const e = {};
    const nombre = h.nombreCompleto.trim();
    if (!nombre) e.nombreCompleto = 'El nombre completo es obligatorio';
    else if (!RE.nombre.test(nombre)) e.nombreCompleto = 'El nombre solo puede contener letras y espacios';
    if (!h.fechaNacimiento) e.fechaNacimiento = 'La fecha de nacimiento es obligatoria';
    else if (h.fechaNacimiento >= hoyISO()) e.fechaNacimiento = 'La fecha de nacimiento debe ser pasada';
    else if (edad(h.fechaNacimiento) < 18) e.fechaNacimiento = 'El huésped titular debe ser mayor de edad';
    const doc = DOCS[h.tipoDocumento];
    if (!h.numeroDocumento.trim()) e.numeroDocumento = 'El número de documento es obligatorio';
    else if (!doc.re.test(h.numeroDocumento.trim())) e.numeroDocumento = `Formato no válido para ${h.tipoDocumento}. Ejemplo: ${doc.ej}`;
    if (!h.telefono.trim()) e.telefono = 'El teléfono es obligatorio';
    else if (!RE.telefono.test(h.telefono.trim())) e.telefono = 'Debe tener entre 8 y 15 dígitos (puede iniciar con +)';
    if (!h.correoElectronico.trim()) e.correoElectronico = 'El correo es obligatorio';
    else if (!RE.email.test(h.correoElectronico.trim())) e.correoElectronico = 'El correo no tiene un formato válido';
    setErrores(e);
    if (Object.keys(e).length === 0) setPaso(4);
  };

  // ---- Paso 4: crear la reserva (queda PENDIENTE hasta pagar)
  const confirmar = async () => {
    limpiar();
    setCargando(true);
    try {
      const body = {
        idHabitacion: hab.id, fechaEntrada: b.entrada, fechaSalida: b.salida, numeroHuespedes: Number(b.huespedes),
        huesped: { ...h, nombreCompleto: h.nombreCompleto.trim(), numeroDocumento: h.numeroDocumento.trim(), telefono: h.telefono.trim(), correoElectronico: h.correoElectronico.trim() },
        servicios: lineasExtra.map((s) => ({ idServicio: s.id, cantidad: s.cantidad })),
      };
      const { data } = await api.post('/reservas', body);
      setReserva(data);
      setPaso(5);
    } catch (err) {
      falla(err);
    } finally {
      setCargando(false);
    }
  };

  const nueva = () => {
    setPaso(1); setHab(null); setReserva(null); setResultados([]); setExtra({}); setErrores({}); limpiar();
  };

  const toggleExtra = (id) => setExtra((x) => { const n = { ...x }; if (n[id]) delete n[id]; else n[id] = 1; return n; });

  return (
    <div>
      <h1>Reserva tu habitación</h1>
      <p className="sub">Sigue los pasos para completar tu reserva.</p>
      <div className="pasos">
        {PASOS.map((p, i) => (
          <span key={p} className={`paso ${paso === i + 1 ? 'activo' : paso > i + 1 ? 'hecho' : ''}`}>{i + 1}. {p}</span>
        ))}
      </div>
      <Alerta mensaje={error} errores={erroresApi} />

      {paso === 1 && (
        <form className="tarjeta" onSubmit={buscar} noValidate>
          <h2>¿A dónde te gustaría ir?</h2>
          <div className="grid-2" style={{ margin: '14px 0' }}>
            <Campo label="Ciudad o ubicación"><input placeholder="Ej. San Salvador, La Libertad…" value={b.ciudad} onChange={(e) => setB({ ...b, ciudad: e.target.value })} /></Campo>
            <Campo label="Tipo de habitación (opcional)">
              <select value={b.tipoId} onChange={(e) => setB({ ...b, tipoId: e.target.value })}>
                <option value="">Cualquiera</option>
                {tipos.map((t) => <option key={t.id} value={t.id}>{t.nombre}</option>)}
              </select>
            </Campo>
            <Campo label="Fecha de entrada *" error={errores.entrada}><input type="date" min={hoyISO()} value={b.entrada} onChange={(e) => setB({ ...b, entrada: e.target.value, salida: b.salida && b.salida <= e.target.value ? '' : b.salida })} /></Campo>
            <Campo label="Fecha de salida *" error={errores.salida}><input type="date" min={b.entrada ? sumarDias(b.entrada, 1) : hoyISO()} value={b.salida} onChange={(e) => setB({ ...b, salida: e.target.value })} /></Campo>
            <Campo label="Número de huéspedes *" error={errores.huespedes}><input type="number" min="1" max="10" value={b.huespedes} onChange={(e) => setB({ ...b, huespedes: e.target.value })} /></Campo>
          </div>
          <button className="btn" disabled={cargando}>{cargando ? 'Buscando…' : 'Buscar habitaciones disponibles'}</button>
        </form>
      )}

      {paso === 2 && (
        <div>
          <h2>{resultados.length} habitación(es) disponible(s)</h2>
          <p className="sub">{b.entrada} → {b.salida} · {nochesN} noche(s) · {b.huespedes} huésped(es)</p>
          {resultados.length === 0 && <div className="tarjeta">No hay habitaciones disponibles con esos criterios. Prueba con otras fechas o ubicación.</div>}
          {resultados.map((r) => (
            <div key={r.id} className="tarjeta hab-card">
              <div>
                <small>{r.hotel.nombre} · {r.hotel.ciudad}</small>
                <h3>{r.tipoHabitacion.nombre} · Hab. {r.numeroHabitacion}</h3>
                <small>Capacidad: {r.tipoHabitacion.capacidadMaxima} persona(s)</small>
                <div>{(r.servicios || []).map((s) => <span className="tag" key={s.id}>{s.nombre}</span>)}</div>
              </div>
              <div className="precio">
                <small>{dinero(r.precioBaseNoche)} por noche</small>
                <strong>{dinero(Number(r.precioBaseNoche) * nochesN)}</strong>
                <button className="btn" onClick={() => { setHab(r); setPaso(3); limpiar(); }}>Seleccionar habitación</button>
              </div>
            </div>
          ))}
          <button className="btn btn-sec" onClick={() => setPaso(1)}>← Ajustar búsqueda</button>
        </div>
      )}

      {paso === 3 && hab && (
        <form onSubmit={continuarDatos} noValidate className="grid-2" style={{ alignItems: 'start' }}>
          <div className="tarjeta">
            <h2>Datos del huésped principal</h2>
            <div className="grid-2" style={{ margin: '14px 0' }}>
              <Campo label="Nombre completo *" error={errores.nombreCompleto}><input value={h.nombreCompleto} onChange={(e) => setH({ ...h, nombreCompleto: e.target.value })} /></Campo>
              <Campo label="Fecha de nacimiento *" error={errores.fechaNacimiento}><input type="date" max={hoyISO()} value={h.fechaNacimiento} onChange={(e) => setH({ ...h, fechaNacimiento: e.target.value })} /></Campo>
              <Campo label="Tipo de documento *">
                <select value={h.tipoDocumento} onChange={(e) => setH({ ...h, tipoDocumento: e.target.value })}>
                  {Object.keys(DOCS).map((d) => <option key={d}>{d}</option>)}
                </select>
              </Campo>
              <Campo label="Número de documento *" error={errores.numeroDocumento}><input placeholder={DOCS[h.tipoDocumento].ej} value={h.numeroDocumento} onChange={(e) => setH({ ...h, numeroDocumento: e.target.value })} /></Campo>
              <Campo label="Teléfono *" error={errores.telefono}><input placeholder="+50370001234" value={h.telefono} onChange={(e) => setH({ ...h, telefono: e.target.value })} /></Campo>
              <Campo label="Correo electrónico *" error={errores.correoElectronico}><input type="email" value={h.correoElectronico} onChange={(e) => setH({ ...h, correoElectronico: e.target.value })} /></Campo>
            </div>
            <strong style={{ fontSize: 13 }}>Servicios adicionales</strong>
            {servicios.map((s) => (
              <div key={s.id} className="fila espacio" style={{ marginTop: 6 }}>
                <label className="check">
                  <input type="checkbox" checked={!!extra[s.id]} onChange={() => toggleExtra(s.id)} />
                  {s.nombre} <small>({Number(s.precio) === 0 ? 'incluido' : dinero(s.precio)})</small>
                </label>
                {extra[s.id] && (
                  <input type="number" min="1" max="20" style={{ width: 70 }} value={extra[s.id]}
                    onChange={(e) => setExtra({ ...extra, [s.id]: Math.min(20, Math.max(1, Number(e.target.value) || 1)) })} />
                )}
              </div>
            ))}
            <div className="fila" style={{ marginTop: 16 }}>
              <button className="btn">Continuar al resumen</button>
              <button type="button" className="btn btn-sec" onClick={() => setPaso(2)}>← Volver</button>
            </div>
          </div>
          <div className="tarjeta">
            <small>{hab.hotel.nombre}</small>
            <h3>{hab.tipoHabitacion.nombre} · Hab. {hab.numeroHabitacion}</h3>
            <div className="resumen-linea"><span>Noches</span><span>{nochesN}</span></div>
            <div className="resumen-linea"><span>Precio por noche</span><span>{dinero(hab.precioBaseNoche)}</span></div>
            <div className="resumen-linea resumen-total"><span>Subtotal habitación</span><span>{dinero(subtotalHab)}</span></div>
          </div>
        </form>
      )}

      {paso === 4 && hab && (
        <div className="grid-2" style={{ alignItems: 'start' }}>
          <div className="tarjeta">
            <h2>Revisa tu reserva</h2>
            <p><strong>Huésped:</strong> {h.nombreCompleto}<br />{h.tipoDocumento} {h.numeroDocumento} · {h.correoElectronico}</p>
            <p><strong>Hotel y habitación:</strong><br />{hab.hotel.nombre} · {hab.tipoHabitacion.nombre} {hab.numeroHabitacion}</p>
            <p><strong>Estadía:</strong> {b.entrada} → {b.salida} ({nochesN} noche(s)) · {b.huespedes} huésped(es)</p>
          </div>
          <div className="tarjeta">
            <Badge valor="DISPONIBLE" /> <small>Habitación confirmada como disponible</small>
            <div className="resumen-linea" style={{ marginTop: 10 }}><span>Subtotal habitación</span><span>{dinero(subtotalHab)}</span></div>
            {lineasExtra.map((s) => <div key={s.id} className="resumen-linea"><span>{s.nombre} × {s.cantidad}</span><span>{dinero(s.subtotal)}</span></div>)}
            <div className="resumen-linea resumen-total"><span>Precio total</span><span>{dinero(subtotalHab + totalExtras)}</span></div>
            <div className="fila" style={{ marginTop: 14 }}>
              <button className="btn" onClick={confirmar} disabled={cargando}>{cargando ? 'Creando reserva…' : 'Confirmar y continuar al pago'}</button>
              <button className="btn btn-sec" onClick={() => setPaso(3)}>← Editar</button>
            </div>
          </div>
        </div>
      )}

      {paso === 5 && reserva && (
        <div className="tarjeta" style={{ maxWidth: 520 }}>
          <h2>Confirmación y pago</h2>
          <p className="sub">Reserva {reserva.codigo} creada. Completa el pago para confirmarla.</p>
          <PagoForm reserva={reserva} metodos={esStaff ? METODOS_STAFF : METODOS_CLIENTE} onPagado={(r) => { setReserva(r); setPaso(6); }} />
        </div>
      )}

      {paso === 6 && reserva && (
        <div className="tarjeta" style={{ maxWidth: 520, textAlign: 'center' }}>
          <h2>{reserva.estado === 'CONFIRMADA' ? '¡Reserva confirmada!' : '¡Reserva registrada!'}</h2>
          <p className="sub">
            {reserva.estado === 'CONFIRMADA'
              ? 'Presenta este código al llegar al hotel.'
              : 'Tu reserva queda pendiente: paga en recepción para confirmarla.'}
          </p>
          <div className="codigo">{reserva.codigo}</div>
          <div style={{ textAlign: 'left' }}>
            <div className="resumen-linea"><span>Huésped</span><span>{reserva.huespedNombre}</span></div>
            <div className="resumen-linea"><span>Hotel</span><span>{reserva.hotel}</span></div>
            <div className="resumen-linea"><span>Habitación</span><span>{reserva.tipoHabitacion} · {reserva.numeroHabitacion}</span></div>
            <div className="resumen-linea"><span>Fechas</span><span>{reserva.fechaEntrada} → {reserva.fechaSalida}</span></div>
            <div className="resumen-linea"><span>Estado de la reserva</span><Badge valor={reserva.estado} /></div>
            <div className="resumen-linea"><span>Estado del pago</span><Badge valor={reserva.estadoPago} /></div>
            <div className="resumen-linea resumen-total"><span>Total</span><span>{dinero(reserva.precioTotal)}</span></div>
          </div>
          <div className="fila" style={{ justifyContent: 'center', marginTop: 14 }}>
            <Link className="btn btn-oscuro" to="/mis-reservas">Ver mis reservas</Link>
            <button className="btn btn-sec" onClick={nueva}>Nueva búsqueda</button>
          </div>
        </div>
      )}
    </div>
  );
}
