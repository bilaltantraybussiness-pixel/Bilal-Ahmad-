package com.example.ui.components

val KASHMIR_DISTRICTS = listOf(
    "Srinagar",
    "Anantnag",
    "Baramulla",
    "Budgam",
    "Pulwama",
    "Shopian",
    "Kulgam",
    "Kupwara",
    "Bandipora",
    "Ganderbal",
    "Jammu",
    "Udhampur",
    "Kathua",
    "Rajouri",
    "Poonch",
    "Doda",
    "Ramban",
    "Reasi",
    "Kishtwar",
    "Samba"
)

val KASHMIR_PROFESSIONS = listOf(
    "Apple Pickers & Orchard Workers",
    "Walnut Wood Carvers & Artisans",
    "Sozni & Tilla Embroidery Artisans",
    "Carpenters & Woodworkers",
    "Electricians & Solar Technicians",
    "Plumbers & Sanitation Fitters",
    "Masons & Construction Workers (Raj Mistri)",
    "Painters & Polishers",
    "Welders & Metal Fabricators",
    "Mechanics & Auto Technicians",
    "Drivers (Light & Heavy Vehicles)",
    "Chefs, Wazwan Cooks & Bakers",
    "Cleaners & Housekeeping Staff",
    "Security Guards",
    "Tailors & Fashion Designers",
    "Beauticians & Salon Specialists",
    "Teachers & Academic Tutors",
    "Doctors & Healthcare Workers",
    "Nurses & Clinic Assistants",
    "Engineers (Civil, Electrical, Mech)",
    "Lawyers & Legal Advisors",
    "Accountants & Tax Consultants",
    "Software Developers & IT Specialists",
    "Graphic Designers & Calligraphers",
    "Artists, Calligraphers & Writers",
    "Photographers & Videographers",
    "Translators (Kashmiri, Urdu, English)",
    "Farmers & Agricultural Labourers",
    "Fruit Pickers & Mandi Sorters",
    "Gardeners & Landscapers",
    "Daily-Wage Labourers",
    "Hotel & Tourism Staff",
    "Shikara Boatmen & Tour Guides",
    "Shop Workers & Sales Representatives",
    "Delivery Workers & Couriers",
    "Warehouse & Logistics Staff",
    "Freelancers & Content Creators",
    "Handicraft & Paper Mache Artisans",
    "Snow Removal & Winter Crews",
    "Other / Add Your Profession"
)

val JOB_TYPES = listOf(
    "Full-time",
    "Part-time",
    "Contract",
    "Temporary",
    "Seasonal",
    "Freelance",
    "Daily wage",
    "Remote",
    "Hybrid",
    "On-site",
    "Project-based"
)

val WORK_TYPES = listOf(
    "Full-time",
    "Part-time",
    "Contract",
    "Temporary",
    "Seasonal",
    "Freelance",
    "Daily wage",
    "Project-based"
)

val WORK_PREFERENCES = listOf(
    "On-site",
    "Remote",
    "Hybrid"
)

val PAYMENT_FREQUENCIES = listOf(
    "Hourly",
    "Daily",
    "Weekly",
    "Monthly",
    "Per Project"
)

val WORKER_AVAILABILITY = listOf(
    "Available Now",
    "Available Within a Week",
    "Seasonal Worker",
    "Looking for Daily Work",
    "Available From Date",
    "Weekends Only"
)

val EXPERIENCE_LEVELS = listOf(
    "Fresher / Beginner",
    "1-2 years",
    "3-5 years",
    "5-10 years",
    "10+ years (Master / Ustad)"
)

// Verification Fee and Details
const val VERIFICATION_FEE_INR = 49.0
const val OFFICIAL_UPI_ID = "rozgarkashmir@upi"
val ID_PROOF_TYPES = listOf(
    "Aadhaar Card",
    "Artisan Pehchan Card",
    "Driving License",
    "Voter ID",
    "Business / Shop Registration",
    "Passport / Govt ID"
)

// Kashmiri Literature & Books
val KASHMIRI_LITERATURE_TYPES = listOf(
    "Book",
    "Poetry (شٲعِری)",
    "Article (مضمون)",
    "Story (افسانہ / دٔلیٖل)",
    "Folklore (لۄک کَتھ)",
    "Sufi Philosophy (تصوّف)",
    "History & Research (تٲریخ)"
)

val KASHMIRI_GENRES = listOf(
    "All Genres",
    "Poetry (شٲعِری)",
    "Short Stories (افسانے)",
    "Sufi Literature (تصوّف)",
    "Folk Tales (لۄک کَتھ)",
    "Language & Grammar (زَباندٲنی)",
    "Kashmir History (تٲریخ)",
    "Handicraft Heritage",
    "Children Stories"
)

// Comprehensive Top Skills Mapping by Profession
val SUGGESTED_SKILLS_BY_PROFESSION: Map<String, List<String>> = mapOf(
    "Carpenters & Woodworkers" to listOf(
        "Khatamband Ceiling Craft",
        "Pinjra Kari Lattice Work",
        "Walnut Wood Carving",
        "Door & Window Framing",
        "Modular Kitchens",
        "Wooden Flooring & Paneling",
        "Furniture Making",
        "Roof Timbering & Truss",
        "Wood Sanding & Buffing",
        "Cabinetry & Wardrobes"
    ),
    "Masons & Construction Workers (Raj Mistri)" to listOf(
        "Stone Masonry (Sangtash)",
        "Bricklaying & Jointing",
        "Cement Plastering",
        "Traditional Dajji Dewari",
        "Concrete Mixing & Slab Pouring",
        "Tile & Marble Fixing",
        "Lintel & Column Casting",
        "Waterproofing & Damp Proofing",
        "Scaffolding Assembly",
        "Wall Leveling & Plumb Line"
    ),
    "Apple Pickers & Orchard Workers" to listOf(
        "Apple Picking Without Bruising",
        "High-Density Tree Pruning",
        "Apple Grading & Sizing",
        "Wooden / Corrugated Crate Packing",
        "Orchard Pesticide Spraying",
        "Tree Grafting & Budding",
        "Cold Store Palletizing",
        "Drip Irrigation Setup",
        "Fruit Thinning & Blossom Care",
        "Ladder Safety & Harvesting Crews"
    ),
    "Sozni & Tilla Embroidery Artisans" to listOf(
        "Fine Sozni Needlework",
        "Real Gold & Silver Tilla Stitching",
        "Pashmina Shawl Border Hashia",
        "Kashmiri Aari Hook Embroidery",
        "Bridal Phiran Crafting",
        "Hand-Spun Yarn Handling",
        "Jacquard & Paisley Motif Tracing",
        "Heirloom Shawl Restoration",
        "Silk Thread Color Blending",
        "Velvet Tilla Work"
    ),
    "Walnut Wood Carvers & Artisans" to listOf(
        "Deep Chinar Leaf Relief Carving",
        "Dragon & Floral Woodwork",
        "Dry Fruit Box Crafting",
        "Traditional Wax Finishing",
        "Lattice Screen Carving",
        "Seasoned Walnut Wood Selection",
        "Inlay Work",
        "Hand Chisel Sharpening",
        "Table & Tray Carving",
        "Heirloom Furniture Restoration"
    ),
    "Electricians & Solar Technicians" to listOf(
        "House Concealed Wiring",
        "Solar Inverter & Battery Setup",
        "Three-Phase Load Balancing",
        "MCB & Circuit Breaker Installation",
        "Generator Setup & Changeover",
        "LED False Ceiling Illumination",
        "Earthing / Grounding Installation",
        "Submersible Water Pump Wiring",
        "Short Circuit Fault Finding",
        "Winter Heating Appliance Wiring"
    ),
    "Plumbers & Sanitation Fitters" to listOf(
        "PVC & CPVC Pipe Fitting",
        "PPR Thermal Pipe Welding",
        "Frost-Proof Winter Pipe Insulation",
        "Geyser & Water Heater Installation",
        "Bathroom Fixture Assembly",
        "Drainage & Septic Tank Piping",
        "Water Storage Tank Setup",
        "Water Pressure Pump Fitting",
        "Pipe Leakage Detection",
        "Kitchen Sink & Tap Repair"
    ),
    "Chefs, Wazwan Cooks & Bakers" to listOf(
        "Traditional Kashmiri Wazwan Preparation",
        "Rista & Gushtaba Pounding & Cooking",
        "Rogan Josh & Tabakhmaaz",
        "Kashmiri Harissa Preparation",
        "Samovar Kehwa & Noon Chai",
        "Kashmiri Traditional Bakery (Girda, Lavasa, Telvor)",
        "Large Wedding Banquet Management",
        "Authentic Spice Blending",
        "Firewood Bukhari & Hearth Cooking",
        "Food Hygiene & Meat Selection"
    ),
    "Doctors & Healthcare Workers" to listOf(
        "General Medical Checkups",
        "Elderly Home Care Visits",
        "Blood Pressure & Sugar Management",
        "First Aid & Emergency Triage",
        "Chronic Illness Follow-Up",
        "Wound Dressing & Suturing",
        "Medication Reconciliation",
        "Pediatric Basic Care",
        "Patient Counseling",
        "Tele-consultation"
    ),
    "Teachers & Academic Tutors" to listOf(
        "Mathematics Teaching",
        "Kashmiri Language Reading & Writing",
        "Urdu Grammar & Literature",
        "English Communication",
        "Science & Physics Coaching",
        "CBSE & JKBOSE Exam Preparation",
        "Primary School Foundation",
        "Online Classes & Lesson Plans",
        "Homework Support",
        "Student Progress Mentorship"
    ),
    "Software Developers & IT Specialists" to listOf(
        "Android Jetpack Compose",
        "Kotlin Multiplatform",
        "Room Database Integration",
        "Firebase Firestore & Auth",
        "REST API Development",
        "Web Development (React / Next.js)",
        "UI / UX Design (Figma)",
        "Database Architecture & SQL",
        "Git & Code Collaboration",
        "Performance Optimization"
    ),
    "Drivers (Light & Heavy Vehicles)" to listOf(
        "Hilly Terrain & Mountain Driving",
        "Snow Chain Mounting & Winter Driving",
        "Srinagar-Jammu Highway Navigation",
        "Defensive Driving Certification",
        "Commercial Fruit Truck Driving",
        "Tourist Taxi Guiding & Srinagar Routes",
        "Basic Mechanical Maintenance & Tyre Repair",
        "Night Highway Driving",
        "Luggage & Cargo Security",
        "GPS Navigation & Map Reading"
    ),
    "Tailors & Fashion Designers" to listOf(
        "Kashmiri Phiran Tailoring",
        "Bridal Velvet Phiran Styling",
        "Ladies Shalwar Kameez Custom Stitching",
        "Men's Waistcoat & Pathani Suits",
        "Buttonhole & Hemming Finishing",
        "Size Alteration & Fitting",
        "Pattern Drafting & Cutting",
        "Embroidery Collar Attachment",
        "Curtain & Drapes Stitching",
        "Modern Fusion Wear"
    )
)

val GENERAL_POPULAR_SKILLS = listOf(
    "Hardworking & Punctual",
    "Customer Service",
    "Problem Solving",
    "Quality Assurance",
    "Team Leadership",
    "Budget & Cost Estimation",
    "Safety Compliance",
    "Tool Maintenance",
    "Fluent in Kashmiri & Urdu",
    "Reliable & Trustworthy"
)

val MARKETPLACE_CATEGORIES = listOf(
    "All Categories",
    "Pherans & Traditional Wear",
    "Kashmiri Cultural Clothing",
    "Men's Clothing",
    "Women's Clothing",
    "Kids & Toddlers Wear",
    "Kangri & Winter Crafts",
    "Copper Utensils & Samovars",
    "Walnut Wood & Carvings",
    "Kashmir Saffron & Spices",
    "Walnuts, Kernels & Seeds",
    "Kashmir Flowers & Gardening",
    "Kashmir Groceries & Dried Foods",
    "Kashmiri Handicrafts & Shawls",
    "Topi, Caps & Headwear",
    "Art, Calligraphy & Paper Mache",
    "Books & Kashmir Literature",
    "Electronics & Mobile Phones",
    "Household Items & Furniture",
    "Second-Hand / Pre-Loved Marketplace",
    "Agricultural Tools & Equipment",
    "Other"
)

val PRODUCT_CONDITIONS = listOf(
    "Brand New",
    "New",
    "Like New",
    "Good",
    "Used",
    "Needs Repair"
)

val SEASONAL_WORK_TYPES = listOf(
    "Apple Picking",
    "Fruit Picking",
    "Orchard Work & Pruning",
    "Fruit Packing & Sorting",
    "Harvesting & Saffron",
    "Farm Work",
    "Tourism & Skiing Jobs",
    "Snow Removal",
    "All Seasonal Work"
)
