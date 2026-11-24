// Robot del asistente (dibujo propio en SVG)
export default function Robot({ tam = 120 }) {
    return (
        <svg width={tam} height={tam} viewBox="0 0 120 120" fill="none" role="img" aria-label="Asistente">
            <defs>
                <linearGradient id="robot-cuerpo" x1="20" y1="10" x2="100" y2="110" gradientUnits="userSpaceOnUse">
                    <stop stopColor="#7DD3FC" />
                    <stop offset="1" stopColor="#3B82F6" />
                </linearGradient>
            </defs>

            <path d="M60 24v-9" stroke="#3B82F6" strokeWidth="4" strokeLinecap="round" />
            <circle cx="60" cy="11" r="5" fill="#7DD3FC" />

            <rect x="17" y="40" width="9" height="20" rx="4.5" fill="#3B82F6" />
            <rect x="94" y="40" width="9" height="20" rx="4.5" fill="#3B82F6" />
            <rect x="24" y="24" width="72" height="54" rx="24" fill="url(#robot-cuerpo)" />
            <rect x="33" y="34" width="54" height="34" rx="16" fill="#0F2A4A" />
            <circle cx="49" cy="50" r="5" fill="#67E8F9" />
            <circle cx="71" cy="50" r="5" fill="#67E8F9" />
            <path d="M53 59q7 6 14 0" stroke="#67E8F9" strokeWidth="3" strokeLinecap="round" />

            <rect x="38" y="82" width="44" height="26" rx="13" fill="url(#robot-cuerpo)" />
            <path d="M38 92q-13 3-16 15" stroke="#3B82F6" strokeWidth="8" strokeLinecap="round" />
            <path d="M82 92q16-4 20-24" stroke="#3B82F6" strokeWidth="8" strokeLinecap="round" />
            <circle cx="102" cy="64" r="6" fill="#7DD3FC" />

            <path d="M104 14l2 6 6 2-6 2-2 6-2-6-6-2 6-2z" fill="#7DD3FC" />
        </svg>
    );
}
