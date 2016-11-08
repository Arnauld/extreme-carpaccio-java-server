import fr.arolla.exploratory.Tax

taxes = [
        new Tax("FR", { d -> d * 0.5 }),
        new Tax("UK", { d -> if (d > 100) d * 0.8 else d * 0.9 })
]