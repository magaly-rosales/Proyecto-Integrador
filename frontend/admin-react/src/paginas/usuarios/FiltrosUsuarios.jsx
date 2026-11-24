import { IconoBuscar } from '../../components/ui/IconosUi';

// Buscador por nombre o correo y filtro por estado de la cuenta
export default function FiltrosUsuarios({ texto, onTexto, estado, onEstado }) {
    return (
        <div className="filtros">
            <label className="filtros-buscar">
                <IconoBuscar />
                <input
                    type="search"
                    value={texto}
                    onChange={(e) => onTexto(e.target.value)}
                    placeholder="Buscar por nombre o correo"
                    aria-label="Buscar usuario"
                />
            </label>

            <label className="filtros-categoria">
                <span>Estado</span>
                <select value={estado} onChange={(e) => onEstado(e.target.value)}>
                    <option value="">Todos</option>
                    <option value="activo">Activos</option>
                    <option value="inactivo">Inactivos</option>
                </select>
            </label>
        </div>
    );
}
