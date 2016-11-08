import fr.arolla.exploratory.Tax

def tax(String country, fn) {
    return new Tax(country, { d -> fn(d) as double })
}

taxes = [
        tax("FR", { d -> 500.0 }),
        tax("UK", { d -> d * 0.3 }),
        tax("NL", { d -> 7.0 })
]