import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler

class BasicWebVerticle : AbstractVerticle() {
    val LOGGER: Logger = LoggerFactory.getLogger(BasicWebVerticle::class.java)

    override fun start() {
        LOGGER.info("Verticle BasicWebVerticle Started")
        val router: Router = Router.router(vertx)

        //API routing
        router.get("/api/v1/products*").handler(BodyHandler.create())
        router.get("/api/v1/products/:id").handler(this::getProductById)
        router.post("/api/v1/products").handler(this::addProduct)
        router.put("/api/v1/products/:id").handler(this::updateProductById)
        router.delete("/api/v1/products/:id").handler(this::deleteProductById)

        //Default if no routes are matched
        router.route().handler(StaticHandler.create().setCachingEnabled(false))
        vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port"))
    }

    override fun stop() {
        LOGGER.info("Verticle BasicWebVerticle Started")
    }

    fun getAllProducts(routingContext: RoutingContext) {
        val responseJson = JsonObject()
        val firstItem = Product("123", "My item 123")
        val secondItem = Product("321", "My item 321")
        val products = mutableListOf<Product>()

        products.add(firstItem)
        products.add(secondItem)
        responseJson.put("products", products)

        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application-json")
                .end(Json.encodePrettily(responseJson))
    }

    //Get one products that matches the input id and return as single json object
    fun getProductById(routingContext: RoutingContext) {
        val productId: String = routingContext.request().getParam("id")

        val product = Product(productId, "My item $productId")

        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application-json")
                .end(Json.encodePrettily(product))
    }

    //Insert one item passed in from the http post body return what was added with unique id from the insert
    fun addProduct(routingContext: RoutingContext) {
        val jsonBody: JsonObject = routingContext.getBodyAsJson()

        val productId = jsonBody.getString("productId")
        val description = jsonBody.getString("description")
        val newItem = Product(productId, description)

        routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application-json")
                .end(Json.encodePrettily(newItem))
    }

    //Update the item based on the url product id and return updated product info
    fun updateProductById(routingContext: RoutingContext) {
        val jsonBody: JsonObject = routingContext.getBodyAsJson()

        val productId: String = routingContext.request().getParam("id")
        val description = jsonBody.getString("description")

        val updatedItem = Product(productId, description)

        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application-json")
                .end(Json.encodePrettily(updatedItem))
    }

    //Delete item and return 200 on success or 400 on fail
    fun deleteProductById(routingContext: RoutingContext) {
        val productId = routingContext.request().getParam("id")

        routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application-json")
                .end()
    }


}

fun main(args: Array<String>) {
    val vertx: Vertx = Vertx.vertx()
    vertx.deployVerticle(BasicWebVerticle())
}