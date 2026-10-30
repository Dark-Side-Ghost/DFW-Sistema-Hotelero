import CrudPage from '../components/CrudPage';
import { Badge } from '../components/ui';
import api, { mensajeError } from '../api';
import { RE } from '../validators';

const campos = [
  { name: 'nombres', label: 'Nombres', required: true, maxLength: 100, pattern: RE.nombre, patternMsg: 'Solo se permiten letras y espacios' },
  { name: 'apellidos', label: 'Apellidos', required: true, maxLength: 100, pattern: RE.nombre, patternMsg: 'Solo se permiten letras y espacios' },
  { name: 'email', label: 'Correo electrónico', type: 'email', required: true, pattern: RE.email, patternMsg: 'El correo no tiene un formato válido' },
  {
    name: 'password', label: 'Contraseña (vacío = no cambiar)', type: 'password', required: true, opcionalAlEditar: true,
    pattern: RE.clave, patternMsg: 'Mínimo 8 caracteres, con al menos una letra y un número',
  },
  {
    name: 'rol', label: 'Rol', type: 'select', required: true,
    opciones: [{ value: 'ADMIN', label: 'Administrador' }, { value: 'RECEPCIONISTA', label: 'Recepcionista' }, { value: 'CLIENTE', label: 'Cliente' }],
  },
  {
    name: 'estado', label: 'Estado', type: 'select', required: true,
    opciones: [{ value: 'ACTIVO', label: 'Activo' }, { value: 'INACTIVO', label: 'Inactivo' }, { value: 'BLOQUEADO', label: 'Bloqueado' }],
  },
];

export default function Usuarios() {
  const alternar = async (u, { recargar, avisar, fallo }) => {
    try {
      const nuevo = u.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
      await api.patch(`/usuarios/${u.id}/estado`, { estado: nuevo });
      avisar(`El usuario ${u.email} ahora está ${nuevo.toLowerCase()}.`);
      recargar();
    } catch (e) {
      fallo(mensajeError(e));
    }
  };

  return (
    <CrudPage
      titulo="Usuarios"
      descripcion="Crear, editar, eliminar, activar y desactivar usuarios del sistema."
      endpoint="/usuarios"
      nombreItem="usuario"
      campos={campos}
      formVacio={{ nombres: '', apellidos: '', email: '', password: '', rol: 'CLIENTE', estado: 'ACTIVO' }}
      aForm={(u) => ({ nombres: u.nombres, apellidos: u.apellidos, email: u.email, password: '', rol: u.rol, estado: u.estado })}
      aPayload={(f) => ({ ...f, password: f.password ? f.password : null })}
      columnas={[
        { titulo: 'Nombre', render: (u) => <strong>{u.nombres} {u.apellidos}</strong> },
        { titulo: 'Correo', render: (u) => u.email },
        { titulo: 'Rol', render: (u) => u.rol },
        { titulo: 'Estado', render: (u) => <Badge valor={u.estado} /> },
      ]}
      acciones={(u, ctx) => (
        <button className="btn btn-sec btn-sm" onClick={() => alternar(u, ctx)}>
          {u.estado === 'ACTIVO' ? 'Desactivar' : 'Activar'}
        </button>
      )}
    />
  );
}
