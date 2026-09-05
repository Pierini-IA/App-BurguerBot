/**
 * Tipos relacionados con productos, ingredientes, extras y categorías
 */

/**
 * Unidad de medida para ingredientes. Coincide con el enum `UnidadMedida` del backend.
 */
export enum UnidadMedida {
  UNIDAD = "UNIDAD",
  FETA = "FETA",
  HOJA = "HOJA",
  RODAJA = "RODAJA",
  PORCION = "PORCION",
  PUNADO = "PUNADO",
}

/** Etiqueta legible (plural) para cada unidad. */
export const UNIDAD_MEDIDA_LABEL: Record<UnidadMedida, string> = {
  [UnidadMedida.UNIDAD]: "Unidades",
  [UnidadMedida.FETA]: "Fetas",
  [UnidadMedida.HOJA]: "Hojas",
  [UnidadMedida.RODAJA]: "Rodajas",
  [UnidadMedida.PORCION]: "Porciones",
  [UnidadMedida.PUNADO]: "Puñados",
};

/**
 * Referencia mínima al local (el backend serializa el objeto completo,
 * pero en el front solo usamos id/nombre).
 */
export interface LocalRef {
  id: number;
  nombre?: string;
}

/**
 * Ingrediente del local. Coincide con la entidad `Ingrediente` del backend.
 */
export interface Ingrediente {
  id: number;
  nombre: string;
  stockActual: number;
  unidadMedida: UnidadMedida;
  local?: LocalRef;
  /**
   * Umbral de stock bajo. El backend todavía no lo persiste;
   * se usa solo en la UI para el indicador de "stock bajo".
   */
  stockMinimo?: number;
}

/**
 * Datos para crear/editar ingrediente.
 */
export interface IngredienteFormData {
  nombre: string;
  stockActual: number;
  unidadMedida: UnidadMedida;
}

/**
 * Item de receta: un ingrediente y la cantidad que consume el producto.
 * Coincide con la entidad `Receta` del backend.
 */
export interface RecetaItem {
  id?: number;
  ingredienteId?: number;
  ingrediente?: Ingrediente | { id: number };
  cantidadRequerida: number;
}

/**
 * Producto del menú. Coincide con la entidad `Producto` del backend.
 */
export interface Producto {
  id: number;
  nombre: string;
  descripcion?: string;
  precio: number;
  precioBase?: number;
  precioPromocion?: number;
  tienePromocion?: boolean;
  /** true = producto sin stock o "cortado" manualmente desde el panel. */
  estaAgotado: boolean;
  permiteExtras?: boolean;
  recetas?: RecetaItem[];
  categoria?: Categoria;
  local?: LocalRef;
  /** URL de imagen. El backend todavía no lo persiste. */
  imagen?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Datos para crear/editar producto.
 * El backend recibe la entidad `Producto`: para asociar categoría y receta
 * se envían objetos anidados con `id`.
 */
export interface ProductoFormData {
  nombre: string;
  descripcion?: string;
  precio: number;
  categoria?: { id: number };
  recetas?: { ingrediente: { id: number }; cantidadRequerida: number }[];
}

/**
 * Categoría de productos
 */
export interface Categoria {
  id: number;
  nombre: string;
  descripcion?: string;
  localId: number;
  localNombre?: string;
  orden: number;
  activo: boolean;
  /** Solo en respuestas: cuántos productos usan la categoría. */
  cantidadProductos?: number;
  /** Solo en respuestas: cuántos extras cuelgan de la categoría. */
  cantidadExtras?: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Datos para crear/editar categoría
 */
export interface CategoriaFormData {
  nombre: string;
  descripcion?: string;
  orden?: number;
  activo?: boolean;
}

/**
 * Extra (adicional) para productos
 */
export interface Extra {
  id: number;
  nombre: string;
  descripcion?: string;
  precioAdicional: number;
  localId: number;
  localNombre?: string;
  categoriaId?: number;
  categoriaNombre?: string;
  categoria?: Categoria;
  activo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Datos para crear/editar extra
 */
export interface ExtraFormData {
  nombre: string;
  descripcion?: string;
  precioAdicional: number;
  categoriaId?: number;
  activo?: boolean;
}

/**
 * Relación producto-extra
 */
export interface ProductoExtra {
  productoId: number;
  extraId: number;
  esObligatorio: boolean;
}
