import CrudPage from '../components/CrudPage';
import { Badge } from '../components/ui';
import { RE, dinero } from '../validators';

const campos = [
  { name: 'idHotel', label: 'Hotel', type: 'select', required: true, fuente: '/hoteles', etiqueta: (h) => `${h.nombre} - ${h.ciudad}` },
  { name: 'idTipoHabitacion', label: 'Tipo de habitación', type: 'select', required: true, fuente: '/tipos-habitacion', etiqueta: (t) => `${t.nombre} (hasta ${t.capacidadMaxima})` },
  { name: 'numeroHabitacion', label: 'Número de habitación', required: true, pattern: RE.numeroHab, patternMsg: 'Solo letras, números y guiones (máx. 20)', placeholder: 'Ej. 204' },
  { name: 'precioBaseNoche', label: 'Tarifa por noche (USD)', type: 'number', step: '0.01', required: true, min: 0.01 },
  {
    name: 'estado', label: 'Estado', type: 'select', required: true,
    opciones: [{ value: 'DISPONIBLE', label: 'Disponible' }, { value: 'OCUPADA', label: 'Ocupada' }, { value: 'MANTENIMIENTO', label: 'Mantenimiento' }],
  },
  { name: 'serviciosIds', label: 'Servicios incluidos en la habitación', type: 'multi', fuente: '/servicios', etiqueta: (s) => s.nombre },
];

export default function Habitaciones() {
  return (
    <CrudPage
      titulo="Habitaciones y tarifas"
      descripcion="Define el inventario de habitaciones, su tipo, tarifa y servicios incluidos."
      endpoint="/habitaciones"
      nombreItem="habitación"
      campos={campos}
      formVacio={{ idHotel: '', idTipoHabitacion: '', numeroHabitacion: '', precioBaseNoche: '', estado: 'DISPONIBLE', serviciosIds: [] }}
      aForm={(h) => ({
        idHotel: h.hotel.id, idTipoHabitacion: h.tipoHabitacion.id, numeroHabitacion: h.numeroHabitacion,
        precioBaseNoche: h.precioBaseNoche, estado: h.estado, serviciosIds: (h.servicios || []).map((s) => s.id),
      })}
      aPayload={(f) => ({
        idHotel: Number(f.idHotel), idTipoHabitacion: Number(f.idTipoHabitacion), numeroHabitacion: f.numeroHabitacion.trim(),
        precioBaseNoche: Number(f.precioBaseNoche), estado: f.estado, serviciosIds: f.serviciosIds || [],
      })}
      columnas={[
        { titulo: 'N.º', render: (h) => <strong>{h.numeroHabitacion}</strong> },
        { titulo: 'Hotel', render: (h) => h.hotel.nombre },
        { titulo: 'Tipo', render: (h) => h.tipoHabitacion.nombre },
        { titulo: 'Tarifa/noche', render: (h) => dinero(h.precioBaseNoche) },
        { titulo: 'Estado', render: (h) => <Badge valor={h.estado} /> },
        { titulo: 'Servicios', render: (h) => (h.servicios || []).map((s) => <span className="tag" key={s.id}>{s.nombre}</span>) },
      ]}
    />
  );
}
