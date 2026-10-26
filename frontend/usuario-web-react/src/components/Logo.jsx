// Logo de SeñasApp (imagen en public/logo.png), mostrado en círculo.
export default function Logo({ tamano = 40 }) {
  return (
    <img
      src="/logo.png"
      width={tamano}
      height={tamano}
      alt=""
      className="sa-logo-img"
      style={{ borderRadius: "50%", flex: "none" }}
    />
  );
}
