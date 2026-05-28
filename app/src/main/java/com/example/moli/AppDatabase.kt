package com.example.moli

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [RecipeEntity::class], version = 11) // Subimos a v11 para asegurar la carga completa de Masa Madre
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moli_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = database.recipeDao()
                                if (dao.getAllRecipes().isEmpty()) {
                                    populateDatabase(dao)
                                }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDatabase(recipeDao: RecipeDao) {
            val converters = Converters()
            
            val recipes = listOf(
                RecipeEntity(
                    title = "Pan de Masa Madre Clásico",
                    category = "Masa Madre",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(500.0, "gr", "Harina de fuerza"),
                        Ingredient(350.0, "ml", "Agua tibia"),
                        Ingredient(100.0, "gr", "Masa madre activa"),
                        Ingredient(10.0, "gr", "Sal")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Mezclar harina, agua y masa madre. Dejar reposar (autólisis).", 30),
                        Step("Agregar sal y amasar hasta obtener una textura elástica."),
                        Step("Realizar pliegues cada 30 minutos durante 3 horas.", 180),
                        Step("Formar la hogaza y dejar fermentar en el refrigerador toda la noche.", 720),
                        Step("Hornear en olla de hierro a 230°C con tapa por 20 min y destapado otros 20 min.", 40)
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1585478259715-876a6a81fc08?q=80&w=500&auto=format&fit=crop",
                    basePortions = 1, totalTimeMinutes = 900
                ),
                RecipeEntity(
                    title = "Focaccia de Masa Madre",
                    category = "Masa Madre",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(400.0, "gr", "Harina de trigo"),
                        Ingredient(300.0, "ml", "Agua"),
                        Ingredient(80.0, "gr", "Masa madre"),
                        Ingredient(30.0, "ml", "Aceite de oliva"),
                        Ingredient(1.0, "ramita", "Romero fresco"),
                        Ingredient(1.0, "pizca", "Sal de grano")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Mezclar ingredientes y realizar un amasado ligero."),
                        Step("Fermentación en bloque a temperatura ambiente hasta doblar tamaño.", 240),
                        Step("Pasar a charola con aceite, estirar con los dedos y añadir romero."),
                        Step("Hornear a 220°C hasta que esté dorada y crujiente.", 25)
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1598103442097-8b74394b95c6?q=80&w=500&auto=format&fit=crop",
                    basePortions = 4, totalTimeMinutes = 270
                ),
                RecipeEntity(
                    title = "Chilaquiles Verdes con Pollo",
                    category = "Desayuno",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(12.0, "pzas", "Tortillas de maíz"),
                        Ingredient(500.0, "gr", "Tomate verde"),
                        Ingredient(2.0, "pzas", "Chile serrano"),
                        Ingredient(1.0, "pechuga", "Pollo cocido y deshebrado"),
                        Ingredient(0.5, "taza", "Crema ácida"),
                        Ingredient(100.0, "gr", "Queso fresco")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Cortar las tortillas en triángulos y freír hasta que estén doradas. Escurrir."),
                        Step("Hervir los tomates y chiles en agua con sal.", 10),
                        Step("Sofreír la salsa en una olla y cocinar para sazonar.", 5),
                        Step("Colocar los totopos en un plato, bañar con la salsa caliente."),
                        Step("Decorar con pollo, crema, queso y cebolla.")
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1640719028782-903001848b81?q=80&w=500&auto=format&fit=crop",
                    basePortions = 2, totalTimeMinutes = 25
                ),
                RecipeEntity(
                    title = "Tacos al Pastor",
                    category = "Comida",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(500.0, "gr", "Lomo de cerdo en filetes"),
                        Ingredient(3.0, "pzas", "Chiles guajillo (adobo)"),
                        Ingredient(0.25, "taza", "Vinagre de manzana"),
                        Ingredient(1.0, "pza", "Piña fresca"),
                        Ingredient(10.0, "pzas", "Tortillas de maíz"),
                        Ingredient(1.0, "ramita", "Cilantro y cebolla")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Licuar los chiles hidratados con vinagre y especias para el adobo."),
                        Step("Marinar la carne en el adobo dentro del refrigerador.", 30),
                        Step("Asar la carne en un sartén caliente hasta que dore y picar."),
                        Step("Servir en tortillas con piña, cebolla y cilantro.")
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?q=80&w=500&auto=format&fit=crop",
                    basePortions = 3, totalTimeMinutes = 45
                ),
                RecipeEntity(
                    title = "Guacamole Clásico",
                    category = "Otros",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(3.0, "pzas", "Aguacate Haas"),
                        Ingredient(1.0, "pza", "Tomate rojo picado"),
                        Ingredient(0.25, "pza", "Cebolla morada"),
                        Ingredient(1.0, "pza", "Chile serrano"),
                        Ingredient(1.0, "pza", "Limón (jugo)")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Machacar la pulpa de los aguacates en un tazón."),
                        Step("Agregar el jugo de limón inmediatamente para evitar oxidación."),
                        Step("Incorporar tomate, cebolla, chile y cilantro."),
                        Step("Sazonar con sal y servir con totopos.")
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1628191139360-408306cd953a?q=80&w=500&auto=format&fit=crop",
                    basePortions = 4, totalTimeMinutes = 10
                ),
                RecipeEntity(
                    title = "Enchiladas Verdes",
                    category = "Comida",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(12.0, "pzas", "Tortillas de maíz"),
                        Ingredient(1.0, "pechuga", "Pollo deshebrado"),
                        Ingredient(500.0, "gr", "Tomate verde"),
                        Ingredient(200.0, "gr", "Queso manchego"),
                        Ingredient(0.5, "taza", "Crema")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Hervir tomates y chiles hasta que cambien de color.", 10),
                        Step("Licuar los vegetales cocidos con un poco de cilantro."),
                        Step("Pasar tortillas por aceite caliente y rellenar con pollo."),
                        Step("Bañar con la salsa caliente, cubrir con queso y gratinar.", 8)
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1662116039535-645391d8f804?q=80&w=500&auto=format&fit=crop",
                    basePortions = 3, totalTimeMinutes = 35
                ),
                RecipeEntity(
                    title = "Arroz con Leche",
                    category = "Postres",
                    isDiscovery = true,
                    ingredientsJson = converters.fromIngredientList(listOf(
                        Ingredient(1.0, "taza", "Arroz blanco"),
                        Ingredient(1.0, "lt", "Leche entera"),
                        Ingredient(1.0, "lata", "Leche condensada"),
                        Ingredient(1.0, "pza", "Rama de canela"),
                        Ingredient(0.5, "taza", "Pasas")
                    )),
                    stepsJson = converters.fromStepList(listOf(
                        Step("Cocer el arroz con agua y canela hasta que el agua se evapore.", 15),
                        Step("Añadir la leche y cocinar a fuego lento moviendo constantemente.", 20),
                        Step("Cuando espese, agregar la leche condensada y cocinar un poco más.", 5),
                        Step("Servir frío o tibio con canela en polvo.")
                    )),
                    imageUrl = "https://images.unsplash.com/photo-1590005354167-62e3f3b36413?q=80&w=500&auto=format&fit=crop",
                    basePortions = 6, totalTimeMinutes = 50
                )
            )
            
            for (recipe in recipes) {
                recipeDao.insertRecipe(recipe)
            }
        }
    }
}
