# Gatling-random

Gatling is a stress tool. Development is currently focusing on HTTP support.
Current stable version of Gatling is 1.5.x.

In many cases, we need to randomly generate a large set of data. Gatling provides a data-feed option to achieve that. It supports the data reading from CSV or SQL database. However, we want to generate random names, addresses, or other string patterns right in memory, rather than loading from database. I write a customized data feed, and leverage log-synth libraries to generate meaningfully random data on the fly. The log-synth libraries are included into customized gatling lib folder.

Example: `RandomTest.scala`

```
 // custom feed generate the usernames randomly.
  val myCustomFeeder = new Feeder[String] {
    // always return true as this feeder can be polled infinitively
    override def hasNext = true

    val s: SchemaSampler = new SchemaSampler(Resources.asCharSource(Resources.getResource("schema003.json"), Charsets.UTF_8).read)

    override def next: Map[String, String] = {
      val record: JsonNode = s.sample
      var name = record.get("name").asText
      var id = record.get("id").asText
      val today = Calendar.getInstance().getTime()
      val formater = new SimpleDateFormat("yyyyMMddHHmmssSSS")
      var todayStr = formater.format(today)
      Map("name" -> name, "id" -> id, "time" -> todayStr)
    }
  }

  val scn = scenario("Write Test")
    .feed(myCustomFeeder)
    .exec(
      http("insert_request")
        .put("/keyvalue/test_collection/${id}") // must be lowercase
        .header("Content-Type", "application/json")
        .body("""{ "username": "${name}" }""").asJSON
        .check(status.in(200 to 210))

    )
  .exec(
      http("get_request")
        .get("/keyvalue/test_collection/${id}") // must be lowercase
        .header("Content-Type", "application/json")
        .check(status.in(200 to 210))

    )
  setUp(
    scn.users(threads).ramp(rampup).delay(1).protocolConfig(httpConf)

  )

  ```