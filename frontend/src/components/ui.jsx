// Componentes pequeños reutilizables: Alerta, Modal, Badge y Campo
const ETIQUETAS = {
  PENDIENTE: 'Pendiente', CONFIRMADA: 'Confirmada', CANCELADA: 'Cancelada', FINALIZADA: 'Finalizada',
  EN_CURSO: 'Check-in realizado', PAGADO: 'Pagado', SIN_PAGO: 'Sin pago', PARCIAL: 'Pago parcial',
  REEMBOLSADO: 'Reembolsado', EN_PROCESO: 'En proceso', RESUELTO: 'Resuelto', RECHAZADO: 'Rechazado',
  DISPONIBLE: 'Disponible', OCUPADA: 'Ocupada', MANTENIMIENTO: 'Mantenimiento',
  ACTIVO: 'Activo', INACTIVO: 'Inactivo', BLOQUEADO: 'Bloqueado',
};
const COLORES = {
  CONFIRMADA: 'verde', PAGADO: 'verde', RESUELTO: 'verde', DISPONIBLE: 'verde', ACTIVO: 'verde',
  PENDIENTE: 'amarillo', SIN_PAGO: 'amarillo', PARCIAL: 'amarillo', OCUPADA: 'amarillo',
  CANCELADA: 'rojo', RECHAZADO: 'rojo', MANTENIMIENTO: 'rojo', BLOQUEADO: 'rojo',
  EN_CURSO: 'azul', EN_PROCESO: 'azul',
  FINALIZADA: 'gris', REEMBOLSADO: 'gris', INACTIVO: 'gris',
};

export function Badge({ valor }) {
  return <span className={`badge ${COLORES[valor] || 'gris'}`}>{ETIQUETAS[valor] || valor}</span>;
}

/** Muestra un mensaje y, si existen, la lista de errores por campo que devuelve el backend */
export function Alerta({ tipo = 'error', mensaje, errores }) {
  const lista = errores ? Object.values(errores) : [];
  if (!mensaje && lista.length === 0) return null;
  return (
    <div className={`alerta ${tipo}`} role="alert">
      {mensaje && <strong>{mensaje}</strong>}
      {lista.length > 0 && (
        <ul>
          {lista.map((m, i) => (
            <li key={i}>{m}</li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function Modal({ titulo, onCerrar, children }) {
  return (
    <div className="modal-fondo" onMouseDown={(e) => e.target === e.currentTarget && onCerrar()}>
      <div className="modal" role="dialog" aria-modal="true">
        <div className="fila espacio" style={{ marginBottom: 12 }}>
          <h2>{titulo}</h2>
          <button className="btn btn-sec btn-sm" onClick={onCerrar}>Cerrar</button>
        </div>
        {children}
      </div>
    </div>
  );
}

export function Campo({ label, error, children }) {
  return (
    <label className="campo">
      <span>{label}</span>
      {children}
      {error && <small className="error">{error}</small>}
    </label>
  );
}
