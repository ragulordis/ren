package com.example.data.model

enum class BudgetFilter(val label: String, val minPrice: Long = 0L, val maxPrice: Long = Long.MAX_VALUE) {
    ALL("All Budgets"),
    UNDER_20L("< ₹20 Lakhs", 0L, 2000000L),
    RANGE_20L_40L("₹20L – ₹40L", 2000000L, 4000000L),
    RANGE_40L_80L("₹40L – ₹80L", 4000000L, 8000000L),
    RANGE_80L_1CR("₹80L – ₹1.5 Cr", 8000000L, 15000000L),
    ABOVE_1CR("> ₹1.5 Cr", 15000000L, Long.MAX_VALUE),
    RENT_UNDER_15K("< ₹15k/mo", 0L, 15000L),
    RENT_15K_30K("₹15k – ₹30k/mo", 15000L, 30000L)
}

enum class SortOption(val label: String) {
    URGENCY("Urgent Deals First"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    AREA_HIGH_LOW("Largest Area")
}
