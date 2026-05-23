package com.printxpress.app.utils;

import java.util.HashMap;
import java.util.Map;

public class PriceCalculator {

    // Base Prices for each category (per unit)
    private static final Map<String, Double> CATEGORY_BASE_PRICES = new HashMap<>();
    static {
        CATEGORY_BASE_PRICES.put("Cards", 8.00);
        CATEGORY_BASE_PRICES.put("Flyers", 15.00);
        CATEGORY_BASE_PRICES.put("Posters", 150.00);
        CATEGORY_BASE_PRICES.put("Banners", 800.00);
        CATEGORY_BASE_PRICES.put("Stickers", 5.00);
        CATEGORY_BASE_PRICES.put("T-Shirts", 1200.00);
        CATEGORY_BASE_PRICES.put("Mugs", 650.00);
    }

    // Multipliers for different sizes
    private static final Map<String, Double> SIZE_MULTIPLIERS = new HashMap<>();
    static {
        // Business Cards
        SIZE_MULTIPLIERS.put("Standard (3.5\" × 2\")", 1.0);
        SIZE_MULTIPLIERS.put("Square (2.5\" × 2.5\")", 1.2);
        SIZE_MULTIPLIERS.put("Mini (3.5\" × 1.5\")", 0.9);
        
        // Flyers / Posters
        SIZE_MULTIPLIERS.put("A4", 1.0);
        SIZE_MULTIPLIERS.put("A5", 0.7);
        SIZE_MULTIPLIERS.put("A6", 0.5);
        SIZE_MULTIPLIERS.put("DL", 0.6);
        SIZE_MULTIPLIERS.put("A3", 2.0);
        SIZE_MULTIPLIERS.put("A2", 4.0);
        SIZE_MULTIPLIERS.put("A1", 8.0);
        SIZE_MULTIPLIERS.put("A0", 15.0);
        
        // T-Shirts
        SIZE_MULTIPLIERS.put("XS", 0.9);
        SIZE_MULTIPLIERS.put("S", 1.0);
        SIZE_MULTIPLIERS.put("M", 1.0);
        SIZE_MULTIPLIERS.put("L", 1.1);
        SIZE_MULTIPLIERS.put("XL", 1.2);
        SIZE_MULTIPLIERS.put("XXL", 1.4);
    }

    // Multipliers for different materials
    private static final Map<String, Double> MATERIAL_MULTIPLIERS = new HashMap<>();
    static {
        MATERIAL_MULTIPLIERS.put("Glossy", 1.1);
        MATERIAL_MULTIPLIERS.put("Matte", 1.0);
        MATERIAL_MULTIPLIERS.put("Silk", 1.5);
        MATERIAL_MULTIPLIERS.put("Canvas", 2.5);
        MATERIAL_MULTIPLIERS.put("Vinyl", 1.3);
        MATERIAL_MULTIPLIERS.put("Cotton", 1.4);
        MATERIAL_MULTIPLIERS.put("Ceramic", 1.0);
        MATERIAL_MULTIPLIERS.put("Enamel", 1.2);
    }

    public static final double DELIVERY_FEE = 350.00;

    public static double calculateTotal(String category, String size, String material, int quantity, boolean isDelivery) {
        Double base = CATEGORY_BASE_PRICES.get(category);
        double basePrice = (base != null) ? base : 10.0;
        
        Double sm = SIZE_MULTIPLIERS.get(size);
        double sizeMult = (sm != null) ? sm : 1.0;

        Double mm = MATERIAL_MULTIPLIERS.get(material);
        double matMult = (mm != null) ? mm : 1.0;

        double total = (basePrice * sizeMult * matMult) * quantity;
        
        if (isDelivery) {
            total += DELIVERY_FEE;
        }

        return total;
    }
}
