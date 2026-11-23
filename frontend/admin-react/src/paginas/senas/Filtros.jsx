import { IconoBuscar, IconoMas } from '../../components/ui/IconosUi';

// Buscador por nombre/descripción, filtro por categoría y botón de nueva seña
export default function Filtros({ texto, onTexto, categoria, onCategoria, categorias, onNueva }) {
    return (
        <div className="filtros">
            <label className="filtros-buscar">
                <IconoBuscar />
                <input
                    type="search"
                    value={texto}
                    onChange={(e) => onTexto(e.target.value)}
                    placeholder="Buscar por nombre o descripción"
                    aria-label="Buscar seña"
                />
            </label>

            <label className="filtros-categoria">
                <span>Categoría</span>
                <select value={categoria} onChange={(e) => onCategoria(e.target.value)}>
                    <option value="">Todas</option>
                    {categorias.map((c) => (
                        <option key={c.id} value={c.id}>{c.nombre}</option>
                    ))}
                </select>
            </label>

            <button type="button" className="boton-principal filtros-nueva" onClick={onNueva}>
                <IconoMas /> Nueva seña
            </button>
        </div>
    );
}