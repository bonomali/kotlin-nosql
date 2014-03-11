# Kotlin NoSQL

Kotlin NoSQL is a NoSQL database query and access library for [Kotlin](http://github.com/JetBrains/Kotlin) language.
It offers a powerful and type-safe DSL for working with key-value, column and document NoSQL databases.

The following NoSQL databases are supported now:

- [MongoDB](https://www.mongodb.org/)

## Principles

The following key principles lie behind Kotlin NoSQL:

#### First-class query

Unlike to ORM frameworks with its object persistence strategy
Kotlin NoSQL uses another approach based on immutability of data and queries.
Each operation on data may be described via a query:

```kotlin
Albums columns { Details.Tracks } filter { Details.Artist.Title eq artistTitle } delete { Duration eq 200 }
```

#### Type-safety

Once you've defined a schema you can access documents via statically-typed queries always getting type-safe results:

```kotlin
for (product in Products filter { Pricing.Savings ge 1000 }) {
    when (product) {
        is Album -> // ...
        else -> // ...
    }
}
```

```kotlin
for ((slug, fullSlug, posted, text, authorInfo) in Comments columns { Slug +
    FullSlug + Posted + Text + AuthorInfo } filter { DiscussionID eq discussion Id }) {
}
```

#### Immutability

Queries enable you to access and modify any part of documents without a necessity to load and change its state to memory:

```kotlin
Products columns { Pricing.Retail + Pricing.Savings } find productId set values(newRetail, newSavings)
```

## Download

To use it with Maven insert the following in your pom.xml file:

```xml
<dependency>
    <groupId>org.jetbrains.kotlin</groupId>
    <artifactId>kotlin-nosql</artifactId>
    <version>$version</version>
 </dependency>
```

To use it with Gradle insert the following in your build.gradle:

```groovy
dependencies {
    compile 'org.jetbrains.kotlin:kotlin-nosql:$version'
}
```

## Getting Started

### Basics

#### Define a schema

```kotlin
object Comments: MongoDBSchema<Comment>("comments", javaClass()) {
    val DiscussionID = id("discussion_id")
    val Slug = string("slug")
    val FullSlug = string("full_slug")
    val Posted = dateTime("posted")
    val Text = string("text")

    val AuthorInfo = AuthorInfoColumn()

    class AuthorInfoColumn() : Column<AuthorInfo, Comments>("author", javaClass()) {
        val ID = id("id")
        val Name = string("name")
    }
}

class Comment(val: id: Id? = null, val discussionId: Id, val slug: String,
    val fullSlug: String, posted: DateTime, text: String, authorInfo: AuthorInfo)

class AuthorInfo(val: id: Id, val name: String)
```

#### Define a database

```kotlin
val db = MongoDB(database = "test", schemas = array(Comments))

db {
    // ...
}
```

#### Insert a document

```kotlin
val commentId = Comments insert Comment(discussionId, slug, fullSlug, posted,
    text, AuthorInfo(author.id, author.name))
```

#### Get a document by id

```kotlin
val comment = Comments get commentId
```

#### Get a list of documents by a filter expression

```kotlin
for (comment in Comments filter { AuthorInfo.ID eq authorId } sort { Posted } drop 10 take 5) {
}
```

#### Get selected fields by document id

```kotlin
val authorInfo = Comments columns { AuthorInfo } get commentId
```

#### Get selected fields by a filter expression

```kotlin
for ((slug, fullSlug, posted, text, authorInfo) in Comments columns { Slug +
    FullSlug + Posted + Text + AuthorInfo } filter { DiscussionID eq discussion Id }) {
}
```

#### Update selected fields by document id

```kotlin
Comments columns { Posted } find commentId set newDate
```

```kotlin
Comments columns { Posted + Text } find commentId set values(newDate, newText)
```

### Inheritance

#### Define a base schema

```kotlin
open class ProductSchema<V, T : MongoDBSchema>(javaClass: Class<V>, discriminator: String) : DocumentSchema<String, V>("products",
            javaClass, discriminator = Discriminator(string("type"), discriminator)) {
    val SKU = string<T>("sku")
    val Title = string<T>("title")
    val Description = string<T>("description")
    val ASIN = string<T>("asin")

    val Shipping = ShippingColumn<T>()
    val Pricing = PricingColumn<T>()

    class ShippingColumn<T : Schema>() : Column<Shipping, T>("shipping", javaClass()) {
        val Weight = integer<T>("weight")
        val Dimensions = DimensionsColumn<T>()
    }

    class DimensionsColumn<T : Schema>() : Column<Dimensions, T>("dimensions", javaClass()) {
        val Width = integer<T>("width")
        val Height = integer<T>("height")
        val Depth = integer<T>("depth")
    }

    class PricingColumn<T : Schema>() : Column<Pricing, T>("pricing", javaClass()) {
        val List = integer<T>("list")
        val Retail = integer<T>("retail")
        val Savings = integer<T>("savings")
        val PCTSavings = integer<T>("pct_savings")
    }
}

object Products : ProductSchema<Product, Products>(javaClass(), "")

abstract class Product(val id: String? = null, val sku: String, val title: String, val description: String,
                       val asin: String, val available: Boolean, val cost: Double,
                       val createdAtDate: LocalDate, val nullableBooleanNoValue: Boolean?,
                       val nullableBooleanWithValue: Boolean?,
                       val nullableDateNoValue: LocalDate?, val nullableDateWithValue: LocalDate?,
                       val nullableDoubleNoValue: Double?, val nullableDoubleWithValue: Double?,
                       val shipping: Shipping, val pricing: Pricing)

class Shipping(val weight: Int, val dimensions: Dimensions)

class Dimensions(val width: Int, val height: Int, val depth: Int)

class Pricing(val list: Int, val retail: Int, val savings: Int, val pctSavings: Int)
```

#### Define an inherited schema

```kotlin
object Albums : ProductSchema<Album, Albums>(javaClass(), discriminator = "Audio Album") {
    val Details = DetailsColumn<Albums>()

    class DetailsColumn<T : Schema>() : Column<Details, T>("details", javaClass()) {
        val Title = string<T>("title")
        val ArtistId = id<T>("artistId")
        val Genre = setOfString<T>("genre")

        val Tracks = TracksColumn<T>()
    }

    class TracksColumn<T : Schema>() : ListColumn<Track, T>("tracks", javaClass()) {
        val Title = string<T>("title")
        val Duration = integer<T>("duration")
    }
}

class Album(sku: String, title: String, description: String, asin: String, shipping: Shipping,
    pricing: Pricing, val details: Details) : Product(sku, title, description, asin, shipping, pricing)

class Details(val title: String, val artistId: Id, val genre: Set<String>, val tracks: List<Track>)
```

#### Access documents via an abstract schema

```kotlin
val product = Products get productId
    if (product is Album) {
        // ...
    }
}
```

#### Access documents via an inherited schema

```kotlin
for (albums in Albums filter { Details.ArtistId eq artistId }) {
    // ...
}
```