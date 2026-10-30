import { useState } from 'react';
import api, { mensajeError, erroresCampos } from '../api';
import { RE, dinero } from '../validators';
import { Alerta, Campo } from './ui';

export const METODOS_CLIENTE = [
  { value: 'TARJETA_CREDITO', label: 'Tarjeta de crédito' },
  { value: 'TARJETA_DEBITO', label: 'Tarjeta de débito' },
  { value: 'TRANSFERENCIA', label: 'Transferencia bancaria' },
  { value: 'EFECTIVO', label: 'Efectivo (pagar en recepción)' },
];
export const METODOS_STAFF = [
  { value: 'EFECTIVO', label: 'Efectivo' },
  { value: 'TRANSFERENCIA', label: 'Transferencia bancaria' },
];

/** Formulario de pago (simulado). Valida la tarjeta antes de enviar y vuelve a validarse en el backend. */
export default function PagoForm({ reserva, metodos, onPagado }) {
  const saldo = Number(reserva.precioTotal) - Number(reserva.montoPagado);
  const [f, setF] = useState({ metodoPago: metodos[0].value, numeroTarjeta: '', vencimiento: '', cvv: '', titular: '' });
  const [errores, setErrores] = useState({});
  const [error, setError] = useState('');
  const [enviando, setEnviando] = useState(false);
  const esTarjeta = f.metodoPago.startsWith('TARJETA');
  const cambiar = (k, v) => setF((x) => ({ ...x, [k]: v }));

  const validar = () => {
    const e = {};
    if (!esTarjeta) return e;
    if (!/^\d{13,19}$/.test(f.numeroTarjeta.replace(/\s/g, ''))) e.numeroTarjeta = 'El número de tarjeta debe tener entre 13 y 19 dígitos';
    const m = /^(0[1-9]|1[0-2])\/(\d{2})$/.exec(f.vencimiento);
    if (!m) e.vencimiento = 'Usa el formato MM/AA (ejemplo: 08/29)';
    else {
      const hoy = new Date();
      if ((2000 + Number(m[2])) * 12 + Number(m[1]) < hoy.getFullYear() * 12 + hoy.getMonth() + 1) e.vencimiento = 'La tarjeta está vencida';
    }
    if (!/^\d{3,4}$/.test(f.cvv)) e.cvv = 'El CVV debe tener 3 o 4 dígitos';
    if (!f.titular.trim()) e.titular = 'El nombre del titular es obligatorio';
    else if (!RE.nombre.test(f.titular.trim())) e.titular = 'El nombre solo puede contener letras';
    return e;
  };

  const pagar = async (ev) => {
    ev.preventDefault();
    setError('');
    const e = validar();
    setErrores(e);
    if (Object.keys(e).length) return;
    setEnviando(true);
    try {
      const body = esTarjeta ? f : { metodoPago: f.metodoPago };
      const { data } = await api.post(`/reservas/${reserva.id}/pagos`, body);
      onPagado(data);
    } catch (err) {
      setError(mensajeError(err));
      setErrores(erroresCampos(err));
    } finally {
      setEnviando(false);
    }
  };

  return (
    <form onSubmit={pagar} noValidate style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
      <div className="resumen-linea resumen-total"><span>Total a pagar</span><span>{dinero(saldo)}</span></div>
      <Alerta mensaje={error} />
      <Campo label="Método de pago">
        <select value={f.metodoPago} onChange={(e) => cambiar('metodoPago', e.target.value)}>
          {metodos.map((m) => <option key={m.value} value={m.value}>{m.label}</option>)}
        </select>
      </Campo>
      {esTarjeta && (
        <>
          <Campo label="Número de tarjeta" error={errores.numeroTarjeta}>
            <input inputMode="numeric" placeholder="4111 1111 1111 1111" value={f.numeroTarjeta} onChange={(e) => cambiar('numeroTarjeta', e.target.value)} />
          </Campo>
          <div className="grid-2">
            <Campo label="Vencimiento (MM/AA)" error={errores.vencimiento}>
              <input placeholder="08/29" maxLength={5} value={f.vencimiento} onChange={(e) => cambiar('vencimiento', e.target.value)} />
            </Campo>
            <Campo label="CVV" error={errores.cvv}>
              <input inputMode="numeric" maxLength={4} placeholder="123" value={f.cvv} onChange={(e) => cambiar('cvv', e.target.value)} />
            </Campo>
          </div>
          <Campo label="Nombre del titular" error={errores.titular}>
            <input placeholder="Como aparece en la tarjeta" value={f.titular} onChange={(e) => cambiar('titular', e.target.value)} />
          </Campo>
        </>
      )}
      {f.metodoPago === 'EFECTIVO' && metodos.length > 2 && (
        <p className="sub" style={{ margin: 0 }}>Tu reserva quedará pendiente hasta que pagues en recepción.</p>
      )}
      <button className="btn" disabled={enviando}>{enviando ? 'Procesando…' : `Pagar ${dinero(saldo)}`}</button>
    </form>
  );
}
