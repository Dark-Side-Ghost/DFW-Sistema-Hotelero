// Las mismas reglas que valida el backend (así el usuario ve el error antes de enviar)
export const RE = {
  nombre: /^[\p{L}][\p{L} .'-]*$/u,
  email: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  clave: /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/,
  telefono: /^\+?[0-9]{8,15}$/,
  telefonoHotel: /^[0-9+\- ]{8,20}$/,
  numeroHab: /^[A-Za-z0-9-]{1,20}$/,
};

export const DOCS = {
  DUI: { re: /^\d{8}-\d$/, ej: '01234567-8' },
  PASAPORTE: { re: /^[A-Za-z0-9]{6,15}$/, ej: 'A1234567' },
  DNI: { re: /^[A-Za-z0-9-]{6,20}$/, ej: '12345678-9' },
  NIT: { re: /^\d{4}-\d{6}-\d{3}-\d$/, ej: '0614-010190-101-1' },
};

const dos = (n) => String(n).padStart(2, '0');

/** Fecha de hoy (hora local) como AAAA-MM-DD */
export const hoyISO = () => {
  const d = new Date();
  return `${d.getFullYear()}-${dos(d.getMonth() + 1)}-${dos(d.getDate())}`;
};

export const sumarDias = (iso, n) => {
  const [y, m, d] = iso.split('-').map(Number);
  const f = new Date(y, m - 1, d + n);
  return `${f.getFullYear()}-${dos(f.getMonth() + 1)}-${dos(f.getDate())}`;
};

export const noches = (a, b) => Math.round((new Date(b) - new Date(a)) / 86400000);

export const edad = (iso) => {
  const hoy = new Date();
  const n = new Date(iso);
  let a = hoy.getFullYear() - n.getUTCFullYear();
  const m = hoy.getMonth() - n.getUTCMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < n.getUTCDate())) a--;
  return a;
};

export const dinero = (n) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(n || 0));

/** Valida un campo según su definición: required, number/integer/min/max, pattern, maxLength */
export function validarCampo(c, valor, editando) {
  const vacio = valor === undefined || valor === null || String(valor).trim() === '';
  const requerido = c.required && !(c.opcionalAlEditar && editando);
  if (vacio) return requerido ? `${c.label} es obligatorio` : null;
  if (c.type === 'number') {
    const n = Number(valor);
    if (Number.isNaN(n)) return `${c.label} debe ser un número`;
    if (c.integer && !Number.isInteger(n)) return `${c.label} debe ser un número entero`;
    if (c.min !== undefined && n < c.min) return `${c.label} debe ser mayor o igual a ${c.min}`;
    if (c.max !== undefined && n > c.max) return `${c.label} debe ser menor o igual a ${c.max}`;
  }
  if (c.pattern && !c.pattern.test(String(valor))) return c.patternMsg || `${c.label} no tiene un formato válido`;
  if (c.maxLength && String(valor).length > c.maxLength) return `${c.label} no puede superar ${c.maxLength} caracteres`;
  return null;
}

export function validarForm(campos, form, editando) {
  const errores = {};
  campos.forEach((c) => {
    const e = validarCampo(c, form[c.name], editando);
    if (e) errores[c.name] = e;
  });
  return errores;
}
