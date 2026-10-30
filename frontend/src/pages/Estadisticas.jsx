import { useEffect, useState } from 'react';
import api, { mensajeError } from '../api';
import { dinero } from '../validators';
import { Alerta, Badge } from '../components/ui';

export default function Estadisticas() {
  const [d, setD] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/estadisticas').then((r) => setD(r.data)).catch((e) => setError(mensajeError(e)));
  }, []);

  if (error) return <Alerta mensaje={error} />;
  if (!d) return <p>Cargando…</p>;

  const max = Math.max(...d.ingresosMensuales.map((m) => Number(m.total)), 1);

  return (
    <div>
      <h1>Estadísticas</h1>
      <p className="sub">Ocupación, reservaciones, cancelaciones e ingresos de la cadena.</p>

      <div className="kpis">
        <div className="kpi"><small>Reservas del mes</small><strong>{d.reservasMes}</strong><small>{d.totalReservas} en total</small></div>
        <div className="kpi"><small>Ingresos del mes</small><strong>{dinero(d.ingresosMes)}</strong><small>{dinero(d.ingresosTotal)} en total</small></div>
        <div className="kpi"><small>Ocupación hoy</small><strong>{d.ocupacionPromedio}%</strong></div>
        <div className="kpi"><small>Cancelaciones</small><strong>{d.cancelaciones}</strong></div>
      </div>

      <div className="grid-2">
        <div className="tarjeta">
          <h3>Ingresos mensuales</h3>
          <div className="barras">
            {d.ingresosMensuales.map((m) => (
              <div key={m.mes} title={dinero(m.total)}>
                <span>{dinero(m.total)}</span>
                <div className="col" style={{ height: `${(Number(m.total) / max) * 100}%`, flex: 'none' }} />
                <span>{m.mes}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="tarjeta">
          <h3>Ocupación por hotel (hoy)</h3>
          {d.ocupacionPorHotel.map((h) => (
            <div key={h.hotel} style={{ marginBottom: 12 }}>
              <div className="fila espacio"><span>{h.hotel}</span><strong>{h.porcentaje}% ({h.ocupadas}/{h.total})</strong></div>
              <div className="progreso"><span style={{ width: `${h.porcentaje}%` }} /></div>
            </div>
          ))}
          <h3 style={{ marginTop: 18 }}>Reservas por estado</h3>
          <div className="fila">
            {Object.entries(d.reservasPorEstado).map(([estado, n]) => (
              <span key={estado}><Badge valor={estado} /> {n}</span>
            ))}
            {Object.keys(d.reservasPorEstado).length === 0 && <span>Aún no hay reservas.</span>}
          </div>
        </div>
      </div>
    </div>
  );
}
