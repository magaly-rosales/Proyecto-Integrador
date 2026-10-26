export default function Avatar({ usuario, grande = false }) {
  const inicial = (usuario?.nombre || "?").charAt(0).toUpperCase();
  return (
    <span className={grande ? "sa-avatar sa-avatar-grande" : "sa-avatar"}>
      {usuario?.foto ? <img src={usuario.foto} alt="" /> : inicial}
    </span>
  );
}