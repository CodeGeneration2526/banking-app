const eur = new Intl.NumberFormat("nl-NL", { style: "currency", currency: "EUR" });

// Format an integer amount of cents as a EUR currency string
export const formatCents = (cents: number) => eur.format(cents / 100);
