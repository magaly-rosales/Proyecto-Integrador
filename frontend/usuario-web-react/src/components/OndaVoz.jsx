const ALTURAS = [10, 18, 12, 26, 16, 34, 22, 44, 28, 56, 36, 48, 60, 40, 52, 30, 44, 24, 36, 18, 28, 14, 22, 12];

// Barras que se mueven mientras la app habla (activa = true).
export default function OndaVoz({ activa }) {
  return (
    <div className={activa ? "sa-onda activa" : "sa-onda"} aria-hidden="true">
      {ALTURAS.map((alto, i) => (
        <span
          key={i}
          className={i % 3 === 0 ? "naranja" : ""}
          style={{ "--alto": `${alto}px`, animationDelay: `${(i % 8) * 90}ms` }}
        />
      ))}
    </div>
  );
}
