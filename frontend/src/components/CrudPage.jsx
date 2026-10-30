import { useEffect, useState } from 'react';
import api, { mensajeError, erroresCampos } from '../api';
import { validarForm } from '../validators';
import { Alerta, Campo } from './ui';

/**
 * Pantalla CRUD genérica (listar, crear, editar, eliminar) usada por Hoteles, Habitaciones,
 * Tipos, Servicios, Políticas y Usuarios. Cada página solo define sus campos y columnas.
 */
export default function CrudPage({
  titulo, descripcion, endpoint, nombreItem = 'registro',
  campos, columnas, formVacio, aForm, aPayload, acciones,
}) {
  const [items, setItems] = useState([]);
  const [opciones, setOpciones] = useState({});
  const [form, setForm] = useState(null); // null = formulario cerrado
  const [editId, setEditId] = useState(null);
  const [errores, setErrores] = useState({});
  const [ok, setOk] = useState('');
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(true);

  const cargar = async () => {
    try {
      const { data } = await api.get(endpoint);
      setItems(data);
    } catch (e) {
      setError(mensajeError(e));
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargar();
    // Carga las listas para los campos select/multi que dependen de otra tabla
    campos.filter((c) => c.fuente).forEach(async (c) => {
      try {
        const { data } = await api.get(c.fuente);
        setOpciones((o) => ({ ...o, [c.name]: data }));
      } catch (e) {
        setError(mensajeError(e));
      }
    });
  }, []);

  const opcionesDe = (c) =>
    c.fuente ? (opciones[c.name] || []).map((x) => ({ value: x.id, label: c.etiqueta(x) })) : c.opciones || [];

  const abrirNuevo = () => { setForm({ ...formVacio }); setEditId(null); setErrores({}); setOk(''); setError(''); };
  const abrirEditar = (item) => { setForm(aForm(item)); setEditId(item.id); setErrores({}); setOk(''); setError(''); };
  const cerrar = () => { setForm(null); setEditId(null); setErrores({}); };
  const cambiar = (name, valor) => setForm((f) => ({ ...f, [name]: valor }));

  const alternarMulti = (name, id) =>
    setForm((f) => {
      const actual = f[name] || [];
      return { ...f, [name]: actual.includes(id) ? actual.filter((x) => x !== id) : [...actual, id] };
    });

  const guardar = async (e) => {
    e.preventDefault();
    setOk('');
    setError('');
    const errs = validarForm(campos.filter((c) => c.type !== 'multi'), form, !!editId);
    setErrores(errs);
    if (Object.keys(errs).length) {
      setError('Revisa los campos marcados en rojo.');
      return;
    }
    try {
      const payload = aPayload(form);
      if (editId) await api.put(`${endpoint}/${editId}`, payload);
      else await api.post(endpoint, payload);
      setOk(editId ? `El ${nombreItem} se actualizó correctamente.` : `El ${nombreItem} se creó correctamente.`);
      cerrar();
      await cargar();
    } catch (err) {
      setError(mensajeError(err));
      setErrores(erroresCampos(err));
    }
  };

  const eliminar = async (item) => {
    if (!window.confirm(`¿Eliminar este ${nombreItem}? Esta acción no se puede deshacer.`)) return;
    setOk('');
    setError('');
    try {
      await api.delete(`${endpoint}/${item.id}`);
      setOk(`El ${nombreItem} se eliminó correctamente.`);
      await cargar();
    } catch (err) {
      setError(mensajeError(err));
    }
  };

  const ctx = { recargar: cargar, avisar: setOk, fallo: setError };

  const renderInput = (c) => {
    const valor = form[c.name] ?? '';
    if (c.type === 'select') {
      return (
        <select value={valor} onChange={(e) => cambiar(c.name, e.target.value)}>
          <option value="">Seleccione…</option>
          {opcionesDe(c).map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      );
    }
    return (
      <input
        type={c.type || 'text'}
        step={c.step || (c.type === 'number' ? 1 : undefined)}
        value={valor}
        placeholder={c.placeholder}
        autoComplete={c.type === 'password' ? 'new-password' : 'off'}
        onChange={(e) => cambiar(c.name, e.target.value)}
      />
    );
  };

  return (
    <div>
      <div className="fila espacio">
        <div>
          <h1>{titulo}</h1>
          <p className="sub">{descripcion}</p>
        </div>
        {!form && <button className="btn" onClick={abrirNuevo}>+ Agregar</button>}
      </div>

      <Alerta tipo="ok" mensaje={ok} />
      <Alerta mensaje={error} />

      {form && (
        <form className="tarjeta" onSubmit={guardar} noValidate>
          <h3>{editId ? `Editar ${nombreItem}` : `Nuevo ${nombreItem}`}</h3>
          <div className="grid-2" style={{ margin: '12px 0' }}>
            {campos.filter((c) => c.type !== 'multi').map((c) => (
              <Campo key={c.name} label={c.label + (c.required && !(c.opcionalAlEditar && editId) ? ' *' : '')} error={errores[c.name]}>
                {renderInput(c)}
              </Campo>
            ))}
          </div>
          {campos.filter((c) => c.type === 'multi').map((c) => (
            <div key={c.name} style={{ marginBottom: 12 }}>
              <strong style={{ fontSize: 13 }}>{c.label}</strong>
              <div className="checks" style={{ marginTop: 6 }}>
                {opcionesDe(c).map((o) => (
                  <label key={o.value} className="check">
                    <input type="checkbox" checked={(form[c.name] || []).includes(o.value)} onChange={() => alternarMulti(c.name, o.value)} />
                    {o.label}
                  </label>
                ))}
              </div>
            </div>
          ))}
          <div className="fila">
            <button className="btn btn-oscuro" type="submit">Guardar</button>
            <button className="btn btn-sec" type="button" onClick={cerrar}>Cancelar</button>
          </div>
        </form>
      )}

      {cargando ? (
        <p>Cargando…</p>
      ) : (
        <div className="scroll-x">
          <table className="tabla">
            <thead>
              <tr>
                {columnas.map((c) => <th key={c.titulo}>{c.titulo}</th>)}
                <th style={{ textAlign: 'right' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 && (
                <tr><td colSpan={columnas.length + 1}>No hay registros todavía.</td></tr>
              )}
              {items.map((item) => (
                <tr key={item.id}>
                  {columnas.map((c) => <td key={c.titulo}>{c.render(item)}</td>)}
                  <td>
                    <div className="acciones">
                      {acciones && acciones(item, ctx)}
                      <button className="btn btn-sec btn-sm" onClick={() => abrirEditar(item)}>Editar</button>
                      <button className="btn btn-peligro btn-sm" onClick={() => eliminar(item)}>Eliminar</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
