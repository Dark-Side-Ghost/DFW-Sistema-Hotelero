import CrudPage from '../components/CrudPage';

const campos = [
  { name: 'nombre', label: 'Nombre del tipo', required: true, maxLength: 100, placeholder: 'Ej. Suite' },
  { name: 'capacidadMaxima', label: 'Capacidad máxima (personas)', type: 'number', required: true, integer: true, min: 1, max: 10 },
];

export default function Tipos() {
  return (
    <CrudPage
      titulo="Tipos de habitación"
      descripcion="Define los tipos de habitación y cuántas personas admiten."
      endpoint="/tipos-habitacion"
      nombreItem="tipo de habitación"
      campos={campos}
      formVacio={{ nombre: '', capacidadMaxima: 2 }}
      aForm={(t) => ({ nombre: t.nombre, capacidadMaxima: t.capacidadMaxima })}
      aPayload={(f) => ({ nombre: f.nombre, capacidadMaxima: Number(f.capacidadMaxima) })}
      columnas={[
        { titulo: 'Tipo', render: (t) => <strong>{t.nombre}</strong> },
        { titulo: 'Capacidad', render: (t) => `${t.capacidadMaxima} persona(s)` },
      ]}
    />
  );
}
