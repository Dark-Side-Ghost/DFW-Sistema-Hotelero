import CrudPage from '../components/CrudPage';
import { RE } from '../validators';

const campos = [
  { name: 'nombre', label: 'Nombre del hotel', required: true, maxLength: 150, placeholder: 'Ej. Hotel Lago Coatepeque' },
  { name: 'ciudad', label: 'Ciudad', required: true, maxLength: 100 },
  { name: 'pais', label: 'País', required: true, maxLength: 100 },
  { name: 'categoria', label: 'Categoría (estrellas 1-5)', type: 'number', required: true, integer: true, min: 1, max: 5 },
  { name: 'direccion', label: 'Dirección', maxLength: 255 },
  { name: 'telefono', label: 'Teléfono', pattern: RE.telefonoHotel, patternMsg: 'Teléfono no válido (ejemplo: 2260-0000)', placeholder: '2260-0000' },
];

export default function Hoteles() {
  return (
    <CrudPage
      titulo="Hoteles y sucursales"
      descripcion="Administra las propiedades que forman parte de la cadena."
      endpoint="/hoteles"
      nombreItem="hotel"
      campos={campos}
      formVacio={{ nombre: '', ciudad: '', pais: 'El Salvador', categoria: 3, direccion: '', telefono: '' }}
      aForm={(h) => ({ nombre: h.nombre, ciudad: h.ciudad, pais: h.pais, categoria: h.categoria ?? '', direccion: h.direccion ?? '', telefono: h.telefono ?? '' })}
      aPayload={(f) => ({ ...f, categoria: Number(f.categoria), direccion: f.direccion || null, telefono: f.telefono || null })}
      columnas={[
        { titulo: 'Hotel', render: (h) => <strong>{h.nombre}</strong> },
        { titulo: 'Ciudad', render: (h) => `${h.ciudad}, ${h.pais}` },
        { titulo: 'Estrellas', render: (h) => '★'.repeat(h.categoria || 0) },
        { titulo: 'Dirección', render: (h) => h.direccion || '—' },
        { titulo: 'Teléfono', render: (h) => h.telefono || '—' },
      ]}
    />
  );
}
