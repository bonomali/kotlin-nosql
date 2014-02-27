package kotlin.nosql.mongodb

import org.junit.Test
import kotlin.nosql.*

open class ProductsBase<V, T : AbstractSchema>(javaClass: Class<V>) : DocumentSchema<String, V>("products", javaClass, PK.string("_id")) {
    val SKU = string<T>("sku")
    val Title = string<T>("title")
    val Description = string<T>("description")
    val ASIN = string<T>("asin")

    val Shipping = ShippingColumn<T>()
    val Pricing = PricingColumn<T>()
    val Details = DetailsColumn<T>()

    class ShippingColumn<T : AbstractSchema>() : Column<Shipping, T>("shipping", javaClass()) {
        val Weight = integer<T>("weight")
    }

    class DimensionsColumn<V, T : AbstractSchema>() : Column<V, T>("dimensions", javaClass()) {
        val Width = integer<T>("width")
        val Height = integer<T>("height")
        val Depth = integer<T>("depth")
    }

    class PricingColumn<T : AbstractSchema>() : Column<Pricing, T>("pricing", javaClass()) {
        val List = integer<T>("list")
        val Retail = integer<T>("retail")
        val Savings = integer<T>("savings")
        val PCTSavings = integer<T>("pct_savings")
    }

    class DetailsColumn<T : AbstractSchema>() : Column<Details, T>("details", javaClass()) {
        val Title = string<T>("title")
        val Artist = string<T>("artist")
        val Savings = integer<T>("savings")
        val PCTSavings = integer<T>("pct_savings")
    }
}

object Products : ProductsBase<Product, Products>(javaClass()) {
}

object Albums : ProductsBase<Album, Albums>(javaClass()) {
}

abstract class Product(val sku: String, val title: String, val description: String,
                       val asin: String, val shipping: Shipping, val pricing: Pricing, val details: Details) {
    val id: String? = null
}

class Shipping(val weight: Int, dimensions: Dimensions) {
}

class Dimensions(val weight: Int, val height: Int, val depth: Int) {
}

class Pricing(val list: Int, val retail: Int, val savings: Int, val pctSavings: Int) {
}

class Details(val title: String, artist: String) {
}

class Album(sku: String, title: String, description: String, asin: String, shipping: Shipping, pricing: Pricing,
            details: Details) : Product(sku, title, description, asin, shipping, pricing, details) {
    override fun toString(): String {
        return "[id: $id, sku: $sku, title: $title]"
    }
}

class MongoDBTests {
    Test
    fun test() {
        val db = MongoDB(database = "test")

        db {
            Albums insert {
                Album(sku = "00e8da9b", title = "A Love Supreme", description = "by John Coltrane",
                        asin = "B0000A118M", shipping = Shipping(weight = 6, dimensions = Dimensions(10, 10, 1)),
                        pricing = Pricing(list = 1200, retail = 1100, savings = 100, pctSavings = 8),
                        details = Details(title = "A Love Supreme [Original Recording Reissued]",
                                artist = "John Coltrane"))
            }

            for (product in Products filter { SKU eq "00e8da9b" }) {
                println(product)
            }

            /*Albums columns { ID + Title } filter { SKU eq "00e8da9b" } forEach { id, title ->
                // ...
            }*/
        }
    }
}