package com.printxpress.app.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.printxpress.app.models.Category;
import com.printxpress.app.models.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatabaseSeeder {

    public static void seedDatabase() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // 1. Add Categories
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("c1", "Cards", "cat_business_cards"));
        categories.add(new Category("c2", "Posters",        "cat_posters"));
        categories.add(new Category("c3", "Banners",        "cat_banners"));
        categories.add(new Category("c4", "Flyers",         "cat_flyers"));
        categories.add(new Category("c5", "Stickers",       "cat_stickers"));
        categories.add(new Category("c6", "Mugs",           "cat_mugs"));
        categories.add(new Category("c7", "T-Shirts",       "cat_tshirts"));

        for (Category cat : categories) {
            db.child("categories").child(cat.getId()).setValue(cat);
        }

        // 2. Add Products
        List<Product> products = new ArrayList<>();

        // --- Business Cards ---
        Product p1 = new Product();
        p1.setId("p1");
        p1.setName("Premium Matte Business Cards");
        p1.setDescription("Sleek matte-finish cards that feel professional and modern — perfect for making a lasting first impression.");
        p1.setPrice(1250.0);
        p1.setCategory("Cards");
        p1.setImageUrl("prod_business_matte");
        p1.setMaterials(Arrays.asList("Matte Cardstock", "Recycled Paper"));
        p1.setSizes(Arrays.asList("Standard (3.5\" x 2\")", "Square (2.5\" x 2.5\")"));
        products.add(p1);

        Product p2 = new Product();
        p2.setId("p2");
        p2.setName("Glossy UV Business Cards");
        p2.setDescription("High-gloss UV-coated cards with vibrant colours that pop and resist fingerprints.");
        p2.setPrice(1450.0);
        p2.setCategory("Cards");
        p2.setImageUrl("prod_business_glossy");
        p2.setMaterials(Arrays.asList("Glossy Cardstock", "UV Coated"));
        p2.setSizes(Arrays.asList("Standard (3.5\" x 2\")", "Mini (3\" x 1.5\")"));
        products.add(p2);

        Product p3 = new Product();
        p3.setId("p3");
        p3.setName("Embossed Luxury Business Cards");
        p3.setDescription("Premium thick cards with raised embossing that convey elegance and exclusivity.");
        p3.setPrice(2100.0);
        p3.setCategory("Cards");
        p3.setImageUrl("prod_business_embossed");
        p3.setMaterials(Arrays.asList("400gsm Cardstock", "Soft Touch Laminate"));
        p3.setSizes(Collections.singletonList("Standard (3.5\" x 2\")"));
        products.add(p3);

        // --- Posters ---
        Product p4 = new Product();
        p4.setId("p4");
        p4.setName("Large Format Posters");
        p4.setDescription("Big, bold posters to make your message stand out from a distance — ideal for events and retail.");
        p4.setPrice(450.0);
        p4.setCategory("Posters");
        p4.setImageUrl("prod_poster_large");
        p4.setMaterials(Arrays.asList("Standard Poster Paper", "Weatherproof Vinyl"));
        p4.setSizes(Arrays.asList("A0", "A1", "A2", "A3"));
        products.add(p4);

        Product p5 = new Product();
        p5.setId("p5");
        p5.setName("Photo Poster Prints");
        p5.setDescription("Gallery-quality photo prints on premium paper with rich colour reproduction.");
        p5.setPrice(350.0);
        p5.setCategory("Posters");
        p5.setImageUrl("prod_poster_photo");
        p5.setMaterials(Arrays.asList("Satin Photo Paper", "Gloss Photo Paper"));
        p5.setSizes(Arrays.asList("A3", "A2", "A1"));
        products.add(p5);

        Product p6 = new Product();
        p6.setId("p6");
        p6.setName("Event Promotion Posters");
        p6.setDescription("Eye-catching event posters designed to draw crowds — printed on vibrant gloss stock.");
        p6.setPrice(300.0);
        p6.setCategory("Posters");
        p6.setImageUrl("prod_poster_event");
        p6.setMaterials(Arrays.asList("170gsm Gloss", "250gsm Silk"));
        p6.setSizes(Arrays.asList("A3", "A2", "A1", "A0"));
        products.add(p6);

        // --- Banners ---
        Product p7 = new Product();
        p7.setId("p7");
        p7.setName("PVC Roller Banners");
        p7.setDescription("Portable pull-up roller banners great for exhibitions, trade shows, and in-store promotions.");
        p7.setPrice(3500.0);
        p7.setCategory("Banners");
        p7.setImageUrl("prod_banner_roller");
        p7.setMaterials(Arrays.asList("PVC Vinyl", "Satin Finish Vinyl"));
        p7.setSizes(Arrays.asList("850mm x 2000mm", "1000mm x 2000mm"));
        products.add(p7);

        Product p8 = new Product();
        p8.setId("p8");
        p8.setName("Outdoor PVC Banners");
        p8.setDescription("Weather-resistant PVC banners built for outdoor use — fences, scaffolding, and building wraps.");
        p8.setPrice(4200.0);
        p8.setCategory("Banners");
        p8.setImageUrl("prod_banner_outdoor");
        p8.setMaterials(Arrays.asList("Heavy-Duty PVC", "Perforated Mesh Vinyl"));
        p8.setSizes(Arrays.asList("1m x 2m", "1m x 3m", "2m x 4m"));
        products.add(p8);

        Product p9 = new Product();
        p9.setId("p9");
        p9.setName("Fabric Display Banners");
        p9.setDescription("Lightweight, wrinkle-resistant fabric banners with vibrant dye-sublimation printing.");
        p9.setPrice(5000.0);
        p9.setCategory("Banners");
        p9.setImageUrl("prod_banner_fabric");
        p9.setMaterials(Collections.singletonList("Polyester Fabric"));
        p9.setSizes(Arrays.asList("1m x 2m", "1.5m x 3m"));
        products.add(p9);

        // --- Flyers ---
        Product p10 = new Product();
        p10.setId("p10");
        p10.setName("Glossy Event Flyers");
        p10.setDescription("Vibrant, eye-catching flyers perfect for promoting events, sales, and special offers.");
        p10.setPrice(800.0);
        p10.setCategory("Flyers");
        p10.setImageUrl("prod_flyer_glossy");
        p10.setMaterials(Arrays.asList("Glossy Paper", "Satin Finish"));
        p10.setSizes(Arrays.asList("A4", "A5", "DL"));
        products.add(p10);

        Product p11 = new Product();
        p11.setId("p11");
        p11.setName("Double-Sided Leaflets");
        p11.setDescription("Double-sided leaflets with full-colour printing — great for menus, product catalogues, and handouts.");
        p11.setPrice(650.0);
        p11.setCategory("Flyers");
        p11.setImageUrl("prod_flyer_double");
        p11.setMaterials(Arrays.asList("135gsm Silk", "170gsm Gloss"));
        p11.setSizes(Arrays.asList("A5", "A6", "DL"));
        products.add(p11);

        // --- Stickers ---
        Product p12 = new Product();
        p12.setId("p12");
        p12.setName("Custom Die-Cut Stickers");
        p12.setDescription("Professionally cut to any shape — perfect for branding, packaging, and giveaways.");
        p12.setPrice(500.0);
        p12.setCategory("Stickers");
        p12.setImageUrl("prod_sticker_diecut");
        p12.setMaterials(Arrays.asList("Gloss Vinyl", "Matte Vinyl"));
        p12.setSizes(Arrays.asList("5cm x 5cm", "7cm x 7cm", "10cm x 10cm"));
        products.add(p12);

        Product p13 = new Product();
        p13.setId("p13");
        p13.setName("Waterproof Outdoor Stickers");
        p13.setDescription("UV-resistant and waterproof stickers built to last outdoors on vehicles, bottles, and equipment.");
        p13.setPrice(700.0);
        p13.setCategory("Stickers");
        p13.setImageUrl("prod_sticker_waterproof");
        p13.setMaterials(Arrays.asList("Heavy-Duty Vinyl", "Laminated Vinyl"));
        p13.setSizes(Arrays.asList("5cm x 5cm", "10cm x 10cm", "15cm x 15cm"));
        products.add(p13);

        Product p14 = new Product();
        p14.setId("p14");
        p14.setName("Roll Labels & Stickers");
        p14.setDescription("Bulk roll stickers ideal for product labelling, jars, bottles, and retail packaging.");
        p14.setPrice(1200.0);
        p14.setCategory("Stickers");
        p14.setImageUrl("prod_sticker_roll");
        p14.setMaterials(Arrays.asList("Paper Label", "Gloss Vinyl Label"));
        p14.setSizes(Arrays.asList("3cm x 3cm", "5cm x 3cm", "7cm x 5cm"));
        products.add(p14);

        // --- Mugs ---
        Product p15 = new Product();
        p15.setId("p15");
        p15.setName("Personalized Photo Mugs");
        p15.setDescription("Start your morning with a custom ceramic mug featuring your favourite photo or design.");
        p15.setPrice(850.0);
        p15.setCategory("Mugs");
        p15.setImageUrl("prod_mug_photo");
        p15.setMaterials(Collections.singletonList("Ceramic"));
        p15.setSizes(Arrays.asList("Standard 11oz", "Large 15oz"));
        products.add(p15);

        Product p16 = new Product();
        p16.setId("p16");
        p16.setName("Magic Heat-Sensitive Mugs");
        p16.setDescription("Black mug that reveals a hidden image or message when filled with a hot drink — a perfect gift.");
        p16.setPrice(1050.0);
        p16.setCategory("Mugs");
        p16.setImageUrl("prod_mug_magic");
        p16.setMaterials(Collections.singletonList("Magic Heat-Sensitive Ceramic"));
        p16.setSizes(Collections.singletonList("Standard 11oz"));
        products.add(p16);

        Product p17 = new Product();
        p17.setId("p17");
        p17.setName("Stainless Steel Travel Mugs");
        p17.setDescription("Double-walled insulated travel mugs with custom printing — keeps drinks hot or cold for hours.");
        p17.setPrice(1800.0);
        p17.setCategory("Mugs");
        p17.setImageUrl("prod_mug_travel");
        p17.setMaterials(Collections.singletonList("Stainless Steel"));
        p17.setSizes(Arrays.asList("350ml", "450ml"));
        products.add(p17);

        // --- T-Shirts ---
        Product p18 = new Product();
        p18.setId("p18");
        p18.setName("Custom Logo T-Shirts");
        p18.setDescription("Comfortable cotton t-shirts with high-quality screen or digital printing for your brand or event.");
        p18.setPrice(2200.0);
        p18.setCategory("T-Shirts");
        p18.setImageUrl("prod_tshirt_logo");
        p18.setMaterials(Arrays.asList("100% Cotton", "Polyester Blend"));
        p18.setSizes(Arrays.asList("S", "M", "L", "XL", "XXL"));
        products.add(p18);

        Product p19 = new Product();
        p19.setId("p19");
        p19.setName("Premium Polo Shirts");
        p19.setDescription("Smart embroidered or printed polo shirts — ideal for uniforms, corporate events, and teams.");
        p19.setPrice(2800.0);
        p19.setCategory("T-Shirts");
        p19.setImageUrl("prod_tshirt_polo");
        p19.setMaterials(Arrays.asList("Pique Cotton", "Performance Polyester"));
        p19.setSizes(Arrays.asList("S", "M", "L", "XL", "XXL"));
        products.add(p19);

        Product p20 = new Product();
        p20.setId("p20");
        p20.setName("All-Over Print T-Shirts");
        p20.setDescription("Full sublimation print covering the entire shirt — perfect for bold designs and creative expression.");
        p20.setPrice(3200.0);
        p20.setCategory("T-Shirts");
        p20.setImageUrl("prod_tshirt_allover");
        p20.setMaterials(Collections.singletonList("100% Polyester"));
        p20.setSizes(Arrays.asList("S", "M", "L", "XL", "XXL"));
        products.add(p20);

        for (Product prod : products) {
            db.child("products").child(prod.getId()).setValue(prod);
        }
    }
}
