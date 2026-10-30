import CrudPage from '../components/CrudPage';
import { dinero } from '../validators';

export const CATEGORIAS = [
  { value: 'ALIMENTACION', label: 'Alimentación' },
  { value: 'TRANSPORTE', label: 'Transporte' },
  { value: 'LIMPIEZA', label: 'Lavandería / limpieza' },
  { value: 'OTROS', label: 'Otros' },
];

const campos = [
  { name: 'nombre', label: 'Servicio', required: true, maxLength: 100, placeholder: 'Ej. Desayuno buffet' },
  { name: 'categoria', label: 'Categoría', type: 'select', required: true, opciones: CATEGORIAS },
  { name: 'precio', label: 'Precio (USD, 0 = incluido)', type: 'number', step: '0.01', required: true, min: 0 },
];

export default function Servicios() {
  return (
    <CrudPage
      titulo="Servicios adicionales"
      descripcion="Alimentación, transporte, lavandería y otros servicios que el huésped puede agregar."
      endpoint="/servicios"
      nombreItem="servicio"
      campos={campos}
      formVacio={{ nombre: '', categoria: '', precio: '' }}
      aForm={(s) => ({ nombre: s.nombre, categoria: s.categoria, precio: s.precio })}
      aPayload={(f) => ({ nombre: f.nombre, categoria: f.categoria, precio: Number(f.precio) })}
      columnas={[
        { titulo: 'Servicio', render: (s) => <strong>{s.nombre}</strong> },
        { titulo: 'Categoría', render: (s) => CATEGORIAS.find((c) => c.value === s.categoria)?.label || s.categoria },
        { titulo: 'Precio', render: (s) => (Number(s.precio) === 0 ? 'Incluido' : dinero(s.precio)) },
      ]}
    />
  );
}
