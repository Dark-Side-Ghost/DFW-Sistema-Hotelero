import CrudPage from '../components/CrudPage';

const campos = [
  { name: 'idHotel', label: 'Hotel', type: 'select', required: true, fuente: '/hoteles', etiqueta: (h) => `${h.nombre} - ${h.ciudad}` },
  { name: 'diasPreviosLimite', label: 'Días previos sin cargo', type: 'number', required: true, integer: true, min: 0, max: 365 },
  { name: 'porcentajeCargo', label: 'Cargo si cancela tarde (%)', type: 'number', step: '0.01', required: true, min: 0, max: 100 },
];

export default function Politicas() {
  return (
    <CrudPage
      titulo="Políticas de cancelación"
      descripcion="Si el cliente cancela con menos días de anticipación que el límite, se cobra el porcentaje indicado."
      endpoint="/politicas"
      nombreItem="política"
      campos={campos}
      formVacio={{ idHotel: '', diasPreviosLimite: 2, porcentajeCargo: 50 }}
      aForm={(p) => ({ idHotel: p.hotel.id, diasPreviosLimite: p.diasPreviosLimite, porcentajeCargo: p.porcentajeCargo })}
      aPayload={(f) => ({ idHotel: Number(f.idHotel), diasPreviosLimite: Number(f.diasPreviosLimite), porcentajeCargo: Number(f.porcentajeCargo) })}
      columnas={[
        { titulo: 'Hotel', render: (p) => <strong>{p.hotel.nombre}</strong> },
        { titulo: 'Días previos sin cargo', render: (p) => p.diasPreviosLimite },
        { titulo: 'Cargo por cancelación tardía', render: (p) => `${p.porcentajeCargo}%` },
      ]}
    />
  );
}
